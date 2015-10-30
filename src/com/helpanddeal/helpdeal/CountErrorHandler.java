package com.helpanddeal.helpdeal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.content.Context;

public class CountErrorHandler extends Handler {
	private Context context;
	CountErrorHandler(Context context) {
		this.context = context;
	}
	
	 @Override
    public void handleMessage(Message msg) {
		 
        // TODO Auto-generated method stub
		    AlertDialog alertDlg = new AlertDialog.Builder(context).
	    setTitle(R.string.count_error_title)
	       .setMessage(R.string.count_error_message)
	       .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {	         
	           @Override
	           public void onClick(DialogInterface dialog, int which) {
	               // TODO Auto-generated method stub
	           }
	        }).create();
		    alertDlg.show();
		 
		    //sg.mCountPref.setText("0");
		    //sg.mCountPref.setSummary("0");
    }
}
