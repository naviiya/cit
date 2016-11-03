package com.cit.test.runintest.runin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.cit.test.R;
import com.cit.test.runintest.DeviceTest;
import com.cit.test.runintest.FileUtility;
import com.cit.test.runintest.TestCase.RESULT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
//hehg import jp.co.byd.Charge;

/*
 * [CHG-0903] change the range of capacity to 58%~60%
 * [CHG-0917] change the range of capacity to 55%~58%, if >= 58, discharge to 55; if <= 55, charge to 58.
 */
public class VideoPlayActivity extends Activity
	implements OnBufferingUpdateListener, OnCompletionListener, MediaPlayer.OnPreparedListener, SurfaceHolder.Callback {
	
	public final static String TAG = "VideoPlayActivity";
	/** Called when the activity is first created. */

	//public final static String FILE_CPU_TEMP = "/sys/devices/platform/rk30_i2c.1/i2c-1/1-004c/rtemperature"; // UNIT: ℃
	//public final static String FILE_CPU_TEMP = "/sys/class/hwmon/hwmon1/device/temp2_input"; // millidegree Celsius
	public final static String FILE_CPU_TEMP = "/sys/class/thermal/thermal_zone0/temp"; // millidegree Celsius #device -> ../../../coretemp.0
	
//	public final static String SYSFS_CHARGER_CTRL = "/sys/bus/i2c/devices/5-0034/dollar_cove_charger/";
//	public final static String SYSFS_CHARGER_CTRL__VBUS_DISABLE = SYSFS_CHARGER_CTRL + "vbus_path_dis"; //0:VBUS selected; 1:VBUS not selected.

	//public final static String SYSFS_CHARGER_CTRL = "/sys/class/power_supply/dollar_cove_charger/device/ChargerEnable";
	public final static String SYSFS_CHARGER_CTRL = "/sys/class/power_supply/bq24192_charger/device/factory_charger_ctl";
	//public final static String FILE_BATTERY_CTRL = "/sys/devices/platform/rk30_i2c.1/i2c-1/1-0032/ricoh619-battery.16/";
	
	// 0: enable; 1: disable.
	//public final static String FILE_BATTERY_CTRL_ENABLE_BATTERY = FILE_BATTERY_CTRL + "charge_enable_battery";
	//public final static String FILE_BATTERY_CTRL_ENABLE_INPUT = FILE_BATTERY_CTRL + "charge_enable_input";
//     private final static String chargeSysFile = 
//    		 "/sys/devices/platform/rk30_i2c.1/i2c-1/1-0032/ricoh619-battery.16/charge_enable_battery";
//     private final static String dischargeSysFile = 
//    		 "/sys/devices/platform/rk30_i2c.1/i2c-1/1-0032/ricoh619-battery.16/charge_enable_input";

	public final static String FILE_BACKLIGHT_BRIGHTNESS = "/sys/class/backlight/intel_backlight/brightness"; // =3124
	public final static String FILE_BACKLIGHT_MAX_BRIGHTNESS = "/sys/class/backlight/intel_backlight/max_brightness"; // =7812
	public final static String FILE_BACKLIGHT_ACTUAL_BRIGHTNESS = "/sys/class/backlight/intel_backlight/actual_brightness"; // =3124
//	public final static String FILE_BACKLIGHT_BRIGHTNESS = "/sys/class/backlight/rk28_bl/brightness"; // 修改背光亮度(1~255)
//	public final static String FILE_BACKLIGHT_MAX_BRIGHTNESS = "/sys/class/backlight/rk28_bl/max_brightness";
//	public final static String FILE_BACKLIGHT_ACTUAL_BRIGHTNESS = "/sys/class/backlight/rk28_bl/actual_brightness";
	public int mBrightnessBak = 0;
	public int mBrightness = 0;
	
	public final static String FILE_BATTERY = "/sys/class/power_supply/TBQ27541:00-0/";
 	//public final static String FILE_BATTERY = "/sys/class/power_supply/battery/";

 	public final static String FILE_BATTERY_CAPACITY = FILE_BATTERY + "capacity"; // Capacity: 100
 	public final static String FILE_BATTERY_CURRENT = FILE_BATTERY + "current_now"; // Current: -355 mA (Charging: >0; Discharging: <0)
 	public final static String FILE_BATTERY_VOLTAGE = FILE_BATTERY + "voltage_now"; // Voltage: 4103 mV
// 	public final static String FILE_BATTERY_POWER = FILE_BATTERY + "power_now"; // Remain Capacity: 3742 mAh

 	public final static String FILE_BATTERY_TECHNOLOGY = FILE_BATTERY + "technology"; //
 	public final static String FILE_BATTERY_TEMP = FILE_BATTERY + "temp"; // UNIT: 0.1℃
 	public final static String FILE_BATTERY_TYPE = FILE_BATTERY + "type"; //

// 	public final static String FILE_ACPWR = "/sys/class/power_supply/acpwr/";
// 	public final static String FILE_ACPWR_ONLINE = FILE_ACPWR + "online";	// 0: no plugged; 1: plugged
// 	public final static String FILE_ACPWR_TYPE = FILE_ACPWR + "type";
 	
 //	public final static String FILE_USBPWR = "/sys/class/power_supply/usbpwr/";
 //	public final static String FILE_USBPWR_ONLINE = FILE_USBPWR + "online";	// 0: no plugged; 1: plugged
 //	public final static String FILE_USBPWR_TYPE = FILE_USBPWR + "type";

	private int mBatCurr = 0;
	private int mBatVolt = 0;
	private int mBatPower = 0;
	private int mBatCapacity = 0;
	private int mAConline = 0;
	private int mUSBonline = 0;
	
	private final static int BAT_CHARGE_CURRENT = 200; // 充电电流判定标准：>= 200mA
	private final static int BAT_AFTER_TIME = 3 * 60 * 1000; // ms, 视频播放开始后3分钟
	private int mCurrAfter = 0; // 读取充电电流，并记录下来
	private boolean bBatChargeCurrLow = false; 
	
	private TextView textviewBatteryInfo;
	
	private boolean m_bEnableCharge = false;
	private boolean m_bEnableDischarge = false;
	
	private float mCpuTemp = 0;
	private float mBatTemp = 0;
	public NumberFormat mNFTemp;
	
	private boolean mCpuTempOutRange = false;
	private boolean mBatTempOutRange = false;
	
	private boolean mBatCapacityOutRange = false;

	private final static int BAT_MONITOR_STEP1__CHARGING_TO_MAX = 0; //  charging to 85%.
	private final static int BAT_MONITOR_STEP2__CONTROL_TO_RANGE = 1; //  monitor battery capacity in range of 60%~65%.
	private int mBatMonitorStep = BAT_MONITOR_STEP1__CHARGING_TO_MAX;

	private final static int BAT_CAPACITY_MAX = 70; //85; (after 90 minutes, it only charge to 70% from 30%)
	private final static int BAT_CAPACITY_RANGE_MIN = 65; //58; //60;
	private final static int BAT_CAPACITY_RANGE_MAX = 80; //60; //65;
	
	public Context mContext;
	
	static Intent resultIntent = new Intent();
	private Handler mTimeoutHandler = new Handler();
	
	public boolean mResult = false;
	private MonitorHandler mHandler;
	private MonitorThread mThread;
	
	private Button startButton;
	
	String path = "/mnt/sdcard/video.mp4";
	private int mOldVolume;
	private AudioManager mAudioManager;
    
	public Button mBtnStop;
	public TextView mViewCurrentTime;
	public TextView mViewSetTime;
	public NumberFormat mNF;

	public int mTimeSet = 0; // unit: minute

	public TextView mViewCpuTemp; // CPU temp
	public TextView mViewBatTemp; // Battery temp
	
	public TextView mViewBatCap; // Battery Capacity
	public TextView mViewBatCurr;
	
    boolean bPause = false;
    SurfaceHolder surfaceHolder;
    MediaPlayer mediaPlayer;
    SurfaceView surfaceView;
	
    public String mFilePlaytime = DeviceTest.FILE_PLAYTIME;
	private File videoFile;
	
	public int secd = 0;
	
	public int testType = DeviceTest.TESTCASE_TYPE_RUNIN;
	
	//public boolean bChargeContrl = false;
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.runin_videoplay);

		mContext = getApplicationContext();
		
		testType = getIntent().getIntExtra( "testType", DeviceTest.TESTCASE_TYPE_RUNIN );
		Log.i( TAG, "onCreate() testType=" + testType );
		
		mViewCurrentTime = (TextView) findViewById( R.id.runin_current_time );
		mViewSetTime = (TextView) findViewById( R.id.runin_set_time );
		
		mViewCpuTemp = (TextView) findViewById( R.id.runin_cpu_temp );
		mViewBatTemp = (TextView) findViewById( R.id.runin_bat_temp );
		mViewBatCap = (TextView) findViewById( R.id.runin_bat_capacity );
		mViewBatCurr = (TextView) findViewById( R.id.runin_bat_current );

	//	EditText editStandby = (EditText)findViewById( R.id.runin_standby );
	//	EditText editReboot = (EditText)findViewById( R.id.runin_reboot );
	//	EditText editPlaytime = (EditText)findViewById( R.id.runin_playtime );
	//	DeviceTest.writeFileLine( DeviceTest.FILE_STANDBY, editStandby.getText().toString().trim() + ",0", false );
	//	DeviceTest.writeFileLine( DeviceTest.FILE_REBOOT, editReboot.getText().toString().trim() + ",0", false );
	//	DeviceTest.writeFileLine( DeviceTest.FILE_PLAYTIME, editPlaytime.getText().toString().trim(), false );
		
		 // Clock used
		String str = DeviceTest.readFileLine( mFilePlaytime );
		if ( str != null ) {
			mTimeSet = Integer.valueOf( str );
			secd = 60 * mTimeSet;
		}
		else {
			mTimeSet = 10;
			secd = 60 * mTimeSet;
		}
		
		//String ChargeControlEn = SystemProperties.get("ro.ft.chgctrl_en", "0");
		//bChargeContrl = ChargeControlEn.equals("1");
		
		mNF = NumberFormat.getInstance();
		mNF.setMaximumIntegerDigits( 2 );
		mNF.setMinimumIntegerDigits( 2 );
		mViewSetTime.setText( mNF.format(mTimeSet/60) + ":" + mNF.format(mTimeSet%60) + ":00" );
		
		mNFTemp = NumberFormat.getInstance();
		mNFTemp.setMaximumFractionDigits(1);
		mNFTemp.setMinimumFractionDigits(1);
		
