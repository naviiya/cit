package com.cit.test.runintest.pcba;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.cit.test.R;
import com.cit.test.runintest.TestCase;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

//import android.app.AlertDialog;
//import android.os.ServiceManager;
//import android.os.SystemProperties;
//import android.os.storage.IMountService;
//import android.os.storage.StorageEventListener;
//import android.os.storage.StorageVolume;


/* StorageTest_SDCardSlotActivity
 * 1. Request (PCBA, FT)
 *    1) detected pin check  without card 	
 *    2) automatic judgement
 *    3) auto generation log
 * 2. Program
 *    /sys/devices/platform/rk29_sdmmc.0/present: 0=SD not found; 1=SD found.
 *    1) read this file, if one, FAIL, else PASS. 
 *  
 */
public class StorageTest_SDCardSlotActivity extends Activity {
    
	private static final String TAG = "StorageTest_SDCardSlotActivity";
    
    //private static final String SYSFS_SD_PRESENT = "/sys/devices/platform/rk29_sdmmc.0/present"; // 0:NOT FOUND; 1: FOUND.
    private static final String SYSFS_SD_PRESENT = "/sys/class/gpio/gpio309/value"; // "1": no; "0": found
    
//    private Context mContext;
//	private final static int TIMEOUT_SECOND = 10;
//	private int mSec = TIMEOUT_SECOND;
	
	private final static int INFO_DISPLAY_TIME = 1000; // unit: ms
    TextView textviewInfo;
	
	
//    private static final String TEST_STRING = "Rockchip UsbHostTest File";
//    private static final int BACK_TIME = 1000;
//    private static final int R_PASS = 1;
//    private static final int R_FAIL = 2;
    private StringBuilder sBuilder;
//    private SdcardReceiver sdcardReceiver = null;
//    public String SUCCESS;
//    public String FAIL;
//    private boolean isFindSd = true;
//    private StorageManager mStorageManager = null;
//    TextView mResult;
//hehg
//	public static String flash_path = null;
//	public static String sdcard_path = null;
//	public static String usb_path = null;

    private Handler mTimeoutHandler = new Handler();
    
	private boolean mResult = false;
	private HeadsetThread mThread;
	private HeadsetHandler mHandler;
	
	public StorageTest_SDCardSlotActivity() {
		mHandler = new HeadsetHandler();
		mThread = new HeadsetThread();
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.storagetest_sdcard);

		textviewInfo = (TextView)findViewById( R.id.sdresultText );
		//textviewInfo.setGravity( 17 );
		
//        ControlButtonUtil.initControlButtonView(this);
//        findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
//        findViewById(R.id.btn_Fail).setVisibility(View.INVISIBLE);
//        findViewById(R.id.btn_Skip).setVisibility(View.INVISIBLE);
        
        sBuilder = new StringBuilder();
        
        mThread.start();
    }
