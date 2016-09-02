package com.cit.test.fragment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.cit.test.R;
import com.cit.test.TestItemActivity;
import com.cit.test.view.TouchTestView;

import java.util.Timer;
import java.util.TimerTask;

/**
 *
 */

public class TouchTestFragment extends Fragment implements TouchTestView.TouchCompleteListener {

    private Timer timer;
    private TouchTestView touchTestView;
    private TouchTestView actualView;//cover view on activity

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        touchTestView = new TouchTestView(getActivity());
        return touchTestView;
    }

    @Override
    public void onResume() {
        super.onResume();
        addCoverView();
        Toast.makeText(getActivity(),getResources().getString(R.string.touch_test_tip),Toast.LENGTH_LONG).show();
        startTimer();
    }

    private void startTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // time is up
               mHandler.sendEmptyMessage(0);
            }
        },1000 * 60);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            removeCoverView();
            Toast.makeText(getActivity(),getResources().getString(R.string.touch_test_fail),Toast.LENGTH_LONG).show();
            touchTestView.setAllowDraw(false);
            ((TestItemActivity)getActivity()).displayPanel();
            ((TestItemActivity)getActivity()).disableButton(R.id.btn_next);
        }
    };

    @Override
    public void complete() {
        removeCoverView();
        ((TestItemActivity)getActivity()).onNext();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(timer != null){
            timer.cancel();
            timer = null;
        }
        mHandler.removeCallbacksAndMessages(null);
    }
    private void addCoverView() {
        manager = ((WindowManager) getActivity()
                .getSystemService(Context.WINDOW_SERVICE));
        WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
        localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        localLayoutParams.gravity = Gravity.TOP;
        localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                // this is to enable the notification to recieve touch events
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                // Draws over status bar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        localLayoutParams.height = /*(int) (50 * getResources()
                .getDisplayMetrics().scaledDensity);*/WindowManager.LayoutParams.MATCH_PARENT;
        localLayoutParams.format = PixelFormat.TRANSPARENT;
        actualView = new TouchTestView(getActivity());
        actualView.setListener(this);
        manager.addView(actualView, localLayoutParams);
    }

    private WindowManager manager;

    private void removeCoverView(){
        manager.removeView(actualView);
    }


}
