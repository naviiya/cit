package com.cit.test;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 *
 */

public class RecoverBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "RecoverBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("com.android.settings.MasterClear")) {
            Log.i(TAG, "onReceive: ");
            materClear(context);
        }
    }

    public static void materClear(Context context)
    {
        Intent clearIntent = new Intent();
        ComponentName cn = new ComponentName("com.android.settings","com.android.settings.MasterClear");
        clearIntent.setComponent(cn);
        context.startService(clearIntent);
    }
}