//hehg
//========================================================================
   
    public void TestResult(int result) {
//       if (result == R_PASS) {
//            ((Button) findViewById(R.id.btn_Pass)).performClick();
//        } else if (result == R_FAIL) {
//            ((Button) findViewById(R.id.btn_Fail)).performClick();
//        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}

	///////////////////////////////////////////////////////////////////
	//hehg
	/*
	 * arg1: String filename
	 * return: String  ---- data got from file.
	 */
	public static String getSysfsFile(String filename ) {
		String result = "";
		try {
			FileReader fr = new FileReader( filename );
			BufferedReader br = new BufferedReader( fr );
			result = br.readLine().trim();
			br.close();
		}
		catch ( FileNotFoundException e ) {
			e.printStackTrace();
		}
		catch ( IOException e ) {
			e.printStackTrace();
		}
		return result;
	}

	private void promptTestAgain( int titleId, int messageId ) {
		AlertDialog dlg = null;
			AlertDialog.Builder bu = new AlertDialog.Builder(this);

			//bu.setTitle(titleId);
			bu.setTitle(getString(titleId) + " ( " + mAgainTimes + " )");

			bu.setMessage(messageId);
			bu.setPositiveButton( android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
				//	m_bAgain = true;
					mTestStep = 0;

					Log.i( TAG, "<MSG_SDCARD_AGAIN> test again" );
				}
			});
			bu.setNegativeButton( android.R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
				//	m_bAgain = false;
					mResult = false;
					mHandler.sendEmptyMessageDelayed(MSG_SDCARD_RESULT, INFO_DISPLAY_TIME);
					Log.i( TAG, "<MSG_SDCARD_AGAIN> not again, so failed" );
				}
			});
			dlg = bu.create();
			dlg.setCanceledOnTouchOutside( false );
			
			dlg.show();

	}
	
	
	private static final int MSG_SDCARD_NOT_FOUND = 0;
	private static final int MSG_SDCARD_FOUND = 1;
	private static final int MSG_SDCARD_WRITE_FILE = 2;
	private static final int MSG_SDCARD_READ_FILE = 3;
	private static final int MSG_SDCARD_REMOVED = 4;
	private static final int MSG_SDCARD_TIMEOUT = 5;
	private static final int MSG_SDCARD_RESULT = 6;
	private static final int MSG_SDCARD_AGAIN = 7;
	
	private boolean mThreadRunning = true;
	private boolean mReport = false;
	
	private int mTestStep = 0; // 0: no sdcard; 1: sdcard; 2: write; 3: read; 4: compare; 5: removed sdcard.
	private int TESTSTEP0_NO_SDCARD = 0;
	private int TESTSTEP1_FOUND_SDCARD = 1;
	private int TESTSTEP2_WRITE_SDCARD = 2;
	private int TESTSTEP3_READ_SDCARD = 3;
	private int TESTSTEP4_COMPARE_SDCARD = 4;
	private int TESTSTEP5_REMOVED_SDCARD = 5;
	
	private int mAgainTimes = 3;
	private boolean m_bAgain = false;
	private boolean m_bTimeout = false;
	private int mState = 0;
	
	
	private class HeadsetHandler extends Handler {
		@Override
		public void handleMessage( Message msg ) {
			switch ( msg.what ) {
			case MSG_SDCARD_AGAIN:
				removeMessages( MSG_SDCARD_AGAIN );
				if ( mAgainTimes > 0 ) {
					promptTestAgain( R.string.testagain__prompt_title, R.string.testagain__prompt_msg );
					mAgainTimes --;
				}
				else {
					mResult = false;
					sendEmptyMessageDelayed( MSG_SDCARD_RESULT, INFO_DISPLAY_TIME );
				}
				Log.i( TAG, "<MSG_SDCARD_AGAIN> mAgainTimes=" + mAgainTimes + "; m_bAgain=" + m_bAgain );

				break;
			case MSG_SDCARD_NOT_FOUND:
				removeMessages( MSG_SDCARD_NOT_FOUND );

				if ( mTestStep == 0 ) {
					mTestStep = 2;
					sBuilder.append( "检测到SD卡槽的侦测脚为高！PASS\n" );
					textviewInfo.setText( sBuilder.toString() );
					//textviewInfo.setText( "检测到SD卡槽的侦测脚为高！PASS" );
					mResult = true;
					sendEmptyMessageDelayed( MSG_SDCARD_RESULT, INFO_DISPLAY_TIME );
				}
				break;
			case MSG_SDCARD_FOUND:
				removeMessages( MSG_SDCARD_FOUND );
				
				if ( mTestStep == 0 ) {
					mTestStep = 1;
					sBuilder.append( "检测到SD卡槽的侦测脚为低！FAIL\n" );
					textviewInfo.setText( sBuilder.toString() );
					//textviewInfo.setText( "检测SD卡槽的侦测脚为低！FAIL" );
					sendEmptyMessage( MSG_SDCARD_AGAIN );
				}
				break;
			case MSG_SDCARD_RESULT:
				removeMessages( MSG_SDCARD_RESULT );
				mThreadRunning = false;

				Log.i( TAG, "<MSG_SPEAKER_RESULT> report mResult: " + mResult );
				mTestStep = 0;
				
				if ( !mReport ) {
					mReport = true;
					setResult( (mResult ? TestCase.RESULT.OK.ordinal() : TestCase.RESULT.NG.ordinal()) );
					mTimeoutHandler.postDelayed(new Runnable() {
						public void run() {
							finish();
						}
					}, 2000 );
				}
			
				break;

			default:
				break;
			}
			
			
		}
		
	}

	private class HeadsetThread extends Thread {
		public void exit() {
			mThreadRunning = false;
/*			try {
				this.join();
				Log.i( TAG, "thread join() ..." );
			}
			catch ( InterruptedException e ) {
				e.printStackTrace();
			}
*/		}
		
		public void msleep( long ms ) {
			try {
				Thread.sleep( ms );
			}
			catch ( InterruptedException e ) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			Log.i(TAG, "Thread.run() is start.");
			while ( mThreadRunning ) {
			/*	
				int state = Integer.valueOf( getSysfsFile(SYSFS_SD_PRESENT).trim() );
//				Log.i( TAG, "Thread: " + SYSFS_SD_PRESENT + " = " + state );
				
				if ( state == 0 ) { // not found SD card
					mHandler.sendEmptyMessage( MSG_SDCARD_NOT_FOUND );
				}
				else if ( state == 1 ) { // found SD card
					mHandler.sendEmptyMessage( MSG_SDCARD_FOUND );
				}
				mState = state;
			*/
				String state = getSysfsFile(SYSFS_SD_PRESENT).trim();
				
				Log.i( TAG, "Thread: " + SYSFS_SD_PRESENT + " = " + state );
				
				if ( state.equals("1") ) { // the slot's CD pin is high level (this meaning that no sd card in)
					mHandler.sendEmptyMessage( MSG_SDCARD_NOT_FOUND );
				}
				else if ( state.equals("0") ) { // the slot's CD pin is low level (this meaning that sd card in)
					mHandler.sendEmptyMessage( MSG_SDCARD_FOUND );
				}
			
				msleep( 100 );
    		}
			
			Log.i(TAG, "Thread.run() is exit.");
    	}
	}
}
