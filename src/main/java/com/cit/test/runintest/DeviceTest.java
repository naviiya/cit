package com.cit.test.runintest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cit.test.R;
import com.cit.test.runintest.runin.StandbyActivity;
import com.cit.test.runintest.runin.VideoPlayActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static android.view.WindowManager.LayoutParams;

//import android.os.SystemProperties;
//import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
//import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

public class DeviceTest extends Activity {

    public static final int DEVICE_TEST_MAX_NUM = 1000;
    public static final int TEST_FAILED_DELAY = 5000;
    public static final String EXTRA_TEST_PROGRESS = "test progress";
    public static final String EXTRA_TEST_RESULT_INFO = "test result info";

    public static final String RESULT_INFO_HEAD = ";";
    public static final String RESULT_INFO_HEAD_JUST_INFO = "just info;";

    public static final String EXTRA_PATH = "/data/";
    private static final String CONFIG_FILE_NAME1 = "PCBA_TestConfig.xml";
    private static final String CONFIG_FILE_NAME2 = "runin_test_config.xml";
    private static final String CONFIG_FILE_NAME3 = "FT_TestConfig.xml";
    private static final String CONFIG_FILE_NAME4 = "LCDModule_TestConfig.xml";
    private static final String CONFIG_FILE_NAME5 = "Other_TestConfig.xml";
    private static final String CONFIG_FILE_NAME6 = "Auto_TestConfig.xml";

    private static final String EXTRA_CONFIG_FILE_NAME1 = EXTRA_PATH + CONFIG_FILE_NAME1;
    private static final String EXTRA_CONFIG_FILE_NAME2 = EXTRA_PATH + CONFIG_FILE_NAME2;
    private static final String EXTRA_CONFIG_FILE_NAME3 = EXTRA_PATH + CONFIG_FILE_NAME3;
    private static final String EXTRA_CONFIG_FILE_NAME4 = EXTRA_PATH + CONFIG_FILE_NAME4;

    public static final int TESTCASE_TYPE_PCBA = 1;
    public static final int TESTCASE_TYPE_RUNIN = 2;
    public static final int TESTCASE_TYPE_FT = 3;
    public static final int TESTCASE_TYPE_LCD = 4;
    public static final int TESTCASE_TYPE_OTHER = 5;
    public static final int TESTCASE_TYPE_AUTO = 6;

    private int testType = TESTCASE_TYPE_RUNIN;

    private Context mContext;

    public static final String DATA_PATH = "/data/data/com.byd8.test/";
    private static final String TAG = "DeviceTest";
    private static final String SAVE_DATA_PATH = DATA_PATH + "DeviceTest.tmp";
    public static final String TEMP_FILE_PATH = DeviceTest.DATA_PATH + "test";

    private int pcba_bar_code[] = { R.drawable.hpcba002, R.drawable.hpcba003,
            R.drawable.hpcba004, R.drawable.hpcba005, R.drawable.hpcba006,
            R.drawable.hpcba007, R.drawable.hpcba008, R.drawable.hpcba009,
            R.drawable.hpcba010, R.drawable.hpcba011, R.drawable.hpcba012,
            R.drawable.hpcba013, R.drawable.hpcba014, R.drawable.hpcba015,
            R.drawable.hpcba016, R.drawable.hpcba017, R.drawable.hpcba018,
            R.drawable.hpcba019, R.drawable.hpcba020, R.drawable.hpcba021,
            R.drawable.hpcba022, R.drawable.hpcba023, R.drawable.hpcba024,
            R.drawable.hpcba025, R.drawable.hpcba026, R.drawable.hpcba027,
            R.drawable.hpcba028, R.drawable.hpcba029, R.drawable.hpcba030,
            R.drawable.pcbapass };

    private XmlDeal xmldoc = null;
    private Button mButtonCancel;
    private Button mTestChecked;
    MyGridView myGridView;

    public String mGPS;	 // "1": GPS; "0": no GPS

    /*private Button btn1;
    private Button btn2;
    private Button btn3;
    private Button btn4;
    private Button btn5;
    private Button btn6;*/	// 自动化测�?

    private List<TestCase> mTestCases;
    private List<TestCase> mCurrentCaseGroup;
    Object[] mTestGroupNames;


    private String logPath = "/mnt/sdcard/";
    private File logFilePass;
    private File logFileFail;
    private File logpcba;
    private File logrunin;
    private File logft;

    private boolean isGroup = true;

    private int mCount;
    private int nRunTime = 0;

    //hehg : for Runin Standby, Reboot, VideoPlay
    private LinearLayout mLayoutRuninSetting;
    private LinearLayout mLayoutGridView;
    private RelativeLayout.LayoutParams mLP1; // no runin-setting
    private RelativeLayout.LayoutParams mLP2; // hav runin-setting

    public final static String FILE_STANDBY = "/mnt/sdcard/runin_standby";
    public final static String FILE_REBOOT = "/mnt/sdcard/runin_reboot";
    public final static String FILE_PLAYTIME = "/mnt/sdcard/runin_playtime";

    public final static String FILE_REBOOT_DONE = "/mnt/sdcard/runin_reboot_done";

    public static boolean bChargeContrl = false;

    public static String mMBSN = "";
    public static String mSN = "";
    public static String mBIOSVer = "";

    public static String mFT_CPU = "";
    public static String mFT_MEM = "";
    public static String mFT_EMMC = "";
    public static String mFT_WIFI = "";
    public static String mFT_BT = "";
    public static String mFT_GPS = "";
    public static String mFT_BIOS = "";
    public static String mFT_RCAM = "1";
    public static String mFT_TPVer = "1";


    public final static String TABLETTEST = "tablettest.";
    public final static String TABLETTEST__GRID_LOCKED = TABLETTEST + "grid_locked";
    public final static boolean TABLETTEST__GRID_LOCKED__DEFAULT = false;
    private boolean m_bGridLocked = false;


    /** Called when the activity is first created. */


