package com.cit.test.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cit.test.R;
import com.cit.test.TestItemActivity;
import com.cit.test.Utils;

import java.util.HashMap;


public class TmpKeyTestFragment extends Fragment implements TestItemActivity.MyKeyListener {

    private static final String TAG = TmpKeyTestFragment.class.getSimpleName();
    private int[] mButtonIds;
    private HashMap<Integer, Integer> mButtonMaps = new HashMap<>();
    private HashMap<Integer, Integer> mButtonStatus = new HashMap<>();
    private int[] mKeyCodes;
    private View v;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mReceiver == null){
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_GLXSS_DEVICE_ATTACHED);
            filter.addAction(ACTION_GLXSS_DEVICE_DETACHED);
            mReceiver = new GlxssReceiver();
            getActivity().registerReceiver(mReceiver,filter);
        }
    }
    private GlxssReceiver mReceiver;


    private class GlxssReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(ACTION_GLXSS_DEVICE_ATTACHED)){
                Log.i(TAG, "onReceive: " + " attach ");
                Toast.makeText(getActivity(), getResources().getString(R.string.glxss_connected), Toast.LENGTH_LONG).show();
                ((TestItemActivity)getActivity()).resetButton();
                startTest();
            }else if(action.equals(ACTION_GLXSS_DEVICE_DETACHED)){
                Log.i(TAG, "onReceive: detach");
                Toast.makeText(getActivity(), getResources().getString(R.string.insert_glxss), Toast.LENGTH_LONG).show();
                stopTest();
            }
        }
    }
    @Override
    public void onKey(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int actionCode = event.getAction();
        Log.i(TAG, "KeyCode = " + keyCode);
        Log.i(TAG, "actionCode = " + actionCode);
        int value = 0;
        value = mButtonMaps.get(keyCode);
        switch (actionCode) {
            case KeyEvent.ACTION_DOWN:
                setButtonBackgroundDown(value);
                mButtonStatus.put(keyCode, 1);
                break;

            case KeyEvent.ACTION_UP:
                setButtonBackgroundUp(value);
                mButtonStatus.put(keyCode, 1);
                break;
            default:
                break;
        }
    }
    private void initButtonsMaps() {
        Log.d(TAG, "===========initButtonsMaps======");
        int[] resId = {
                R.id.bt_F12

        };
        mButtonIds = resId;
        int[] keycode = {
                KeyEvent.KEYCODE_F12
        };
        mKeyCodes = keycode;
        int i = 0;
        int j = mButtonIds.length;
        Log.d(TAG, "I=" + i + " j = " + j);
        for (i = 0; i < j; i++) {
            int key = mKeyCodes[i];
            int value = mButtonIds[i];
            mButtonMaps.put(key, value);
        }
        resetButtonBackground();
    }

    private void resetButtonBackground() {
        Log.d(TAG, "resetButtonBackground()... ...");
        int i = mButtonIds.length;
        int j = 0;
        while (true) {
            if (j >= i)
                return;
            int k = mButtonIds[j];
            v.findViewById(k).setBackgroundResource(R.drawable.button_bg_normal);
            ((TextView) v.findViewById(k)).setTextColor(Color.BLACK);
            j += 1;
        }
    }

    private void setButtonBackgroundDown(int resId) {
        v.findViewById(resId).setBackgroundResource(R.drawable.button_bg_down);
    }

    private void setButtonBackgroundUp(int resId) {
        v.findViewById(resId).setBackgroundResource(R.drawable.button_bg_up);
    }
    private void stopTest() {
        ((TestItemActivity)getActivity()).unRegisterKeyListener(this);
    }

    private void startTest() {
        ((TestItemActivity)getActivity()).registerKeyListener(this);
    }

    public static final String ACTION_GLXSS_DEVICE_ATTACHED = UsbManager.ACTION_MRDEVICE_ATTACHED;
    public static final String ACTION_GLXSS_DEVICE_DETACHED = UsbManager.ACTION_MRDEVICE_DETACHED;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.tmp_key_test,container,false);
        initButtonsMaps();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
//        ((TestItemActivity)getActivity()).disableNextButton();
        if(!Utils.isGlxssConnect(getActivity())){
            Toast.makeText(getActivity(),getString(R.string.insert_glxss),Toast.LENGTH_LONG).show();
            return;
        }
        if(isAdded()) {
            ((TestItemActivity)getActivity()).resetButton();
            startTest();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTest();
        if(mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }
}
