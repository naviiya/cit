package com.cit.test.runintest.runin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.cit.test.R;
import com.cit.test.runintest.FileUtility;
import com.cit.test.runintest.TestCase.RESULT;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

//hehg import jp.co.byd.Charge;

/* BatteryActivity
 * 
 * Battery Charging(AC adapter)	
 * 1. Request (RUNIN)
 *   1) charging battery to 60%-65%，keep test result in the machine, next RUIN PASS	
 *   2) automatic judgement
 *   3) auto generation log
 * 2. Program
 * 
 * [CHG-0731] charging to 85%, then discharging to 60%~65%
 * 
 * [CHG-0903] change the range of capacity to 58%~60%
 * [CHG-0917] change the range of capacity to 55%~58%, if >= 58, discharge to 55; if <= 55, charge to 58.
 * 
T7T Battery Charge/Discharge Control:
/sys/devices/platform/rk30_i2c.1/i2c-1/1-0032/ricoh619-battery.16/charge_enable_battery
 * "0"=Disable Charge; "1"=Enable Charge.
/sys/devices/platform/rk30_i2c.1/i2c-1/1-0032/ricoh619-battery.16/charge_enable_input
 * "0"=Disable Discharge; "1"=Enable Discharge.

From Rockchip-Chris Zhong (Zhong yongwang)
/sys/devices/platform/rk30_i2c.1/i2c-1/1-0032/ricoh619-battery.16/charge_enable_input
 *  = 0: system power is supplied by AC. 
 *  if connected PC (500mA max supply), when system power consumed over 500mA, the battery will supply it.
 *  ：从充电器取电，若连接的是PC，供电最大500mA的话，如果系统消耗大于这个数，就会从电池取部分电。
/sys/devices/platform/rk30_i2c.1/i2c-1/1-0032/ricoh619-battery.16/charge_enable_battery
 *  = 0: system power is supplied by battery.
 *  ：从电池取电
 *  when not charging, this two file return 0.
 *  不充电时，这两个节点返回的都是0.

 */

public class BatteryActivity extends Activity {
	
	private final static String TAG = "BatteryActivity";

	//public final static String SYSFS_CHARGER_CTRL = "/sys/bus/i2c/devices/5-0034/dollar_cove_charger/";
	//public final static String SYSFS_CHARGER_CTRL__VBUS_DISABLE = SYSFS_CHARGER_CTRL + "vbus_path_dis"; //0:VBUS selected; 1:VBUS not selected.
	//public final static String SYSFS_CHARGER_CTRL = "/sys/class/power_supply/dollar_cove_charger/device/ChargerEnable";
	public final static String SYSFS_CHARGER_CTRL = "/sys/class/power_supply/bq24192_charger/device/factory_charger_ctl";
//	public final static String FILE_BATTERY_CTRL = "/sys/devices/platform/rk30_i2c.1/i2c-1/1-0032/ricoh619-battery.16/";
	
	// 0: enable; 1: disable.
//	public final static String FILE_BATTERY_CTRL_ENABLE_BATTERY = FILE_BATTERY_CTRL + "charge_enable_battery";
//	public final static String FILE_BATTERY_CTRL_ENABLE_INPUT = FILE_BATTERY_CTRL + "charge_enable_input";
//     private final static String chargeSysFile = 
//    		 "/sys/devices/platform/rk30_i2c.1/i2c-1/1-0032/ricoh619-battery.16/charge_enable_battery";
//     private final static String dischargeSysFile = 
//    		 "/sys/devices/platform/rk30_i2c.1/i2c-1/1-0032/ricoh619-battery.16/charge_enable_input";

	public final static String FILE_BACKLIGHT_BRIGHTNESS = "/sys/class/backlight/intel_backlight/brightness"; // =3124
	public final static String FILE_BACKLIGHT_MAX_BRIGHTNESS = "/sys/class/backlight/intel_backlight/max_brightness"; // =7812
	public final static String FILE_BACKLIGHT_ACTUAL_BRIGHTNESS = "/sys/class/backlight/intel_backlight/actual_brightness"; // =3124
	public int mBrightnessBak = 0;
	public int mBrightnessMin = 0;
	public int mBrightnessMax = 0;
	public int mBrightness = 0;
	
	public final static String FILE_BATTERY = "/sys/class/power_supply/TBQ27541:00-0/";
	public final static String FILE_BATTERY_CURRENT = FILE_BATTERY + "current_now"; // Current: -355 mA (Charging: >0; Discharging: <0)
	
