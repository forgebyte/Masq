package com.rcythr.masq;

import com.rcythr.masq.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * The activity for the Main Menu
 * @author Richard Laughlin
 */
public class MainMenuActivity extends Activity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ((Button) findViewById(R.id.sms)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				MainMenuActivity.this.startActivity(new Intent(MainMenuActivity.this, SMSActivity.class));
			}
		});
        
        ((Button) findViewById(R.id.keyManage)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				MainMenuActivity.this.startActivity(new Intent(MainMenuActivity.this, KeyManagementActivity.class));
			}
		});
        
        ((Button) findViewById(R.id.help)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				MainMenuActivity.this.startActivity(new Intent(MainMenuActivity.this, HelpActivity.class));
			}
		});
        
        ((Button) findViewById(R.id.exit)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				MainMenuActivity.this.finish();
			}
		});
    }
    
}