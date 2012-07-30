package com.rcythr.secretsms;

import com.secretconversations.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SecretConversationsActivity extends Activity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ((Button) findViewById(R.id.button1)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				SecretConversationsActivity.this.startActivity(new Intent(SecretConversationsActivity.this, SMSSending.class));
			}
		});
        
        ((Button) findViewById(R.id.button2)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				SecretConversationsActivity.this.startActivity(new Intent(SecretConversationsActivity.this, SMSListing.class));
			}
		});
        
        ((Button) findViewById(R.id.button3)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
			}
		});
        
        ((Button) findViewById(R.id.button4)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
			}
		});
    }
    
}