 	//public final static String FILE_BATTERY = "/sys/class/power_supply/battery/";

// 	public final static String FILE_BATTERY_CAPACITY = FILE_BATTERY + "capacity"; // Capacity: 100
 	//public final static String FILE_BATTERY_CURRENT = FILE_BATTERY + "current_avg"; // Current: -355 mA (Charging: >0; Discharging: <0)
// 	public final static String FILE_BATTERY_VOLTAGE = FILE_BATTERY + "voltage_now"; // Voltage: 4103 mV (Unit: uV)
// 	public final static String FILE_BATTERY_POWER = FILE_BATTERY + "power_now"; // Remain Capacity: 3742 mAh

// 	public final static String FILE_BATTERY_TECHNOLOGY = FILE_BATTERY + "technology"; //
// 	public final static String FILE_BATTERY_TEMP = FILE_BATTERY + "temp"; //
// 	public final static String FILE_BATTERY_TYPE = FILE_BATTERY + "type"; //

// 	public final static String FILE_ACPWR = "/sys/class/power_supply/acpwr/";
// 	public final static String FILE_ACPWR_ONLINE = FILE_ACPWR + "online";	// 0: no plugged; 1: plugged
// 	public final static String FILE_ACPWR_TYPE = FILE_ACPWR + "type";
 	
// 	public final static String FILE_USBPWR = "/sys/class/power_supply/usbpwr/";
// 	public final static String FILE_USBPWR_ONLINE = FILE_USBPWR + "online";	// 0: no plugged; 1: plugged
// 	public final static String FILE_USBPWR_TYPE = FILE_USBPWR + "type";
 	
	private final static int BAT_CAPACITY_RANGE_MIN = 70; //58; //60;
	private final static int BAT_CAPACITY_RANGE_MAX = 80; //60; //65;
	
//	private int mCurr = 0;
//	private int mVolt = 0;
//	private int mPower = 0;
//	private int mCapacity = 0;
//	private int mAConline = 0;
//	private int mUSBonline = 0;
	
	//private TextView textviewBatteryInfo;
    //private TextView textviewInfo;
//	private boolean m_bEnableCharge = false;
//	private boolean m_bEnableDischarge = false;
 	
	
	
	
	private TextView mBatStatusView;
	private TextView mBatPowerplugView;
	private TextView mBatLevelView;
	private TextView mBatScaleView;
	private TextView mBatHealthView;
	private TextView mBatVoltageView;
	private TextView mBatCurrentView;
	private TextView mBatCapacityView;
	private TextView mBatTemperatureView;
	private TextView mBatTechnologyView;
	private TextView mHintView;
	private TextView mResultView;
	private TextView mTitleView;
	
	private NumberFormat mNumberFmt;
    private NumberFormat mPercentFmt;
    
    private boolean mIsPass;
	private boolean mIsPlugged;

	private String mTimeHintStr;
	private int mCountDown;
	private int mCountDown_CurrentCheck;
    
	private int mHealth;
	private int mPowerplug;
	private String mPowerplugStr;
    private int mLevel;
    private int mScale;
    private float mCapacity; // mLevel/mScale %
    private int mVoltage;
    private int mCurrent;
    private int mTemperature;
    private String mTechnology;
	
	

     private int testType = 1;
     private boolean isGroup = false;
     
     private boolean mResult = false;
     private BatteryReceiver receiver;
     private int batteryCapacity = 0;
     private int status=0;

	private MediaPlayer mAudioPlayer;
	
	private Handler mTimeoutHandler = new Handler();
	
	private Runnable mRunner = new Runnable() {
		public void run() {
			setResult( mResult ? RESULT.OK.ordinal() : RESULT.NG.ordinal() );
			finish();
		}
	};


	public BatteryActivity() {
		mHandler = new BatteryHandler();
		mThread = new BatteryThread();
	}
	
