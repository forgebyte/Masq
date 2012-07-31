package com.rcythr.secretsms.keymanagement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map.Entry;

import com.google.zxing.integration.android.IntentIntegrator;
import com.secretconversations.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

public class KeyManagementAdapter extends BaseExpandableListAdapter {
	
	public class Contact {
		public String address;
		public String name;
		public boolean hasPermaKey;
		public int privateKeyCount;
	}

	public abstract class ClickListener implements OnClickListener {
		protected Contact contact;
		
		public ClickListener(Contact contact) {
			this.contact = contact;
		}
		
	}
	
	private Activity context;
	private ArrayList<Contact> contacts = new ArrayList<Contact>();
	
	public Contact pendingRequestContact = null;
	
	public KeyManagementAdapter(Activity context) {
		this.context = context;
		populate();
	}
	
	public void populate() {
		contacts.clear();
		
		for(Entry<String, Key> entry : KeyManager.instance.getLookup().entrySet()) {
			Contact c = new Contact();
			Key value = entry.getValue();
			
			c.address = entry.getKey();
			c.name = value.displayName;
			c.hasPermaKey = value.permanentKey != null;
			c.privateKeyCount = value.singleUseKeys.size();
			
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
		TextView keyCount = (TextView) view.findViewById(R.id.remaining);
		
		Button clearSingle = (Button) view.findViewById(R.id.clearSingle);
		clearSingle.setOnClickListener(new ClickListener(contact) {
			public void onClick(View v) {
				
			}
		});
		
		Button genPerma = (Button) view.findViewById(R.id.genPerma);
		genPerma.setOnClickListener(new ClickListener(contact) {

			public void onClick(View v) {
				Intent intent = new Intent(context, GenerateActivity.class);
				pendingRequestContact = contact;
				intent.putExtra("count", 1);
				context.startActivityForResult(intent, KeyManagementActivity.GENERATE_PERMA_KEY_RESULT);
			}
			
		});
		
		Button genSingle = (Button) view.findViewById(R.id.genSingle);
		genSingle.setOnClickListener(new ClickListener(contact) {
			public void onClick(View v) {
				Intent intent = new Intent(context, GenerateActivity.class);
				pendingRequestContact = contact;
				context.startActivityForResult(intent, KeyManagementActivity.GENERATE_KEYS_RESULT);
			}
		});
		
		Button addSingle = (Button) view.findViewById(R.id.addSingle);
		addSingle.setOnClickListener(new ClickListener(contact) {
			public void onClick(View v) {
				IntentIntegrator integrator = new IntentIntegrator(context);
				pendingRequestContact = contact;
			    integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES, KeyManagementActivity.SCAN_SINGLE_CODE);
			}
		});
		
		Button delete = (Button) view.findViewById(R.id.delete);
		delete.setOnClickListener(new ClickListener(contact) {
			public void onClick(View v) {
				
			}
		});
		
		Button addButton = (Button) view.findViewById(R.id.addPerma);
		addButton.setOnClickListener(new ClickListener(contact) {
			public void onClick(View v) {
				IntentIntegrator integrator = new IntentIntegrator(context);
				pendingRequestContact = contact;
			    integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES, KeyManagementActivity.SCAN_PERMA_CODE);
			}
		});
		
		Button removeButton = (Button) view.findViewById(R.id.removePerma);
		removeButton.setOnClickListener(new ClickListener(contact) {
			public void onClick(View v) {
				
			}
		});
		
		genPerma.setEnabled(!contact.hasPermaKey);
		addButton.setEnabled(!contact.hasPermaKey);
		removeButton.setEnabled(contact.hasPermaKey);
	
		keyCount.setText(Integer.toString(contact.privateKeyCount) +" "+ context.getString(R.string.remaining));
		if(contact.privateKeyCount == 0) {
			clearSingle.setEnabled(false);
		} else {
			clearSingle.setEnabled(true);
		}
		
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
