package com.rcythr.secretsms;

import java.io.Serializable;

import com.rcythr.masq.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

public class GenerateActivity extends Activity {
	
	private ProgressBar progress;
	
	public static class GeneratedData implements Serializable {
		private static final long serialVersionUID = 1L;
		
		public byte[] data = new byte[32];
	}
	
	private GeneratedData generatedData;
	private int cursor;
	
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		setContentView(R.layout.generate);
		
		generatedData = new GeneratedData();
		
		progress = (ProgressBar) findViewById(R.id.generateProgress);
		
		Button begin = (Button) findViewById(R.id.begin);
		begin.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				findViewById(R.id.generateExplain).setVisibility(View.GONE);
				findViewById(R.id.begin).setVisibility(View.GONE);
				
				progress.setMax(32);
				progress.setProgress(0);
				
				View view = ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0);
				view.setOnTouchListener(new OnTouchListener() {
					
					public boolean onTouch(View v, MotionEvent event) {
						if(cursor == 32) {
							Intent intent = new Intent();
							intent.putExtra("data", generatedData);
							GenerateActivity.this.setResult(RESULT_OK, intent);
							GenerateActivity.this.finish();
						} else {
						
							generatedData.data[cursor] = (byte) ((event.getX() % 1) * 255);
							generatedData.data[cursor+1] = (byte) ((event.getY() % 1) * 255);
							
							cursor += 2;
							
							progress.setProgress(cursor);
						}
						return true;
					}
				});
			}
		});
		
		
	}
	
}