    public static int get_facotry_hwinfo() {
        int ret = 0;
        String fname = "/sdcard/hwinfo";    //Modify by lsg
        String hwinfo = "";
        try {
            File f = new File( fname );
            if ( f.exists() ) {
                FileReader fr = new FileReader( f );
                BufferedReader br = new BufferedReader( fr );
                hwinfo = br.readLine();
                br.close();
                Log.i(TAG, "hwinfo: " + hwinfo);

                final String HWINFO_REGEX = "^CPU=(\\S+)#MEM=(\\S+)#EMMC=(\\S+)#WIFI=(\\S+)#BT=(\\S+)#GPS=(\\S+)#BIOS=(\\S+)#TPVER=(\\S+)#RCAM=(\\S+)#.*";     //Modify by lsg

                try {
                    Pattern p = Pattern.compile(HWINFO_REGEX);
                    Matcher m = p.matcher(hwinfo);
                    boolean r = false;
                    r = m.matches();
                    if ( !r ) {
                        Log.e(TAG, "NOT matches! HWINFO_REGEX=\"" + HWINFO_REGEX + "\"");
                        return -1;
                    }

                    for (int i = 0; i < m.groupCount(); i ++) {
                        Log.e(TAG, "FOUND(" + i + "): " + m.group(i+1));
                    }

                    if ( m.groupCount() < 7 ) {
                        Log.e(TAG, "matched group count = " + m.groupCount() + ", less than 7! HWINFO_REGEX=\"" + HWINFO_REGEX + "\"");
                        return -1;
                    }
                    else {
                        mFT_CPU = m.group(1);
                        mFT_MEM = m.group(2);
                        mFT_EMMC = m.group(3);
                        mFT_WIFI = m.group(4);
                        mFT_BT = m.group(5);
                        mFT_GPS =  m.group(6);                      //Modify by lsg
                        mFT_BIOS = m.group(7);
                        mFT_TPVer = m.group(8);
                        mFT_RCAM = m.group(9);
                        String s = "mFT_CPU=" + mFT_CPU + "; mFT_MEM=" + mFT_MEM +
                                "; mFT_EMMC=" + mFT_EMMC + "; mFT_WIFI=" + mFT_WIFI +
                                "; mFT_BT=" + mFT_BT + "; mFT_GPS=" + mFT_GPS +
                                ";mFT_BIOS=" + mFT_BIOS +"; mFT_TPVer=" + mFT_TPVer + "; mFT_RCAM=" + mFT_RCAM;
                        Log.i(TAG, "get_facotry_hwinfo(): " + s );
                    }

                }
                catch ( PatternSyntaxException e ) {
                    Log.e(TAG, e.getDescription() );
                    Log.e(TAG, e.getMessage() );
                    Log.e(TAG, e.getPattern() );
                    return -1;
                }




            }
        }
        catch ( FileNotFoundException e ) {
            e.printStackTrace();
            return -1;
        }
        catch ( IOException e ) {
            e.printStackTrace();
            return -1;
        }

        if ( hwinfo == "" )
            return -1;

        return 0;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mCount = 0;
        nRunTime++;

        Log.i(TAG, "----> onCreate() <----,RunTime is " + nRunTime);

        int ret = get_facotry_hwinfo();
        if ( ret < 0 ) {
            Log.e(TAG, "get_facotry_hwinfo() failed!!!");
        }

        if (!InitTestData()) {
            System.exit(-1);
        }

        Log.i(TAG, "----> onCreate(): call InitTestData() OK, testType=" + testType );

        setTitle( getResources().getString(R.string.app_name) + "-" + getResources().getString(R.string.device_name) +
                "-" + getResources().getString(R.string.Version) );
//        String OSversion = SystemProperties.get( "ro.build.display.id", "unknown" );
//        setTitle( getTitle() + "    {OS: " + OSversion + "}" );

        getWindow().addFlags( LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_KEEP_SCREEN_ON );

        mContext = this.getBaseContext();

//        mGPS = SystemProperties.get("sys.gps.enable", "0");
//
//        String ChargeControlEn = SystemProperties.get("ro.ft.chgctrl_en", "1");
//        bChargeContrl = ChargeControlEn.equals("1");

        mMBSN = FileUtility.getSysfsFile( "/sys/class/dmi/id/board_serial" ).trim();
        if ( mMBSN == "" ) {
            mMBSN = "unknown";
        }
        Log.i(TAG, "onCreate(): mMBSN=" + mMBSN );


        mBIOSVer = FileUtility.getSysfsFile( "/sys/class/dmi/id/board_version" ).trim();
        if ( mBIOSVer == "" ) {
            mBIOSVer = "unknown";
        }
        Log.i(TAG, "onCreate(): mBIOSVer=" + mBIOSVer );

        mTestCases = xmldoc.mTestCases;
        myGridView = (MyGridView) findViewById(R.id.myGridView);
        myGridView.setColumnCount(3);

        Log.i(TAG, "----> onCreate(): init MyGridView, testType=" + testType );

        for (TestCase testCase : mTestCases) {
            MyItemView itemView = new MyItemView(this);
            itemView.setText(testCase.getTestName());
            itemView.setTag(testCase.getTestNo());
            itemView.setCheck(testCase.getneedtest());
            if (testCase.isShowResult()) {
                TestCase.RESULT result = testCase.getResult();
                itemView.setResult(result);
            }
            myGridView.addView(itemView);
            Log.i(TAG, "----> onCreate(): init MyGridView, testType=" + testType + " | TestName=" + testCase.getTestName() );
        }

        myGridView.setOnItemClickListener(new MyGridView.OnItemClickListener() {
            public void onItemClick(ViewParent parent, View view, int position) {

                if(((MyItemView)view).setCheckClick()){
                    if(!((MyItemView)view).getischeck()){
                        mTestCases.get(position).setneedtest(false);
                    }else{
                        mTestCases.get(position).setneedtest(true);
                    }
                    Log.v(TAG, ((MyItemView)view).setCheckClick()+"((MyItemView)view).setCheckClick()");
                    return;
                }
                Intent intent = new Intent();
                try {
                    if ( mTestCases.get( position ) != null ) {
                        String className = mTestCases.get( position ).getClassName();
                        String strClsPath;
                        if ( testType == TESTCASE_TYPE_LCD || testType == TESTCASE_TYPE_OTHER || !m_bGridLocked ) {
                            strClsPath = getPackageName() + className;
                            Log.i( TAG, "[hehg]OnItemClickListener TestCase: " + strClsPath );

                            intent.setClass( DeviceTest.this, Class.forName( strClsPath ).newInstance().getClass() );

                            intent.putExtra(EXTRA_TEST_PROGRESS, "0/1");
                            intent.putExtra("testType", testType);
                            intent.putExtra("isGroup", false);
                            isGroup = false;
                            if ( testType == TESTCASE_TYPE_RUNIN ) {
                                if ( className.contains( "StandbyActivity" ) ) {
                                    EditText editStandby = (EditText)findViewById( R.id.runin_standby );
                                    writeFileLine( FILE_STANDBY, editStandby.getText().toString().trim() + ",0", false );
                                    int set = Integer.valueOf( ((EditText)findViewById( R.id.runin_standby )).getText().toString().trim() );
                                    intent.putExtra( StandbyActivity.EXTRA_NAME_STANDBY_SET, set );
                                    intent.putExtra( StandbyActivity.EXTRA_NAME_STANDBY_DONE, 0 );
                                }
                                if ( className.contains( "RebootActivity" ) ) {
                                    EditText editReboot = (EditText)findViewById( R.id.runin_reboot );
                                    writeFileLine( FILE_REBOOT, editReboot.getText().toString().trim() + ",0", false );
                                }
                                if ( className.contains( "VideoPlayActivity" ) ) {
                                    EditText editPlaytime = (EditText)findViewById( R.id.runin_playtime );
                                    writeFileLine( FILE_PLAYTIME, editPlaytime.getText().toString().trim(), false );
                                }
                            }
                            startActivityForResult(intent, position);
                            Log.i(TAG, "satrt " + strClsPath);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        this.mLayoutRuninSetting = (LinearLayout) findViewById( R.id.layout_runin_setting );
        this.mLayoutRuninSetting.setVisibility( View.INVISIBLE );
        mLayoutGridView = (LinearLayout) findViewById( R.id.layout_gridview );
        mLP1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);//important
        mLP1.addRule( RelativeLayout.BELOW, R.id.btnLayout2 );
        mLP1.addRule( RelativeLayout.ABOVE, R.id.btnLayout );
        mLP2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);//important
        mLP2.addRule( RelativeLayout.BELOW, R.id.layout_runin_setting );
        mLP2.addRule( RelativeLayout.ABOVE, R.id.btnLayout );

        mLayoutGridView.setLayoutParams(mLP1);

       /* btn1 = (Button) findViewById(R.id.btn1);
        btn1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if(testType==TESTCASE_TYPE_PCBA) {
                    return;
                }
                testType = TESTCASE_TYPE_PCBA;
                mLayoutRuninSetting.setVisibility( View.INVISIBLE );
                mLayoutGridView.setLayoutParams(mLP1);
                changTestType();

                btn1.setBackgroundColor(Color.BLUE);
                btn2.setBackgroundColor(Color.GRAY);
                btn3.setBackgroundColor(Color.GRAY);
                btn4.setBackgroundColor(Color.GRAY);
                btn5.setBackgroundColor(Color.GRAY);
                btn6.setBackgroundColor(Color.GRAY);
            }
        });
        btn2 = (Button) findViewById(R.id.btn2);
        btn2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if(testType==TESTCASE_TYPE_RUNIN) {
                    return;
                }
// 此处添加WIFI/BT/GPS信号检测PASS的log文件检查，如果存在，则显示Runin测试项，否则提示“未过WIFI/BT/GPS信号测试，请先进行信号测试，谢谢！”�?
// log文件�?mnt/sdcard/SN-SIGNALTEST-PASS.txt
//
*//*				String snNum = SystemProperties.get( "ro.dmi.serialnumber", "unknown" );
				//File file = new File( logPath + snNum + "-SIGNALTEST-PASS.txt");
				File file = new File( logPath + snNum + "-AFTEST-PASS.txt");
				if ( !file.exists() ) {
					//弹出对话框提�?
					//promptSignalTestNotPASS();
					//return;
					promptFTNotPASS();
				}
*//*
                testType = TESTCASE_TYPE_RUNIN;
                mLayoutRuninSetting.setVisibility( View.VISIBLE );
                mLayoutGridView.setLayoutParams(mLP2);
                changTestType();

                btn2.setBackgroundColor(Color.BLUE);
                btn1.setBackgroundColor(Color.GRAY);
                btn3.setBackgroundColor(Color.GRAY);
                btn4.setBackgroundColor(Color.GRAY);
                btn5.setBackgroundColor(Color.GRAY);
                btn6.setBackgroundColor(Color.GRAY);
            }
        });
       *//* btn3 = (Button) findViewById(R.id.btn3);
        btn3.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if(testType==TESTCASE_TYPE_FT) {
                    return;
                }
// 此处添加Runin PASS log文件的检查，如果存在，则显示FT测试项，否则提示“未过老化测试，请先Runin！”�?
// 由于MES系统会在FT测试前将此log文件拷走并删除，所以换成如下文件，在操作员点击“成功”界面上的确定按钮时生成，内容为Runin的所有测试项名称�?PASS>
// /data/data/com.byd8.test/SN-ARUNIN-PASS.txt
*//**//*				String snNum = SystemProperties.get( "ro.dmi.serialnumber", "unknown" );
				//File file = new File( logPath + snNum + "-ARUNIN-PASS.txt");
				//File file = new File( DeviceTest.DATA_PATH + snNum + "-ARUNIN-PASS.txt");
				File file = new File( logPath + snNum + "-SIGNALTEST-PASS.txt");
				if ( !file.exists() ) {
					//弹出对话框提�?
					//promptRuninNotPASS();
					//return;
					promptSignalTestNotPASS();
				}

		//		File f = new File( logPath + snNum + "-SIGNALTEST-PASS.txt");
		//		if ( f.exists() ) {
		//			f.delete();
		//		}
*//**//*
                testType = TESTCASE_TYPE_FT;
                mLayoutRuninSetting.setVisibility( View.INVISIBLE );
                mLayoutGridView.setLayoutParams(mLP1);
                changTestType();

                btn3.setBackgroundColor(Color.BLUE);
                btn2.setBackgroundColor(Color.GRAY);
                btn1.setBackgroundColor(Color.GRAY);
                btn4.setBackgroundColor(Color.GRAY);
                btn5.setBackgroundColor(Color.GRAY);
                btn6.setBackgroundColor(Color.GRAY);
            }
        });
       *//*

        btn1.setBackgroundColor(Color.BLUE);
        btn2.setBackgroundColor(Color.GRAY);
        btn3.setBackgroundColor(Color.GRAY);
        btn4.setBackgroundColor(Color.GRAY);
        btn5.setBackgroundColor(Color.GRAY);
        btn6.setBackgroundColor(Color.GRAY);
        mButtonCancel = (Button) findViewById(R.id.btn_cancel);
        mButtonCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                finish();
            }

        });*/
        mTestChecked = (Button) findViewById( R.id.btn_testall );
        mTestChecked.setOnClickListener( new OnClickListener() {
            public void onClick( View v ) {
                String strTmp = null;

//                String snNum = SystemProperties.get( "ro.dmi.serialnumber", "unknown" );

                switch ( testType ) {
                    case TESTCASE_TYPE_PCBA:
                        strTmp = "PCBATS";
                        testGroup( "TESTGROUP" );
                        break;
                    case TESTCASE_TYPE_RUNIN:
                        // 此处添加Runin PASS log文件的检查，如果存在，则显示FT测试项，否则提示“未过老化测试，请先Runin！”�?
                        // 由于MES系统会在FT测试前将此log文件拷走并删除，所以换成如下文件，在操作员点击“成功”界面上的确定按钮时生成，内容为Runin的所有测试项名称�?PASS>
//                        File fileFT = new File( logPath + snNum + "-AFTEST-PASS.txt");
                        testGroup( "TESTGROUP" );

                        strTmp = "ARUNIN";

                        // get the standby times, reboot times, video play time from TextEdit.
                        EditText editStandby = (EditText)findViewById( R.id.runin_standby );
                        EditText editReboot = (EditText)findViewById( R.id.runin_reboot );
                        EditText editPlaytime = (EditText)findViewById( R.id.runin_playtime );

                        writeFileLine( FILE_STANDBY, editStandby.getText().toString().trim() + ",0", false );
                        writeFileLine( FILE_REBOOT, editReboot.getText().toString().trim() + ",0", false );
                        writeFileLine( FILE_PLAYTIME, editPlaytime.getText().toString().trim(), false );

                        break;
                    case TESTCASE_TYPE_FT:
                    case TESTCASE_TYPE_AUTO:
                        // 此处添加WIFI/BT/GPS信号检测PASS的log文件检查，如果存在，则显示Runin测试项，否则提示“未过WIFI/BT/GPS信号测试，请先进行信号测试，谢谢！”�?
                        // log文件�?mnt/sdcard/SN-SIGNALTEST-PASS.txt
                        testGroup( "TESTGROUP" );
                        strTmp = "AFTEST";
                        break;
                    default:
                        break;
                }
                if ( strTmp != null ) {
                    logfilecheck("/sdcard/", strTmp);
                }
            }
        });

        Button clearButton = (Button) findViewById(R.id.btn_clear);
        clearButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_CLEAR_ID);
            }
        });


        Log.i(TAG, "----> onCreate(): check reboot done file: " + FILE_REBOOT_DONE + "; testType=" + testType );

        File f = new File( FILE_REBOOT_DONE );
        if ( f.exists() )
        {
            String str = DeviceTest.readFileLine( FILE_REBOOT_DONE );
            Log.i(TAG, "read file (" + FILE_REBOOT_DONE + "): " + str );

            f.delete();

            String [] s = str.split(",");
            int mRebootSet = Integer.valueOf( s[0] );
            int mRebootDone = Integer.valueOf( s[1] );
            Log.i(TAG, "mRebootSet: " + mRebootSet + "; mRebootDone: " + mRebootDone);
            int reboot = mRebootDone;

            if ( reboot > 0 )
            {
                if(testType==TESTCASE_TYPE_RUNIN)
                {
                    return;
                }
                testType = TESTCASE_TYPE_RUNIN;
                mLayoutRuninSetting.setVisibility( View.INVISIBLE );
                mLayoutGridView.setLayoutParams(mLP1);
                changTestType();

                /*btn2.setBackgroundColor(Color.BLUE);
                btn1.setBackgroundColor(Color.GRAY);
                btn3.setBackgroundColor(Color.GRAY);
                btn4.setBackgroundColor(Color.GRAY);*/

                ///////////////////////////////////////////////////////////////////////////////
                int reboot_pos = 0;

                File ff = new File( "/sdcard/group-runin" );
                for (TestCase testCase : mTestCases)
                {

                    MyItemView myItemView = (MyItemView) myGridView.getChildAt(reboot_pos);
                    if (ff.exists())
                    {
                        myItemView.setResult(TestCase.RESULT.OK);
                        mTestCases.get(reboot_pos).setShowResult(true);
                        Log.i(TAG, "reboot -----> reboot_pos:" + reboot_pos + " ClassName: " + testCase.getClassName() );
                    }
                    reboot_pos ++;

                    if ( testCase.getClassName().contains( "RebootActivity" ) )
                    {
                        break;
                    }
                }
                writeLog(true, reboot_pos-1); // 将Reboot测试结果附加到log文件�?
                //hehg
                Log.i(TAG, "[HEHG] RUNIN return 'reboot' is " + reboot + "; reboot_pos: " + reboot_pos );
                if ( ff.exists() )
                {
                    ff.delete();
                    Log.i(TAG, "======= found /sdcard/group-runin , so set isGroup=true =====");
/****************************************Add by lsg***********************************/
                    Intent i = new Intent();
                    i.setClass(DeviceTest.this, VideoPlayActivity.class );
                    i.putExtra("testType", testType);
                    i.putExtra("isGroup", isGroup);
                    startActivityForResult(i, reboot_pos + DEVICE_TEST_MAX_NUM);
/****************************************Add by lsg***********************************/
                }
            }

        }
        // 程序启动时，关闭WIFI、BT，为WIFI/BT信号测试做准备�?
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
        BluetoothAdapter.getDefaultAdapter().disable();
        Log.i(TAG, "----> Disabled WIFI & BT <----");

        // 程序启动时，关闭音效增强（SRS�?
    }

