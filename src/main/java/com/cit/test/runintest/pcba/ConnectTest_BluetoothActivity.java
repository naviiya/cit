package com.cit.test.runintest.pcba;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cit.test.R;
import com.cit.test.runintest.ControlButtonUtil;
import com.cit.test.runintest.TestCase.RESULT;

import java.util.List;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

//import android.webkit.WebView;

/*
 * ConnectTest_BluetoothActivity
 * Request: (PCBA)
 *	1. Detecting device.
 *	2. automatic judgement.
 *	3. auto generation log.
 * Program:
 *  1. get BluetootAdapter
 *  2. enable it
 *  3. scan device by startDiscovery()
 *  4. get and display the device's name and address
 *  5. disable bt
 *  
历年海华的MAC地址段
D0E782
B0EE45
0008CA
DC85DE
002423
0025D3
1C4BD6
485D60
6C71D9
742F68
74F06D
94DBC9
E0B9A5
4CAA16

6CADF8 //2013.08.28 add

用在AW-NH660上的MAC地址段：
0008CA
94DBC9
4CAA16
B0EE45
D0E782
只有上述五组。

4K库存，Jinzhong.Wang@azurewave.com.cn提供了4K的BT MAC地址 范围（D0E782CDC6D6~D0E782CDD675）给T7T项目使用

[T7GT2] WIFI COB Chip: Realtek RTL8723BS
BYD MAC field:
00:1E:DE
BT MAC = WIFI_MAC + 1

  */


public class ConnectTest_BluetoothActivity extends Activity {

	private static final String TAG = "ConnectTest_BluetoothActivity";
	
	private static final String[] AW_MAC_FIELD = {
		"6C:AD:F8", // 2013.08.28 new add
		"D0:E7:82", // for BT MAC field
		"B0:EE:45", // below is for WIFI MAC field.
		"00:08:CA",
		"DC:85:DE",
		"00:24:23",
		"00:25:D3",
		"1C:4B:D6",
		"48:5D:60",
		"6C:71:D9",
		"74:2F:68",
		"74:F0:6D",
		"94:DB:C9",
		"E0:B9:A5",
		"4C:AA:16",
	};
	
	private static final String[] AMPAK_MAC_FIELD = {
		"98:3B:16", // after 2013.08
		"00:22:F4", // before 2013.08
	};
	
	private static final String[] BYD_MAC_FIELD = {
		"00:1E:DE",
	};
	
	
	private WIFIHandler mWifiHandler;
//	private WifiManager mWifiManager;
//	private BroadcastReceiver mWifiReceiver;
	private BluetoothManager mBtManager;
	private BluetoothAdapter mBtAdapter;
	private BroadcastReceiver mBtReceiver;
	
	private StringBuilder sBuilder;
	
	private boolean mResult = false;
	
//	private BroadcastReceiver mReceiver;
//	TextView mResult;
	TextView mInfoText;
	boolean stop = false;
	private boolean mReadyToTest = false;
	TextView mText;
	TextView mTitle;
	ProgressBar wifiProgressBar;

	private List<String> mWifiList;
	

	private final static String ERRMSG = "Wifi test failed!";
	private int testType = 1;
	private boolean flag = true;
	private Handler mTimeOutHandler = new Handler();
	//steven add for mac check 
	private Runnable mmRunner = new Runnable(){

		public void run() {
			// TODO Auto-generated method stub
			((Button) findViewById(R.id.btn_Fail)).performClick();
	}};
	//steven add end 
	public ConnectTest_BluetoothActivity() {

//		this.mWifiList = new ArrayList<String>();

		mWifiHandler = new WIFIHandler();
	}
	
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.connecttest_bluetooth);
		
		testType = getIntent().getIntExtra("testType", 1);

		mInfoText = (TextView) findViewById(R.id.infoText);

		wifiProgressBar = (ProgressBar) findViewById(R.id.progress);
		wifiProgressBar.setVisibility(View.VISIBLE);
		
		ControlButtonUtil.initControlButtonView(this);
	    findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
	    findViewById(R.id.btn_Fail).setVisibility(View.INVISIBLE);
	    findViewById(R.id.btn_Skip).setVisibility(View.INVISIBLE);

//hehg		
	    //mBtAdapter = BluetoothAdapter.getDefaultAdapter();
	    //mBtAdapter = (BluetoothAdapter)getSystemService( Context.BLUETOOTH_SERVICE );
	    // For API level 18 and above, get a reference to BluetoothAdapter through BluetoothManager.
		mBtManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		if (mBtManager == null) {
			Log.e(TAG, "Unable to initialize BluetoothManager.");
		}

	    mBtAdapter = mBtManager.getAdapter();
	    if (mBtAdapter == null) {
	        Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
	    }
	    
		mBtReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
			//	if (stop) {
			//		return;
			//	}
				String action = intent.getAction();

				Log.i( TAG, "action:" + action);
				
				if ( action.equals( BluetoothAdapter.ACTION_STATE_CHANGED ) ) {
					int state = intent.getIntExtra( BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF );
					Log.i( TAG, "state:" + state);
					if ( state == BluetoothAdapter.STATE_ON ) {
						mWifiHandler.sendEmptyMessage( MSG_CHECK_MAC );
						Log.i( TAG, "BluetoothAdapter.STATE_ON, so check WIFI MAC..." );
					}
					else if ( state == BluetoothAdapter.STATE_OFF ) {
						if ( wifi_enabled ) {
							mWifiHandler.sendEmptyMessageDelayed( MSG_RESULT, 2000L );
							wifi_enabled = false;
						}
						Log.i( TAG, "BluetoothAdapter.STATE_OFF, so report the result ... wifi_enabled=" + wifi_enabled );
					}
				}
