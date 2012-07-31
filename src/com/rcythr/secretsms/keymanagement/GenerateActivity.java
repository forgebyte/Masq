package com.rcythr.secretsms.keymanagement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.secretconversations.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GenerateActivity extends Activity {
	
	private ProgressBar progress;
	private int generateCount;
	
	public static class GeneratedData implements Serializable {
		private static final long serialVersionUID = 1L;
		
		public List<Byte[]> data = new ArrayList<Byte[]>();
	}
	
	private GeneratedData generatedData;
	
	private Byte[] currentBlock;
	private int cursor;
	
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		setContentView(R.layout.generate);
		
		generatedData = new GeneratedData();
		
		progress = (ProgressBar) findViewById(R.id.generateProgress);
		
		if(this.getIntent() != null) {
			int count = getIntent().getIntExtra("count", 0);
			if(count != 0) {
				EditText countBox = (EditText) findViewById(R.id.generateCount);
				countBox.setText(Integer.toString(count));
				countBox.setVisibility(View.INVISIBLE);
				
				((TextView) findViewById(R.id.generateExplain)).setText(R.string.generate_explain_short);
			}
		}
		
		
		Button begin = (Button) findViewById(R.id.begin);
		begin.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				findViewById(R.id.generateExplain).setVisibility(View.GONE);
				EditText text = (EditText) findViewById(R.id.generateCount);
				findViewById(R.id.begin).setVisibility(View.GONE);
				
				String genText = text.getText().toString();
				if(genText == "") {
					genText = "1";
				}
				generateCount = Integer.parseInt(genText);
				
				text.setVisibility(View.GONE);
				
				progress.setMax(generateCount*32);
				progress.setProgress(0);
				
				View view = ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0);
				view.setOnTouchListener(new OnTouchListener() {
					
					public boolean onTouch(View v, MotionEvent event) {
						if(currentBlock == null) {
							if(generatedData.data.size() == generateCount) {
								//We're done here
								
								Intent intent = new Intent();
								intent.putExtra("data", generatedData);
								GenerateActivity.this.setResult(RESULT_OK, intent);
								GenerateActivity.this.finish();
							}
							
							currentBlock = new Byte[32];
							cursor = 0;
						}
						
						currentBlock[cursor] = (byte) ((event.getX() % 1) * 255);
						currentBlock[cursor+1] = (byte) ((event.getY() % 1) * 255);
						
						cursor += 2;
						
						progress.setProgress(generatedData.data.size()*32 + cursor);
						
						if(cursor == 32) {
							generatedData.data.add(currentBlock);
							currentBlock = null;
						}
						return true;
					}
				});
			}
		});
		
		
	}
	
}
