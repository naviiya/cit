package com.cit.test;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.VideoView;

//import com.byd8.test.helper.ControlButtonUtil;
//import com.byd8.test.helper.SystemUtil;
//import com.byd8.test.helper.TestCase.RESULT;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

public class RuninTestActivity extends Activity {

    private static final String BATTERY_TEMP_PATH = "/sys/class/power_supply/*battery/temp";
    private static final String VIDEO_PATH = DeviceTest.EXTRA_PATH + "Earth.mp4";
    private static final int MSG_DO_TEST = 0;
    private static final int MSG_TEST_FAILED = 1;
    private static final int MSG_TEST_PASS = 2;
    private static final int MSG_STOP = 3;
    private static final int MSG_SLEEP_WAKE = 5;

    private static final int SLEEP_WAKE_COUNT = 10;
    private static final long VIDEO_TEST_TIME = 0;
    private static final int MEM_TEST_SIZE = 200;

    private static final int SLEEP_WAKE_DIST = 5 * 1000;

    private String sleepWakeCountString;
    private String videoTimeString;
    private String memSizeString;

    int sleepWakeCount = 0;
    long videoTime = 0;

    VideoView videoView;
    long videoStart = 0;

    private enum TEST_STEP {
        SLEEP_WAKE_TEST, MEM_TEST, VIDEO_TEST, BATTERY_TEMP_TEST
    }

    String memTestResult;
    private int mTestStep = 0;
    float batteryTemp = 0;


    TextView[] mTextViews;

    Spinner sleepWakeCountSpinner, videoTimeSpinner, memSizeSpinner;
    String[] sleepWakeCountVal = { "Def(10)", "0", "1", "2", "5", "10", "20", };
    String[] memSizeVal = { "Def(200M)", "0", "1", "10", "100", "200", "250", };
    String[] videoTimeVal = { "Def(--m)", "0", "1", "10", "30", "60", "90",
            "120", "--" };

    PowerManager powerManager;

    ProgressBar progressBar;
    KeyguardManager keyguardManager;
    KeyguardLock keyguardLock;
    Button stopVideo;

    final String SLEEP_WAKE = "Sleep:";

    boolean videoStoped = false;
    protected WakeLock wakeLock;
    final String ACTION = "sleep_wake_action";
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    BroadcastReceiver receiver;

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);

        setTitle(getTitle() + "----("
                + getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
                + ")");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.runintest);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

}
