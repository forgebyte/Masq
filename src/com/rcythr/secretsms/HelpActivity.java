package com.rcythr.secretsms;

import com.rcythr.masq.R;

import android.app.Activity;
import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class HelpActivity extends ExpandableListActivity {

	@Override
	public void onCreate(Bundle instanceData) {
		super.onCreate(instanceData);
		setContentView(R.layout.help);
		
		String[] questions = getResources().getStringArray(R.array.questions);
		String[] answers = getResources().getStringArray(R.array.answers);
		
		getExpandableListView().setAdapter(new HelpAdapter(this, questions, answers));
	}
	
	private static class HelpAdapter extends BaseExpandableListAdapter {

		private Activity context;
		String[] questions;
		String[] answers;
		
		public HelpAdapter(Activity context, String[] questions, String[] answers) {
			assert questions.length == answers.length;
			
			this.context = context;
			this.questions = questions;
			this.answers = answers;
		}
		
		public Object getChild(int groupPosition, int childPosition) {
			return answers[groupPosition];
		}

		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		public View getChildView(int groupPosition, int childPosition, boolean isExpanded, View view, ViewGroup parent) {
			if(view == null) {
				LayoutInflater inflater = context.getLayoutInflater();
				view = inflater.inflate(R.layout.help_child, null);
			}
			
			TextView textView = (TextView) view.findViewById(R.id.text);
			textView.setText(answers[groupPosition]);
			
			return view;
		}

		public int getChildrenCount(int arg0) {
			return 1;
		}

		public Object getGroup(int groupPosition) {
			return questions[groupPosition];
		}

		public int getGroupCount() {
			return questions.length;
		}

		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup parent) {
			if(view == null) {
				LayoutInflater inflater = context.getLayoutInflater();
				view = inflater.inflate(R.layout.help_group, null);
			}
			
			TextView textView = (TextView) view.findViewById(R.id.text);
			textView.setText(questions[groupPosition]);
			
			return view;
		}

		public boolean hasStableIds() {
			return true;
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}
		
	}
	
}