//		clock = new Clock(this);
		mClockHandler = new ClockHandler();
		mClockThread = new ClockThread();
		
		this.surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
	    this.surfaceHolder = this.surfaceView.getHolder(); 
	    this.surfaceHolder.addCallback(this); 
	    this.surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
           
		
//		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
//             receiver = new BatteryReceiver();
//             registerReceiver(receiver, filter);
	 
//       IntentFilter intentFilter = new IntentFilter();
//       intentFilter.addAction("TEST OVER");   //BroadcastReceiver指(Test over)
//       overReceiver myrecv = new overReceiver();
//       registerReceiver(myrecv,intentFilter);    
             
//             videoFile = new File("/mnt/sdcard/video.mp4");
//             if(!videoFile.exists()){
//					copyFile(videoFile);
//				}

		
//		m_bSupplyByBattery = (0 == Integer.valueOf( readSysfile( FILE_BATTERY_CTRL_ENABLE_BATTERY ).trim() ));
//		m_bSupplyByInput = (0 == Integer.valueOf( readSysfile( FILE_BATTERY_CTRL_ENABLE_INPUT ).trim() ));
		
//		mBrightnessBak = Integer.valueOf( readSysfile( FILE_BACKLIGHT_BRIGHTNESS ).trim() );
		Log.i( TAG, "mBrightnessBak:" + mBrightnessBak );
		
		// prepare for mediaplay

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int maxvolume =  mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		mOldVolume = mAudioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxvolume*10/100, 0);
		
