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
