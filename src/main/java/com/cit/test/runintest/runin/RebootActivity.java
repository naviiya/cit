package com.cit.test.runintest.runin;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.cit.test.R;
import com.cit.test.runintest.DeviceTest;
import com.cit.test.runintest.TestCase.RESULT;

import java.io.File;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

public class RebootActivity extends Activity {

	
	public final static String TAG = "RebootActivity";
	
//	public final static String INTENT_NAME = "android.intent.action.StandbyBroadcast";
	AlarmManager am = null;
	PendingIntent send;
	Handler handler;
	Runnable runnable;
//	Context context = null;
	private int total = 0;
	private int runTime = 0;
	
//	private TimeCount time;
	private CountDownTimer time;
	
//	private long settime;
	PowerManager powerMgr;


	private PendingIntent mPendingIntent;
	private AlarmManager mAlarmManager;
	private PowerManager mPowerManager;
	
	private CountDownTimer mTimer;
	private Context mContext;
	
	private TextView mViewTimesSet;
	private TextView mViewTimesDone;
	
	private TextView mViewTimeout;
	private Button mBtnStop;
	
	public int mRebootSet = 0;
	public int mRebootDone = 0;
	
	public String mFileReboot = DeviceTest.FILE_REBOOT;
	
	public final static String EXTRA_NAME_STANDBY_SET = "StandbySet";
	public final static String EXTRA_NAME_STANDBY_DONE = "StandbyDone";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
		
        setContentView( R.layout.runin_reboot );
        
//        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE); 
//        pm.reboot("Stress Test"); 

        
	
		mContext = getBaseContext();

//		mStandbySet = getIntent().getIntExtra( EXTRA_NAME_STANDBY_SET, 0 );
//		int done = getIntent().getIntExtra( EXTRA_NAME_STANDBY_DONE, -1 );
//		if ( done == -1 ) { // first run
		
			//File resultFile = new File("/sdcard/Reboot_Standby.txt");
			//int totaltmp = Integer.parseInt(readerText(resultFile));
			//total = totaltmp;
		
	// res = EncodingUtils.getString(buffer, "UTF-8");
			String str = DeviceTest.readFileLine( mFileReboot );
			Log.i(TAG, "read file (" + mFileReboot + "): " + str );
			
			String[] s = str.split(",");
			mRebootSet = Integer.valueOf( s[0] );
			mRebootDone = Integer.valueOf( s[1] );
			Log.i(TAG, "onCreate(): mRebootSet: " + mRebootSet + "; mRebootDone: " + mRebootDone);
//		}
//		else {
//			Log.i(TAG, "mStandbySet: " + mStandbySet + "; mStandbyDone: " + mStandbyDone);
//			mStandbyDone = done;
			if ( mRebootDone >= mRebootSet ) {
				// Reboot测试完成
				File f = new File( mFileReboot );
				f.delete();
				this.setResult( RESULT.OK.ordinal() );
				finish();               //not effect  by lsg
				Log.i(TAG, "onCreate(): Reboot() Ok ..." );
			}
//		}	
			Log.i(TAG, "mRebootDone >= mRebootSet failed onCreate(): Prepare Reboot()  ..." );
		
		mViewTimesSet = (TextView) findViewById( R.id.runin_times_set );
		mViewTimesDone = (TextView) findViewById( R.id.runin_times_done );
		
		mViewTimesSet.setText( "" + mRebootSet );
		mViewTimesDone.setText( "" + mRebootDone );

		mViewTimeout = (TextView)findViewById(R.id.runin_timeout);
		mBtnStop = (Button)findViewById(R.id.runin_btn_stop);
		
//		time.start();
		mBtnStop.setOnClickListener( new OnClickListener() {
			public void onClick( View v ) {
				mTimer.cancel();
				File f = new File( mFileReboot );
				f.delete();

//				if ( mAlarmManager != null ) {
//					mAlarmManager.cancel(mPendingIntent);
//					mPendingIntent.cancel();
//				}
				setResultValue( RESULT.UNDEF.ordinal() );
				finish();
			}
		});
		
		mTimer = new CountDownTimer( 5000, 1000 ) {
			public void onTick( long millisUntilFinished ) {
				mViewTimeout.setText( millisUntilFinished/1000 + "秒" );
				Log.i(TAG, "onTick(): " + millisUntilFinished/1000 + "秒" );
			}
			public void onFinish() {
				Log.i(TAG, "onFinish(): start Reboot() ..." );
				mViewTimeout.setText( "0秒" );
//				wakeup( mContext );
				// 设置平板唤醒时执行的操作，原代码是执行RebootReceiver，在里面进行次数判断及处理
				long time = 10000;
			//	Intent i = new Intent( this, RebootReceiver.class );
			//	i.setAction( "wakeup" );
//				Intent i = new Intent( mContext, StandbyActivity.class );
//				i.putExtra(EXTRA_NAME_STANDBY_DONE, (mStandbyDone+1));
//				i.putExtra( EXTRA_NAME_STANDBY_SET, mStandbySet );
//				mPendingIntent = PendingIntent.getBroadcast(mContext, 3, i, PendingIntent.FLAG_CANCEL_CURRENT);
				
//				long interval = System.currentTimeMillis() + 10000;
//				mAlarmManager = (AlarmManager) getSystemService( Context.ALARM_SERVICE );
//				mAlarmManager.set(AlarmManager.RTC_WAKEUP, interval, mPendingIntent);
				
				// 增加计数，保存文件
				mRebootDone += 1;
				String s = mRebootSet + "," + mRebootDone;
				DeviceTest.writeFileLine( mFileReboot, s, false );
				Log.i(TAG, "[reboot] mRebootSet: " + mRebootSet + "; mRebootDone: " + mRebootDone);
				
				// 执行休眠操作
				mPowerManager = (PowerManager) getSystemService( Context.POWER_SERVICE );
				Log.i( TAG, "PowerManager.reboot************************** " );
				mPowerManager.reboot( "runin_reboot" );
				finish();
			}
		}.start();
	}

	//public void setResult( int result ) { // activity.setResult()
	public void setResultValue( int result ) {
		this.setResult(result);
	}

	public boolean dispatchKeyEvent( KeyEvent event ) {
		if ( event.getKeyCode() == KeyEvent.KEYCODE_BACK ) {
			//this.setResult( RESULT.UNDEF.ordinal() );
			//finish();
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		Log.i( TAG, "onPause************************** " );
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.i( TAG, "onResume************************** " );
		super.onResume();
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		Log.i( TAG, "onStart************************** " );
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.i( TAG, "onStop************************** " );
		super.onStop();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.i( TAG, "onDestroy************************** " );
		super.onDestroy();
	}
}
