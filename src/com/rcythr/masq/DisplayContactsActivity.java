package com.rcythr.masq;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class DisplayContactsActivity extends ListActivity {
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		@SuppressWarnings("unchecked")
		HashMap<String, String> temp = (HashMap<String, String>) l.getItemAtPosition(position);
		
		Intent resultIntent = new Intent();
		resultIntent.putExtra("name", temp.get("name"));
		resultIntent.putExtra("address", temp.get("address"));
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}
	
	@Override
	public void onCreate(Bundle instanceState) {
		super.onCreate(instanceState);
		
		//Setup the list
		setContentView(R.layout.contacts_display);
		
		//Load the contacts
		ArrayList<HashMap<String, String>> contacts = new ArrayList<HashMap<String, String>>();
		Cursor c = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
	    String name, number = "";
	    String id;
	    c.moveToFirst();
	    for (int i = 0; i < c.getCount(); i++) {
	        name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
	        id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));

	        if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
	            Cursor pCur = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id },
	                    null);
	            while (pCur.moveToNext()) {
	                number = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
	                
	                HashMap<String, String> contact = new HashMap<String, String>();
	                contact.put("name", name);
	                contact.put("address", number);
	                
	                contacts.add(contact);
	                
	            }
	        }
	        c.moveToNext();
	    }
		
	    //Display it
	    getListView().setAdapter( new SimpleAdapter(this, 
	    		contacts, 
	    		R.layout.contacts_display_row,
	    		new String[] {"name", "address"},
	    		new int[] {R.id.name, R.id.address}));
	    	    
	}
	
}
