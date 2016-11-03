package com.cit.test.runintest.runin;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.cit.test.R;
import com.cit.test.runintest.DeviceTest;
import com.cit.test.runintest.StandbyReceiver;
import com.cit.test.runintest.TestCase.RESULT;

import java.io.File;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

public class StandbyActivity extends Activity {
	
	public final static String TAG = "StandbyActivity";
	
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
	private CountDownTimer mTimerWakeup;
	private Context mContext;
	
	private TextView mViewTimesSet;
	private TextView mViewTimesDone;
	
	private TextView mViewTimeout;
	private Button mBtnStop;
	
	public int mStandbySet = 0;
	public int mStandbyDone = 0;
	
	public String mFileStandby = DeviceTest.FILE_STANDBY;
	
	public final static String EXTRA_NAME_STANDBY_SET = "StandbySet";
	public final static String EXTRA_NAME_STANDBY_DONE = "StandbyDone";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
		
		setContentView( R.layout.runin_standby );
		mContext = getBaseContext();

		mStandbySet = getIntent().getIntExtra( EXTRA_NAME_STANDBY_SET, 0 );
		int done = getIntent().getIntExtra( EXTRA_NAME_STANDBY_DONE, -1 );
		if ( done == -1 ) { // first run
		
			//File resultFile = new File("/sdcard/Reboot_Standby.txt");
			//int totaltmp = Integer.parseInt(readerText(resultFile));
			//total = totaltmp;
		
	// res = EncodingUtils.getString(buffer, "UTF-8");
			String str = DeviceTest.readFileLine( mFileStandby );
			Log.i(TAG, "read file (" + mFileStandby + "): " + str );
			
			String[] s = str.split(",");
			mStandbySet = Integer.valueOf( s[0] );
			mStandbyDone = Integer.valueOf( s[1] );
			Log.i(TAG, "get from File --> mStandbySet: " + mStandbySet + "; mStandbyDone: " + mStandbyDone);
		}
		else {
			mStandbyDone = done;
			Log.i(TAG, "get from Extra --> mStandbySet: " + mStandbySet + "; mStandbyDone: " + mStandbyDone);
			
			// 增加计数，保存文件
		//	mStandbyDone += 1;
			String s = mStandbySet + "," + mStandbyDone;
			DeviceTest.writeFileLine( mFileStandby, s, false );
			Log.i(TAG, "[standby] mStandbySet: " + mStandbySet + "; mStandbyDone: " + mStandbyDone);


			if ( mStandbyDone >= mStandbySet ) {
				// 休眠测试完成
				File f = new File( mFileStandby );
				f.delete();
				
				this.setResult( RESULT.OK.ordinal() );
				Log.i(TAG, "[standby] <<OK>> mStandbySet: " + mStandbySet + "; mStandbyDone: " + mStandbyDone);

				if ( mTimer != null ) {
					mTimer.cancel();
				}
				
				if ( mAlarmManager != null ) {
					mAlarmManager.cancel(mPendingIntent);
					mPendingIntent.cancel();
				}
				setResult( RESULT.OK.ordinal() );
				finish();
			}
		}
		
		
		mViewTimesSet = (TextView) findViewById( R.id.runin_times_set );
		mViewTimesDone = (TextView) findViewById( R.id.runin_times_done );
		
		mViewTimesSet.setText( "" + mStandbySet );
		mViewTimesDone.setText( "" + mStandbyDone );
//		File countRunFile = new File("/sdcard/countStandby.dat");
//		int countRuntmp = Integer.parseInt(readerText(countRunFile));
		//runTime = total - countRuntmp + 1;
		//runTime = countRuntmp;
		//TextView totalView = (TextView)findViewById(R.id.totalView);
		//TextView countRun = (TextView)findViewById(R.id.countRun);
		//totalView.setText(total + "");
		//countRun.setText(runTime + "");
		//stop watch
	//	settime = getTime();
		//hehg settime=10000;
//		settime=6000;
//		time = new TimeCount(settime, 1000);

