package com.rcythr.secretsms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bouncycastle.crypto.InvalidCipherTextException;

import com.rcythr.secretsms.encryption.AES;
import com.rcythr.secretsms.util.EndlessScrollListener;
import com.secretconversations.R;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class SMSListing extends ListActivity {

	SimpleAdapter adapter;
	List<HashMap<String, String>> elements = new ArrayList<HashMap<String, String>>();
	
	public String getContactDisplayNameByNumber(String number) {
	    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
	    String name = number;

	    ContentResolver contentResolver = getContentResolver();
	    Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
	            ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

	    try {
	        if (contactLookup != null && contactLookup.getCount() > 0) {
	            contactLookup.moveToNext();
	            name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
	            //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
	        }
	    } finally {
	        if (contactLookup != null) {
	            contactLookup.close();
	        }
	    }

	    return name;
	}
	
	public void populateSMS(int limit) {
		ContentResolver contentResolver = getContentResolver();
		Cursor cursor = contentResolver.query( Uri.parse("content://sms/inbox"), null, null, null, null);

		int indexAddr = cursor.getColumnIndex( "address" );
		int indexBody = cursor.getColumnIndex( "body" );

		if ( indexBody < 0 || !cursor.moveToFirst() ) return;

		elements.clear();
		
		do
		{
			HashMap<String, String> temp = new HashMap<String, String>();
			temp.put("sender", getContactDisplayNameByNumber(cursor.getString(indexAddr)));
			temp.put("message", cursor.getString(indexBody));
			elements.add(temp);
		} while( cursor.moveToNext() && elements.size() < limit );
		
		adapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		HashMap<String, String> lookup = elements.get(position);
		
		try {
			lookup.put("message", new String(AES.handle(false, AES.fromHex(lookup.get("message")), SMSSending.key)));
			adapter.notifyDataSetChanged();
		} catch (InvalidCipherTextException e) {
			e.printStackTrace();
		} catch (Exception e) {
			Toast.makeText(this, "This message is not encrypted", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.listing);
		
		getListView().setOnScrollListener(new EndlessScrollListener(this));
		
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		adapter = new SimpleAdapter(
		this,
		elements,
		R.layout.listing_row,
		new String[] {"sender", "message"},
		new int[] {R.id.sender,R.id.message});
		setListAdapter(adapter);
		
		populateSMS(25);
	}
	
}