	/*
	 * arg1: String filename
	 * return: String  ---- data got from file.
	 */
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
/*		private boolean m_bSupplyByBattery = false;
		private boolean m_bSupplyByInput = false;
		
		private int supplyByBattery( boolean sw ) {
			int ret = 0;
			if ( m_bSupplyByBattery == sw )
				return 0;
			m_bSupplyByBattery = sw;
			Log.i(TAG, "" + FILE_BATTERY_CTRL_ENABLE_BATTERY + "-->" + (sw?"0":"1") );
			
			ret = writeSysfile( FILE_BATTERY_CTRL_ENABLE_INPUT, "1" );
			ret = writeSysfile( FILE_BATTERY_CTRL_ENABLE_BATTERY, (sw?"0":"1") );

			return ret;
	}
		
		private int supplyByInput( boolean sw ) {
			int ret = 0;
			if ( m_bSupplyByInput == sw )
				return 0;
			m_bSupplyByInput = sw;
			Log.i(TAG, "" + FILE_BATTERY_CTRL_ENABLE_INPUT + "-->" + (sw?"0":"1") );
			ret = writeSysfile( FILE_BATTERY_CTRL_ENABLE_BATTERY, "1" );
			ret = writeSysfile( FILE_BATTERY_CTRL_ENABLE_INPUT, (sw?"0":"1") );

			return ret;
		}
*/	
	//public final static String SYSFS_CHARGER_CTRL = "/sys/class/power_supply/dollar_cove_charger/device/ChargerEnable";
	//private int supplyByBattery( boolean sw ) {
	private int supplyByBattery(boolean sw) { // disable VBUS
		//FileUtility.setSysfsFile( SYSFS_CHARGER_CTRL__VBUS_DISABLE, "1" );
		String Flag = FileUtility.getSysfsFile( SYSFS_CHARGER_CTRL ).trim();
		Log.i(TAG,"Disable charging! Before Flasg is "+Flag);
		FileUtility.setSysfsFile( SYSFS_CHARGER_CTRL, "0" ); 
		Log.i(TAG,"Disable charging." + " Capacity is " + mLevel);
		return 0;
	}
	private int supplyByInput(boolean sw) { // enable VBUS
		//FileUtility.setSysfsFile( SYSFS_CHARGER_CTRL__VBUS_DISABLE, "0" );
		String Flag = FileUtility.getSysfsFile( SYSFS_CHARGER_CTRL ).trim();
		Log.i(TAG,"Enable charging! Before Flasg is "+Flag);
		FileUtility.setSysfsFile( SYSFS_CHARGER_CTRL, "1" );  
		Log.i(TAG,"Enable charging." + " Capacity is " + mLevel);
		return 0;
	}
		
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.powertest_batterydischarge);
		getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_KEEP_SCREEN_ON);
		//getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
		
		mTitleView = (TextView)findViewById(R.id.title);
		mTitleView.setText(R.string.runin_battery_charging__title);
		
		mBatStatusView = (TextView)findViewById(R.id.bat_status);
		mBatPowerplugView = (TextView)findViewById(R.id.bat_powerplug);
		mBatLevelView = (TextView)findViewById(R.id.bat_level);
		mBatScaleView = (TextView)findViewById(R.id.bat_scale);
		mBatHealthView = (TextView)findViewById(R.id.bat_health);
		mBatVoltageView = (TextView)findViewById(R.id.bat_voltage);
		mBatTemperatureView = (TextView)findViewById(R.id.bat_temperature);
		mBatTechnologyView = (TextView)findViewById(R.id.bat_technology);

		mBatCurrentView = (TextView)findViewById(R.id.bat_current);
		mBatCapacityView = (TextView)findViewById(R.id.bat_capacity);
		
		mHintView = (TextView)findViewById(R.id.hint);
		mResultView  = (TextView)findViewById(R.id.result);
		
		mTimeHintStr = getResources().getString(R.string.bat_connect_charger);
		
		mNumberFmt = NumberFormat.getNumberInstance(Locale.US);
		mPercentFmt = NumberFormat.getPercentInstance(Locale.US);
		
		
		registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
		
//		setContentView( R.layout.runin_battery );
//		getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		testType = getIntent().getIntExtra("testType", 1);
		isGroup = getIntent().getBooleanExtra("isGroup", false);

		
//		textviewInfo = (TextView) findViewById (R.id.batTemInfo);
//		textviewBatteryInfo = (TextView)findViewById( R.id.batteryInfo );
		
/*		try {
			Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, -1);
		}
		catch (Exception localException) {
			localException.printStackTrace();
		}
*/
//		writeSysfile( FILE_BATTERY_CTRL_ENABLE_INPUT, "1" );
//		writeSysfile( FILE_BATTERY_CTRL_ENABLE_BATTERY, "1" );
		supplyByInput( true );

		mBrightnessBak = Integer.valueOf( FileUtility.getSysfsFile( FILE_BACKLIGHT_BRIGHTNESS ).trim() );
		mBrightnessMax = Integer.valueOf( FileUtility.getSysfsFile( FILE_BACKLIGHT_MAX_BRIGHTNESS ).trim() );
		mBrightnessMin = (mBrightnessMax/100) * 2; // 2%
		Log.i( TAG, "mBrightnessBak: " + mBrightnessBak );
		Log.i( TAG, "mBrightnessMax: " + mBrightnessMax );
		Log.i( TAG, "mBrightnessMin: " + mBrightnessMin );
		
		mThread.start();
		