//
		mClockThread.start();
		
		
		mHandler = new MonitorHandler();
		mThread = new MonitorThread();
		mThread.start();
		
		mTimeoutHandler.postDelayed( new Runnable() {
			public void run() {
				supplyByInput(true);
				Log.i(TAG, "Before judge Enable charge");
				mTimeoutHandler.postDelayed( new Runnable() {
					public void run() {
					mCurrAfter = Integer.valueOf( DeviceTest.readFileLine( FILE_BATTERY_CURRENT ).trim() );
					mCurrAfter /= 1000;
					mBatCapacity = Integer.valueOf( DeviceTest.readFileLine( FILE_BATTERY_CAPACITY ).trim() );
					if ( mCurrAfter < BAT_CHARGE_CURRENT&& BAT_CAPACITY_RANGE_MIN > mBatCapacity) { //Modify by lsg
						bBatChargeCurrLow = true;
						
						mResult = false;
						mHandler.sendEmptyMessage( MSG_RESULT );
					}
					Log.i(TAG, "Got the charge current: " + mCurrAfter + "mA after " + BAT_AFTER_TIME/1000 + "s"+"mBatCapacity is :" + mBatCapacity );
				}
				}, 1000);
			}
		}, BAT_AFTER_TIME );
    }
	
    protected void copyFile(File file) {
		// TODO Auto-generated method stub
		Log.v(TAG,"copy video");
		InputStream is = getResources().openRawResource(R.raw.video);
		try {
			FileOutputStream fos = new FileOutputStream(file.toString());
			byte[] buff = new byte[8192];
			int count = 0;
			while((count= is.read(buff))>0){
				fos.write(buff, 0, count);
			}
			fos.close();
			is.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    @Override
    public void onStart()
    {
    	Log.i( TAG,"onStart() ...  " );
    	super.onStart();
    	//this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);// TYPE_KEYGUARD TYPE_KEYGUARD_DIALOG
    }
    
    @Override
    public void onResume()
    {
    	Log.i( TAG,"onResume() ...  " );
//    	if ( bPause ) {
//   		mediaPlayer.start();
//    		bPause = false;
//    	}
    	
    	//if(!mediaPlayer.isPlaying()) mediaPlayer.start();
		super.onResume();

    }
    
    @Override
    public void onPause()
    {
    	Log.i( TAG,"onPause() ...  " );
//    	if(mediaPlayer.isPlaying()) {
//    		mediaPlayer.pause();
//    		bPause = true;
//    	}
		super.onPause();
    }
    
    @Override
    public void onStop()
    {
    	Log.i( TAG,"onStop() ...  " );
//    	if(mediaPlayer.isPlaying()) mediaPlayer.stop();
//		mediaPlayer.release();
		super.onStop();
    	
    }
    
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        // TODO Auto-generated method stub 
    	Log.i( TAG,"onBufferingUpdate() ...  " );
    } 
   
   public void onCompletion(MediaPlayer mp) {
        // TODO Auto-generated method stub 
	   Log.i( TAG,"onCompletion() ...  " );
	   	mp.start(); // 0826
    } 
    
    public void onPrepared(MediaPlayer mp) {
    	Log.i( TAG,"onPrepared() ...  " );
    	//this.surfaceHolder.setFixedSize(this.videoWidth, this.videoHeight); 
   // 	mp.start();
    } 
/*
    public void onPrepared(MediaPlayer arg0) {
		videoWidth = mediaPlayer.getVideoWidth();
		videoHeight = mediaPlayer.getVideoHeight();
		if (videoHeight != 0 &amp;&amp; videoWidth != 0) {
			arg0.start();
		}
		Log.e("mediaPlayer", "onPrepared");
	}
    */
    @Override
    protected void onDestroy() {
    	//unregisterReceiver( receiver );
    	Log.i( TAG,"onDestroy() ...  " );
        super.onDestroy(); 
        
        mAudioManager.setStreamVolume( AudioManager.STREAM_MUSIC, mOldVolume, 0 );
        
        if (this.mediaPlayer != null) { 
            this.mediaPlayer.release(); 
            this.mediaPlayer = null; 
        }   

        
//		AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//		mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC , false);

		supplyByInput( true );
		//writeSysfile( FILE_BATTERY_CTRL_ENABLE_INPUT, "1" );
		//writeSysfile( FILE_BATTERY_CTRL_ENABLE_BATTERY, "1" );
		
//		writeSysfile( FILE_BACKLIGHT_BRIGHTNESS, "" + mBrightnessBak );
		
		mThread.exit();
		mClockThread.exit();
    } 

    @Override
    public void onAttachedToWindow()

    { // TODO Auto-generated method stub 

       super.onAttachedToWindow();

    } 

/*
    @Override 
    public boolean onKey(int keyCode, KeyEvent event) {

       	//return super.onKey(keyCode, event);
      
       if(keyCode == KeyEvent.KEYCODE_BACK)
       {  
             return true; 
       }
       
       if(keyCode == KeyEvent.KEYCODE_MENU)
       {
    	   mediaPlayer.pause();
//hehg     	   clock.count();
    	 //hehg    	   Intent serviceIntent = new Intent(this,MyService.class);
    	 //hehg    	   stopService(serviceIntent);
    		
    	   return true;
       	}
       	
       	if(keyCode == KeyEvent.KEYCODE_HOME)
       	{
       		return true;
       	}
       	else
       	{
       		return super.onKey(keyCode, event);
       	}
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
	
/*   
    private boolean playVideo(String strPath){
    	try{
            this.mediaPlayer = new MediaPlayer(); 
            this.mediaPlayer.setDataSource(path); 
            this.mediaPlayer.setDisplay(this.surfaceHolder); 
            this.mediaPlayer.setLooping(true);
            this.mediaPlayer.prepare(); 
            this.mediaPlayer.setOnBufferingUpdateListener(this); 
            this.mediaPlayer.setOnPreparedListener(this); 
            this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	        //steven add for mute play
            AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			 int maxvolume =  mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			// mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,5,0);			
//                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC , true);
           //steven add end

        }
        catch (Exception e){ 
            e.printStackTrace();
            return false;
        }
       
//        mediastop = false;
        
        return true;

    }
*/    
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    	Log.i( TAG,"surfaceChanged() ...  " );
    }

	public void surfaceCreated(SurfaceHolder arg0) {
//		isPause = false;

		Log.i( TAG,"surfaceCreated() ... start -----------------------------------------------------------" );

//		mediaPlayer.setDisplay(this.surfaceHolder); 
//		mediaPlayer.start();
		
		try {

	//		if( !mediaPlayer.isPlaying() ) {
			//mediaPlayer = new MediaPlayer(); 
//			mediaPlayer.create(getApplicationContext(), R.raw.video);
//			mediaPlayer.setDataSource(getApplicationContext(), R.raw.video);
			//mediaPlayer.setDataSource(path);

//1. play the resurces file (res/raw/)
			//mediaPlayer = MediaPlayer.create( VideoPlayActivity.this, R.raw.video ); // 无内容播放出来

			//
			mediaPlayer = new MediaPlayer();
		//	AssetFileDescriptor afd = getResources().openRawResourceFd( R.raw.video );
		//	mediaPlayer.setDataSource( afd.getFileDescriptor() ); // 无内容播放出来
			
//			Uri uri = Uri.parse("android.resource://com.example.myapp/" + R.raw.my_resource);
//			Uri uri = Uri.parse("android.resource://com.example.myapp/raw/my_resource");
			//Uri uri = Uri.parse( "android.resource://com.byd8.test/raw/video.mp4" ); // failed
			Uri uri = Uri.parse( "android.resource://com.byd8.test/" + R.raw.video ); // OK
			//Uri uri = Uri.parse( "android.resource://com.byd8.test/raw/video" ); // OK
			mediaPlayer.setDataSource( mContext, uri );
/*
 * void setDataSource( String path ) // 指定加载path所代表的文件
 * void setDataSource( FileDescriptor fd, long offset, long length ) // 指定加载fd所代表的文件中从offset开始长度为length的内容
 * void setDataSource( FileDescriptor fd ) // 指定加载fd所代表的文件
 * void setDataSource( Context context, Uri uri ) // 指定加载uri所代表的文件
 * void setDataSource( Context context, Uri uri, Map<String, String> headers ) // 指定加载uri所代表的文件
 * 
 */
//2. play assets file (assets/)
		//	AssetFileDescriptor asfd = this.getApplicationContext().getAssets().openFd( "testvideo.mp4" );
		//	mediaPlayer.setDataSource( asfd.getFileDescriptor() );
		//	Log.i( TAG,"surfaceCreated() ... asfd.getFileDescriptor() = " + asfd.getFileDescriptor() );
			
//			mediaPlayer.setDataSource( "/mnt/sdcard/video.mp4.00" ); // It played OK
			
			mediaPlayer.setDisplay(this.surfaceHolder); 
			mediaPlayer.setLooping(true);
			mediaPlayer.setOnBufferingUpdateListener(this); 
			mediaPlayer.setOnPreparedListener(this); 
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

	//		AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	//		int maxvolume =  mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	//		mOldVolume = mAudioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
	//		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxvolume*10/100, 0);			
//			mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC , true);

			mediaPlayer.prepare(); 
			mediaPlayer.start();
			
			bPause = false;
	//		}
		}
		catch (Exception e){
			e.printStackTrace();
			return ;
		}
/*    	
   	 try {
			if(!playVideo(path))
			 {
				Log.i("TimeserviceActivity.java","++++++++++++++++++worong+++++++++++++++");
				Log.i("TimeserviceActivity.java","++++++++++++++++++worong+++++++++++++++");
				 
				mResult = false;
				mHandler.sendEmptyMessage( MSG_RESULT );
			 }
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

   	 	try {
   	 		if ( !mClockThread.isAlive() ) {
   	 			mClockThread.start();
   	 		}
   	 	}
   	 	catch ( IllegalThreadStateException e ) {
   	 		e.printStackTrace();
   	 	}
*/    }
    public void surfaceDestroyed(SurfaceHolder arg0) {
    	Log.i( TAG,"surfaceDestroyed() ...  " );
    	//this.mediaPlayer.pause();
//    	mediaPlayer.pause();
    	//clock.count();
    	
    	if (mediaPlayer != null) {
    		if(mediaPlayer.isPlaying()) mediaPlayer.stop();
    		mediaPlayer.release();
    		bPause = true;
    	}
    	
    	
    }
    
