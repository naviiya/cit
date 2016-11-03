package com.cit.test.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cit.test.R;
import com.cit.test.TestItemActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */

public class KeyBoardTestFragment extends Fragment implements View.OnKeyListener, TestItemActivity.MyKeyListener {
    private static final String TAG = "KeyBoardTestFragment";

    private int[] mButtonIds;
    private HashMap<Integer, Integer> mButtonMaps = new HashMap<>();
    private HashMap<Integer, Integer> mButtonStatus = new HashMap<>();
    private int[] mKeyCodes;
    private View v = null;


    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
    }
    public static final String ACTION_POWER_CLICKED = "com.cit.factorytest.keytest";
    public void onResume() {
        super.onResume();
        ((TestItemActivity)getActivity()).hideStatusBarAndNavigationBar();
        Settings.System.putInt(getActivity().getContentResolver(), "powerstatus", 1);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_POWER_CLICKED);
        mReceiver = new PowerKeyReceiver();
        getActivity().registerReceiver(mReceiver,filter);
        startTest();
    }

    private void startTest() {
        ((TestItemActivity)getActivity()).registerKeyListener(this);
    }

    @Override
    public void onKey(KeyEvent event) {
        doKeyAction(event);
    }

    private PowerKeyReceiver mReceiver;
    private class PowerKeyReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
             if(action.equals(ACTION_POWER_CLICKED)){
                Log.i(TAG, "onReceive: power click");
                if(powerDown){
                    powerDown = false;
                    setButtonBackgroundUp(R.id.bt_power);
                    mButtonStatus.put(KeyEvent.KEYCODE_POWER,1);
                    checkKeyTestOver();
                }else {
                    powerDown = true;
                    setButtonBackgroundDown(R.id.bt_power);
                    mButtonStatus.put(KeyEvent.KEYCODE_POWER,1);
                }
            }
        }
    }

    private boolean powerDown = false;

    @Override
    public void onPause() {
        super.onPause();
        Settings.System.putInt(getActivity().getContentResolver(), "powerstatus", 0);
        if(mReceiver != null){
            getActivity().unregisterReceiver(mReceiver);
        }
        stopTest();
    }
    private void stopTest(){
        ((TestItemActivity)getActivity()).unRegisterKeyListener(this);
    }

    private void initButtonsMaps() {
        Log.d(TAG, "===========initButtonsMaps======");
        int[] resId = {
                R.id.bt_sounddown,
                R.id.bt_soundup,
                R.id.bt_power,
                R.id.bt_F7,
                R.id.bt_F8,
                R.id.bt_F9,
                R.id.bt_F10

        };
        mButtonIds = resId;
        int[] keycode = {
                KeyEvent.KEYCODE_VOLUME_DOWN,
                KeyEvent.KEYCODE_VOLUME_UP,
                KeyEvent.KEYCODE_POWER,
                KeyEvent.KEYCODE_BACK,
                KeyEvent.KEYCODE_DPAD_LEFT,
                KeyEvent.KEYCODE_DPAD_RIGHT,
                KeyEvent.KEYCODE_DPAD_CENTER

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

    private boolean isTestKey(int keycode) {
        int j = mKeyCodes.length;
        int i = 0;
        while (i < j) {
            if (keycode == mKeyCodes[i]) {
                return true;
            } else {
                i++;
            }
        }
        return false;
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

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        return doKeyAction(keyEvent);
    }


    @Nullable
    private Boolean doKeyAction(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int actionCode = event.getAction();
        Log.i(TAG, "KeyCode = " + keyCode);
        Log.i(TAG, "actionCode = " + actionCode);
        if (!isTestKey(keyCode)) {
            return false;
        }
        int value = 0;
        value = mButtonMaps.get(keyCode);
        switch (actionCode) {
            case KeyEvent.ACTION_DOWN:
                setButtonBackgroundDown(value);
                mButtonStatus.put(keyCode, 1);
                break;

            case KeyEvent.ACTION_UP:
                setButtonBackgroundUp(value);
                checkKeyTestOver();
                mButtonStatus.put(keyCode, 1);

                break;
            default:
                break;
        }
        return true;
    }

    private void checkKeyTestOver() {
        if(mButtonStatus.size() != mButtonMaps.size())return;
        Set<Map.Entry<Integer, Integer>> entrySet = mButtonStatus.entrySet();
        boolean success = true;
        for(Map.Entry<Integer, Integer> entry : entrySet){
            if(entry.getValue() != 1){
                success = false;
            }
        }
        if(success){
            ((TestItemActivity)getActivity()).onNext();
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.key_test,container,false);
        initButtonsMaps();
        ((TestItemActivity) getActivity()).disableButton(R.id.btn_next);
        return v;
    }


}