//		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
//		receiver = new BatteryReceiver();
//		registerReceiver(receiver, filter);

	}
	
	@Override
	protected void onDestroy() {

		//supplyByBattery( false );
		//supplyByInput( false );

//		writeSysfile( FILE_BATTERY_CTRL_ENABLE_INPUT, "1" );
//		writeSysfile( FILE_BATTERY_CTRL_ENABLE_BATTERY, "1" );
		supplyByInput( true );
		
		if ( mAudioPlayer != null ) {
			mAudioPlayer.release();
		}
		
		FileUtility.setSysfsFile( FILE_BACKLIGHT_BRIGHTNESS, "" + mBrightnessBak );

		//unregisterReceiver(receiver);
		unregisterReceiver(mBatteryReceiver);
		mThread.exit();
		super.onDestroy();
	}	
	
	
	private void updateBatteryStatus(Intent i) {
		// get battery level
		mLevel = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		mScale = i.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		if (0 == mScale)
		{
			Log.i(TAG,"Scale is zero why????");
			mScale = 1;
		}
		Log.i(TAG,"Scale is "+mScale+"; Level is "+mLevel);
		mCapacity = mLevel/(float)mScale;
		
		
		mBatCapacityView.setText(mPercentFmt.format(mCapacity));
		
		mBatLevelView.setText("" + mLevel);
		mBatScaleView.setText("" + mScale);
		
		mVoltage = i.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
		mTemperature = i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
		mTechnology = i.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
		
		
		
		mBatVoltageView.setText(mNumberFmt.format(mVoltage) + " mV");
		mBatTemperatureView.setText(mNumberFmt.format(mTemperature/(float)10) + " ℃");
		mBatTechnologyView.setText(mTechnology);
		
		mHealth = i.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
		String healthStr = null;
		switch (mHealth) {
		case BatteryManager.BATTERY_HEALTH_COLD:
			healthStr = getResources().getString(R.string.bat_health_cold);
			break;
		case BatteryManager.BATTERY_HEALTH_DEAD:
			healthStr = getResources().getString(R.string.bat_health_dead);
			break;
		case BatteryManager.BATTERY_HEALTH_GOOD:
			healthStr = getResources().getString(R.string.bat_health_good);
			break;
		case BatteryManager.BATTERY_HEALTH_OVERHEAT:
			healthStr = getResources().getString(R.string.bat_health_overheat);
			break;
		case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
			healthStr = getResources().getString(R.string.bat_health_overvoltage);
			break;
		default:
			healthStr = getResources().getString(R.string.bat_health_cold);
			break;
		}
		mBatHealthView.setText(healthStr);
		
		
		// get charger plugged state
		mPowerplug = i.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		if ( mPowerplug > 0 ) {
			mIsPlugged = true;
			mHandler.sendEmptyMessage( MSG_BATTERY_PLUGGED );
		}
		else {
			mIsPlugged = false;
			mHandler.sendEmptyMessage( MSG_BATTERY_UNPLUGGED );
		}
		mPowerplugStr = null;
		switch (mPowerplug) {
		case BatteryManager.BATTERY_PLUGGED_AC:
			mPowerplugStr = getResources().getString(R.string.bat_plugged_ac);
			break;
		case BatteryManager.BATTERY_PLUGGED_USB:
			mPowerplugStr = getResources().getString(R.string.bat_plugged_usb);
			break;
		case BatteryManager.BATTERY_PLUGGED_WIRELESS:
			mPowerplugStr = getResources().getString(R.string.bat_plugged_wireless);
			break;
		default:
			mPowerplugStr = getResources().getString(R.string.bat_plugged_none);
			break;
		}
		mBatPowerplugView.setText(mPowerplugStr);
		
		// get charging state
		int status = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		String stateStr = null;
		switch (status) {
		case BatteryManager.BATTERY_STATUS_CHARGING:
			stateStr = getResources().getString(R.string.bat_status_charging);
			break;
		case BatteryManager.BATTERY_STATUS_DISCHARGING:
			stateStr = getResources().getString(R.string.bat_status_discharging);
			break;
		case BatteryManager.BATTERY_STATUS_FULL:
			stateStr = getResources().getString(R.string.bat_status_full);
			break;
		case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
			stateStr = getResources().getString(R.string.bat_status_notcharging);
			break;
		default:
			stateStr = getResources().getString(R.string.bat_status_unknown);
			break;
		}
		
		mBatStatusView.setText(stateStr);
    }

    private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isPlugged = mIsPlugged;
            updateBatteryStatus(intent);
            //if (mIsPass) {
            //    return;
            //}

            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            int batteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

            if (isPlugged) {
                if (chargePlug == 0 &&
                        (batteryStatus == BatteryManager.BATTERY_STATUS_DISCHARGING ||
                        batteryStatus == BatteryManager.BATTERY_STATUS_NOT_CHARGING)) {
                    mIsPass = true;
                }
            } else {
                if (chargePlug > 0 &&
                        (batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                        batteryStatus == BatteryManager.BATTERY_STATUS_FULL)) {
                    mIsPass = true;
                }
            }

            if (mIsPass) {
                //mHintView.setTextColor(Color.GREEN);
                //mHintView.setText(getResources().getString(R.string.Passed));
            }
        }
    };
    
	
	public void TestResult(boolean result) {
         if (result == true) {
             ((Button) findViewById(R.id.btn_Pass)).performClick();
         } else if (result == false) {
             ((Button) findViewById(R.id.btn_Fail)).performClick();
         }
     }

  /*   private class BatteryReceiver extends BroadcastReceiver {
     @Override
     public void onReceive(Context context, Intent intent) {
         String action = intent.getAction();
         if(Intent.ACTION_BATTERY_CHANGED.equals(action)) {
        	 batteryCapacity = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        	  status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
				Log.v("BatteryChargeRuninActivity", "Battery level is " + batteryCapacity + "%");
				if(status==BatteryManager.BATTERY_STATUS_DISCHARGING)
				   {	
					if(acflag==true)
					{batTemInfoView.setText("please connect the AC");return;}
					if(batteryCapacity<=80)
					{	
						batTemInfoView.setText(batteryCapacity + "%");
						//if(!runinresult.exists())
						//	{createFile(runinresult, "RunIn Test Pass"+"");}
						//else 
						//{
						//WriteTxt(runinresult, "RunIn Test Pass"+"");
						try{
							FileWriter writer = new FileWriter(logFileFail, true);
							 if(logFileFail.exists()) {
						 	 writer.write("<Battery Charging"+"><PASS>\n");
							 writer.close();
							 logFileFail.renameTo(logFilePass);
							  	
							//File pcbaLog = new File(logPath + snNumb + "_PCBA_PASS.txt");
                    				        //if(pcbaLog.exists()) {
							//pcbaLog.delete();
							//		} 
										  }
						   }catch (IOException e) {
                          			      e.printStackTrace();
                        			 }

						mcharge.Charger_Control(1);
						//PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
						//pm.reboot("************************************************for test");
						Intent newIntent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
						newIntent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
						newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(newIntent);
						}
					
					batTemInfoView.setText(batteryCapacity + "%,and wait to shut down");	
				   }
			         else if(status==BatteryManager.BATTERY_STATUS_FULL || batteryCapacity >= 98)
					{
					acflag=false;
					Log.v("BatteryChargeRuninActivity", "here need to cut the ac ");
					mcharge.Charger_Control(0);
			  		}	
				  else if(status==BatteryManager.BATTERY_STATUS_CHARGING)
                                    {
                                                batTemInfoView.setText(batteryCapacity + "%,and wait to charge full");
                                    }
							
           }
      }
     }*/

