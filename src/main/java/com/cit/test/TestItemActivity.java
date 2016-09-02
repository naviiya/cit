package com.cit.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import android.service.persistentdata.PersistentDataBlockManager;
import com.android.internal.os.storage.ExternalStorageFormatter;
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
        displayFragment();
    }

    private void displayFragment() {
        header.setText(PreferenceRecorder.findPreference(index).getPreference().getSummary());
        showOrHideSomePanel(index);
        displayFragment(formatFragment(index));
    }

    private void displayFragment(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.content_fragment, fragment);
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

    private CameraTestFragment cameraTestFragment;

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
                cameraTestFragment = new CameraTestFragment();
                return cameraTestFragment;
            case WIFI_TEST:
                return new WIFITestFragment();
            case BLUETOOTH_TEST:
                return new BlueToothFragment();
            case GPS_TEST:
                return new GpsTestFragment();
            case RGB_TEST:
                return new RGBTestFragment();
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
    public static final int WIFI_TEST = 13;
    public static final int BLUETOOTH_TEST = 14;
    public static final int GPS_TEST = 15;
    public static final int RGB_TEST = 16;
    // hide
//    public static final int PASS_STORAGE_TEST = 18;


    public void onFail() {
        PreferenceRecorder.saveResult(false, index);
        Toast.makeText(this, getResources().getString(R.string.test_fail), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, CitTest.class));
        finish();
    }

    public void onNext() {
        // recorder result
        if(index != CAMERA_TEST) {
            PreferenceRecorder.saveResult(true, index);
        }

        if (PreferenceRecorder.isEnd(index)) {
            // display alert dialog
            alertEndIfNecessary();
        } else {
            //reset buttons
            resetButton();
            if (index == CAMERA_TEST) {
                if (cameraTestFragment != null && cameraTestFragment.isAdded()) {
                    // take photo
                    cameraTestFragment.takePhoto();
                    return;
                } else {
                    // delete photo
                    deletePhoto();
                    // save result
                    PreferenceRecorder.saveResult(true, CAMERA_TEST);
                }
            }
            // if has next test
            index++;
            displayFragment();
        }
    }

    private void deletePhoto() {
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                if (!TextUtils.isEmpty(mPath)) {
                    File file = new File(mPath);
                    if (file.exists() && file.isFile()) {
                        boolean delete = file.delete();
                        Log.i(TAG, "deletePhoto: " + delete);
                    } else {
                        Log.i(TAG, "deletePhoto: file is not exist!");
                    }
                }
                return null;
            }
        };
        task.execute();
    }

    private String mPath;

    public void displayCameraPhoto(String path) {
        mPath = path;
        header.setText(getString(R.string.camera_photo_test));
        CameraPhotoTestFragment fragment = new CameraPhotoTestFragment();
        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        fragment.setArguments(bundle);
        displayFragment(fragment);
    }

    private void alertEndIfNecessary() {
        boolean testResult = PreferenceRecorder.isAllPass();
        writeResultToDisk(testResult);
        if (testResult) {
            Toast.makeText(this, getString(R.string.test_success), Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setMessage(getResources().
                    getString(R.string.press_exit)).setPositiveButton(R.string.next, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startRecover();
                    dialogInterface.dismiss();
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
        } else {
            Toast.makeText(this, R.string.test_fail_uncomplete, Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, CitTest.class));
            this.finish();
        }
    }

    private void writeResultToDisk(final boolean testResult) {
        File file = new File(getFilesDir(), "citflag");
        Utils.writeResultToDisk(testResult, file);
    }


    public void onRetry() {
        //reset result
        PreferenceRecorder.saveResult(false, index);
        displayFragment();
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

    public void disableNextButton() {
        disableButton(R.id.btn_next);
    }

    public void resetButton() {
        mNext.setEnabled(true);
        mFail.setEnabled(true);
        mRetry.setEnabled(true);
    }

    private void startRecover() {
        final PersistentDataBlockManager pdbManager = (PersistentDataBlockManager)
                TestItemActivity.this.getSystemService(Context.PERSISTENT_DATA_BLOCK_SERVICE);

        if (pdbManager != null && !pdbManager.getOemUnlockEnabled() &&
                Settings.Global.getInt(TestItemActivity.this.getContentResolver(),
                        Settings.Global.DEVICE_PROVISIONED, 0) != 0) {
            // if OEM unlock is enabled, this will be wiped during FR process. If disabled, it
            // will be wiped here, unless the device is still being provisioned, in which case
            // the persistent data block will be preserved.
            final ProgressDialog progressDialog = getProgressDialog();
            progressDialog.show();

            // need to prevent orientation changes as we're about to go into
            // a long IO request, so we won't be able to access inflate resources on flash
            final int oldOrientation = TestItemActivity.this.getRequestedOrientation();
            TestItemActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    // workaround: master reset process dialog not show
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                    }
                    pdbManager.wipe();
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    progressDialog.hide();
                    TestItemActivity.this.setRequestedOrientation(oldOrientation);
                    doMasterClear();
                }
            }.execute();
        } else {
            doMasterClear();
        }
    }
    private ProgressDialog getProgressDialog() {
        final ProgressDialog progressDialog = new ProgressDialog(TestItemActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(
                TestItemActivity.this.getString(R.string.master_clear_progress_title));
        progressDialog.setMessage(
                TestItemActivity.this.getString(R.string.master_clear_progress_text));
        return progressDialog;
    }

    private void doMasterClear() {
//        if (mEraseSdCard) {
//            Intent intent = new Intent(ExternalStorageFormatter.FORMAT_AND_FACTORY_RESET);
//            intent.putExtra(Intent.EXTRA_REASON, "MasterClearConfirm");
//            intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
//            getActivity().startService(intent);
//        } else {
            Intent intent = new Intent(Intent.ACTION_MASTER_CLEAR);
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            intent.putExtra(Intent.EXTRA_REASON, "MasterClearConfirm");
            TestItemActivity.this.sendBroadcast(intent);
            // Intent handling is asynchronous -- assume it will happen soon.
//        }
    }

}