/*
    
	public void startNotify()
    {
    	Log.e("TimeserverActivity", "startNotify() ===========true=======");
A
//		this.mediaPlayer.stop();
		startDeviceTestActivity(true);
		finish();
    }

	void startDeviceTestActivity( boolean result ) {
B		
		this.setResult( result ? RESULT.OK.ordinal() : RESULT.NG.ordinal() );
		finish();

	}
*/

	
	private final static int MSG_TIMEDOWN = 0; // 1 second display ...
	private final static int MSG_TESTAGAIN = 1;
	private final static int MSG_RESULT = 2;
	private final static int MSG_EXIT = 3;
	private final static int MSG_PROMPT = 4;

	
	private boolean mThreadRunning = true;
	
	
	private boolean mReport = false;

	private boolean m_bBatteryLevelOK = false;
	private int m_iBatteryLevel = 0; // -1: <60%; 0: 60%~65%; 1: >65%
	
	private boolean mFlashDisplay = false;
	private boolean bCurrLow = false;
	
	private class MonitorHandler extends Handler {
		@Override
		public void handleMessage( Message msg ) {
			switch ( msg.what ) {
			case MSG_TIMEDOWN:
				removeMessages( MSG_TIMEDOWN );
				// Update the display
				mViewCpuTemp.setTextColor( Color.LTGRAY );
				mViewBatTemp.setTextColor( Color.LTGRAY );
				mViewBatCap.setTextColor( Color.LTGRAY );
				mViewCpuTemp.setText( mNFTemp.format(mCpuTemp) + "℃" );
				mViewBatTemp.setText( mNFTemp.format(mBatTemp) + "℃" );
				
				mViewBatCap.setText( mBatCapacity + "%" );
				
				mViewBatCurr.setTextColor( Color.LTGRAY );
				bCurrLow = false;
				if ( !bBatChargeCurrLow ) {
					mViewBatCurr.setText( mBatCurr + "mA" );
					if ( mBatCurr < BAT_CHARGE_CURRENT ) {
						bCurrLow = true;
					}
				}
				else {
					mViewBatCurr.setText( mCurrAfter + "mA" );
				}
				
				if ( mFlashDisplay ) {
					if ( mCpuTempOutRange ) {
						mViewCpuTemp.setTextColor( mFlashDisplay ? Color.RED : Color.LTGRAY );
					}
					if ( mBatTempOutRange ) {
						mViewBatTemp.setTextColor( mFlashDisplay ? Color.RED : Color.LTGRAY );
					}
					if ( mBatCapacityOutRange ) {
						mViewBatCap.setTextColor( mFlashDisplay ? Color.RED : Color.LTGRAY );
					}
					
					if ( bBatChargeCurrLow || bCurrLow ) {
						mViewBatCurr.setTextColor( mFlashDisplay ? Color.RED : Color.LTGRAY );
						//mViewBatCurr.setTextColor( mFlashDisplay ? (bCurrLow?Color.YELLOW:Color.RED) : Color.LTGRAY );
					}
					mFlashDisplay = !mFlashDisplay;
				}
				else {
					if ( mCpuTempOutRange ) {
						mViewCpuTemp.setTextColor( mFlashDisplay ? Color.RED : Color.LTGRAY );
					}
					if ( mBatTempOutRange ) {
						mViewBatTemp.setTextColor( mFlashDisplay ? Color.RED : Color.LTGRAY );
					}
					if ( mBatCapacityOutRange ) {
						mViewBatCap.setTextColor( mFlashDisplay ? Color.RED : Color.LTGRAY );
					}
					if ( bBatChargeCurrLow ) {
						mViewBatCurr.setTextColor( mFlashDisplay ? Color.RED : Color.LTGRAY );
					}
					mFlashDisplay = !mFlashDisplay;
				}
				
				break;
			case MSG_TESTAGAIN:
				removeMessages( MSG_TESTAGAIN );
				break;
			case MSG_RESULT:
				removeMessages( MSG_RESULT );
				
				File f = new File( mFilePlaytime );
				f.delete();
				
				//setResult( mResult ? RESULT.OK.ordinal() : RESULT.NG.ordinal() );
				sendEmptyMessage( MSG_EXIT );
				break;
			case MSG_EXIT:
				removeMessages( MSG_EXIT );
				//mThreadRunning = false;
				
				mThread.exit();
				mClockThread.exit();

//				if ( !mReport ) {
//					mReport = true;
//					TestResult( mResult );
//				}
				if ( DeviceTest.bChargeContrl ) 
				{
					if ( mBatMonitorStep != BAT_MONITOR_STEP2__CONTROL_TO_RANGE ) 
					{
						mResult = false;
						resultIntent.putExtra( "charge85", "no" );
					}
					else {
						resultIntent.putExtra( "charge85", "yes" );
					}			
					resultIntent.putExtra( "Capacity_after", mBatCapacity );
	
					if ( mBatCapacity < BAT_CAPACITY_RANGE_MIN-1 ) 
					{
						mResult = false;
					}
					else if	( mBatCapacity > BAT_CAPACITY_RANGE_MAX+1) 
					{
						mResult = false;
					}
				}
				resultIntent.putExtra( "testitem", "videoplay" );
				resultIntent.putExtra( "testType", testType );
				Log.i( TAG, "MonitorHandler.handleMessage() testType=" + testType );
				
				setResult( (mResult ? RESULT.OK.ordinal() : RESULT.NG.ordinal()), resultIntent );
				
				mTimeoutHandler.postDelayed(new Runnable() {
					public void run() {
						finish();
					}
				}, 500);
				
			//	finish();
				break;
			}
		}
		
	}

	public static String readSysfile(String sysfile ) {
		String result = "";
		try {
			FileReader fr = new FileReader( sysfile );
			BufferedReader br = new BufferedReader( fr );
			result = br.readLine().trim();
			br.close();
//			Log.i( TAG, "[FileReader] \"" + sysfile + "\": " + result );
		}
		catch ( FileNotFoundException e ) {
			e.printStackTrace();
		}
		catch ( IOException e ) {
			e.printStackTrace();
		}
		return result;
	}
	
	private int writeSysfile(String sysfile, String val ) {
		try {
			FileWriter wr = new FileWriter( sysfile, false );
			wr.write( val );
			wr.close();
			Log.i( TAG, "[FileWriter] \"" + sysfile + "\": " + val );
		}
		catch ( IOException e ) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
	
/*
 经过与Rockchip（钟勇汪）讨论，开机都为“1”，写“0”即是由谁供电，写“1”即是恢复原状，读只是指示是否充电，“0”不充电，“1”充电。
如charge_enable_battery=0，则由电池供电，断开USB。
如charge_enable_input=0，则由USB供电，断开电池；如果消耗大于USB供电，则电池也提供部分电流。
 */
/*	private boolean m_bSupplyByBattery = false;
	private boolean m_bSupplyByInput = false;
	
	private int supplyByBattery( boolean sw ) {
		int ret = 0;
		if ( m_bSupplyByBattery == sw )
			return 0;
		m_bSupplyByBattery = !m_bSupplyByBattery;
		Log.i(TAG, "" + FILE_BATTERY_CTRL_ENABLE_BATTERY + "-->" + (sw?"0":"1") );
		
		ret = writeSysfile( FILE_BATTERY_CTRL_ENABLE_INPUT, "1" );
		ret = writeSysfile( FILE_BATTERY_CTRL_ENABLE_BATTERY, "1" );
		if ( sw ) {
			ret = writeSysfile( FILE_BATTERY_CTRL_ENABLE_BATTERY, "0" );
		}
		return ret;

	}
	
	private int supplyByInput( boolean sw ) {
		int ret = 0;
		if ( m_bSupplyByInput == sw )
			return 0;
		m_bSupplyByInput = !m_bSupplyByInput;
		Log.i(TAG, "" + FILE_BATTERY_CTRL_ENABLE_INPUT + "-->" + (sw?"0":"1") );
		ret = writeSysfile( FILE_BATTERY_CTRL_ENABLE_INPUT, "1" );
		ret = writeSysfile( FILE_BATTERY_CTRL_ENABLE_BATTERY, "1" );
		if ( sw ) {
			ret = writeSysfile( FILE_BATTERY_CTRL_ENABLE_INPUT, "0" );
		}
		return ret;
	}
*/
	private int supplyByBattery(boolean sw) { // disable VBUS
		if ( DeviceTest.bChargeContrl ) {
			//FileUtility.setSysfsFile( SYSFS_CHARGER_CTRL__VBUS_DISABLE, "1" );
			String Flag = FileUtility.getSysfsFile( SYSFS_CHARGER_CTRL ).trim();
			Log.i(TAG,"Disable charging! Before Flasg is "+Flag+" Capacity is "+mBatCapacity);
			FileUtility.setSysfsFile( SYSFS_CHARGER_CTRL, "0" );
		}
		return 0;
	}
	private int supplyByInput(boolean sw) { // enable VBUS
		if ( DeviceTest.bChargeContrl ) {
			//FileUtility.setSysfsFile( SYSFS_CHARGER_CTRL__VBUS_DISABLE, "0" );
			String Flag = FileUtility.getSysfsFile( SYSFS_CHARGER_CTRL ).trim();
			Log.i(TAG,"Enable charging! Before Flasg is "+Flag+" Capacity is "+mBatCapacity);
			FileUtility.setSysfsFile( SYSFS_CHARGER_CTRL, "1" );
		}
		return 0;
	}
	
	
	private class MonitorThread extends Thread {
		public void exit() {
			mThreadRunning = false;
		}
		public void msleep( long ms ) {
			try {
				Thread.sleep( ms );
			}
			catch ( InterruptedException e ) {
				e.printStackTrace();
			}
		}
		public void run() {

			mCpuTempOutRange = false;
			mBatTempOutRange = false;
			mBatCapacityOutRange = false;
			mBatMonitorStep = BAT_MONITOR_STEP1__CHARGING_TO_MAX;
			
			while ( mThreadRunning ) {
				String strTemp = null;
				int nReTry = 10;
				while (0 != nReTry)
				{
					strTemp =  DeviceTest.readFileLine (FILE_CPU_TEMP);
					Log.i(TAG,"Read Curr CPU Temp is "+strTemp+" Retry time is "+(11-nReTry));
					if (null != strTemp)
					{
						break;
					}	
					try {
						Thread.sleep(2000);
					}
					catch ( InterruptedException e ) {
						e.printStackTrace();
					}
					nReTry--;
				}
				if (null == strTemp)
				{
					Log.i(TAG,"Read Curr CPU TEMP Failed");
					mResult = false;
					mHandler.sendEmptyMessage( MSG_RESULT );
					break;
				}
				mCpuTemp = Integer.valueOf(strTemp.trim() ); // UNIT: 0.001℃
				mCpuTemp /= 1000;
				Log.i(TAG,"Read CPUTemp is "+mCpuTemp);
				
				nReTry = 10;
				while (0 != nReTry)
				{
					strTemp=  DeviceTest.readFileLine (FILE_BATTERY_TEMP);
					Log.i(TAG,"Read Curr Bat Temp is "+strTemp+" Retry time is "+(11-nReTry));
					if (null != strTemp)
					{
						break;
					}	
					try 
					{
						Thread.sleep(2000);
					}
					catch ( InterruptedException e )
					{
						e.printStackTrace();
					}
					nReTry--;
				}
				if (null == strTemp)
				{
					Log.i(TAG,"Read Curr BAT TEMP Failed");
					mResult = false;
					mHandler.sendEmptyMessage( MSG_RESULT );
					break;
				}
				mBatTemp = Integer.valueOf( strTemp.trim() ); // UNIT: 0.1℃
				mBatTemp /=  10;
				Log.i(TAG,"Read BATTemp is "+mBatTemp);

				if ( mCpuTemp < 0 || mCpuTemp > 85 ) { // range: 0~85
					mCpuTempOutRange = true;
				}
				else {
					mCpuTempOutRange = false;
				}

				if ( mBatTemp < 0 || mBatTemp > 50 ) { // range: 0~50
					mBatTempOutRange = true;
				}
				else {
					mBatTempOutRange = false;
				}
				nReTry = 10;
				while (0 != nReTry)
				{
					strTemp=  DeviceTest.readFileLine (FILE_BATTERY_CURRENT);
					Log.i(TAG,"Read Curr Curr is "+strTemp+" Retry time is "+(11-nReTry));
					if (null != strTemp)
					{
						break;
					}	
					try
					{
						Thread.sleep(2000);
					}
					catch 
					( InterruptedException e ) {
						e.printStackTrace();
					}
					nReTry--;
				}				
				if (null == strTemp)
				{
					Log.i(TAG,"Read Curr Current Failed");
					mResult = false;
					mHandler.sendEmptyMessage( MSG_RESULT );
					break;
				}
				mBatCurr = Integer.valueOf( strTemp.trim());
				mBatCurr /= 1000;
				Log.i(TAG,"Read Curr Current is "+mBatCurr);
				
				nReTry = 10;
				while (0 != nReTry)
				{
					strTemp=  DeviceTest.readFileLine (FILE_BATTERY_VOLTAGE);
					Log.i(TAG,"Read Curr Volt is "+strTemp+" Retry time is "+(11-nReTry));
					if (null != strTemp)
					{
						break;
					}	
					try {
						Thread.sleep(2000);
					}
					catch ( InterruptedException e ) {
						e.printStackTrace();
					}
					nReTry--;
				}
				if (null == strTemp)
				{
					Log.i(TAG,"Read Curr Volt Failed");
					mResult = false;
					mHandler.sendEmptyMessage( MSG_RESULT );
					break;
				}
				mBatVolt = Integer.valueOf(strTemp.trim()) / 1000; //(Unit: uV)
				Log.i(TAG,"Read Curr Volt is "+mBatVolt);
				mBatPower = 0; //Integer.valueOf( DeviceTest.readFileLine( FILE_BATTERY_POWER ).trim() );
				
				nReTry = 10;
				while (0 != nReTry)
				{
					strTemp=  DeviceTest.readFileLine (FILE_BATTERY_CAPACITY);
					Log.i(TAG,"Read Curr Capacity is "+strTemp+" Retry time is "+(11-nReTry));
					if (null != strTemp)
					{
						break;
					}	
					try {
						Thread.sleep(2000);
					}
					catch ( InterruptedException e ) {
						e.printStackTrace();
					}
					nReTry--;
				}
				if (null == strTemp)
				{
					Log.i(TAG,"Read Curr Capacity Failed");
					mResult = false;
					mHandler.sendEmptyMessage( MSG_RESULT );
					break;
				}	
				mBatCapacity = Integer.valueOf(strTemp.trim());
//				mAConline = Integer.valueOf( DeviceTest.readFileLine( FILE_ACPWR_ONLINE ).trim() );
//				mUSBonline = Integer.valueOf( DeviceTest.readFileLine( FILE_USBPWR_ONLINE ).trim() );

				//Log.i( TAG, "[MonitorThread] mBatCapacity: " + mBatCapacity + "; mAConline: " + mAConline + "; mUSBonline: " + mUSBonline );
				
	//			if ( mAConline == 0 && mUSBonline == 0 ) { // no usb cable
	//				// prompt: please plug in the USB cable
	//				mHandler.sendEmptyMessage( MSG_PROMPT );
	//			}
	//			else {
				if ( true ) {

/*
 * Step 1: charging to 85%
 * Step 2: monitor the battery capacity range in 60%~65%
 * 
 * backlight's brightness operation function. float.
 * WindowManager.LayoutParams lp= mContext.getWindow().getAttributes();
				lp.screenBrightness=mBrightness;
				mContext.getWindow().setAttributes(lp);
 */
				
					switch ( mBatMonitorStep ) {
					case BAT_MONITOR_STEP1__CHARGING_TO_MAX:
						if ( mBatCapacity >= BAT_CAPACITY_MAX ) {
							mBatMonitorStep = BAT_MONITOR_STEP2__CONTROL_TO_RANGE;
							Log.i( TAG, "[Step1] mBatCapacity: " + mBatCapacity + ", gt " + BAT_CAPACITY_MAX + ", So turn to step2." );
						}
						else { // charging ... 
							//supplyByBattery( false );
							supplyByInput( true );
							Log.i( TAG, "[Step1] mBatCapacity: " + mBatCapacity + "Enable charge" );
							
							if ( mBrightness != 156 ) {
								writeSysfile( FILE_BACKLIGHT_BRIGHTNESS, "156" );
								mBrightness = 156;
								Log.i( TAG, "[Step1] mBatCapacity: " + mBatCapacity + "; mBrightness: " + mBrightness );
							}
						}
						break;
						
					case BAT_MONITOR_STEP2__CONTROL_TO_RANGE:
						if ( mBatCapacity < BAT_CAPACITY_RANGE_MIN ) {
							// out of range, charging ...
							mBatCapacityOutRange = true;
							//supplyByBattery( false );
							supplyByInput( true );
							Log.i( TAG, "[Step2] mBatCapacity: " + mBatCapacity + "; Enable charge");
							
							if ( mBrightness != 156 ) {
								writeSysfile( FILE_BACKLIGHT_BRIGHTNESS, "156" );
								mBrightness = 156;
								Log.i( TAG, "[Step2] mBatCapacity: " + mBatCapacity + "; mBrightness: " + mBrightness );
							}
						}
						else if ( mBatCapacity > BAT_CAPACITY_RANGE_MAX ) {
							// out of range, discharging ...
							mBatCapacityOutRange = true;
							supplyByBattery( true );
							
							if ( mBrightness != 7812 ) {
								writeSysfile( FILE_BACKLIGHT_BRIGHTNESS, "7812" );
								mBrightness = 7812;
								Log.i( TAG, "[Step2] mBatCapacity: " + mBatCapacity + "; mBrightness: " + mBrightness );
							}
						}
						else {
							// ok
							mBatCapacityOutRange = false;
							
							//supplyByInput( true );
							Log.i( TAG, "[Step2] mBatCapacity: " + mBatCapacity + "; charge do nothing");
							if ( mBrightness != mBrightnessBak ) {
//								writeSysfile( FILE_BACKLIGHT_BRIGHTNESS, "" + mBrightnessBak );
								mBrightness = mBrightnessBak;
								Log.i( TAG, "[Step2] mBatCapacity: " + mBatCapacity + "; mBrightness: " + mBrightness );
							}
							
						}
						break;
					}
				
				}
				mHandler.sendEmptyMessage( MSG_TIMEDOWN ); // update the display
				
				try {
					//Thread.sleep( 100 );
					Thread.sleep( 4000 );
				}
				catch ( InterruptedException e ) {
					e.printStackTrace();
				}
			}
		}
	}

	
	private AlertDialog mPromptDlg = null;
	private int mPromptDlgShow = 0;
	
	private void promptDialog( int resid_title, int resid_message ) {
		if ( mPromptDlgShow == 0 ) {
			mPromptDlgShow = 1;
			
			mPromptDlg = new AlertDialog.Builder(this).setTitle(resid_title).setMessage(resid_message).setPositiveButton(
				android.R.string.ok, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						mPromptDlgShow = 0;
					}
				}).create();
			mPromptDlg.show();
					//}).show();
				//	}).setNegativeButton(android.R.string.cancel, null).show();
		}
	}
	
	public ClockHandler mClockHandler;
	public ClockThread mClockThread;
	
	private final static int MSG_CLOCK_DSEC = 0;
	private final static int MSG_CLOCK_SECOND = 1;
	private final static int MSG_CLOCK_MINUTE = 2;
	private final static int MSG_CLOCK_HOUR = 3;
	private final static int MSG_CLOCK_UI_UPDATE = 4;
	
	private class ClockHandler extends Handler {
		public void handleMessage( Message msg ) {
			switch ( msg.what ) {
			case MSG_CLOCK_DSEC:
				removeMessages( MSG_CLOCK_DSEC );
//				parent.dsecshow.setText( "" + dsec );
				
				mViewCurrentTime.setText( mNF.format( hour ) + ":" + mNF.format( min ) + ":" + mNF.format( sec ) + ":" + dsec );
				break;
			case MSG_CLOCK_SECOND:
				removeMessages( MSG_CLOCK_SECOND );
//				parent.secshow.setText("" + nf.format(sec));
				break;
			case MSG_CLOCK_MINUTE:
				removeMessages( MSG_CLOCK_MINUTE );
//				parent.minshow.setText("" + nf.format(min));
				break;
			case MSG_CLOCK_HOUR:
				removeMessages( MSG_CLOCK_HOUR );
//				parent.hourshow.setText("" + hour);
				break;
			case MSG_CLOCK_UI_UPDATE:
				removeMessages( MSG_CLOCK_UI_UPDATE );
//				parent.tital.setText("Auto Media Test Over!");
				break;
			
			}
		}
	}

	private int seconds;
	private int dsec = 0;
	private int sec = 0;
	private int min = 0;
	private int hour = 0;
	public boolean mClockRunning = true;
	private class ClockThread extends Thread {
		public void exit() {
			mClockRunning = false;
		}
		public void run() {
			while ( mClockRunning ) {
				
				if ( !bPause ) {
//					continue;
//				}
				
				dsec ++;
				if ( dsec == 10 ) {
					seconds ++;
					sec ++;
					dsec = 0;
					if ( sec == 60 ) {
						min ++;
						sec = 0;
						if ( min == 60 ) {
							hour ++;
							min = 0;
						}
					}
				}
				
				if ( seconds == secd ) {
					dsec = 0;
					mClockHandler.sendEmptyMessage(MSG_CLOCK_DSEC);
					
					//startNotify();
					mResult = true;
					mHandler.sendEmptyMessage( MSG_RESULT );
					Log.i(TAG, "[ClockThread] VideoPlay Test OK!!!" );

					//mClockHandler.sendEmptyMessage(MSG_CLOCK_UI_UPDATE);
					break;
				}
				
				mClockHandler.sendEmptyMessage(MSG_CLOCK_DSEC);
				}
				try {
					sleep( 100 );
				}
				catch ( InterruptedException e ) {
					e.printStackTrace();
					return ;
				}
			}
			
		}
	}
}