//steven add receiver for batter

	private class BatteryReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if ( Intent.ACTION_BATTERY_CHANGED.equals(action) ) {
				batteryCapacity = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
				status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
				Log.i(TAG, "Battery level is " + batteryCapacity + "%");

				//if(batteryCapacity==63||batteryCapacity==64||batteryCapacity==65)
/*hehg				if ( batteryCapacity >= 60 && batteryCapacity <= 65 ) { // 60~65%
					//write log
					try {
						FileWriter writer = new FileWriter(logFileFail, true);
						if(logFileFail.exists()) {
							writer.write("<System Information><PASS>\n<Wifi><PASS>\n<Bluetooth><PASS>\n<Front Camera><PASS>\n<Rear Camera><PASS>\n<DDR Pattern Test><PASS>\n<eMMC Pattern Test><PASS>\n<Thermal Test><PASS>\n<Battery Charging"+"><PASS>\n");
							writer.close();
							logFileFail.renameTo(logFilePass);
						}
					}
					catch (IOException e) {
						e.printStackTrace();
					}
					
					textviewInfo.setText( batteryCapacity + "% in the range (60%~65%), Please Shut Down!" );
					
//hehg				mcharge.Charger_Control(1);
					
//					mTimeOutHandler.postDelayed(mRunner,5000);
					
//hehg				if ( dialogflag == true ) {
//						dialog_headphone();
//					}
					
					if ( musicflag == true ) {
						//audioPlayerInit();
						//playAudio();
						mAudioPlayer.start();
						musicflag = false;
					}
					mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC , false);
					m_bBatteryLevelOK = true;
					m_iBatteryLevel = 0;
					
					supplyByBattery( false );
					supplyByInput( true );
//						disableCharge();
//						disableDischarge();
					
                    //Intent newIntent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
					//newIntent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
					//newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					//startActivity(newIntent);
				}
				else if ( batteryCapacity > 65 ) { // enable discharge, disable charge
					m_iBatteryLevel = 1;
					m_bBatteryLevelOK = false;
					textviewInfo.setText( batteryCapacity + "% out of range (60%~65%), so discharging ..." );
//					Log.i(TAG, "Battery Capacity is " + batteryCapacity + "%, out of range (60%~65%), so discharging ..." );
//					enableDischarge();
//					disableCharge();
					supplyByBattery( true );
					supplyByInput( false );
					mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC , true);
				}
				else if ( batteryCapacity < 60 ) { // disable discharge, enable charge
					m_bBatteryLevelOK = false;
					m_iBatteryLevel = -1;
					textviewInfo.setText( batteryCapacity + "% out of range (60%~65%), so charging ..." );
//					Log.i(TAG, "Battery Capacity is " + batteryCapacity + "%, out of range (60%~65%), so charging ..." );
//					disableDischarge();
//					enableCharge();
					supplyByBattery( false );
					supplyByInput( true );
					mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC , true);
				}
				if ( batteryCapacity < 63 ) { // disable discharge, enable charge
				//	textviewInfo.setText( batteryCapacity + "% out of range (60%~65%), so charging ..." );
//					disableDischarge();
//					enableCharge();
					supplyByBattery( false );
					supplyByInput( true );
				}
*/			}
		}
	}


		

	

	public void showRuninResult( boolean result ) {
		View vi = View.inflate(this, R.layout.show_result, null);
		TextView text = (TextView) vi.findViewById( R.id.result );
		text.setText( (result ? "成功" : "失败") );
		text.setTextColor( (result ? Color.GREEN : Color.RED) );
		
		text.setVisibility(View.VISIBLE);
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setCancelable(true);
		b.setTitle("RUNIN测试结果显示");
		b.setView(vi);
		b.setPositiveButton(android.R.string.ok, null);
		b.setPositiveButton(android.R.string.ok, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				Log.i( TAG, "showRuninResult().onClick() mCapacity: " + mCapacity + "%%" );
				mThreadRunning = false;
				mTimeoutHandler.postDelayed( mRunner, 200L );
			}
		});

		AlertDialog d = b.create();
		d.show();
	}

	private static final int MSG_RESULT = 0;
	private static final int MSG_REDO_PROMPT = 1;
	private static final int MSG_REDO = 2;
	private static final int MSG_DISPLAY = 3;
	private static final int MSG_BATTERY_PLUGGED = 4;
	private static final int MSG_BATTERY_UNPLUGGED = 5;


	private static final int MSG_BATTERY_USB_CABLE_PLUGGED = 1;
	private static final int MSG_BATTERY_DISPLAY = 2;
	private static final int MSG_BATTERY_RESULT = 3;
	private static final int MSG_BATTERY_USB_CABLE_NOT_PLUGGED = 4;

