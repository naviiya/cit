package com.cit.test.fragment;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cit.test.R;

/**
 *
 */

public class CheckVersionFragment extends Fragment {


    private static final String TAG = "CheckVersionFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        String display = Build.DISPLAY;
//        Log.i(TAG, "onCreate: " + display);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.check_version_layout, container, false);
        TextView checkVersion = (TextView) view.findViewById(R.id.tv_check_version);
        checkVersion.setText(getResources().getString(R.string.check_version_detail,Build.DISPLAY));
        return view;
    }
}
