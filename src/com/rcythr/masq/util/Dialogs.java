package com.rcythr.masq.util;


import com.rcythr.masq.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/**
 * Utility class for showing confirmation dialogs
 * @author Richard Laughlin
 */
public class Dialogs {

	/**
	 * Shows a conversation dialog
	 * @param context the context to use
	 * @param onYes a click listener for what to do when they say yes
	 */
	public static void showConfirmation(Context context, int message, OnClickListener onYes) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
 
			// set title
			alertDialogBuilder.setTitle(R.string.confirmation);
 
			// set dialog message
			alertDialogBuilder
				.setMessage(message)
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
