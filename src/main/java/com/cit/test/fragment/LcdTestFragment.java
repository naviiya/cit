package com.cit.test.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cit.test.R;
import com.cit.test.TestItemActivity;

/**
 *
 */

public class LcdTestFragment extends Fragment {

    private int cur = 0;
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.lcd_test_layout, container, false);
        view.setBackgroundResource(R.color.lcd_test_red);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        view.postDelayed(modifyBgRunnable,1000);

    }

    private final Runnable modifyBgRunnable = new Runnable() {
        @Override
        public void run() {
            cur ++;
            switch (cur){
                case 0:
                    view.setBackgroundResource(R.color.lcd_test_red);
                    break;
                case 1:
                    view.setBackgroundResource(R.color.lcd_test_blue);

                    break;
                case 2:
                    view.setBackgroundResource(R.color.lcd_test_green);

                    break;
                case 3:
                    view.setBackgroundResource(R.color.lcd_test_white);

                    break;
                case 4:
                    view.setBackgroundResource(R.color.lcd_test_black);

                    break;
            }
            if(cur < 5){
                view.postDelayed(modifyBgRunnable,1000);
            }else {
                ((TestItemActivity)getActivity()).showBottomPanel();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        view.removeCallbacks(modifyBgRunnable);
    }
}
