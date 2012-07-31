package com.rcythr.secretsms;

import org.bouncycastle.crypto.InvalidCipherTextException;

import com.secretconversations.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SMSSending extends Activity {

	public static byte[] key = new byte[] {
			0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00,
			
			0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00,
	};
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send);
        
        final EditText text = (EditText) findViewById(R.id.editText1);
        
        ((Button) findViewById(R.id.next)).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		        try {
					sendIntent.putExtra(Intent.EXTRA_TEXT, AES.toHex(AES.handle(true, text.getText().toString().getBytes(), key)));
				} catch (InvalidCipherTextException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
		        sendIntent.setType("vnd.android-dir/mms-sms");
		        startActivity(sendIntent);
			}
			
		});
    }
	
}
