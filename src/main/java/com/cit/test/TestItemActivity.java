package com.cit.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cit.test.fragment.AudioHeadsetTestFragment;
import com.cit.test.fragment.BlueToothFragment;
import com.cit.test.fragment.BrightnessTestFragment;
import com.cit.test.fragment.CameraPhotoTestFragment;
import com.cit.test.fragment.CameraTestFragment;
import com.cit.test.fragment.ChargingTestFragment;
import com.cit.test.fragment.CheckVersionFragment;
import com.cit.test.fragment.EMMCTestFragment;
import com.cit.test.fragment.GpsTestFragment;
import com.cit.test.fragment.MHLTestFragment;
import com.cit.test.fragment.KeyBoardTestFragment;
import com.cit.test.fragment.LcdTestFragment;
import com.cit.test.fragment.MainAudioTestFragment;
import com.cit.test.fragment.PassStorageTestFragment;
import com.cit.test.fragment.RGBTestFragment;
import com.cit.test.fragment.SpeakerTestFragment;
import com.cit.test.fragment.TFlashCardTestFragment;
import com.cit.test.fragment.TouchTestFragment;
import com.cit.test.fragment.UsbTestFragment;
import com.cit.test.fragment.WIFITestFragment;
import com.cit.test.view.InterceptTouchView;

import java.lang.reflect.Method;

/**
 *
 */

public class TestItemActivity extends Activity {

    private static final String TAG = "TestItemActivity";
    private String title;
    private int index;
    private TextView header;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            Intent intent = getIntent();
            String key = intent.getStringExtra("key");
            index = PreferenceRecorder.findPreferenceIndex(key);
            if (index < 0) {
                throw new RuntimeException("index error!");
            }
            title = intent.getStringExtra("title");
        }
        setContentView(R.layout.base_layout);
        displayStatusBarAndNavigationBar();
        bindView();
    }

    protected Button mNext;

    protected Button mRetry;

    protected Button mFail;

    private View mFootView;

    private void bindView() {
        mFootView = findViewById(R.id.footer_view);
        header = (TextView) findViewById(R.id.header);
        header.setText(title);
        mNext = (Button) findViewById(R.id.btn_next);
        mRetry = (Button) findViewById(R.id.btn_retry);
        mFail = (Button) findViewById(R.id.btn_fail);
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNext();
            }
        });
        mRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRetry();
            }
        });
        mFail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFail();
            }
        });
        displayFragment(index);
    }

    private void displayFragment(int i) {
        showOrHideSomePanel(i);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.content_fragment, formatFragment(i));
        transaction.commit();
    }

    private void showOrHideSomePanel(int i) {

        if (i == LCD_TEST || i == TOUCH_TEST) {
            hidePanel();

        } else {
            displayPanel();

        }
    }

    public void displayPanel() {
        // show footer
        mFootView.setVisibility(View.VISIBLE);
        // show header
        header.setVisibility(View.VISIBLE);

        displayStatusBarAndNavigationBar();
    }

    public void hidePanel() {
        // hide footer
        mFootView.setVisibility(View.GONE);
        // hide header
        header.setVisibility(View.GONE);

        hideStatusBarAndNavigationBar();
    }

    public void hideStatusBarAndNavigationBar() {
        View decorView = getWindow().getDecorView();
//        int option = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
//                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        int option = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(option);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    public void displayStatusBarAndNavigationBar() {
        View decorView = getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(option);
    }

    private android.app.Fragment formatFragment(int index) {
        switch (index) {

            case CHECKVERSION_TEST:
                return new CheckVersionFragment();
            case LCD_TEST:
                return new LcdTestFragment();
            case TOUCH_TEST:
                return new TouchTestFragment();
            case SPEAKER_TEST:
                return new SpeakerTestFragment();
            case BRIGHTNESS_TEST:
                return new BrightnessTestFragment();
//            case HDMI_TEST:
//                return new MHLTestFragment();
            case USB_TEST:
                return new UsbTestFragment();
            case MAIN_AUDIO_TEST:
                return new MainAudioTestFragment();
            case AUDIO_HEADSET_TEST:
                return new AudioHeadsetTestFragment();
            case KEY_TEST:
                return new KeyBoardTestFragment();
            case TFLASHCARD_TEST:
                return new TFlashCardTestFragment();
            case EMMC_TEST:
                return new EMMCTestFragment();
            case CHARGING_TEST:
                return new ChargingTestFragment();
            case CAMERA_TEST:
                return new CameraTestFragment();
            case CAMERA_PHOTO_TEST:
                return new CameraPhotoTestFragment();
            case WIFI_TEST:
                return new WIFITestFragment();
            case BLUETOOTH_TEST:
                return new BlueToothFragment();
            case GPS_TEST:
                return new GpsTestFragment();
            case RGB_TEST:
                return new RGBTestFragment();
//            case PASS_STORAGE_TEST:
//                return new PassStorageTestFragment();

        }

        return null;
    }


    public static final int CHECKVERSION_TEST = 0;
    public static final int LCD_TEST = 1;
    public static final int TOUCH_TEST = 2;
    public static final int SPEAKER_TEST = 3;
    public static final int BRIGHTNESS_TEST = 4;
    public static final int USB_TEST = 5;
    public static final int MAIN_AUDIO_TEST = 6;
    public static final int AUDIO_HEADSET_TEST = 7;
    public static final int KEY_TEST = 8;
    public static final int TFLASHCARD_TEST = 9;
    public static final int EMMC_TEST = 10;
    public static final int CHARGING_TEST = 11;
    public static final int CAMERA_TEST = 12;
    public static final int CAMERA_PHOTO_TEST = 13;
    public static final int WIFI_TEST = 14;
    public static final int BLUETOOTH_TEST = 15;
    public static final int GPS_TEST = 16;
    public static final int RGB_TEST = 17;
    // hide
