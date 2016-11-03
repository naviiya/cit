package com.cit.test.runintest.runin;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.cit.test.R;
import com.cit.test.runintest.FileUtility;
import com.cit.test.runintest.TestCase.RESULT;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

//import android.os.SystemProperties;

/* ThermalActivity
 * Thermal Test
 * 1. Request (RUNIN)
 *    1) read CPU temp., standard <85℃
 *    2) read battery  temp., standard :0~50℃
 *    3) automatic judgement
 *    4) auto generation log
 * 2. Program
 * 
 */
public class ThermalActivity extends Activity {
	
	private final static String TAG = "ThermalActivity";
	
//	private String cpuTempFile = "/sys/devices/platform/rk30_i2c.1/i2c-1/1-004c/rtemperature";
	
//	private final static String SYSFS_CPU_CORE_0_TEMP = "/sys/class/hwmon/hwmon1/device/temp2_input"; //(Unit: millidegree Celsius)
//	private final static String SYSFS_CPU_CORE_1_TEMP = "/sys/class/hwmon/hwmon1/device/temp3_input";
//	private final static String SYSFS_CPU_CORE_2_TEMP = "/sys/class/hwmon/hwmon1/device/temp4_input";
//	private final static String SYSFS_CPU_CORE_3_TEMP = "/sys/class/hwmon/hwmon1/device/temp5_input";
	private final static String SYSFS_CPU_CORE_0_TEMP = "/sys/class/thermal/thermal_zone0/temp"; //(Unit: millidegree Celsius)
	private final static String SYSFS_CPU_CORE_1_TEMP = "/sys/class/thermal/thermal_zone1/temp";
	private final static String SYSFS_CPU_BOARD_TEMP = "/sys/class/thermal/thermal_zone2/temp";
	private final static String SYSFS_CPU_CHARGER_TEMP = "/sys/class/thermal/thermal_zone3/temp";
	
	private final static String SYSFS_CPU_CORE_0_TYPE = "/sys/class/thermal/thermal_zone0/type";
	private final static String SYSFS_CPU_CORE_1_TYPE = "/sys/class/thermal/thermal_zone1/type";
	
	private final static int CPU_TEMP_MAX = 85; // temp5_crit: 90000 (Unit: millidegree Celsius)
	private final static int BAT_TEMP_MAX = 50;
	
	private float temp_core0;
	private float temp_core1;
	private float temp_board;
	private float temp_charger;
	
	private float temp_battery;
	
	private TextView viewCPUTemp;
	private TextView viewBatteryTemp;
	
	private boolean mResult = false;
	
	//private String cpuTemfilePath = "/sys/module/tsadc/parameters/temp1";
	private FileReader cpuTemfile;
	private String cpuTemInfo;
	private int cpuNum;
	
	private String batTemInfo;
	private float batNum;
	
	private Handler mTimeOutHandler = new Handler();
	
	private BatteryReceiver receiver;
	
	//private static final String filePAth = "/mnt/sdcard/temperature.txt";
	private static final String filePAth = "/sdcard/temperature.txt";
	private File temperatureFile;
	//private File logFilePass;
	//private File logFileFail;
//	private String logPath = "/mnt/sdcard/";
	private String logPath = "/sdcard/";
	//String snNumber = SystemProperties.get("ro.serialno","unknow");	
//	String snNumber = SystemProperties.get("ro.dmi.serialnumber","unknow");
//	private File logFileFail = new File(logPath + snNumber + "+ARUNIN+FAIL.txt");
//	private File logFilePass = new File(logPath + snNumber + "+ARUNIN+PASS.txt");	
		

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView( R.layout.runin_thermal );
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
		
		viewCPUTemp = (TextView) findViewById (R.id.cpuTemInfo);
		viewBatteryTemp = (TextView) findViewById (R.id.batTemInfo);
		
		int tmp;
		String strTemp = "";
		String strType1;
		String strType2;
		
