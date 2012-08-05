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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map.Entry;

import org.bouncycastle.util.encoders.Base64;

import com.google.zxing.integration.android.IntentIntegrator;
import com.rcythr.masq.R;
import com.rcythr.masq.keymanagement.Contact;
import com.rcythr.masq.keymanagement.Key;
import com.rcythr.masq.keymanagement.KeyManager;
import com.rcythr.masq.util.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;


/**
 * Manages the Keystore and Contacts
 * @author Richard Laughlin
 */
@SuppressWarnings("deprecation")
public class KeyManagementActivity extends ExpandableListActivity {
	
	public static final int SELECT_CONTACT = 1;
	public static final int SETUP_KEY = 2;
	
	private KeyManagementAdapter adapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	try {
			KeyManager.getInstance().init(this);
		} catch (Exception e) {
			Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_LONG);
		}
    	
    	setContentView(R.layout.management_main);	
		
    	//Inflate the header so we can play with the buttons
    	LayoutInflater inf = (LayoutInflater) this.getLayoutInflater();
		View headerView = inf.inflate(R.layout.management_header, null);
    	
    	final ToggleButton toggleEncryption = (ToggleButton) headerView.findViewById(R.id.encryption_toggle);
    	toggleEncryption.setChecked(KeyManager.getInstance().isPasswordProtected());
    	toggleEncryption.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					AlertDialog.Builder alert = new AlertDialog.Builder(KeyManagementActivity.this);
					
					alert.setTitle(R.string.password_input);
					alert.setMessage(R.string.password_input_msg);
					
					// Set an EditText view to get user input 
					final EditText input = new EditText(KeyManagementActivity.this);
					input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
					alert.setView(input);
					
					alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							String value = input.getText().toString();
							if(!value.equals("")) {
								try {
									MessageDigest md = MessageDigest.getInstance("SHA-256");
									md.update(value.getBytes("UTF-8"));
									KeyManager.getInstance().setKeyStoreKey(md.digest());
									KeyManager.getInstance().setPasswordProtected(true);
									return;
								} catch(Exception e) {
									e.printStackTrace();
								}
							} 
							KeyManager.getInstance().setPasswordProtected(false);
							toggleEncryption.setChecked(false);
						}
					});
					
					alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							KeyManager.getInstance().setPasswordProtected(false);
							toggleEncryption.setChecked(false);
						}
					});
					
					alert.show();
				} else {
					KeyManager.getInstance().setPasswordProtected(false);
				}
			}
		});
    	
    	final Button moveKeystore = (Button) headerView.findViewById(R.id.moveKeystore);
    	moveKeystore.setText((KeyManager.getInstance().isInternalStorage()) ? R.string.move_to_external : R.string.move_to_internal);
    	moveKeystore.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if(KeyManager.getInstance().swap(KeyManagementActivity.this)) {
					moveKeystore.setText((KeyManager.getInstance().isInternalStorage()) 
							? R.string.move_to_external : R.string.move_to_internal);
				} else {
					Toast.makeText(KeyManagementActivity.this, R.string.move_error, Toast.LENGTH_SHORT).show();
				}
			}
		});
    	
    	Button resetKeystore = (Button) headerView.findViewById(R.id.resetKeystore);
    	resetKeystore.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Dialogs.showConfirmation(KeyManagementActivity.this, R.string.confrimation_msg, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						Context context = KeyManagementActivity.this;
						KeyManager.getInstance().delete(context);
						
						context.startActivity(new Intent(context, SetupActivity.class));
						finish();
					}
				});
			}
    		
    	});
    	
		Button addContact = (Button) headerView.findViewById(R.id.addContact);
		// this opens the activity. note the  Intent.ACTION_GET_CONTENT
	    // and the intent.setType
	    addContact.setOnClickListener( new OnClickListener() {
	    	
	        public void onClick(View v) {
				Intent intent = new Intent(KeyManagementActivity.this, DisplayContactsActivity.class);
	            startActivityForResult(intent, SELECT_CONTACT);
	        }
	        
	    });
	    
	    //Add the new header to the list view
	    this.getExpandableListView().addHeaderView(headerView);
	    
	    //Setup the adapter 
	    adapter = new KeyManagementAdapter(this);
		this.setListAdapter(adapter);
		
		//Load the adapter
		adapter.populate();
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            switch(requestCode) {
            case SELECT_CONTACT:
            	if(resultCode == Activity.RESULT_OK) {
            		
	            	//Get the information we need
	            	String name = data.getStringExtra("name");  
	            	String address = data.getStringExtra("address");
	            	
	            	//Clean the address of garbage
	            	address = address.replaceAll("[^0-9]", "");
	            	
	            	//Setup an intent to create a new contact
	            	Intent intent = new Intent(this, NewContactActivity.class);
					intent.putExtra("address", address);
					intent.putExtra("name", name);
					this.startActivityForResult(intent, SETUP_KEY);
            	}
	            break;
            case SETUP_KEY:
            	if(resultCode == Activity.RESULT_OK) {
            		//Get information back from the NewContactActivity
            		String address = data.getStringExtra("address");
            		String name = data.getStringExtra("name");
            		byte[] key = data.getByteArrayExtra("key");
            		
            		//Create the new contact and fill in the data
            		Key contact = new Key();
            		contact.displayName = name;
            		contact.key = key;
                	
            		//Add the contact
                	KeyManager.getInstance().getLookup().put(address, contact);
                	
                	
                	//Re-Populate the listing
                	adapter.populate();
            	}
            	break;
            }
        }
    }

	@Override
	public void onResume(){
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		try {
    		//Commit it to the db
    		KeyManager.getInstance().commit(this);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "An error occured while saving the keystore.", Toast.LENGTH_SHORT);
		}
	}
	
	/**
	 * An adapter that displays contacts
	 * @author Richard Laughlin
	 */
	private static class KeyManagementAdapter extends BaseExpandableListAdapter {

		private abstract class ClickListener implements OnClickListener {
			protected Contact contact;
			
			public ClickListener(Contact contact) {
				this.contact = contact;
			}
			
		}
		
		private int lastExpandedGroupPosition = -1;
		private KeyManagementActivity context;
		private ArrayList<Contact> contacts = new ArrayList<Contact>();
		
		public KeyManagementAdapter(KeyManagementActivity context) {
			this.context = context;
		}
		
		public void populate() {
			//Clear the contacts to avoid duplicates
			contacts.clear();
			
			//Load the contacts from the KeyManager map into the list
			for(Entry<String, Key> entry : KeyManager.getInstance().getLookup().entrySet()) {
				Contact c = new Contact();
				Key value = entry.getValue();
				
				c.address = entry.getKey();
				c.name = value.displayName;
				
				contacts.add(c);
			}
			
			//Sort the list in alphabetical order
			java.util.Collections.sort(contacts, new Comparator<Contact>() {

				public int compare(Contact arg0, Contact arg1) {
					return arg0.name.compareTo(arg1.name);
				}
				
			});
			
			//Notify to update the view
			this.notifyDataSetChanged();
		}
		
		public Object getChild(int groupPosition, int childPosition) {
			return contacts.get(groupPosition);
		}

		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent) {
			Contact contact = (Contact) getGroup(groupPosition);
			if(view == null) {
				LayoutInflater inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inf.inflate(R.layout.management_child, null);
			}
			
			Button edit = (Button) view.findViewById(R.id.edit);
			edit.setOnClickListener(new ClickListener(contact) {

				public void onClick(View v) {
					Intent intent = new Intent(context, NewContactActivity.class);
					intent.putExtra("address", contact.address);
					intent.putExtra("name", contact.name);
					intent.putExtra("key", KeyManager.getInstance().getLookup().get(contact.address).key);
					context.startActivityForResult(intent, SETUP_KEY);
				}
				
			});
			
			Button viewButton = (Button) view.findViewById(R.id.view);
			viewButton.setOnClickListener(new ClickListener(contact) {
				public void onClick(View v) {
					Key key = KeyManager.getInstance().getLookup().get(contact.address);
					
					//Display the QR code
            		(new IntentIntegrator(context)).shareText(new String(Base64.encode(key.key)));
				}
			});
			
			Button delete = (Button) view.findViewById(R.id.delete);
			delete.setOnClickListener(new ClickListener(contact) {
				public void onClick(View v) {
					Dialogs.showConfirmation(context, R.string.confrimation_msg, new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							KeyManager.getInstance().getLookup().remove(contact.address);
							populate();
						}
					});
				}
			});
			

			
			return view;
		}

		public int getChildrenCount(int groupPosition) {
			return 1;
		}

		public Object getGroup(int groupPosition) {
			return contacts.get(groupPosition);
		}

		public int getGroupCount() {
			return contacts.size();
		}

		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
	    public void onGroupExpanded(int groupPosition){
	        //collapse the old expanded group, if not the same
	        //as new group to expand
	        if(groupPosition != lastExpandedGroupPosition){
	            context.getExpandableListView().collapseGroup(lastExpandedGroupPosition);
	        }

	        super.onGroupExpanded(groupPosition);           
	        lastExpandedGroupPosition = groupPosition;
	    }
		
		public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup parent) {
			
			Contact contact = (Contact) getGroup(groupPosition);
			if(view == null) {
				LayoutInflater inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inf.inflate(R.layout.management_group, null);
			}
			
			TextView tv = (TextView) view.findViewById(R.id.contact);
			tv.setText(contact.name);
			
			return view;
		}

		public boolean hasStableIds() {
			return true;
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
		
	}

	
}
