package com.helpanddeal.helpdeal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";
    private static final String INTENT_START_GREETING = "intent.greeting.START";
    
    @Override
    public void onReceive(Context mcontext, Intent mIntent) {
        String action = mIntent.getAction();

        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
        	  Log.d(TAG, "start Service");
            mcontext.startService(new Intent(INTENT_START_GREETING));
        }
    }
}

