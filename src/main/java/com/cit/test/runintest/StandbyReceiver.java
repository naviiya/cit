package com.cit.test.runintest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class StandbyReceiver extends BroadcastReceiver {
	
	public final static String TAG = "StandbyReceiver";
	
	public int mStandbySet = 0;
	public int mStandbyDone = 0;
	
	public String mFileStandby = DeviceTest.FILE_STANDBY;
	
	public final static String EXTRA_NAME_STANDBY_SET = "StandbySet";
	public final static String EXTRA_NAME_STANDBY_DONE = "StandbyDone";
	
	@Override
	public void onReceive(final Context context, Intent intent) {
		Log.i(TAG, "StandbyReceiver.onReceive() -----------intent.getAction(): " + intent.getAction());
		if ( intent.getAction().equals( "wakeup" ) ) {
			
		


		mStandbySet = intent.getIntExtra( EXTRA_NAME_STANDBY_SET, 0 );
		mStandbyDone = intent.getIntExtra( EXTRA_NAME_STANDBY_DONE, 0 );

			Log.i(TAG, "onReceive() --> mStandbySet: " + mStandbySet + "; mStandbyDone: " + mStandbyDone);

			Log.i(TAG, "PowerManager********************** wakeup");
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			WakeLock mWakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "SimpleTimer");
			mWakelock.setReferenceCounted(false);
			mWakelock.acquire();
            
/*			// start Standby ...
			Intent i = new Intent("android.intent.action.StandbyActivity");
		//	i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//i.putExtra(EXTRA_NAME_STANDBY_DONE, (mStandbyDone+1));
			i.putExtra(EXTRA_NAME_STANDBY_DONE, mStandbyDone);
			i.putExtra( EXTRA_NAME_STANDBY_SET, mStandbySet );
		//		Intent i = new Intent( mContext, StandbyActivity.class );
				//	i.setAction( "wakeup" );
				//	Intent i = new Intent( mContext, StandbyActivity.class );
		//			i.putExtra(EXTRA_NAME_STANDBY_DONE, (mStandbyDone+1));
		//			i.putExtra( EXTRA_NAME_STANDBY_SET, mStandbySet );
				
			Log.i(TAG, "startActivity --> mStandbySet: " + mStandbySet + "; mStandbyDone: " + mStandbyDone);
			context.startActivity(i);
*/		}
	}

}
