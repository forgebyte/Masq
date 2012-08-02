package com.rcythr.secretsms.util;


import com.rcythr.masq.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class Dialogs {

	public static void showConfirmation(Context context, OnClickListener onYes) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
 
			// set title
			alertDialogBuilder.setTitle(R.string.confirmation);
 
			// set dialog message
			alertDialogBuilder
				.setMessage(R.string.confrimation_msg)
				.setCancelable(false)
				.setPositiveButton(R.string.yes,onYes)
				.setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {dialog.cancel();}
				});
 
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
	}
	
	
}