		mViewTimeout = (TextView)findViewById(R.id.runin_timeout);
		mBtnStop = (Button)findViewById(R.id.runin_btn_stop);
		
//		time.start();
		mBtnStop.setOnClickListener( new OnClickListener() {
			public void onClick( View v ) {
				mTimer.cancel();
				File f = new File( mFileStandby );
				f.delete();

				if ( mAlarmManager != null ) {
					mAlarmManager.cancel(mPendingIntent);
					mPendingIntent.cancel();
				}
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
				mViewTimeout.setText( "0秒" );
				Log.i(TAG, "onFinish(): start wakeup() ..." );
			//	wakeup();
//				SystemUtil.execShellCmd( "input keyevent 26" );
			//	SystemUtil.execShellCmd( "standby.sh" );
			//	mTimerWakeup.start();
				// 设置平板唤醒时执行的操作，原代码是执行RebootReceiver，在里面进行次数判断及处理
				
			//	Intent i = new Intent( mContext, RebootReceiver.class );
			//	i.setAction( "wakeup" );
				Intent i = new Intent( mContext, StandbyReceiver.class );
				i.setAction( "wakeup" );
				i.putExtra(EXTRA_NAME_STANDBY_DONE, (mStandbyDone+1));
				i.putExtra( EXTRA_NAME_STANDBY_SET, mStandbySet );
				mPendingIntent = PendingIntent.getBroadcast(mContext, 3, i, PendingIntent.FLAG_CANCEL_CURRENT);
				//mPendingIntent = PendingIntent.getBroadcast(mContext, 0, i, 0);
				
				//long interval = System.currentTimeMillis() + 10000;
				long interval = System.currentTimeMillis() + 6000;
				mAlarmManager = (AlarmManager) getSystemService( Context.ALARM_SERVICE );
				mAlarmManager.set(AlarmManager.RTC_WAKEUP, interval, mPendingIntent);
				
				// 执行休眠操作
				mPowerManager = (PowerManager) getSystemService( Context.POWER_SERVICE );
//				mPowerManager.goToSleep( SystemClock.uptimeMillis() );
			//	SystemUtil.execShellCmd( "/system/bin/awaken.sh 2" );
				Log.i( TAG, "PowerManager.goToSleep************************** goToSleep: " + SystemClock.uptimeMillis() );
			//	setResult( RESULT.UNDEF.ordinal() );
			//	finish();
				
				mTimerWakeup.start();
			}
		};

		mTimerWakeup = new CountDownTimer( 6000, 1000 ) {
			public void onTick( long millisUntilFinished ) {
			//	mViewTimeout.setText( millisUntilFinished/1000 + "秒" );
				Log.i(TAG, "mTimeWakeup onTick(): " + millisUntilFinished/1000 + "秒" );
				//mViewTimeout.setText( "5秒" );
			}
			public void onFinish() {
				Log.i(TAG, "mTimeWakeup onFinish(): start wakeup() ..." );
			//	mViewTimeout.setText( "0秒" );
				//wakeup();
				//SystemUtil.execShellCmd( "input keyevent 26" );
//				SystemUtil.execShellCmd( "standby.sh" );
				
				mStandbyDone += 1;
				
				mViewTimesSet.setText( "" + mStandbySet );
				mViewTimesDone.setText( "" + mStandbyDone );
				String s = mStandbySet + "," + mStandbyDone;
				DeviceTest.writeFileLine( mFileStandby, s, false );
				Log.i(TAG, "[mTimeWakeup] mStandbySet: " + mStandbySet + "; mStandbySet: " + mStandbyDone);
				
				if ( mStandbyDone < mStandbySet ) {
					
					mTimer.start();
				}
				else {
					File f = new File( mFileStandby );
					f.delete();
					Log.i(TAG, "[mTimerWakeup.onFinish] <<OK>> mStandbySet: " + mStandbySet + "; mStandbyDone: " + mStandbyDone);

			//		if ( mTimer != null ) {
			//			mTimer.cancel();
			//		}
					
			//		if ( mAlarmManager != null ) {
			//			mAlarmManager.cancel(mPendingIntent);
			//			mPendingIntent.cancel();
			//		}
					setResult( RESULT.OK.ordinal() );
					finish();
				}
				// 设置平板唤醒时执行的操作，原代码是执行RebootReceiver，在里面进行次数判断及处理
			}
		};
		
		mTimer.start();
		
	}
	
//	public void setResult( int result ) { // activity.setResult()
	public void setResultValue( int result ) {
		this.setResult(result);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
/*		Log.i(TAG, "onDestroy() ..." );
		if ( mTimer != null ) {
			mTimer.cancel();
		}
		
		if ( mAlarmManager != null ) {
			mAlarmManager.cancel(mPendingIntent);
			mPendingIntent.cancel();
		}
	*/
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	/*		
	Intent intent = new Intent(Demo2Activity.this, AlarmReceiver.class);  
    PendingIntent pi = PendingIntent.getBroadcast(Demo2Activity.this, 0, intent, 0);  
    //得到AlarmManager实例  
    AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);  
    //根据当前时间预设一个警报  
    am.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);  
     
     // 第一个参数是警报类型；第二个参数是第一次执行的延迟时间，可以延迟，也可以马上执行；第三个参数是重复周期为一天 
     //这句话的意思是设置闹铃重复周期，也就是执行警报的间隔时间 
     //  
//  am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+(60*1000),   
//          (24*60*60*1000), pi);  
    am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+(10*1000),   
            30000, pi);  	
*/	
/*	private void wakeup() {
	//	Intent i = new Intent( mContext, RebootReceiver.class );
	//	i.setAction( "wakeup" );
		Intent i = new Intent( StandbyActivity.this, StandbyReceiver.class );
		i.setAction( "wakeup" );
		i.putExtra(EXTRA_NAME_STANDBY_DONE, (mStandbyDone+1));
		i.putExtra( EXTRA_NAME_STANDBY_SET, mStandbySet );
		//mPendingIntent = PendingIntent.getBroadcast(StandbyActivity.this, 3, i, PendingIntent.FLAG_CANCEL_CURRENT);
		mPendingIntent = PendingIntent.getBroadcast(StandbyActivity.this, 0, i, 0);
		
		//long interval = System.currentTimeMillis() + 10000;
		long interval = System.currentTimeMillis() + 6000;
		mAlarmManager = (AlarmManager) getSystemService( Context.ALARM_SERVICE );
		mAlarmManager.set(AlarmManager.RTC_WAKEUP, interval, mPendingIntent);

	
		
		// 执行休眠操作
		mPowerManager = (PowerManager) getSystemService( Context.POWER_SERVICE );
		mPowerManager.goToSleep( SystemClock.uptimeMillis() );
	//	SystemUtil.execShellCmd( "/system/bin/awaken.sh 2" );
		Log.i( TAG, "PowerManager.goToSleep************************** goToSleep: " + SystemClock.uptimeMillis() );
		finish();
	}
*/
/*	
	private void wakeup( Context context ) {
		
		// 设置平板唤醒时执行的操作，原代码是执行RebootReceiver，在里面进行次数判断及处理
		long time = 10000;
		Intent i = new Intent( this, RebootReceiver.class );
		i.setAction( "wakeup" );
		
		mPendingIntent = PendingIntent.getBroadcast(context, 3, i, PendingIntent.FLAG_CANCEL_CURRENT);
		
		long interval = System.currentTimeMillis() + 10000;
		mAlarmManager = (AlarmManager) getSystemService( Context.ALARM_SERVICE );
		mAlarmManager.set(AlarmManager.RTC_WAKEUP, interval, mPendingIntent);
		
		// 执行休眠操作
		mPowerManager = (PowerManager) getSystemService( Context.POWER_SERVICE );
		mPowerManager.goToSleep( SystemClock.uptimeMillis() );
		Log.i( TAG, "PowerManager.goToSleep************************** goToSleep: " + SystemClock.uptimeMillis() );
		finish();
	}
*/
/*
	private void wakeup(Context context){
		long time = 1*10*1000;
		Intent i = new Intent(this, RebootReceiver.class);
		i.setAction("wakeup");
		Log.i(TAG, "PendingIntent.getBroadcast()****************PendingIntent.FLAG_CANCEL_CURRENT**********" );
		send = PendingIntent.getBroadcast(context, 3, i, PendingIntent.FLAG_CANCEL_CURRENT);
		Log.i(TAG, "PendingIntent.getBroadcast()****************PendingIntent.FLAG_CANCEL_CURRENT*****ok*****" );
		long interval = System.currentTimeMillis() + time;
		am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, interval, send);
		
		Log.i(TAG, "AlarmManager.RTC_WAKEUP************************** set: " + interval );	
		powerMgr = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		powerMgr.goToSleep(SystemClock.uptimeMillis());
		Log.i(TAG, "PowerManager.goToSleep************************** goToSleep: " + SystemClock.uptimeMillis() );	
		finish();
	}

	void cancelAlarmManager(){
		am.cancel(send);
		send.cancel();
	}
	
	//Get time
	private int getTime(){
		String fileName = "/sdcard/time.dat";
		
		int seconds = 0;
		double timetmp;
		final String restmp;
		String res = "";
		try{
			FileInputStream fin = new FileInputStream(fileName);
			int length = fin.available();
			byte [] buffer = new byte[length];
			fin.read(buffer);
			res = EncodingUtils.getString(buffer, "UTF-8");
			fin.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		if(res!=""){
			restmp = res;
			timetmp = Double.parseDouble(restmp);
			seconds = (int)(timetmp*1000);
		}
		return seconds;
	}
	
	class TimeCount extends CountDownTimer{
		public TimeCount(long millisInFuture, long countDownInterval){
			super(millisInFuture, countDownInterval);
		}
		//
		public void onFinish(){
			Log.i(TAG, "onFinish(): start wakeup() ..." );
			wakeup(context);		
		}
		//
		public void onTick(long millisUntilFinished){
			displayTime.setText(millisUntilFinished/1000 + "��");
			Log.i(TAG, "onTick(): " + millisUntilFinished/1000 + "��");
		}
	}
	
	private String readerText(File fileName){
		String res = "";
		try{
			FileInputStream fin = new FileInputStream(fileName);
			int length = fin.available();
			byte [] buffer = new byte[length];
			fin.read(buffer);
			res = EncodingUtils.getString(buffer, "UTF-8");
			
			fin.close();
			Log.v(TAG, "readerText*************times: " + res);
		}catch(Exception e){
			Log.i(TAG, "FileName********************** Readfile error");
			e.printStackTrace();
		}
		return res;
		
	}
*/	
	public boolean dispatchKeyEvent( KeyEvent event ) {
		if ( event.getKeyCode() == KeyEvent.KEYCODE_BACK ) {
			//this.setResult( RESULT.UNDEF.ordinal() );
			//finish();
			return false;
		}
		return super.dispatchKeyEvent(event);
	}

}