//	private boolean mThreadRunning = true;

	public boolean mShowResult = false;

//	private BatteryHandler mHandler;
//	private BatteryThread mThread;
//	private boolean mReport = false;

	private boolean m_bBatteryLevelOK = false;
	private int m_iBatteryLevel = 0; // -1: <60%; 0: 60%~65%; 1: >65%




	private boolean mFlagCurrentCheck = false;
	private boolean mFlagPowerplugCheck = false;

	private boolean mThreadRunning = true;
	private int mCount = 0;

	private BatteryHandler mHandler;
	private BatteryThread mThread;
	private boolean mReport = false;

	private class BatteryHandler extends Handler {
		@Override
		public void handleMessage( Message msg ) {
			switch ( msg.what ) {
			case MSG_RESULT:
				removeMessages( MSG_RESULT );
				mThreadRunning = false;
				Log.i(TAG, "MSG_RESULT: [" + mPowerplugStr + "]" +
						"curr:" + mCurrent + "; volt:" + mVoltage + "; capacity:" + mCapacity );

				if ( !mReport ) {
					mReport = true;
					setResult( (mResult ? RESULT.OK.ordinal() : RESULT.NG.ordinal()) );
					String res = "";
					if ( mResult ) {
						res = getResources().getString(R.string.pass);
						mResultView.setTextColor(Color.GREEN);
					}
					else {
						res = getResources().getString(R.string.fail);
						mResultView.setTextColor(Color.RED);
					}
					mResultView.setText(res);

					new Handler().postDelayed(new Runnable() {
						public void run() {
							finish();
						}
					}, 1000 );
				}

				break;

/*			case MSG_REDO_PROMPT:
				removeMessages( MSG_REDO_PROMPT );
				//mRetestFlag = true;

				Log.i( TAG, "<MSG_REDO> mAgainTimes=" + mAgainTimes );
				if ( mAgainTimes > 0 ) {
					promptTestAgain( R.string.testagain__prompt_title, R.string.testagain__prompt_msg );
					mAgainTimes --;
				}
				else {
					mResult = false;
					sendEmptyMessage( MSG_RESULT ); // FAIL: 3 times re-test
				}

				break;
			case MSG_REDO:
				removeMessages( MSG_REDO );
				if ( mPowerplug > 0 ) {
					mIsPlugged = true;
					//mHandler.sendEmptyMessage( MSG_BATTERY_PLUGGED );
					mCountDown_CurrentCheck = TIMEOUT_CURRENT_CHECK;
					mTimerCurrentCheck.start();
					mFlagCurrentCheck = true;
				}
				else {
					mIsPlugged = false;
					//mHandler.sendEmptyMessage( MSG_BATTERY_UNPLUGGED );
					mCountDown = TIMEOUT_POWER_PLUG;
					mTimer.start();
					mFlagPowerplugCheck = true;
				}
				break;
*/
			case MSG_DISPLAY:
				removeMessages( MSG_DISPLAY );
			//	if ( mPromptDlgShow == 1 ) {
			//		mPromptDlg.getButton( DialogInterface.BUTTON_POSITIVE ).performClick();
			//		mCount = 0;
			//	}

				mBatCurrentView.setText(mNumberFmt.format(mCurrent) + " mA");



			//	mBatCapacityView.setText(mPercentFmt.format(mCapacity) + " (Range: " +
			//				mPercentFmt.format(BAT_CAPACITY_RANGE_MIN) + "~" + mPercentFmt.format(BAT_CAPACITY_RANGE_MAX) + ")" );
				mBatCapacityView.setText(mLevel + "% (Range: " + BAT_CAPACITY_RANGE_MIN + "%~" + BAT_CAPACITY_RANGE_MAX + "%)" );

			//	String batRange = BAT_CAPACITY_RANGE_MIN + "%~" + BAT_CAPACITY_RANGE_MAX + "%";
			//	if ( mCapacity >= BAT_CAPACITY_RANGE_MIN && mCapacity <= BAT_CAPACITY_RANGE_MAX ) { // 60~65%
				if ( mLevel >= BAT_CAPACITY_RANGE_MIN && mLevel <= BAT_CAPACITY_RANGE_MAX ) { // 60~65%
					//textviewInfo.setText( mCapacity + "% in the range (" + batRange + "), Test OK!" );

					mBatCapacityView.setTextColor(Color.GREEN);

					// ok
					supplyByInput( true ); // no charging
					if ( mBrightness != mBrightnessBak ) { // restore the backlight
						FileUtility.setSysfsFile( FILE_BACKLIGHT_BRIGHTNESS, "" + mBrightnessBak );
						mBrightness = mBrightnessBak;
						Log.i( TAG, "[Step2] mBatCapacity: " + mCapacity + "; mBrightness: " + mBrightness );
					}

					m_bBatteryLevelOK = true;
					m_iBatteryLevel = 0;

					mResult = true;
				//	sendEmptyMessage( MSG_BATTERY_RESULT );
					if ( isGroup ) {
						if ( !mShowResult ) {
							mShowResult = true;
							Log.i(TAG, "start showRuninResult() ... mShowResult: " + mShowResult + " mResult: " + mResult );
							showRuninResult( mResult );
						}
					}
					else {
						sendEmptyMessage( MSG_RESULT );
					}

				}
				else if ( mLevel > BAT_CAPACITY_RANGE_MAX ) { // enable discharge, disable charge
					m_iBatteryLevel = 1;
					m_bBatteryLevelOK = false;
					//textviewInfo.setText( mCapacity + "% out of range (" + batRange + "), so discharging ..." );
					mBatCapacityView.setTextColor(Color.RED);
//					Log.i(TAG, "Battery Capacity is " + batteryCapacity + "%, out of range (60%~65%), so discharging ..." );



// out of range, discharging ...

					supplyByBattery( true ); // discharging

					if ( mBrightness != mBrightnessMax ) { // set max brightness
						FileUtility.setSysfsFile( FILE_BACKLIGHT_BRIGHTNESS, "" + mBrightnessMax );
						mBrightness = mBrightnessMax;
						Log.i( TAG, "[Step2] mBatCapacity: " + mCapacity + "; mBrightness: " + mBrightness );
					}
				}
				else if ( mLevel < BAT_CAPACITY_RANGE_MIN ) { // disable discharge, enable charge
					m_bBatteryLevelOK = false;
					m_iBatteryLevel = -1;
					//textviewInfo.setText( mCapacity + "% out of range (" + batRange + "), so charging ..." );
					mBatCapacityView.setTextColor(Color.YELLOW);
//					Log.i(TAG, "Battery Capacity is " + batteryCapacity + "%, out of range (60%~65%), so charging ..." );



					 // out of range, charging ...

					//supplyByBattery( false ); // charging
					supplyByInput( true ); // charging

					if ( mBrightness != mBrightnessMin ) { // set min brightness
						FileUtility.setSysfsFile( FILE_BACKLIGHT_BRIGHTNESS, "" + mBrightnessMin );
						mBrightness = mBrightnessMin;
						Log.i( TAG, "[Step1] mBatCapacity: " + mCapacity + "; mBrightness: " + mBrightness );
					}
				}

				break;
			case MSG_BATTERY_UNPLUGGED:
				removeMessages( MSG_BATTERY_UNPLUGGED );
			//	if ( mPromptDlgShow == 0 ) {
			//		promptCablePlugged();
			//	}
				mHintView.setText("");

/*				if ( mFlagCurrentCheck ) {
					mTimerCurrentCheck.cancel();
					mFlagCurrentCheck = false;
				}
				if ( !mFlagPowerplugCheck ) {
					mCountDown = TIMEOUT_POWER_PLUG;
					mTimer.start();
					mFlagPowerplugCheck = true;
				}
*/
				if ( m_iBatteryLevel < 0 ) { // if battery level is OK, don't prompt again.
					promptDialog(R.string.powertest_batterycharge_prompt_title, R.string.powertest_batterycharge_prompt_message);
				}
				break;
			case MSG_BATTERY_PLUGGED:
				removeMessages( MSG_BATTERY_PLUGGED );

				mHintView.setText("");

/*				if ( mFlagPowerplugCheck ) {
					mTimer.cancel();
					mFlagPowerplugCheck = false;
				}

				if ( !mFlagCurrentCheck ) {
					mCountDown_CurrentCheck = TIMEOUT_CURRENT_CHECK;
					mTimerCurrentCheck.start();
					mFlagCurrentCheck = true;
				}
*/
				if ( mPromptDlgShow == 1 ) {
					mPromptDlg.getButton( DialogInterface.BUTTON_POSITIVE ).performClick();
				}
				break;
			}
		}

	}
