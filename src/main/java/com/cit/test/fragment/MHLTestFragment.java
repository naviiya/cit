package com.cit.test.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cit.test.R;
import com.cit.test.TestItemActivity;
import com.cit.test.Utils;
import com.cit.test.view.LcdTestView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 */

public class MHLTestFragment extends Fragment implements View.OnTouchListener {

    private final static String TAG = MHLTestFragment.class.getSimpleName();

    private final static int CHANGE_COLOR = 1;
    private final static int HDMI_SCAN = 2;
    private int[] TestColor = {Color.RED, Color.GREEN, Color.BLUE };
    private LcdTestView mTestView;
    private TextView mTitle;
    private TextView mResult;
    private TextView mShowTime;
    private int mTestNo;
    private boolean isStart = false;
    private File HdmiFile = null;
    private File HdmiState = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTestNo = 0;
        HdmiFile = new File("/sys/class/hdmi/hdmi-0/enable");
        HdmiState = new File("/sys/class/hdmi/hdmi-0/state");

    }
    public static final String ACTION_GLXSS_DEVICE_ATTACHED = UsbManager.ACTION_MRDEVICE_ATTACHED;
    public static final String ACTION_GLXSS_DEVICE_DETACHED = UsbManager.ACTION_MRDEVICE_DETACHED;
    private GlxssReceiver mReceiver;
    private class GlxssReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(ACTION_GLXSS_DEVICE_ATTACHED)){
                Log.i(TAG, "onReceive: " + " attach ");
                Toast.makeText(getActivity(), getResources().getString(R.string.glxss_connected), Toast.LENGTH_LONG).show();
                ((TestItemActivity)getActivity()).resetButton();
                // start test
            }else if(action.equals(ACTION_GLXSS_DEVICE_DETACHED)){
                Log.i(TAG, "onReceive: detach");
                Toast.makeText(getActivity(), getResources().getString(R.string.insert_glxss), Toast.LENGTH_LONG).show();
                ((TestItemActivity)getActivity()).disableNextButton();
                // stop test
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.mhltest,container,false);
        v.setOnTouchListener(this);
//        mTestView = (LcdTestView) v.findViewById(R.id.lcdtestview);
//        mResult = (TextView) v.findViewById(R.id.result);
//        mShowTime = (TextView) v.findViewById(R.id.TimeShow);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((TestItemActivity)getActivity()).disableNextButton();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_GLXSS_DEVICE_ATTACHED);
        filter.addAction(ACTION_GLXSS_DEVICE_DETACHED);
        mReceiver = new GlxssReceiver();
        getActivity().registerReceiver(mReceiver,filter);
        if(!Utils.isGlxssConnect(getActivity())){
            Toast.makeText(getActivity(),getString(R.string.insert_glxss),Toast.LENGTH_LONG).show();
            return;
        }
        if(isAdded()) {
            ((TestItemActivity)getActivity()).resetButton();
            // start test
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CHANGE_COLOR:
                    if (mTestNo > TestColor.length - 1) {
                        finishHdmiTest();
                        return;
                    }
                    mShowTime.setVisibility(View.VISIBLE);
                    mTestView.setVisibility(View.VISIBLE);
                    mResult.setText(R.string.HdmiStart);
                    mTestView.setBackgroundColor(TestColor[mTestNo++]);
                    sendEmptyMessageDelayed(CHANGE_COLOR, 1500);
                    break;
                case HDMI_SCAN:
                    this.removeMessages(HDMI_SCAN);
                    if (startHdmiTest()) {
                        mResult.setText(R.string.HdmiPrepare);
                        setHdmiConfig(HdmiFile, true);
                        mTestNo = 0;
                        sendEmptyMessageDelayed(CHANGE_COLOR, 4000);
                    }else{
                        sendEmptyMessageDelayed(HDMI_SCAN, 500);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public boolean startHdmiTest() {
        if (!isStart && isHdmiConnected(HdmiState)) {
            mResult.setText(R.string.HdmiPrepare);
            setHdmiConfig(HdmiFile, true);
            mTestNo = 0;
            isStart = true;
            return true;
        }
        mResult.setText(R.string.HdmiNoInsert);
        Log.i(TAG, "Hdmi no insert");
        return false;
    }

    public void finishHdmiTest() {
        isStart = false;
        mShowTime.setVisibility(View.GONE);
        mTestView.setVisibility(View.GONE);
        mResult.setText(R.string.HdmiResult);
//        setHdmiConfig(HdmiFile, false);
    }

    protected boolean isHdmiConnected(File file) {
        boolean isConnected = false;
        if (file.exists()) {
            try {
                FileReader fread = new FileReader(file);
                BufferedReader buffer = new BufferedReader(fread);
                String strPlug = "plug=1";
                String str = null;

                while ((str = buffer.readLine()) != null) {
                    int length = str.length();
                    if ((length == 6) && (str.equals(strPlug))) {
                        isConnected = true;
                        break;
                    } else {
                        isConnected = false;
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "IO Exception");
            }
        } else {
            Log.e(TAG, file + "isHdmiConnected : file no exist");
        }
        return isConnected;
    }

    protected void setHdmiConfig(File file, boolean enable) {
        if (file.exists()) {
            try {
                SharedPreferences.Editor editor = getActivity().getPreferences(0).edit();
                String strChecked = "1";
                String strUnChecked = "0";

                RandomAccessFile rdf = null;
                rdf = new RandomAccessFile(file, "rw");
                if (enable) {
                    rdf.writeBytes(strChecked);
                    editor.putInt("enable", 1);
                } else {
                    rdf.writeBytes(strUnChecked);
                    editor.putInt("enable", 0);
                }
                editor.commit();
            } catch (IOException re) {
                Log.e(TAG, "IO Exception");
            }
        } else {
            Log.i(TAG, "The File " + file + " is not exists");
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN && !isStart) {
//            mHandler.sendEmptyMessageDelayed(HDMI_SCAN, 500);
        }
        return false;
    }
}



