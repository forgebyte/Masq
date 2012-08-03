package com.rcythr.masq;

import org.bouncycastle.util.encoders.Base64;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.rcythr.masq.util.AES;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Activity for getting a new contact's key
 * @author Richard Laughlin
 */
public class NewContactActivity extends Activity {

	private static final int GENERATE_KEY_RESULT = 1;
	private static final int SCAN_CODE = 2;
	
	private Button saveButton;
	
	private String contactAddress;
	private String contactName;
	
	private byte[] key;
	
	@Override
	public void onCreate(Bundle instance) {
		super.onCreate(instance);
		setContentView(R.layout.management_new_contact);
		
		Button generateButton = (Button) findViewById(R.id.generateButton);
		generateButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(NewContactActivity.this, GenerateActivity.class);
				intent.putExtra("count", 1);
				NewContactActivity.this.startActivityForResult(intent, GENERATE_KEY_RESULT);
			}
		});
		
		Button scanButton = (Button) findViewById(R.id.scanButton);
		scanButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				IntentIntegrator integrator = new IntentIntegrator(NewContactActivity.this);
			    integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES, SCAN_CODE);
			}
		});
		
		Button manualButton = (Button) findViewById(R.id.manualButton);
		manualButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(NewContactActivity.this);
			    builder.setTitle(R.string.input);
			    builder.setMessage(R.string.input_desc);
			    
			    final EditText text = new EditText(NewContactActivity.this);
			    builder.setView(text);
			    
			    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) {
			            // Do nothing, you will be overriding this anyway
			        }
			    });
			    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {}
	            });
			    
			    final AlertDialog dialog = builder.create();
			    // Make sure you show the dialog first before overriding the
			    // OnClickListener
			    dialog.show();
			    // Notice that I`m not using DialogInterface.OnClicklistener but the
			    // View.OnClickListener
			    dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(
			            new View.OnClickListener() {

			                public void onClick(View v) {
			                	try {
			                		byte[] values = Base64.decode(text.getText().toString());
			            			if(values != null && values.length == AES.AES_KEY_SIZE) {
			            				key = values;
			            				dialog.dismiss();
			            			}
			            		} catch(Exception e) {
			            			e.printStackTrace();
			            		}
			                	
			                	Toast toast = Toast.makeText(dialog.getContext(),
	            						 R.string.invalid_key,
	                                    Toast.LENGTH_SHORT);
	                            toast.show();
			                }
			            });
			}
		});
		
		saveButton = (Button) findViewById(R.id.saveButton);
		saveButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent resultIntent = new Intent();
				resultIntent.putExtra("address", contactAddress);
				resultIntent.putExtra("name", contactName);
				resultIntent.putExtra("key", key);
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
			}
		});
		
		//Setup contact
		contactAddress = getIntent().getStringExtra("address");
		contactName = getIntent().getStringExtra("name");
		
		if(contactAddress == null || contactName == null) {
			finish();
			Log.d("NewContactActivity", "Intent missing required values");
		}
		
		//Setup key -- used for edit
		key = getIntent().getByteArrayExtra("key");
		saveButton.setEnabled(key != null);
	}
	
	private void setKey(byte[] newKey) {

		//Set the new key
		if(newKey != null && newKey.length == AES.AES_KEY_SIZE) {
			key = newKey;
		} else {
			Toast.makeText(this, R.string.invalid_key, Toast.LENGTH_SHORT).show();
		}
		
		//Set the save button to enabled/disabled
		saveButton.setEnabled(key != null);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            switch(requestCode) {
            case GENERATE_KEY_RESULT:
            	if(resultCode == Activity.RESULT_OK){
            		setKey(data.getByteArrayExtra("data"));
            	}
            	break;
            case SCAN_CODE:
            	if(resultCode == Activity.RESULT_OK) {
            		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            		
            		byte[] values = null;
            		
            		try {
            			values = Base64.decode(result.getContents());
            		} catch(Exception e) {
            			e.printStackTrace();
            		}
            		
            		setKey(values);
            	}
            	break;
            }
        }
    }
	
}
