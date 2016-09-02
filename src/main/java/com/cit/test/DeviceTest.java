package com.cit.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
//import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

//import com.byd8.test.helper.SystemUtil;
//import com.byd8.test.helper.TestCase;
//import com.byd8.test.helper.TestCase.RESULT;
//import com.byd8.test.helper.XmlDeal;
//import com.byd8.test.pcba.InfoTest_SystemActivity;
//import com.byd8.test.runin.StandbyActivity;
//import com.byd8.test.runin.VideoPlayActivity;
//import com.byd8.test.utility.FileUtility;
//import com.byd8.test.view.MyGridView;
//import com.byd8.test.view.MyItemView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
//import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
//import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import com.cit.test.socket.AndroidService;

public class DeviceTest extends Activity implements OnClickListener {

    public static final int DEVICE_TEST_MAX_NUM = 1000;
    public static final int TEST_FAILED_DELAY = 5000;
    public static final String EXTRA_TEST_PROGRESS = "test progress";
    public static final String EXTRA_TEST_RESULT_INFO = "test result info";

    public static final String RESULT_INFO_HEAD = ";";
    public static final String RESULT_INFO_HEAD_JUST_INFO = "just info;";

    public static final String EXTRA_PATH = "/data/";
    private static final String CONFIG_FILE_NAME1 = "PCBA_TestConfig.xml";
    private static final String CONFIG_FILE_NAME2 = "Runin_TestConfig.xml";
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

    private int testType = TESTCASE_TYPE_PCBA;

    private Context mContext;

    public static final String DATA_PATH = "/data/data/com.byd8.test/";
    private static final String TAG = "DeviceTest";
    private static final String SAVE_DATA_PATH = DATA_PATH + "DeviceTest.tmp";
    public static final String TEMP_FILE_PATH = DeviceTest.DATA_PATH + "test";


    private Button mButtonCancel;
    private Button mTestChecked;

    public String mGPS;     // "1": GPS; "0": no GPS

    private Button btn1;
    private Button btn2;
    private Button btn3;
    private Button btn4;
    private Button btn5;
    private Button btn6;

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