//    public static final int PASS_STORAGE_TEST = 18;


    public void onFail() {
        Toast.makeText(this, getResources().getString(R.string.test_fail), Toast.LENGTH_SHORT).show();
        finish();
    }


    public void onNext() {
        // recorder result
        PreferenceRecorder.saveResult(true, index);

        if (PreferenceRecorder.isEnd(index)) {
            // display alert dialog
            alertEndIfNecessary();
        } else {
            // if has next test
            header.setText(PreferenceRecorder.findNextPreference(index).getPreference().getSummary());
            displayFragment(++index);
            //reset buttons
            resetButton();
        }
    }

    private void alertEndIfNecessary() {
        if(PreferenceRecorder.isAllPass()){
            Toast.makeText(this,getString(R.string.test_success),Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setMessage(getResources().
                    getString(R.string.press_exit)).setPositiveButton(R.string.next, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    finish();
                }
            }).setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                    return true;
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }else {
            Toast.makeText(this,R.string.test_fail_uncomplete,Toast.LENGTH_LONG).show();
            startActivity(new Intent(this,CitTest.class));
            this.finish();
        }
    }


    public void onRetry() {
        //reset result
        PreferenceRecorder.saveResult(false, index);
        displayFragment(index);
    }


    @Override
    public void onBackPressed() {

    }


    public void showBottomPanel() {
        mFootView.setVisibility(View.VISIBLE);
    }

    public void disableButton(int id) {

        switch (id) {
            case R.id.btn_next:
                mNext.setEnabled(false);
                break;
            case R.id.btn_fail:
                mFail.setEnabled(false);
                break;
            case R.id.btn_retry:
                mRetry.setEnabled(false);
                break;
        }

    }

    public void disableNextButton(){
        disableButton(R.id.btn_next);
    }

    public void resetButton() {
        mNext.setEnabled(true);
        mFail.setEnabled(true);
        mRetry.setEnabled(true);
    }


}
