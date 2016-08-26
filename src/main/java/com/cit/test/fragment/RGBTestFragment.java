package com.cit.test.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cit.test.R;
import com.cit.test.TestItemActivity;

/**
 */
public class RGBTestFragment extends Fragment{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((TestItemActivity)getActivity()).disableButton(R.id.btn_next);
        return inflater.inflate(R.layout.rgb_test_layout,container,false);
    }

    @Override
    public void onResume() {
        startFlash();
        super.onResume();
    }
    private NotificationCompat.Builder builder;
    private void startFlash() {
        notificationManager = (NotificationManager) getActivity().getSystemService(Activity.NOTIFICATION_SERVICE);

         builder = new NotificationCompat.Builder(getActivity()).setLights(Color.RED,1,0).
                setPriority(NotificationCompat.PRIORITY_MAX);

        mHandler.sendEmptyMessageDelayed(RED,500);

    }

    private static final int RED = 0;
    private static final int YELLOW = 1;
    private static final int BLUE = 2;
    private static final int COMPLETE = 3;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case RED:
                    builder.setLights(Color.RED,1,0);
                    notificationManager.notify(NOTIFY_ID,builder.build());
                    mHandler.sendEmptyMessageDelayed(YELLOW,1000);
                    break;
                case YELLOW:
                    builder.setLights(Color.YELLOW,1,0);
                    notificationManager.notify(NOTIFY_ID,builder.build());
                    mHandler.sendEmptyMessageDelayed(BLUE,1000);
                    break;
                case BLUE:
                    builder.setLights(Color.BLUE,1,0);
                    notificationManager.notify(NOTIFY_ID,builder.build());
                    mHandler.sendEmptyMessageDelayed(COMPLETE,1000);
                    break;
                case COMPLETE:
                    notificationManager.cancel(NOTIFY_ID);
                    ((TestItemActivity)getActivity()).resetButton();
                    break;
            }
        }
    };

    private NotificationManager notificationManager;
    private static final int NOTIFY_ID = 1989;

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(NOTIFY_ID);
        mHandler.removeCallbacksAndMessages(null);
    }
}
