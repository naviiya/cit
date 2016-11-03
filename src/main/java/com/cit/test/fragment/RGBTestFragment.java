package com.cit.test.fragment;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cit.test.R;
import com.cit.test.TestItemActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;


/**
 */
public class RGBTestFragment extends Fragment {

    private static final String TAG = "RGBTestFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((TestItemActivity) getActivity()).disableButton(R.id.btn_next);
        startFlash();
        return inflater.inflate(R.layout.rgb_test_layout, container, false);
    }

    private void startFlash() {
        mHandler.sendEmptyMessageDelayed(RED, 1000);
    }


    private static final int RED = 0;
    private static final int GREEN = 1;
    private static final int BLUE = 2;
    private static final int COMPLETE = 3;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RED:
                    displayLight(RED);
                    mHandler.sendEmptyMessageDelayed(GREEN, 1000);
                    break;
                case GREEN:
                    displayLight(GREEN);
                    mHandler.sendEmptyMessageDelayed(BLUE, 1000);
                    break;
                case BLUE:
                    displayLight(BLUE);
                    mHandler.sendEmptyMessageDelayed(COMPLETE, 1000);
                    break;
                case COMPLETE:
                    displayLight(COMPLETE);
                    ((TestItemActivity) getActivity()).resetButton();
                    break;
            }
        }
    };

    private void displayLight(int color) {
        switch (color) {
            case RED:
            case GREEN:
            case BLUE:
            case COMPLETE:
                writeValue(color);
                break;
            default:
                Log.e(TAG, "displayLight: error!");
                break;
        }
    }

    private void writeValue(final int color) {
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                File fRed = new File("/sys/class/leds/red/brightness");
                File fGreen = new File("/sys/class/leds/green/brightness");
                File fBlue = new File("/sys/class/leds/blue/brightness");
                switch (color) {
                    case RED:
                        write(fRed, "255");
                        write(fGreen, "0");
                        write(fBlue, "0");
                        break;
                    case GREEN:
                        write(fRed, "0");
                        write(fGreen, "255");
                        write(fBlue, "0");
                        break;
                    case BLUE:
                        write(fRed, "0");
                        write(fBlue, "255");
                        write(fGreen, "0");
                        break;
                    case COMPLETE:
                        write(fBlue, "0");
                        write(fGreen, "0");
                        write(fRed, "0");
                        break;
                }
                return null;
            }
        };
        task.execute();
    }

    private void write(File f, String value) {
        if (f.exists() && f.isFile()) {
            OutputStreamWriter out = null;
            try {
                out = new OutputStreamWriter(new FileOutputStream(f));
                out.write(value);
                out.flush();
            } catch (Exception e) {
                Log.e(TAG, "writeValue: ", e);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        Log.e(TAG, "writeValue: ", e);
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        writeValue(COMPLETE);
        mHandler.removeCallbacksAndMessages(null);
    }
}