    /**
     * Called when the activity is first created.
     */
    public static int get_facotry_hwinfo() {
        int ret = 0;
        String fname = "/sdcard/hwinfo";    //Modify by lsg
        String hwinfo = "";
        try {
            File f = new File(fname);
            if (f.exists()) {
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr);
                hwinfo = br.readLine();
                br.close();
                Log.i(TAG, "hwinfo: " + hwinfo);
                final String HWINFO_REGEX = "^CPU=(\\S+)#MEM=(\\S+)#EMMC=(\\S+)#WIFI=(\\S+)#BT=(\\S+)#GPS=(\\S+)#BIOS=(\\S+)#TPVER=(\\S+)#RCAM=(\\S+)#.*";     //Modify by lsg
                try {
                    Pattern p = Pattern.compile(HWINFO_REGEX);
                    Matcher m = p.matcher(hwinfo);
                    boolean r = false;
                    //	r = Pattern.matches( HWINFO_REGEX, hwinfo );
                    r = m.matches();
                    if (!r) {
                        Log.e(TAG, "NOT matches! HWINFO_REGEX=\"" + HWINFO_REGEX + "\"");
                        return -1;
                    }

                    for (int i = 0; i < m.groupCount(); i++) {
                        Log.e(TAG, "FOUND(" + i + "): " + m.group(i + 1));
                    }

                    if (m.groupCount() < 7) {
                        Log.e(TAG, "matched group count = " + m.groupCount() + ", less than 7! HWINFO_REGEX=\"" + HWINFO_REGEX + "\"");
                        return -1;
                    } else {
                        mFT_CPU = m.group(1);
                        mFT_MEM = m.group(2);
                        mFT_EMMC = m.group(3);
                        mFT_WIFI = m.group(4);
                        mFT_BT = m.group(5);
                        mFT_GPS = m.group(6);                      //Modify by lsg
                        mFT_BIOS = m.group(7);
                        mFT_TPVer = m.group(8);
                        mFT_RCAM = m.group(9);
                        String s = "mFT_CPU=" + mFT_CPU + "; mFT_MEM=" + mFT_MEM +
                                "; mFT_EMMC=" + mFT_EMMC + "; mFT_WIFI=" + mFT_WIFI +
                                "; mFT_BT=" + mFT_BT + "; mFT_GPS=" + mFT_GPS +
                                ";mFT_BIOS=" + mFT_BIOS + "; mFT_TPVer=" + mFT_TPVer + "; mFT_RCAM=" + mFT_RCAM;
                        Log.i(TAG, "get_facotry_hwinfo(): " + s);
                    }

                } catch (PatternSyntaxException e) {
                    Log.e(TAG, e.getDescription());
                    Log.e(TAG, e.getMessage());
                    Log.e(TAG, e.getPattern());
                    return -1;
                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return -1;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

        if (hwinfo == "")
            return -1;

        return 0;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        bindViews();
        startService(new Intent(this, AndroidService.class));
        android.util.Log.d("adbsocket", "start service");
    }

    private void bindViews() {
        Button cit = (Button) findViewById(R.id.btn1);
        cit.setOnClickListener(this);
        Button runin = (Button) findViewById(R.id.btn2);
        runin.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btn1) {
            // cit test
            startActivity(new Intent(this, CitTest.class));
        } else if (view.getId() == R.id.btn2) {
            // runin test
            Toast.makeText(this, "runin test!", Toast.LENGTH_LONG).show();
            // write result to disk
            Utils.writeResultToDisk(false,new File(getFilesDir(),"runinflag"));
        }

    }


    private void promptFTNotPASS() {
    }

    private void promptSignalTestNotPASS() {
    }

    private void promptRuninNotPASS() {
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

    private void loadData() throws Exception {}
    public int mGpsTTFF = 0;
    public String mCharge85 = "no";
    public int mCapacityAfter = 0;

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    boolean isNeed(int pos, int requestCode) {
        if (requestCode < DEVICE_TEST_MAX_NUM || (mCount % 3) >= 2) return false;
        if (testType == 1) {
            return true;

        } else if (testType == 3) {
            return true;

        }
        return false;

    }


    class CMDExecute3 {

        public synchronized boolean run(String[] cmd, String workdirectory) throws IOException {
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

    private void logfilecheck(String path, String logflag) {
        File file = new File(path);
        String test[];
        test = file.list();
        for (int i = 0; i < test.length; i++) {
            if (test[i].indexOf(logflag) != -1) {
                Log.d("ddddddddddddd", "222222222222222" + test[i]);
                logpcba = new File(path + test[i]);
                logpcba.delete();
            }
        }
    }

    private boolean enableitemclick = true;

    private boolean bPause = false;
    private boolean bLCDEnd = false;
    private boolean bStandby = false;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(TAG, "onPause() testType=" + testType);
        if (testType == TESTCASE_TYPE_LCD && bLCDEnd) {
            bPause = true;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, AndroidService.class));
        android.util.Log.d("adbsocket", "stop service");
    }

    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.KEYCODE_POWER) {
            Log.i(TAG, "dispatchKeyEvent() KeyEvent.KEYCODE_POWER: bStandby=" + bStandby);
            bStandby = !bStandby;
        }
        return super.dispatchKeyEvent(event);
    }

    public static String getSysfsFile(String filename) {
        String result = "";
        try {
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);
            result = br.readLine();
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String readFileLine(String fname) {
        String ret = null;
        if (fname == null) {
            return ret;
        }
        try {
            FileReader fr = new FileReader(fname);
            BufferedReader br = new BufferedReader(fr);
            ret = br.readLine();
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
            return ret;
        }
        return ret;
    }

    public int delFile(String fname) {
        File f = new File(fname);
        if (f.exists()) {
            f.delete();
        }

        return 0;
    }

    public static int writeFileLine(String fname, String line, boolean append) {
        if (fname == null || line == null) {
            return -1;
        }
        try {
            File f = new File(fname);
            FileWriter wr = new FileWriter(f, append);
            wr.write(line);
            wr.close();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }
}