    private void promptFTNotPASS() {
        AlertDialog dlg;
        //AlertDialog.Builder bd = new AlertDialog.Builder(mContext); // Error: add view
        Builder bd = new Builder(this);
        bd.setMessage("未过 FT 测试，请先进�?FT 测试，谢谢！");
        bd.setTitle("提示信息");
        bd.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "未过FT测试，请先进�?FT 测试，谢谢！" );
                testGroup( "TESTGROUP" );
            }
        });
        dlg = bd.create();
        dlg.setCancelable(false);
        dlg.show();
    }

    private void promptSignalTestNotPASS() {
        AlertDialog dlg;
        //AlertDialog.Builder bd = new AlertDialog.Builder(mContext); // Error: add view
        Builder bd = new Builder(this);
        bd.setMessage("未过WIFI/BT/GPS信号测试，请先进行信号测试，谢谢�?");
        bd.setTitle("提示信息");
        bd.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "未过WIFI/BT/GPS信号测试，请先进行信号测试，谢谢�?" );
                testGroup( "TESTGROUP" );
            }
        });
        dlg = bd.create();
        dlg.setCancelable(false);
        dlg.show();
    }

    private void promptRuninNotPASS() {
        AlertDialog dlg;
        //AlertDialog.Builder bd = new AlertDialog.Builder(mContext); // Error: add view
        Builder bd = new Builder(this);
        bd.setMessage("未过老化测试，请先Runin！谢谢！");
        bd.setTitle("提示信息");
        bd.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "未过老化测试，请先Runin！谢谢！" );
                testGroup( "TESTGROUP" );
            }
        });
        dlg = bd.create();
        dlg.setCancelable(false);
        dlg.show();
    }

    private void createAssetFile(String name, String destPath) {

        InputStream is = null;
        OutputStream os = null;
        try {
            is = getAssets().open(name);
            os = new FileOutputStream(destPath);
            int data = 0;
            while (true) {
                data = is.read();
                if (data < 0) {
                    break;
                }
                os.write(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                }
            }
            SystemUtil.execRootCmd("chmod 777 " + destPath);
        }
    }

    public final static String MEMTESTER_PATH = DeviceTest.DATA_PATH
            + "memtester";
    public final static String GPS_COLD_START_PATH = DeviceTest.DATA_PATH
            + "gps_coldstart";
    static final int DIALOG_CLEAR_ID = 10;

    protected Dialog onCreateDialog(int id) {
        Builder builder = new Builder(this);
        builder.setMessage("Clear all test status?")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                for (int i = 0; i < myGridView.getChildCount(); i++) {
                                    MyItemView myItemView = (MyItemView) myGridView
                                            .getChildAt(i);
                                    myItemView.setResult(TestCase.RESULT.UNDEF);
                                    mTestCases.get(i).setShowResult(false);
                                }
//								try {
//									save(SAVE_FILE_PATH);
//								} catch (IOException e) {
//									e.printStackTrace();
//								}
                            }
                        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        return alert;
    }

    private void loadData() throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
                SAVE_DATA_PATH));
        List<TestCase> savedData = (List<TestCase>) ois.readObject();
        for (TestCase savedCase : savedData) {
            for (TestCase testCase : mTestCases) {
                if (testCase.getClassName().equals(savedCase.getClassName())) {
                    testCase.setResult(savedCase.getResult());
                    testCase.setDetail(savedCase.getDetail());
                    testCase.setShowResult(savedCase.isShowResult());
                }
            }
        }
        ois.close();
    }

    public static String formatResult(String testName, TestCase.RESULT result,
                                      String detail) {
        if (detail == null) {
            return "[" + testName + "]\n" + result.name();
        }
        if (detail.startsWith(RESULT_INFO_HEAD_JUST_INFO)) {
            return detail.substring(RESULT_INFO_HEAD_JUST_INFO.length());
        }
        return "[" + testName + "]\n" + result.name() + detail;
    }

    synchronized private void save(String saveFilePath) throws IOException {
        FileWriter fw;
        String tempSavePath = DATA_PATH + "save";
        fw = new FileWriter(tempSavePath);
        for (TestCase testCase : mTestCases) {
            if (testCase.getClassName().equals(
                    RuninTestActivity.class.getSimpleName())) {
                if (testCase.getDetail() == null) {
                    testCase.setDetail(new RuninTestActivity().getResult());
                }
            } else if(testCase.getClassName().equals(GpsTestActivity.class.getSimpleName())) {
                if (testCase.getDetail() == null) {
                    testCase.setDetail(new GpsTestActivity().getResult());
                }
            }
            fw.write(formatResult(testCase.getTestName(), testCase.getResult(),
                    testCase.getDetail()) + "\n");
        }
        fw.close();

        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
                SAVE_DATA_PATH));
        oos.writeObject(mTestCases);
        oos.close();
    }

    protected void testGroup(String selectGroup) {
        Intent intent = new Intent();
        try {
            String className = mTestCases.get(0).getClassName();
            String strClsPath;
            switch ( testType ) {
                case TESTCASE_TYPE_PCBA:
                    strClsPath = "com.byd8.test" + className;
                    break;
                case TESTCASE_TYPE_FT:
                    strClsPath = "com.byd8.test" + className;
                    break;
                case TESTCASE_TYPE_RUNIN:
                    strClsPath = "com.byd8.test" + className;

                    try {
                        FileWriter wr = new FileWriter( "/sdcard/group-runin", false );
                        wr.write( strClsPath );
                        wr.close();
                        Log.i( TAG, "[wr] \"/sdcard/group-runin\" " + strClsPath );
                    }
                    catch ( IOException e ) {
                        e.printStackTrace();
                    }

                    break;
                case TESTCASE_TYPE_LCD:
                default:
                    strClsPath = "com.byd8.test" + className;
                    break;
            }
            Log.i( TAG, "[hehg]testGroup TestCase: " + strClsPath );

            intent.setClass( DeviceTest.this, Class.forName( strClsPath ).newInstance().getClass() );

            intent.putExtra("testType", testType);
            intent.putExtra("isGroup", true);
            isGroup = true;
            startActivityForResult(intent, DEVICE_TEST_MAX_NUM);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private void uninstallPackage(String packageName) {
        String cmd = "mount -o remount,rw /system /system\n"
                + "rm -r /data/data/*DeviceTest*\n"
                + "rm /data/app/*DeviceTest*\n"
                + "rm /system/app/*DeviceTest*\n";
        SystemUtil.execScriptCmd(cmd, TEMP_FILE_PATH, true);

    }

    private void writeLog(boolean result, int pos) {
        Log.i(TAG, "writeLog(): result=" + result + "; pos=" + pos + "; testType=" + testType );

//        String snNum = SystemProperties.get( "ro.dmi.serialnumber", "unknown" );
//        if (testType == TESTCASE_TYPE_PCBA) {
//            logFileFail = new File(logPath + mMBSN + "-MBFT-FAIL.txt"); //PCBATS
//            logFilePass = new File(logPath + mMBSN + "-MBFT-PASS.txt");
//        } else if(testType == TESTCASE_TYPE_RUNIN) {
//            logFileFail = new File(logPath + snNum + "-RUNIN-FAIL.txt"); //ARUNIN
//            logFilePass = new File(logPath + snNum + "-RUNIN-PASS.txt");
//        } else if(testType == TESTCASE_TYPE_FT) {
//            logFileFail = new File(logPath + snNum + "-SYSFT-FAIL.txt"); //AFTEST
//            logFilePass = new File(logPath + snNum + "-SYSFT-PASS.txt");
//        } else if(testType == TESTCASE_TYPE_AUTO) {
//            logFileFail = new File(logPath + snNum + "-Touchtest-FAIL.txt");
//            logFilePass = new File(logPath + snNum + "-Touchtest-PASS.txt");
//        } else {
//            return;
//        }

        String item = mTestCases.get(pos).getTestName();
        try {
            if (logFilePass.exists()) {
                if (pos == 0) {
                    logFileFail.delete();
                    logFileFail.createNewFile();
                }

                if (pos == 0) {
                    logFilePass.delete();
                    logFileFail.createNewFile();
                }
            }
            if(logFileFail.exists()) {
                if(pos == 0) {
                    logFileFail.delete();
                    logFileFail.createNewFile();
                }

            } else if(logFilePass.exists()) {
                if(pos == 0) {
                    logFilePass.delete();
                    logFileFail.createNewFile();
                }
            }

            FileWriter writer = new FileWriter(logFileFail, true);
            if (testType == TESTCASE_TYPE_PCBA && pos == 0) {
//                writer.write( "<" + InfoTest_SystemActivity.getHardwareInfo() + ">\n" );
            }

            if ( mTestCases.get(pos).getClassName().contains( "GpsActivity" ) ) {
                writer.write( "<" + item + ">" + mGpsTTFF + (result?"<PASS>\n":"<FAIL>\n") );
                Log.i( TAG, "writeLog: <" + item + ">" + mGpsTTFF + (result?"<PASS>\n":"<FAIL>\n") );
            }
            else if ( mTestCases.get(pos).getClassName().contains("VideoPlayActivity") ) {
                writer.write( "<" + item + ">" + "charge60:" + mCharge85 + ",Capacity_after:" + mCapacityAfter + "%" + (result?"<PASS>\n":"<FAIL>\n") );
                Log.i( TAG, "writeLog: <" + item + ">" + "charge60:" + mCharge85 + ",Capacity_after:" + mCapacityAfter + "%" + (result?"<PASS>\n":"<FAIL>\n") );
            }
            else {
                writer.write( "<" + item + ">" + (result?"<PASS>\n":"<FAIL>\n") );
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int mGpsTTFF = 0;
    public String mCharge85 = "no";
    public int mCapacityAfter = 0;

    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG,  "onActivityResult: requestCode=" + requestCode + "; resultCode=" + resultCode );

        //if ( mCurrentCaseGroup != null && (requestCode - DEVICE_TEST_MAX_NUM) >= mCurrentCaseGroup.size() )
        if ( mTestCases != null && (requestCode - DEVICE_TEST_MAX_NUM) >= mTestCases.size() )
            return;
        if ( data != null && data.hasExtra( "testitem" ) )
        {
            //resultIntent.putExtra( "testitem", "videoplay" );
            String testitem = "";
            testitem = data.getStringExtra( "testitem" );
            Log.i(TAG,  "------> onActivityResult(): testitem=" + testitem );
            if ( !testitem.isEmpty() && testitem.equals( "videoplay" ) )
            {

                Log.i(TAG,  "------> onActivityResult(): testType=" + testType );
                //testType = getIntent().getIntExtra( "testType", DeviceTest.TESTCASE_TYPE_PCBA );
                int AtestType = data.getIntExtra( "testType", DeviceTest.TESTCASE_TYPE_RUNIN );
                Log.i(TAG,  "------> onActivityResult() get from intent: AtestType=" + AtestType );
                if ( AtestType != testType )
                {
                    testType = AtestType;
                    changTestType();
                }

            }
        }
        int pos = requestCode;
        boolean ignore = (resultCode == TestCase.RESULT.UNDEF.ordinal());

        if ( requestCode >= DEVICE_TEST_MAX_NUM )
        {
            // test auto judged.
            //	TestCase tmpTestCase = mCurrentCaseGroup.get(requestCode - DEVICE_TEST_MAX_NUM);
            TestCase tmpTestCase = mTestCases.get(requestCode - DEVICE_TEST_MAX_NUM);
            if(tmpTestCase == null){
                Log.d(TAG, " _________________ tmpTestCase == null~~~~!!!!!");
            }
            pos = tmpTestCase.getTestNo();
        }

        if ( !ignore && pos < mTestCases.size() ) {
            MyItemView itemView = (MyItemView) myGridView.getChildAt(pos);
            TestCase.RESULT result = TestCase.RESULT.values()[ resultCode ];
            itemView.setResult( result );
            mTestCases.get( pos ).setResult(result);
            mTestCases.get( pos ).setShowResult( true );

            if ( data != null && mTestCases.get(pos).getClassName().contains( "GpsActivity" ) ) {
                mGpsTTFF = data.getIntExtra( "gps_ttff", 0 );
                Log.i(TAG, "GPS TTFF: " + mGpsTTFF );
            }
            if (data != null && mTestCases.get(pos).getClassName().contains( "VideoPlayActivity" ) ) {
                mCharge85 = data.getStringExtra( "charge85" );
                Log.i(TAG, "VideoPlayActivity return data: mCharge85 = " + mCharge85 );
                mCapacityAfter = data.getIntExtra( "Capacity_after", 0 );
                Log.i(TAG, "VideoPlayActivity return data: Capacity_after = " + mCapacityAfter );
            }

            // if fail then exit group test.
            if ( isGroup ) {
                if ( result == TestCase.RESULT.NG ) {
                    if ( testType == TESTCASE_TYPE_RUNIN ) {
                        mLayoutRuninSetting.setVisibility( View.VISIBLE );
                        mLayoutGridView.setLayoutParams(mLP2);
                        showRuninResult( false );

                        delFile( FILE_STANDBY );
                        delFile( FILE_REBOOT );
                        delFile( FILE_PLAYTIME );
                        delFile( "/sdcard/group-runin" );

                    }
                    if ( testType == TESTCASE_TYPE_LCD ) { // LCD模组检测FAIL，则显示“FAIL”条�?
                        showBarcodeResult( 0 ); // FAIL
                        bLCDEnd = true;
                    }
                    if ( testType == TESTCASE_TYPE_RUNIN || testType == TESTCASE_TYPE_FT || testType == TESTCASE_TYPE_AUTO ) {
                        writeLog( false, pos );
                    }
                    if ( testType == TESTCASE_TYPE_PCBA ) {
                        writeLog( false, pos );

                    }
                    if ( testType == TESTCASE_TYPE_RUNIN && requestCode >= DEVICE_TEST_MAX_NUM ) {
                        alertDialog(pos);
                    }

                    if ( isNeed( pos, requestCode ) ) {
                        alertDialog(pos);
                    }
                    Log.d(TAG, mTestCases.get(pos).getTestName()+":fail");
                    return;
                }
                else if(result == TestCase.RESULT.OK) {
                    mCount = 0;
                    if (  testType == TESTCASE_TYPE_PCBA ||
                            testType == TESTCASE_TYPE_RUNIN ||
                            testType == TESTCASE_TYPE_AUTO ||
                            testType == TESTCASE_TYPE_FT ) {
                        writeLog(true, pos);
                    }
                    Log.d(TAG, mTestCases.get(pos).getTestName() + ":pass");
                    //if (pos + 1 == mCurrentCaseGroup.size()) {
                    if (pos + 1 == mTestCases.size()) { // 测试全部PASS，更改log文件名称
                        if ( testType == TESTCASE_TYPE_PCBA ||
                                testType == TESTCASE_TYPE_RUNIN ||
                                testType == TESTCASE_TYPE_AUTO ||
                                testType == TESTCASE_TYPE_FT ) {
                            logFileFail.renameTo(logFilePass);
                            Log.i(TAG, "onActivityResult(): pos=" + pos + "; logFileFail.renameTo(logFilePass);" );
                        }

// Runin测试PASS后，生成一�?data/data/com.byd8.test/SN-ARUNIN-PASS.txt用于FT测试时检�?
                        // /data/data/com.byd8.test/SN-ARUNIN-PASS.txt
                        if ( testType == TESTCASE_TYPE_RUNIN ) {/*
                            String snNum = SystemProperties.get( "ro.dmi.serialnumber", "unknown" );
                            File f = new File( DeviceTest.DATA_PATH + snNum + "-ARUNIN-PASS.txt" );
                            if ( f.exists() ) {
                                f.delete();
                                try {
                                    f.createNewFile();
                                }
                                catch ( IOException e ) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                FileWriter fw = new FileWriter(f, false);
                                for (TestCase testCase : mTestCases) {
                                    String name = testCase.getTestName();
                                    if ( name.equals("Media Play") ) {
                                        fw.write( "<" + name + ">charge85:yes<PASS>\r\n" );
                                    }
                                    else {
                                        fw.write( "<" + name + "><PASS>\r\n" );
                                    }
                                }
                                fw.close();
                            }
                            catch ( IOException e ) {
                                e.printStackTrace();
                            }
*/
                        }

                        if ( testType == TESTCASE_TYPE_LCD ) { // LCD模组检测全部PASS，则显示“PASS”条�?
                            Log.i( TAG, "[LCDModuleTest] pos=" + pos + "; mTestCases.size()=" + mTestCases.size() );
                            showBarcodeResult( 1 ); // PASS
                            bLCDEnd = true;
                        }

                        if( testType == TESTCASE_TYPE_RUNIN ) {
                            mLayoutRuninSetting.setVisibility( View.VISIBLE );
                            mLayoutGridView.setLayoutParams(mLP2);
                        }
                    }
                }
            }

        }

        if (!ignore && requestCode >= DEVICE_TEST_MAX_NUM) {
            pos = requestCode - DEVICE_TEST_MAX_NUM;// 0
            Log.i(TAG, "onActivityResult() pos=" + pos + "; requestCode=" + requestCode );
            pos++;// 1

            Intent intent = new Intent();
            if (pos < mTestCases.size()) {
                while (!mTestCases.get(pos).getneedtest()) {
                    pos++;
                    if (pos >= mTestCases.size()) {
                        return;
                    }
                }
                try { // hehg
                    String className = mTestCases.get( pos ).getClassName();
                    String strClsPath;
                    switch ( testType ) {
                        case TESTCASE_TYPE_PCBA:
                            strClsPath = "com.byd8.test" + className;
                            break;
                        case TESTCASE_TYPE_FT:
                            strClsPath = "com.byd8.test" + className;
                            break;

                        case TESTCASE_TYPE_RUNIN:
                            strClsPath = "com.byd8.test" + className;
                            break;
                        case TESTCASE_TYPE_LCD:
                        default:
                            strClsPath = "com.byd8.test" + className;
                            break;
                    }
                    Log.i( TAG, "[hehg]onActivityResult TestCase: " + strClsPath );

                    intent.setClass(DeviceTest.this, Class.forName(strClsPath).newInstance().getClass());

                    intent.putExtra(EXTRA_TEST_PROGRESS, pos + "/"
                            + mTestCases.size());
                    intent.putExtra("testType", testType);
                    intent.putExtra("isGroup", true);
                    isGroup = true;
                    // we use nagtiv value to keep the sequence number when
                    // do a all test.
                    startActivityForResult(intent, pos + DEVICE_TEST_MAX_NUM);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    private boolean InitTestData(InputStream is) {
        if (is == null) {
            return false;
        }
        try {
            xmldoc = new XmlDeal(is);
        } catch (Exception e) {
            Log.e(TAG, "parse the xmlfile is fail");
            return false;
        }
        return true;

    }

    private boolean InitTestData() {
        InputStream is = null;
        try {
            File configFile1 = new File(EXTRA_CONFIG_FILE_NAME1);
            File configFile2 = new File(EXTRA_CONFIG_FILE_NAME2);
            File configFile3 = new File(EXTRA_CONFIG_FILE_NAME3);
            File configFile4 = new File(EXTRA_CONFIG_FILE_NAME4);

            if (configFile1.exists()) {
                Log.i(TAG, "Use extra config file:"
                        + EXTRA_CONFIG_FILE_NAME1);
                if (InitTestData(new FileInputStream(configFile1))) {
                    return true;
                }
            }
            if (configFile2.exists()) {
                Log.i(TAG, "Use extra config file:"
                        + EXTRA_CONFIG_FILE_NAME2);
                if (InitTestData(new FileInputStream(configFile2))) {
                    return true;
                }
            }
            if (configFile3.exists()) {
                Log.i(TAG, "Use extra config file:"
                        + EXTRA_CONFIG_FILE_NAME3);
                if (InitTestData(new FileInputStream(configFile3))) {
                    return true;
                }
            }
            if (configFile4.exists()) {
                Log.i(TAG, "Use extra config file:"
                        + EXTRA_CONFIG_FILE_NAME4);
                if (InitTestData(new FileInputStream(configFile4))) {
                    return true;
                }
            }
            switch(testType) {
                case TESTCASE_TYPE_PCBA:
                    is = getAssets().open(CONFIG_FILE_NAME1);
                    Log.i(TAG, "[hehg]InitTestData(): getAssets().open(" + CONFIG_FILE_NAME1 + ")");
                    break;
                case TESTCASE_TYPE_RUNIN:
                    is = getAssets().open(CONFIG_FILE_NAME2);
                    Log.i(TAG, "[hehg]InitTestData(): getAssets().open(" + CONFIG_FILE_NAME2 + ")");
                    break;
                case TESTCASE_TYPE_FT:
                    is = getAssets().open(CONFIG_FILE_NAME3);
                    Log.i(TAG, "[hehg]InitTestData(): getAssets().open(" + CONFIG_FILE_NAME3 + ")");
                    break;
                case TESTCASE_TYPE_LCD:
                    is = getAssets().open(CONFIG_FILE_NAME4);
                    Log.i(TAG, "[hehg]InitTestData(): getAssets().open(" + CONFIG_FILE_NAME4 + ")");
                    break;
                case TESTCASE_TYPE_OTHER:
                    is = getAssets().open(CONFIG_FILE_NAME5);
                    Log.i(TAG, "[hehg]InitTestData(): getAssets().open(" + CONFIG_FILE_NAME5 + ")");
                    break;
                case TESTCASE_TYPE_AUTO:
                    is = getAssets().open(CONFIG_FILE_NAME6);
                    Log.i(TAG, "[hehg]InitTestData(): getAssets().open(" + CONFIG_FILE_NAME6 + ")");
                    break;
                default:
                    break;
            }

            try {
                xmldoc = new XmlDeal(is);
            } catch (Exception e) {
                Log.e(TAG, "parse the xmlfile is fail");
                return false;
            }
        } catch (IOException e) {

            e.printStackTrace();
            Log.e(TAG, "read the xmlfile is fail" + e.getMessage());
            // ForwardErrorActive();
            return false;
        }

        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;

    }

    private void changTestType() {
        myGridView.removeAllViews();
        if (!InitTestData()) {
            System.exit(-1);
        }
        mTestCases = xmldoc.mTestCases;
        Log.e(TAG, mTestCases.size() + "");
        int i = 0;
        for (TestCase testCase : mTestCases) {

            MyItemView itemView = new MyItemView(this);
            itemView.setText(testCase.getTestName());
            itemView.setTag(testCase.getTestNo());
            itemView.setCheck(testCase.getneedtest());
            if (testCase.isShowResult()) {
                TestCase.RESULT result = testCase.getResult();
                itemView.setResult(result);
            }
            myGridView.addView(itemView);
            i++;
        }
        Log.i(TAG, i + "");
    }

    public void showBarCode(int pos) {
        View vi = View.inflate(this, R.layout.barcode, null);
        ImageView imageview = (ImageView) vi.findViewById(R.id.imagebarcode);
        imageview.setImageResource(pcba_bar_code[pos]);
        imageview.setVisibility(View.VISIBLE);
        Builder b = new Builder(this);
        b.setCancelable(true);
        b.setTitle("hellalertdialog");
        b.setView(vi);
        b.setPositiveButton(android.R.string.ok, null);
        AlertDialog d = b.create();
        d.show();
    }

    public void showRuninResult( boolean result ) {
        View vi = View.inflate(this, R.layout.show_result, null);
        TextView text = (TextView) vi.findViewById( R.id.result );
        text.setText( (result ? "成功" : "失败") );
        text.setTextColor( (result ? Color.GREEN : Color.RED) );

        text.setVisibility(View.VISIBLE);
        Builder b = new Builder(this);
        b.setCancelable(true);
        b.setTitle("RUNIN测试结果显示");
        b.setView(vi);
        b.setPositiveButton(android.R.string.ok, null);
        AlertDialog d = b.create();
        d.show();
    }

    private void dialog_pcbatest() {
        AlertDialog dialog;
        Builder builder = new Builder(this);
        builder.setMessage("       RunIn test hasn't pass yet        ");
        builder.setTitle("提示");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                Log.e("steven", "check the pcba test");
            }
        });

        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

    }

    boolean isNeed(int pos, int requestCode)
    {
        if(requestCode < DEVICE_TEST_MAX_NUM || (mCount%3) >= 2) return false;
        if (testType == 1)
        {

            return true;

        }
        else if (testType == 3)
        {
            return true;

        }
        return false;

    }

    void alertAialog(final int pos)
    {
        AlertDialog dialog;
        Builder builder = new Builder(this);
        builder.setMessage("重新再测吗，是�?yes 不是 �?no");


        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                try {
                    Intent intent = new Intent();
                    String strClsPath = "com.byd8.test"
                            + mTestCases.get(pos).getClassName();
                    intent.setClass(DeviceTest.this, Class.forName(strClsPath)
                            .newInstance().getClass());

                    intent.putExtra(EXTRA_TEST_PROGRESS, pos + "/"
                            + mTestCases.size());
                    intent.putExtra("testType", testType);
                    intent.putExtra("isGroup", true);
                    isGroup = true;

                    // we use nagtiv value to keep the sequence number when
                    // do a all test.
                    startActivityForResult(intent, pos + DEVICE_TEST_MAX_NUM);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
                mCount++;

            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mCount = 0;



            }
        });
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
        Log.d("xxxxxxxxxxxxx", "jfljdsklajfkldjaskljfdklajlfjdlasjdfkls");

    }

    public void alertDialog( int pos )
    {
        AlertDialog dlg;
        Builder builder = new Builder( this );

        builder.setMessage( "\"" + mTestCases.get(pos).getTestName() + "\" 测试失败!!!" );
        builder.setTitle( "ERROR" );
        builder.setPositiveButton( getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dlg, int which) {
                // TODO Auto-generated method stub
                dlg.dismiss();
            }
        });

        dlg = builder.create();
        dlg.setCancelable( false );
        dlg.show();
    }

    class CMDExecute3 {

        public synchronized boolean run(String [] cmd, String workdirectory) throws IOException {
            try {
                ProcessBuilder builder = new ProcessBuilder(cmd);

                if (workdirectory != null) {
                    builder.directory(new File(workdirectory));
                    builder.redirectErrorStream(true);
                    Process process = builder.start();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Log.d("ddddddddddddd", "jfljdsklajfkldjaskljfdklajlfjdlasjdfkls");
            return true;
        }
    }
    //11
    private void logfilecheck (String path,String logflag)
    {
        File file=new File(path);
        String test[];
        test=file.list();
        for(int i=0;i<test.length;i++)
        {
            if(test[i].indexOf(logflag)!=-1)
            {
                Log.d("ddddddddddddd", "222222222222222"+test[i]);
                logpcba = new File(path+test[i]);
                logpcba.delete();
            }
        }
    }

    // hehg -- show barcode for LCD Module Test.
    //private int lcd_barcode[] = { R.drawable.barcode_fail_237x147, R.drawable.barcode_pass_237x147 };
    private int lcd_barcode[] = { R.drawable.barcode_fail, R.drawable.barcode_pass };
    private AlertDialog mDlgLCDResult = null;
    public void showBarcodeResult( int barcode ) {
        View v = View.inflate( this, R.layout.lcdtest_barcode, null);
        ImageView imageview = (ImageView)v.findViewById(R.id.imagebarcode);
        imageview.setImageResource( lcd_barcode[barcode] );
        imageview.setVisibility( View.VISIBLE );

        Builder b = new Builder(this);
        b.setCancelable( true );
        b.setTitle( "LCD模组检测结果显�?" );
        b.setView( v );
        b.setPositiveButton( android.R.string.ok, null );

        mDlgLCDResult = b.create();
        mDlgLCDResult.show();
    }

    private boolean enableitemclick = true;

    private boolean bPause = false;
    private boolean bLCDEnd = false;
    private boolean bStandby = false;

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "onResume() testType=" + testType);
        if(testType==TESTCASE_TYPE_LCD && bPause) {
            if ( mDlgLCDResult != null ) {
                mDlgLCDResult.cancel();
                mDlgLCDResult = null;
            }
            Log.i(TAG, "onResume() TESTCASE_TYPE_LCD testType=" + testType);
            for (int i = 0; i < myGridView.getChildCount(); i++) {
                MyItemView myItemView = (MyItemView) myGridView.getChildAt(i);
                myItemView.setResult(TestCase.RESULT.UNDEF);
                mTestCases.get(i).setShowResult(false);
            }
            bStandby = false;
            bLCDEnd = false;
            bPause = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(TAG, "onPause() testType=" + testType);
        if(testType==TESTCASE_TYPE_LCD && bLCDEnd) {
            bPause = true;
        }

    }

    public boolean dispatchKeyEvent(KeyEvent event) {

        if ( event.getKeyCode() == KeyEvent.KEYCODE_POWER ) {
            Log.i(TAG, "dispatchKeyEvent() KeyEvent.KEYCODE_POWER: bStandby=" + bStandby);
            bStandby = !bStandby;
        }
        return super.dispatchKeyEvent(event);
    }

    public static String getSysfsFile( String filename ) {
        String result = "";
        try {
            FileReader fr = new FileReader( filename );
            BufferedReader br = new BufferedReader( fr );
            result = br.readLine();
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

    public static String readFileLine( String fname ) {
        String ret = null;
        if ( fname == null ) {
            return ret;
        }
        try {
            FileReader fr = new FileReader( fname );
            BufferedReader br = new BufferedReader( fr );
            ret = br.readLine();
            br.close();
        }
        catch ( FileNotFoundException e ) {
            e.printStackTrace();
            return ret;
        }
        catch ( IOException e ) {
            e.printStackTrace();
            return ret;
        }
        return ret;
    }

    public int delFile( String fname ) {
        File f = new File( fname );
        if ( f.exists() ) {
            f.delete();
        }

        return 0;
    }

    public static int writeFileLine( String fname, String line, boolean append ) {
        if ( fname == null || line == null ) {
            return -1;
        }
        try {
            File f = new File( fname );
            FileWriter wr = new FileWriter( f, append );
            wr.write(line);
            wr.close();
        }
        catch ( IOException e ) {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

}

