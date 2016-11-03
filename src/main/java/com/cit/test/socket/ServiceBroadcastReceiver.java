package com.cit.test.socket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(null == intent){
            return;
        }
        String action = intent.getAction();
        android.util.Log.d(AndroidService.TAG, "action=" + action);
        if(Intent.ACTION_BOOT_COMPLETED.equals(action)){
            context.startService(new Intent(context, AndroidService.class));
        }
    }

}
