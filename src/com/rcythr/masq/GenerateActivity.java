package com.rcythr.masq;

import com.rcythr.masq.R;
import com.rcythr.masq.util.AES;

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

/**
 * Activity for generating secure keys.
 * @author Richard Laughlin
 */
public class GenerateActivity extends Activity {
	
	private ProgressBar progress;
	
	public byte[] data = new byte[AES.AES_KEY_SIZE];
	private int cursor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		setContentView(R.layout.generate);
		
		//Grab the progress bar so we can modify it
		progress = (ProgressBar) findViewById(R.id.generateProgress);
		progress.setMax(AES.AES_KEY_SIZE);
		progress.setProgress(0);
		
		//Set the action of the begin button
		Button begin = (Button) findViewById(R.id.begin);
		begin.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				//Remove the explaination text
				findViewById(R.id.generateExplain).setVisibility(View.GONE);
				findViewById(R.id.begin).setVisibility(View.GONE);

				//Add an on touch listener so we can get Motion events
				View view = ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0);
				view.setOnTouchListener(new OnTouchListener() {
					
					public boolean onTouch(View v, MotionEvent event) {
						//If we're done finish this activity
						if(cursor == AES.AES_KEY_SIZE) {
							Intent intent = new Intent();
							intent.putExtra("data", data);
							GenerateActivity.this.setResult(RESULT_OK, intent);
							GenerateActivity.this.finish();
						} else {
							//Otherwise use the decimal part of the motion event to choose a byte value
							data[cursor] = (byte) ((event.getX() % 1) * 255);
							data[cursor+1] = (byte) ((event.getY() % 1) * 255);
							
							//Advance the cursor and progress bar
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
