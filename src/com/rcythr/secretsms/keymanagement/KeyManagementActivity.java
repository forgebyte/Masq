package com.rcythr.secretsms.keymanagement;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Base64Encoder;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.rcythr.secretsms.keymanagement.GenerateActivity.GeneratedData;
import com.secretconversations.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;


public class KeyManagementActivity extends ExpandableListActivity {
	
	public static final int GENERATE_PERMA_KEY_RESULT = 1;
	public static final int GENERATE_KEYS_RESULT = 2;
	public static final int SELECT_CONTACT = 3;
	public static final int SCAN_PERMA_CODE = 4;
	public static final int SCAN_SINGLE_CODE = 5;
	
	private KeyManagementAdapter adapter;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.management);	
		
    	LayoutInflater inf = (LayoutInflater) this.getLayoutInflater();
		View headerView = inf.inflate(R.layout.management_header, null);
    	
    	ToggleButton toggleEncryption = (ToggleButton) headerView.findViewById(R.id.encryption_toggle);
    	toggleEncryption.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//TODO: Implement Me
			}
		});
    	
    	Button moveKeystore = (Button) headerView.findViewById(R.id.moveKeystore);
    	moveKeystore.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				//TODO: Implement Me
			}
		});
    	
    	Button resetKeystore = (Button) headerView.findViewById(R.id.resetKeystore);
    	resetKeystore.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				//TODO: Implement Me
			}
    		
    	});
    	
		Button addContact = (Button) headerView.findViewById(R.id.addContact);
		// this opens the activity. note the  Intent.ACTION_GET_CONTENT
	    // and the intent.setType
	    addContact.setOnClickListener( new OnClickListener() {
	    	
	        public void onClick(View v) {
	            @SuppressWarnings("deprecation")
				Intent intent = new Intent(Intent.ACTION_PICK, Contacts.Phones.CONTENT_URI);
	            startActivityForResult(intent, SELECT_CONTACT);
	        }
	        
	    });
	    
	    this.getExpandableListView().addHeaderView(headerView);
	}
	
    @SuppressWarnings("deprecation")
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            switch(requestCode) {
            case GENERATE_PERMA_KEY_RESULT:
            	if(resultCode == Activity.RESULT_OK){
            		GeneratedData generatedData = (GeneratedData) data.getSerializableExtra("data");
            		
            		Key key = KeyManager.instance.getLookup().get(adapter.pendingRequestContact.address);
            		key.permanentKey = ArrayUtils.toPrimitive(generatedData.data.get(0));
            		
            		try {
						KeyManager.instance.commit(this);
					} catch (InvalidCipherTextException e) {
						e.printStackTrace();
						//TODO: HANDLE ME
					} catch (IOException e) {
						e.printStackTrace();
						//TODO: HANDLE ME
					}
            		
            		//Display the QR code
            		(new IntentIntegrator(this)).shareText(new String(Base64.encode(key.permanentKey)));
            	}
            	break;
            case GENERATE_KEYS_RESULT:
            	if(resultCode == Activity.RESULT_OK){
            		GeneratedData generatedData = (GeneratedData) data.getSerializableExtra("data");
            		Key key = KeyManager.instance.getLookup().get(adapter.pendingRequestContact.address);
            		
            		StringBuffer buf = new StringBuffer();
            		for(Byte[] array : generatedData.data) {
            			key.singleUseKeys.add(array);
            			String output = new String(Base64.encode(ArrayUtils.toPrimitive(array)));
            			buf.append(output);
            			Log.i("KeyManagementActivity", output);
            		}
            		
            		try {
						KeyManager.instance.commit(this);
					} catch (InvalidCipherTextException e) {
						e.printStackTrace();
						//TODO: HANDLE ME
					} catch (IOException e) {
						e.printStackTrace();
						//TODO: HANDLE ME
					}
            		
            		(new IntentIntegrator(this)).shareText(buf.toString());
            	}
            	break;
            case SELECT_CONTACT:
            	if(resultCode == Activity.RESULT_OK) {
	        		Uri contactData = data.getData();
	                Cursor c =  managedQuery(contactData, null, null, null, null);
	                startManagingCursor(c);
	                if (c.moveToFirst()) {
	                	String name = c.getString(c.getColumnIndexOrThrow(People.NAME));  
	                	String number = c.getString(c.getColumnIndexOrThrow(People.NUMBER));
	                	
	                	Key key = new Key();
	                	key.displayName = name;
	                	
	                	if(KeyManager.instance.getLookup().get(number) == null) {
		                	KeyManager.instance.getLookup().put(number, key);
		                	try {
								KeyManager.instance.commit(KeyManagementActivity.this);
							} catch (InvalidCipherTextException e) {
								e.printStackTrace();
								//TODO: HANDLE ME
							} catch (IOException e) {
								e.printStackTrace();
								//TODO: HANDLE ME
							}
		                	adapter.populate();
	                	}
	                }
            	}
	            break;
            case SCAN_PERMA_CODE:
            	if(resultCode == Activity.RESULT_OK) {
            		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            		Toast.makeText(this, result.getFormatName(), Toast.LENGTH_SHORT).show();
            		Toast.makeText(this, result.getContents(),Toast.LENGTH_SHORT).show();
            	} else {
            		adapter.pendingRequestContact = null;
            	}
            	break;
            case SCAN_SINGLE_CODE:
            	if(resultCode == Activity.RESULT_OK) {
            		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            		Toast.makeText(this, result.getFormatName(), Toast.LENGTH_SHORT).show();
            		Toast.makeText(this, result.getContents(),Toast.LENGTH_SHORT).show();
            	} else {
            		adapter.pendingRequestContact = null;
            	}
            	break;
            }
        }
    }
    
	@Override
	public void onResume(){
		super.onResume();
		
		adapter = new KeyManagementAdapter(this);
		this.setListAdapter(adapter);
		
	}
}