/*				
				if ( action.equals( WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION ) ) {
					boolean connected = intent.getBooleanExtra( WifiManager.EXTRA_SUPPLICANT_CONNECTED, false );
					Log.i( TAG, "connected:" + connected);
					if (connected && mReadyToTest) {
						Log.i("Jeffy", "already connect to:" + mWifiManager.getConnectionInfo().getSSID());
						mInfoText.setText("connect to " + mWifiManager.getConnectionInfo().getSSID());
//						mHandler.sendEmptyMessage(MSG_PING_TEST);	//change no pingtest
						//mWifiHandler.sendEmptyMessageDelayed( MSG_FINISH_TEST, 1000);
					}
				}
				
				if ( action.equals( WifiManager.SCAN_RESULTS_AVAILABLE_ACTION ) ) {
					List<ScanResult> resultList = mWifiManager.getScanResults();
					Collections.sort(resultList, 
							new Comparator<ScanResult>() {
								public int compare(ScanResult s1, ScanResult s2) {
									return s2.level - s1.level;
								}
							}
					);				
				}
*/
			}
		};

		
		sBuilder = new StringBuilder();

		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction( BluetoothAdapter.ACTION_STATE_CHANGED );
//		wifiFilter.addAction( WifiManager.SCAN_RESULTS_AVAILABLE_ACTION );
//		wifiFilter.addAction( WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION );
		registerReceiver( mBtReceiver, iFilter );
		

		mWifiHandler.sendEmptyMessage( MSG_BT_ON );
		
	}
	
	 @Override
	 protected void onDestroy() {
		unregisterReceiver( mBtReceiver );
		super.onDestroy();
	 }

	protected void onResume() {
		super.onResume();
/*		IntentFilter localIntentFilter = new IntentFilter();
		localIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		localIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		localIntentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		registerReceiver(mReceiver, localIntentFilter);
		this.mWifiList.clear();
*/
		stop = false;
/*		
		if(testType == 1 || testType == 2) {
			mResult.setVisibility(View.INVISIBLE);
			wifiProgressBar.setVisibility(View.INVISIBLE);
			findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
			findViewById(R.id.btn_Fail).setVisibility(View.INVISIBLE);
			findViewById(R.id.btn_Skip).setVisibility(View.INVISIBLE);
			if(mWifiManager.isWifiEnabled()) {
				flag = true;
				mInfoText.setText("WiFi is enable!");
				mTimeOutHandler.postDelayed(mRunner, 2000);
			} else {
				mWifiManager.setWifiEnabled(true);
				if(mWifiManager.isWifiEnabled()) {
					flag = true;
					mInfoText.setText("WiFi is enable!");
					mTimeOutHandler.postDelayed(mRunner, 2000);
				} else {
					flag = false;
					mInfoText.setText("WiFi is disable!");
					mTimeOutHandler.postDelayed(mRunner, 2000);
				}
			}
		} else {
			Log.i("Jeffy", "try to enable wifi");
			mWifiManager.setWifiEnabled(true);
			mHandler.sendEmptyMessage(MSG_SCAN);
		}
*/		
		Log.i("Jeffy", "start test");
	}

	public void onPause() {
		stop = true;
		super.onPause();
//		if (this.mWifiManager == null) {
//			return;
//		}

		Log.i("Jeffy", "end test");
//		this.mHandler.removeMessages(MSG_SCAN);
//		this.mHandler.removeMessages(MSG_ERROR);

//		unregisterReceiver(mReceiver);
		// mWifiManager.setWifiEnabled(false);
//		mHandler.removeMessages(MSG_FAIL);
	}

	private static final int MSG_RESULT = 0;
	private static final int MSG_WIFI_ON = 1;
	private static final int MSG_WIFI_OFF = 2;
	private static final int MSG_BT_ON = 3;
	private static final int MSG_BT_OFF = 4;
	private static final int MSG_CHECK_MAC = 5;
	private int count = 0;
	private boolean reported = false;
	private boolean wifi_enabled = false;
	
	private class WIFIHandler extends Handler {
		@Override
		public void handleMessage( Message msg ) {
			switch ( msg.what ) {
			case MSG_RESULT:
				removeMessages( MSG_RESULT );
				Log.i( TAG, "<MSG_RESULT> report mResult: " + mResult );
				
				if ( !reported ) {
					reported = true;
				
				if ( mResult ) {
					sBuilder.append( "蓝牙检测  成功\n" );
				}
				else {
					sBuilder.append( "蓝牙检测  失败\n" );
				}
				mInfoText.setText(sBuilder.toString());
				
				setResult( mResult ? RESULT.OK.ordinal() : RESULT.NG.ordinal() );
				//finish();
				mTimeOutHandler.postDelayed( mRunner, 2000L );
				}
				break;
			case MSG_BT_ON:
				removeMessages( MSG_BT_ON );
				Log.i( TAG, "<MSG_BT_ON> MSG_BT_ON " );
				
				boolean br = mBtAdapter.isEnabled();
				if ( br ) {
					wifi_enabled = true;
					sBuilder.append( "蓝牙已经打开\n" );
					mInfoText.setText(sBuilder.toString());
					sendEmptyMessage( MSG_CHECK_MAC );
					break;
				}
				br = mBtAdapter.enable(); // BT ON
				
				if ( br ) {
					sBuilder.append( "打开蓝牙成功\n" );
					mInfoText.setText(sBuilder.toString());
					count = 0;
					wifi_enabled = true;
				}
				else {
					count += 1;
					sBuilder.append( "打开蓝牙失败(" + count + ")\n" );
					mInfoText.setText(sBuilder.toString());
					if ( count >= 3 ) {
						// 
						mResult = false;
						sendEmptyMessage( MSG_RESULT );
					}
					
					sendEmptyMessageDelayed( MSG_BT_ON, 2000L );
				}
				
				break;
			case MSG_BT_OFF:
				removeMessages( MSG_BT_OFF );

				Log.i( TAG, "<MSG_BT_OFF> MSG_BT_OFF " );
				br = mBtAdapter.disable(); // BT Off
				if ( br ) {
					sBuilder.append( "关闭蓝牙成功\n" );
					mInfoText.setText(sBuilder.toString());
					count = 0;
				}
				else {
					count += 1;
					sBuilder.append( "关闭蓝牙失败(" + count + ")\n" );
					mInfoText.setText(sBuilder.toString());
					if ( count >= 3 ) {
						// 
						mResult = false;
						sendEmptyMessage( MSG_RESULT );
					}
					
					sendEmptyMessageDelayed( MSG_BT_OFF, 2000L );
				}

				break;
			case MSG_CHECK_MAC:
				removeMessages( MSG_CHECK_MAC );
				Log.i( TAG, "<MSG_CHECK_MAC> MSG_CHECK_MAC " );
				
				String mac = mBtAdapter.getAddress();
				//if ( mac.equals( "" ) ) { // if mac is null, cause app exit abnormally
				if ( mac == null ) {
					sBuilder.append( "未获得蓝牙的MAC地址，检测失败！\n" );
					mInfoText.setText(sBuilder.toString());
					mResult = false;
					sendEmptyMessage( MSG_BT_OFF );
				}
				else {
					mResult = true;
				}
//				int rssi = wi.getRssi();
				
				Log.i( TAG, "BT MAC: " + mac.toUpperCase() );
//				Log.i( TAG, "WIFI RSSI: " + rssi );
				sBuilder.append( "获得蓝牙的MAC地址: " + mac.toUpperCase() + "\n");
				mInfoText.setText(sBuilder.toString());
				
//				mResult = true;
				/* don't check because the MAC is written before ship.
				flag = false;
				String macfield = mac.toUpperCase().substring(0, 8); // mac="94:DB:C9:EB:46:E0",
				for ( int i = 0; i < BYD_MAC_FIELD.length; i ++ ) {
					Log.i( TAG, "BT MAC: " + mac + " BYD_MAC_FIELD[" + i + "]=" + BYD_MAC_FIELD[i] );
					if ( macfield.equals(BYD_MAC_FIELD[i]) ) {
						flag = true;
					}
				}

				if ( flag ) {
					sBuilder.append( "此地址属于  BYD 的蓝牙地址段\n");
					mResult = true;
					//mInfoText.setText( mInfoText.getText().toString() + "此MAC地址属于海华科技的WIFI地址段，检测成功！");
				}
				else {
					sBuilder.append( "此地址不属于  BYD 的蓝牙地址段\n");
					mResult = false;
					//mInfoText.setText( mInfoText.getText().toString() + "此MAC地址不属于海华科技的WIFI地址段，检测失败！");
				}
				*/
				mInfoText.setText(sBuilder.toString());
				sendEmptyMessage( MSG_BT_OFF );
				break;

			default:
				break;
			}
		}
	}


	private Runnable mRunner = new Runnable() {
		public void run() {
			// TODO Auto-generated method stub
			//TestResult(flag);
			finish();
	}};
	
	public void TestResult(boolean result) {
	    if (result == true) {
	        ((Button) findViewById(R.id.btn_Pass)).performClick();
	    } else if (result == false) {
	        ((Button) findViewById(R.id.btn_Fail)).performClick();
	    }
	}
	
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
	
}
