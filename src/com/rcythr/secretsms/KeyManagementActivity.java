package com.rcythr.secretsms;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map.Entry;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.util.encoders.Base64;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.rcythr.secretsms.GenerateActivity.GeneratedData;
import com.rcythr.secretsms.keymanagement.Contact;
import com.rcythr.secretsms.keymanagement.Key;
import com.rcythr.secretsms.keymanagement.KeyManager;
import com.rcythr.secretsms.util.Dialogs;
import com.rcythr.masq.R;

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


@SuppressWarnings("deprecation")
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
    	
    	final ToggleButton toggleEncryption = (ToggleButton) headerView.findViewById(R.id.encryption_toggle);
    	toggleEncryption.setChecked(KeyManager.instance.isPasswordProtected());
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
									KeyManager.instance.setKeyStoneKey(md.digest());
									KeyManager.instance.setPasswordProtected(true);
									KeyManager.instance.commit(KeyManagementActivity.this);
									return;
								} catch(Exception e) {
									e.printStackTrace();
								}
							} 
							KeyManager.instance.setPasswordProtected(false);
							toggleEncryption.setChecked(false);
						}
					});
					
					alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							KeyManager.instance.setPasswordProtected(false);
							toggleEncryption.setChecked(false);
						}
					});
					
					alert.show();
				} else {
					try {
						KeyManager.instance.setPasswordProtected(false);
						KeyManager.instance.commit(KeyManagementActivity.this);
					} catch(Exception e) {
						e.printStackTrace();
						Toast.makeText(KeyManagementActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
    	
    	final Button moveKeystore = (Button) headerView.findViewById(R.id.moveKeystore);
    	moveKeystore.setText((KeyManager.instance.isInternalStorage()) ? R.string.move_to_external : R.string.move_to_internal);
    	moveKeystore.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if(KeyManager.instance.swap(KeyManagementActivity.this)) {
					moveKeystore.setText((KeyManager.instance.isInternalStorage()) 
							? R.string.move_to_external : R.string.move_to_internal);
				} else {
					Toast.makeText(KeyManagementActivity.this, R.string.move_error, Toast.LENGTH_SHORT).show();
				}
			}
		});
    	
    	Button resetKeystore = (Button) headerView.findViewById(R.id.resetKeystore);
    	resetKeystore.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Dialogs.showConfirmation(KeyManagementActivity.this, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						Context context = KeyManagementActivity.this;
						KeyManager.instance.delete(context);
						
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
				Intent intent = new Intent(Intent.ACTION_PICK, Contacts.Phones.CONTENT_URI);
	            startActivityForResult(intent, SELECT_CONTACT);
	        }
	        
	    });
	    
	    this.getExpandableListView().addHeaderView(headerView);
	    
	    adapter = new KeyManagementAdapter(this);
	    
	    if(savedInstanceState != null) {
	    	adapter.pendingRequestContact = savedInstanceState.getString("pendingRequestContact");
	    }
	    
		this.setListAdapter(adapter);
		adapter.populate();
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            switch(requestCode) {
            case GENERATE_PERMA_KEY_RESULT:
            	if(resultCode == Activity.RESULT_OK){
            		GeneratedData generatedData = (GeneratedData) data.getSerializableExtra("data");
            		
            		Key key = KeyManager.instance.getLookup().get(adapter.pendingRequestContact);
            		key.permanentKey = generatedData.data;
            		
            		try {
						KeyManager.instance.commit(this);
						adapter.populate();
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
            case SELECT_CONTACT:
            	if(resultCode == Activity.RESULT_OK) {
	        		Uri contactData = data.getData();
	                Cursor c =  managedQuery(contactData, null, null, null, null);
	                startManagingCursor(c);
	                if (c.moveToFirst()) {
	                	String name = c.getString(c.getColumnIndexOrThrow(People.NAME));  
	                	String number = c.getString(c.getColumnIndexOrThrow(People.NUMBER));
	                	
	                	number = number.replaceAll("[^0-9]", "");
	                	
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
            		
            		Key key = KeyManager.instance.getLookup().get(adapter.pendingRequestContact);
            		key.permanentKey = Base64.decode(result.getContents());
            		
            		try {
						KeyManager.instance.commit(this);
						adapter.populate();
					} catch (InvalidCipherTextException e) {
						e.printStackTrace();
						//TODO: HANDLE ME
					} catch (IOException e) {
						e.printStackTrace();
						//TODO: HANDLE ME
					}
            		
            	} else {
            		adapter.pendingRequestContact = null;
            	}
            	break;
            }
        }
    }
    
	@Override
	public void onSaveInstanceState(Bundle bundle) {
		bundle.putString("pendingRequestContact", adapter.pendingRequestContact);
	}

	@Override
	public void onResume(){
		super.onResume();
	}
	
	private static class KeyManagementAdapter extends BaseExpandableListAdapter {

		public abstract class ClickListener implements OnClickListener {
			protected Contact contact;
			
			public ClickListener(Contact contact) {
				this.contact = contact;
			}
			
		}
		
		private int lastExpandedGroupPosition = -1;
		private KeyManagementActivity context;
		private ArrayList<Contact> contacts = new ArrayList<Contact>();
		
		public String pendingRequestContact = null;
		
		public KeyManagementAdapter(KeyManagementActivity context) {
			this.context = context;
		}
		
		public void populate() {
			contacts.clear();
			
			for(Entry<String, Key> entry : KeyManager.instance.getLookup().entrySet()) {
				Contact c = new Contact();
				Key value = entry.getValue();
				
				c.address = entry.getKey();
				c.name = value.displayName;
				c.hasPermaKey = value.permanentKey != null;
				
				contacts.add(c);
			}
			
			java.util.Collections.sort(contacts, new Comparator<Contact>() {

				public int compare(Contact arg0, Contact arg1) {
					return arg0.name.compareTo(arg1.name);
				}
				
			});
			
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
			
			Button genPerma = (Button) view.findViewById(R.id.genPerma);
			genPerma.setOnClickListener(new ClickListener(contact) {

				public void onClick(View v) {
					Intent intent = new Intent(context, GenerateActivity.class);
					pendingRequestContact = contact.address;
					intent.putExtra("count", 1);
					context.startActivityForResult(intent, KeyManagementActivity.GENERATE_PERMA_KEY_RESULT);
				}
				
			});
			
			Button delete = (Button) view.findViewById(R.id.delete);
			delete.setOnClickListener(new ClickListener(contact) {
				public void onClick(View v) {
					Dialogs.showConfirmation(context, new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							KeyManager.instance.getLookup().remove(contact.address);
							try {
								KeyManager.instance.commit(context);
							} catch (InvalidCipherTextException e) {
								e.printStackTrace();
								//TODO: HANDLE ME
							} catch (IOException e) {
								e.printStackTrace();
								//TODO: HANDLE ME
							}
							populate();
						}
					});
				}
			});
			
			Button addButton = (Button) view.findViewById(R.id.addPerma);
			addButton.setOnClickListener(new ClickListener(contact) {
				public void onClick(View v) {
					IntentIntegrator integrator = new IntentIntegrator(context);
					pendingRequestContact = contact.address;
				    integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES, KeyManagementActivity.SCAN_PERMA_CODE);
				}
			});
			
			Button removeButton = (Button) view.findViewById(R.id.viewPerma);
			removeButton.setOnClickListener(new ClickListener(contact) {
				public void onClick(View v) {
					Key key = KeyManager.instance.getLookup().get(contact.address);
					
					//Display the QR code
            		(new IntentIntegrator(context)).shareText(new String(Base64.encode(key.permanentKey)));
				}
			});
			
			removeButton.setEnabled(contact.hasPermaKey);
			
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
