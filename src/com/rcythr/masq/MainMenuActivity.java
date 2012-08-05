/**	This file is part of Masq.

    Masq is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Masq is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Masq.  If not, see <http://www.gnu.org/licenses/>.
**/

package com.rcythr.masq;

import com.rcythr.masq.R;
import com.rcythr.masq.keymanagement.KeyManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * The activity for the Main Menu
 * @author Richard Laughlin
 */
public class MainMenuActivity extends Activity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
			KeyManager.getInstance().init(this);
		} catch (Exception e) {
			Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_LONG);
		}
        
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