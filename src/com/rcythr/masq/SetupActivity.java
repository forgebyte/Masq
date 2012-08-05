/**	This file is part of Masq.

    Masq is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Masq is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Masq.  If not, see <http://www.gnu.org/licenses/>.
**/

package com.rcythr.masq;

import java.security.MessageDigest;

import com.rcythr.masq.R;
import com.rcythr.masq.keymanagement.KeyManager;
import com.rcythr.masq.util.Dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

/**
 * The main activity which handles setup or login, and the intro explaination
 * @author Richard Laughlin
 */
public class SetupActivity extends Activity {
	
	@Override
	public void onBackPressed() {
		//Disable Back Button
	}
	
	private void handleNewSetup() {
		setContentView(R.layout.setup_new);
		
		final EditText password = (EditText) findViewById(R.id.passwordBox);
		final CheckBox external = (CheckBox) findViewById(R.id.externalCheck);
		
		Button next = (Button) findViewById(R.id.next);
		next.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				try {
					String text = password.getText().toString(); 
					
					if(!text.equals("")) {
						MessageDigest md;
						md = MessageDigest.getInstance("SHA-256");
						md.update(text.getBytes("UTF-8"));
						KeyManager.getInstance().setKeyStoreKey(md.digest());
						KeyManager.getInstance().setPasswordProtected(true);
					} else {
						KeyManager.getInstance().setPasswordProtected(false);
					}
					
					KeyManager.getInstance().setInternalStorage(!external.isChecked());
					KeyManager.getInstance().commit(SetupActivity.this);
					
					handleHelpContacts();
					
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(SetupActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	private void handleHelpContacts() {
		setContentView(R.layout.intro_contacts);
		
		Button toKeysAndSettings = (Button) findViewById(R.id.toKeysAndSettings);
		toKeysAndSettings.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				SetupActivity.this.startActivity(new Intent(SetupActivity.this, KeyManagementActivity.class));
				finish();
			}
		});
		
		Button toMainMenu = (Button) findViewById(R.id.toMainMenu);
		toMainMenu.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				SetupActivity.this.startActivity(new Intent(SetupActivity.this, MainMenuActivity.class));
				finish();
			}
		});
	}
	
	private void handleLogin() {
		setContentView(R.layout.setup_login);
		
		final EditText loginText = (EditText) findViewById(R.id.loginText);
		
		Button login = (Button) findViewById(R.id.login);
		login.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				try {
					MessageDigest md = MessageDigest.getInstance("SHA-256");
					md.update(loginText.getText().toString().getBytes("UTF-8")); // Change this to "UTF-16" if needed
					KeyManager.getInstance().setKeyStoreKey(md.digest());
					KeyManager.getInstance().load(SetupActivity.this);
					
					SetupActivity.this.startActivity(new Intent(SetupActivity.this, MainMenuActivity.class));
					finish();
					
				} catch(Exception e) {
					Toast.makeText(SetupActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		Button forgot = (Button) findViewById(R.id.forgot);
		forgot.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Dialogs.showConfirmation(SetupActivity.this, R.string.forgot_msg, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						KeyManager.getInstance().delete(SetupActivity.this);
						handleNewSetup();
					}
				});
			}
		});
	}
	
	@Override
	public void onCreate(Bundle instanceState) {
		super.onCreate(instanceState);
		if(!KeyManager.getInstance().isSetupComplete()) {
			handleNewSetup();
        } else if(KeyManager.getInstance().isPasswordProtected()) {
        	handleLogin();
        } else {
        	finish();
        }
		
	}
	
}