		tmp = Integer.valueOf( FileUtility.getSysfsFile( SYSFS_CPU_CORE_0_TEMP ).trim() );
		temp_core0 = (float)tmp / 1000;
		tmp = Integer.valueOf( FileUtility.getSysfsFile( SYSFS_CPU_CORE_1_TEMP ).trim() );
		temp_core1 = (float)tmp / 1000;
		tmp = Integer.valueOf( FileUtility.getSysfsFile( SYSFS_CPU_BOARD_TEMP ).trim() );
		temp_board = (float)tmp / 1000;   
		
		strType1 = FileUtility.getSysfsFile( SYSFS_CPU_CORE_0_TYPE ).trim();
		strType2 = FileUtility.getSysfsFile( SYSFS_CPU_CORE_1_TYPE ).trim();
//		tmp = Integer.valueOf( FileUtility.getSysfsFile( SYSFS_CPU_CHARGER_TEMP ).trim() );
//		temp_charger = (float)tmp / 1000;       //can't read 
		
		//strTemp = String.format("%0.1f℃ %0.1f℃ %0.1f℃ %0.1f℃", temp_core0, temp_core1, temp_core2, temp_core3);
		//strTemp = String.format("%0.1f %0.1f %0.1f %0.1f", temp_core0, temp_core1, temp_core2, temp_core3);
		strTemp = temp_core0 + "℃ " + temp_core1 + "℃ " + temp_board + "℃ ";// + temp_charger + "℃ ";
		viewCPUTemp.setText(strTemp);
		Log.i(TAG, "CPU Thermal: " + strTemp);
		
		
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		receiver = new BatteryReceiver();
		registerReceiver(receiver, filter);

		mTimeOutHandler.postDelayed(mRunner, 3000);

//		ControlButtonUtil.initControlButtonView(this);
//		findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
//	    findViewById(R.id.btn_Fail).setVisibility(View.INVISIBLE);
//	    findViewById(R.id.btn_Skip).setVisibility(View.INVISIBLE);
		
	}
	
	private Runnable mRunner = new Runnable() {
		public void run() {
			
			if ( temp_core0 < CPU_TEMP_MAX && temp_core1 < CPU_TEMP_MAX &&
				temp_board < CPU_TEMP_MAX && 
				//temp_charger < CPU_TEMP_MAX &&
				temp_battery < BAT_TEMP_MAX ) {
				mResult = true;
			}
			else {
				mResult = false;
			}
			
			setResult( (mResult ? RESULT.OK.ordinal() : RESULT.NG.ordinal()) );
//			String res = "";
			if ( mResult ) {
//				res = getResources().getString(R.string.pass);
				viewCPUTemp.setTextColor(Color.GREEN);
				viewBatteryTemp.setTextColor(Color.GREEN);
			}
			else {
//				res = getResources().getString(R.string.fail);
				viewCPUTemp.setTextColor(Color.RED);
				viewBatteryTemp.setTextColor(Color.RED);
			}
//			mResultView.setText(res);
			
			new Handler().postDelayed(new Runnable() {
				public void run() {
					finish();
				}
			}, 1000 );
		}
	};
	
	public void writeTestFile() {
		temperatureFile = new File(filePAth);
    	if(!temperatureFile.exists()) {
    		try {
    			temperatureFile.createNewFile();
				FileWriter writer = new FileWriter(temperatureFile, false);
				writer.write("CPU Temperature: " + cpuNum + "℃" + "\n" 
						+ "Battery Temperature: " + batNum + "℃");
				writer.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }

	private class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(Intent.ACTION_BATTERY_CHANGED.equals(action)) {
            	  //batNum = intent.getIntExtra("temperature", 0) / 10;
            	  //batTemInfoView.setText(batNum + "℃");
            	int tmp = intent.getIntExtra("temperature", 0);
            	temp_battery = (float)tmp / 10;
            	viewBatteryTemp.setText(temp_battery + "℃");
            	//writeTestFile();
              }
         }
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
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









