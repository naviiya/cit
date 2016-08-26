package com.cit.test.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.cit.test.R;
import com.cit.test.TestItemActivity;

/**
 *
 */

public class BrightnessTestFragment extends Fragment {

    private static final String TAG = "BrightnessTestFragment";
    private float originBrightness;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((TestItemActivity)getActivity()).disableButton(R.id.btn_next);
        View v = inflater.inflate(R.layout.brightness_test_layout,container,false);
        return v;
    }


    private static final int CLOSE_SCREEN = 0;
    private static final int LIGHT_UP = 1;
    private static final int LIGHT_UP_COMPLETE = 2;
    private float curBrightness;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case CLOSE_SCREEN:
                    curBrightness = 0.05f;
                    setBrightness(curBrightness);
                    mHandler.sendEmptyMessageDelayed(LIGHT_UP,2000);
                    break;
                case LIGHT_UP:
                    curBrightness += 0.05f;
                    if(curBrightness < 1f){
                        setBrightness(curBrightness);
                        mHandler.sendEmptyMessageDelayed(LIGHT_UP,200);
                    }else {
                        mHandler.sendEmptyMessage(LIGHT_UP_COMPLETE);
                    }
                    break;
                case LIGHT_UP_COMPLETE:
                    ((TestItemActivity)getActivity()).resetButton();
                    Toast.makeText(getActivity(),getResources().getString(R.string.brightness_reback),Toast.LENGTH_SHORT).show();
                    setBrightness(originBrightness);
                    break;
            }
        }
    };

    public void onResume() {
        originBrightness = getBrightness();
        mHandler.sendEmptyMessageDelayed(CLOSE_SCREEN,200);
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
    private void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.screenBrightness = brightness;
        getActivity().getWindow().setAttributes(lp);
    }

    private float getBrightness(){
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        return lp.screenBrightness;
    }

}
