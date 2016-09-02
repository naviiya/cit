package com.cit.test.fragment;

import android.app.Fragment;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cit.test.R;
import com.cit.test.TestItemActivity;
import com.cit.test.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static android.content.Context.POWER_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

/**
 *
 */

public class KeyBoardTestFragment extends Fragment implements View.OnKeyListener {
    private static final String TAG = "KeyBoardTestFragment";

    private int[] mButtonIds;
    private HashMap<Integer, Integer> mButtonMaps = new HashMap<>();
    private HashMap<Integer, Integer> mButtonStatus = new HashMap<>();
    private int[] mKeyCodes;
    private View v = null;
    private View windowV;
    private WindowManager wm = null;
    KeyguardManager.KeyguardLock kl = null;
    private TextView tip;
    private View group;

    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        wm = (WindowManager) getActivity().getSystemService(WINDOW_SERVICE);
        KeyguardManager km = (KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE);
        kl = km.newKeyguardLock(null);
    }
    public static final String ACTION_GLXSS_DEVICE_ATTACHED = "android.hardware.usb.action.GLXSS_DEVICE_ATTACHED";
    public static final String ACTION_GLXSS_DEVICE_DETACHED = "android.hardware.usb.action.GLXSS_DEVICE_DETACHED";
    PowerManager.WakeLock wakeLock;
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_GLXSS_DEVICE_ATTACHED);
        filter.addAction(ACTION_GLXSS_DEVICE_DETACHED);
        mReceiver = new GlxssReceiver();
        getActivity().registerReceiver(mReceiver,filter);
        if(!Utils.isGlxssConnect(getActivity())){
            Toast.makeText(getActivity(), getResources().getString(R.string.insert_glxss_and_headset), Toast.LENGTH_LONG).show();
            return;
        }
        startTest();
    }

    private void startTest() {
        tip.setVisibility(View.GONE);
        group.setVisibility(View.VISIBLE);
        wakeLock = ((PowerManager) getActivity().getSystemService(POWER_SERVICE))
                .newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                        | PowerManager.ON_AFTER_RELEASE, TAG);
        wakeLock.acquire();
        kl.disableKeyguard();
        windowV = new View(getActivity());
//        addWindow();
    }

    private GlxssReceiver mReceiver;
    private class GlxssReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(ACTION_GLXSS_DEVICE_ATTACHED)){
                Log.i(TAG, "onReceive: " + " attach ");
                group.setVisibility(View.VISIBLE);
                tip.setVisibility(View.GONE);
            }else if(action.equals(ACTION_GLXSS_DEVICE_DETACHED)){
                Log.i(TAG, "onReceive: detach");
                group.setVisibility(View.GONE);
                tip.setVisibility(View.VISIBLE);
                Toast.makeText(getActivity(), getResources().getString(R.string.insert_glxss_and_headset), Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if(mReceiver != null){
            getActivity().unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (wakeLock != null) {
            wakeLock.release();
        }
//        removeWindow();
        kl.reenableKeyguard();
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
                R.id.bt_F10,
                R.id.bt_F12,
        };
        mButtonIds = resId;
        int[] keycode = {
                KeyEvent.KEYCODE_VOLUME_DOWN,
                KeyEvent.KEYCODE_VOLUME_UP,
                KeyEvent.KEYCODE_POWER,
                KeyEvent.KEYCODE_F7,
                KeyEvent.KEYCODE_F8,
                KeyEvent.KEYCODE_F9,
                KeyEvent.KEYCODE_F10,
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
        return true;
    }

    private void addWindow() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
//        params.type = WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG;
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.flags = /*WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |*/
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
        params.width = 1;//WindowManager.LayoutParams.FILL_PARENT;
        params.height = 1;//WindowManager.LayoutParams.FILL_PARENT;
        params.format = PixelFormat.TRANSLUCENT;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        //
        params.x = 0;
        params.y = 0;
        wm.addView(windowV, params);
        windowV.requestFocus();
        windowV.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCodee, KeyEvent event) {
                Log.i(TAG, " _____________---- onKey(),   " + event.getKeyCode());
                int keyCode = event.getKeyCode();
                int actionCode = event.getAction();
                Log.i(TAG, "KeyCode = " + keyCode);
                Log.i(TAG, "actionCode = " + actionCode);
                if (!isTestKey(keyCode))
                    return false;
                int value = 0;
                value = mButtonMaps.get(keyCode);
                Log.d(TAG, "==================   value = " + value);
                switch (actionCode) {
                    case KeyEvent.ACTION_DOWN:
                        setButtonBackgroundDown(value);
                        mButtonStatus.put(keyCode, 1);
                        break;

                    case KeyEvent.ACTION_UP:
                        setButtonBackgroundUp(value);
                        checkKeyTestOver();
                        mButtonStatus.put(keyCode, 1);
                        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {

                        } else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
                        }else if(keyCode == KeyEvent.KEYCODE_POWER){

                        }
                        break;
                    default:
                        break;
                }
                return true;

            }
        });
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

    private void removeWindow() {
        wm.removeView(windowV);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.key_test, container, false);
        v.setOnKeyListener(this);
        initButtonsMaps();
        tip = (TextView) v.findViewById(R.id.key_test_tip);
        group = v.findViewById(R.id.key_btn_group);
        ((TestItemActivity) getActivity()).disableButton(R.id.btn_next);
        return v;
    }


}
