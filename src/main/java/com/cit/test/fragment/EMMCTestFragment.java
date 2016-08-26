package com.cit.test.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cit.test.R;

import java.io.File;

/**
 *
 */

public class EMMCTestFragment extends Fragment {

    private TextView emmc;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.emmc_layout, container, false);
        emmc = (TextView) v.findViewById(R.id.emmc_test);
        showEMMCInfo();
        return v;
    }

    private void showEMMCInfo() {

        String availableSize = getAvailableInternalMemorySize() / 1024 /1024 + "";
        String totalSize = getTotalInternalMemorySize() / 1024 /1024 + "";

        emmc.setText(getResources().getString(R.string.emmc_detail,availableSize,totalSize));
    }



     public long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }
     public long getTotalInternalMemorySize() {

        File path = Environment.getDataDirectory();//Gets the Android data directory

        StatFs stat = new StatFs(path.getPath());

        long blockSize = stat.getBlockSize();      //每个block 占字节数

        long totalBlocks = stat.getBlockCount();   //block总数

        return totalBlocks * blockSize;

    }

}
