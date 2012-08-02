package com.rcythr.secretsms;

import com.rcythr.masq.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ((Button) findViewById(R.id.sms)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				MainActivity.this.startActivity(new Intent(MainActivity.this, SMSActivity.class));
			}
		});
        
        /*((Button) findViewById(R.id.steganograph)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
			}
		});*/
        
        ((Button) findViewById(R.id.keyManage)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				MainActivity.this.startActivity(new Intent(MainActivity.this, KeyManagementActivity.class));
			}
		});
        
        ((Button) findViewById(R.id.help)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				MainActivity.this.startActivity(new Intent(MainActivity.this, HelpActivity.class));
			}
		});
        
        ((Button) findViewById(R.id.exit)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				MainActivity.this.finish();
			}
		});
    }
    
}