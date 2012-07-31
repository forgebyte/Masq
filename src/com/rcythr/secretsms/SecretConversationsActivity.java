package com.rcythr.secretsms;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.crypto.InvalidCipherTextException;

import com.rcythr.secretsms.keymanagement.KeyManagementActivity;
import com.rcythr.secretsms.keymanagement.KeyManager;
import com.secretconversations.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SecretConversationsActivity extends Activity {
	
	public void handleSetup() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle(R.string.setup_explain);
    	alert.setMessage(R.string.setup_explain);

    	// Set an EditText view to get user input
    	final EditText input = new EditText(this);
    	alert.setView(input);

    	alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int whichButton) {
	        	String value = input.getText().toString();
	        	
	        	if(value.equals("")) {
	        		KeyManager.instance.setPasswordProtected(false);
	        	} else {
	        		KeyManager.instance.setPasswordProtected(true);
	        		MessageDigest md;
					try {
						md = MessageDigest.getInstance("SHA-256");
						md.update(value.getBytes("UTF-8"));
		        		KeyManager.instance.setKeyStoneKey(md.digest());
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
						//TODO: HANDLE ME
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						//TODO: HANDLE ME
					}
	        		
	        	}
	        	
	        	try {
					KeyManager.instance.commit(SecretConversationsActivity.this);
				} catch (InvalidCipherTextException e) {
					e.printStackTrace();
					//TODO: HANDLE ME
				} catch (IOException e) {
					e.printStackTrace();
					//TODO: HANDLE ME
				}
        	}
    	});
    	
    	alert.show();
	}
	
	public void handlePasswordFetch() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle(R.string.unlock);
    	alert.setMessage(R.string.unlock_explain);

    	// Set an EditText view to get user input
    	final EditText input = new EditText(this);
    	alert.setView(input);

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int whichButton) {
        	String value = input.getText().toString();
        		MessageDigest md;
				try {
					md = MessageDigest.getInstance("SHA-256");
					md.update(value.getBytes("UTF-8"));
	        		KeyManager.instance.setKeyStoneKey(md.digest());
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
					//TODO: HANDLE ME
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					//TODO: HANDLE ME
				}
				
				try {
					KeyManager.instance.load(SecretConversationsActivity.this);
				} catch (InvalidCipherTextException e) {
					e.printStackTrace();
					//TODO: HANDLE ME
				} catch (IOException e) {
					e.printStackTrace();
					//TODO: HANDLE ME
				}
        	}
        	});
    	
    	alert.show();
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //SharedPreferences prefs = this.getSharedPreferences("com.rcythr.secretsms", MODE_PRIVATE);
        //prefs.edit().putBoolean("setupComplete", false).commit();
        
        KeyManager.instance = new KeyManager(this);
        
        if(!KeyManager.instance.isSetupComplete()) {
        	handleSetup();
        } else if(KeyManager.instance.isPasswordProtected()) {
        	try {
        		handlePasswordFetch();
			} catch (Exception e) {
				//TODO: Show alert here
			}
        } else {
        	try {
				KeyManager.instance.load(SecretConversationsActivity.this);
			} catch (InvalidCipherTextException e) {
				e.printStackTrace();
				//TODO: HANDLE ME
			} catch (IOException e) {
				e.printStackTrace();
				//TODO: HANDLE ME
			}
        }
        
        ((Button) findViewById(R.id.keyManage)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				SecretConversationsActivity.this.startActivity(new Intent(SecretConversationsActivity.this, KeyManagementActivity.class));
			}
		});
        
        ((Button) findViewById(R.id.sendMessage)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				SecretConversationsActivity.this.startActivity(new Intent(SecretConversationsActivity.this, SMSSending.class));
			}
		});
        
        ((Button) findViewById(R.id.recvMessage)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				SecretConversationsActivity.this.startActivity(new Intent(SecretConversationsActivity.this, SMSListing.class));
			}
		});
        
        ((Button) findViewById(R.id.sendSteganograph)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
			}
		});
        
        ((Button) findViewById(R.id.recvSteganograph)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
			}
		});
    }
    
}