/*
	private class BatteryHandler111 extends Handler {
		@Override
		public void handleMessage( Message msg ) {
			switch ( msg.what ) {
			case MSG_BATTERY_USB_CABLE_PLUGGED:
				removeMessages( MSG_BATTERY_USB_CABLE_PLUGGED );
				if ( mPromptDlgShow == 1 ) {
					mPromptDlg.getButton( DialogInterface.BUTTON_POSITIVE ).performClick();
				}

				break;
			case MSG_BATTERY_USB_CABLE_NOT_PLUGGED: // when discharging ???
				removeMessages( MSG_BATTERY_USB_CABLE_NOT_PLUGGED );
				//if ( !m_bBatteryLevelOK ) { // if battery level is OK, don't prompt again.
				if ( m_iBatteryLevel < 0 ) { // if battery level is OK, don't prompt again.
					promptDialog(R.string.powertest_batterycharge_prompt_title, R.string.powertest_batterycharge_prompt_message);
				}
				break;
			case MSG_BATTERY_DISPLAY:
				removeMessages( MSG_BATTERY_DISPLAY );
				// display the battery current, voltage, capacity ...
				String batteryInfo = "ChargeType: ";
				textviewBatteryInfo.setText( batteryInfo );

				String batRange = BAT_CAPACITY_RANGE_MIN + "%~" + BAT_CAPACITY_RANGE_MAX + "%";
				if ( mCapacity >= BAT_CAPACITY_RANGE_MIN && mCapacity <= BAT_CAPACITY_RANGE_MAX ) { // 60~65%

					textviewInfo.setText( mCapacity + "% in the range (" + batRange + "), Test OK!" );
					m_bBatteryLevelOK = true;
					m_iBatteryLevel = 0;

					mResult = true;
				//	sendEmptyMessage( MSG_BATTERY_RESULT );
					if ( isGroup ) {
						if ( !mShowResult ) {
							mShowResult = true;
							Log.i(TAG, "start showRuninResult() ... mShowResult: " + mShowResult + " mResult: " + mResult );
							showRuninResult( mResult );
						}
					}
					else {
						sendEmptyMessage( MSG_BATTERY_RESULT );
					}

				}
				else if ( mCapacity > BAT_CAPACITY_RANGE_MAX ) { // enable discharge, disable charge
					m_iBatteryLevel = 1;
					m_bBatteryLevelOK = false;
					textviewInfo.setText( mCapacity + "% out of range (" + batRange + "), so discharging ..." );
//					Log.i(TAG, "Battery Capacity is " + batteryCapacity + "%, out of range (60%~65%), so discharging ..." );

					//mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC , true);
				}
				else if ( mCapacity < BAT_CAPACITY_RANGE_MIN ) { // disable discharge, enable charge
					m_bBatteryLevelOK = false;
					m_iBatteryLevel = -1;
					textviewInfo.setText( mCapacity + "% out of range (" + batRange + "), so charging ..." );
//					Log.i(TAG, "Battery Capacity is " + batteryCapacity + "%, out of range (60%~65%), so charging ..." );

					//mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC , true);
				}


				break;
			case MSG_BATTERY_RESULT:
				removeMessages( MSG_BATTERY_RESULT );
				mThreadRunning = false;


				if ( !mReport ) {
					mReport = true;
	//				TestResult( mResult );
					mTimeoutHandler.postDelayed( mRunner, 2000L );
				}
				break;
			}
		}

	}

	private class BatteryThread111 extends Thread {
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
			String batteryInfo;
			while ( mThreadRunning ) {

				// copy from VideoPlay ...
				if ( mCapacity < BAT_CAPACITY_RANGE_MIN ) {
					// out of range, charging ...

					supplyByBattery( false ); // charging

					if ( mBrightness != 1 ) {
						writeSysfile( FILE_BACKLIGHT_BRIGHTNESS, "1" );
						mBrightness = 1;
						Log.i( TAG, "[Step1] mBatCapacity: " + mCapacity + "; mBrightness: " + mBrightness );
					}
				}
				else if ( mCapacity > BAT_CAPACITY_RANGE_MAX ) {
					// out of range, discharging ...

					supplyByBattery( true ); // discharging

					if ( mBrightness != 255 ) {
						writeSysfile( FILE_BACKLIGHT_BRIGHTNESS, "255" );
						mBrightness = 255;
						Log.i( TAG, "[Step2] mBatCapacity: " + mCapacity + "; mBrightness: " + mBrightness );
					}
				}
				else {
					// ok
					supplyByInput( true ); // no charging
					if ( mBrightness != mBrightnessBak ) { // restore the backlight
						writeSysfile( FILE_BACKLIGHT_BRIGHTNESS, "" + mBrightnessBak );
						mBrightness = mBrightnessBak;
						Log.i( TAG, "[Step2] mBatCapacity: " + mCapacity + "; mBrightness: " + mBrightness );
					}
				}

				try {
					Thread.sleep( 100 );
				}
				catch ( InterruptedException e ) {
					e.printStackTrace();
				}
			}
		}
	}
*/
	private class BatteryThread extends Thread {
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
			while ( mThreadRunning ) {

				mCurrent = Integer.valueOf( FileUtility.getSysfsFile( FILE_BATTERY_CURRENT ).trim() ) / 1000;

				mHandler.sendEmptyMessage( MSG_DISPLAY );

				try {
					Thread.sleep(4000);
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
				android.R.string.ok, new OnClickListener() {
					
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

	public boolean dispatchKeyEvent( KeyEvent event ) {
		if ( event.getKeyCode() == KeyEvent.KEYCODE_BACK ) {
			//this.setResult( RESULT.UNDEF.ordinal() );
			//finish();
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
}

