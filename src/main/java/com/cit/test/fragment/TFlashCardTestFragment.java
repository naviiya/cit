package com.cit.test.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cit.test.R;
import com.cit.test.TestItemActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 */

public class TFlashCardTestFragment extends Fragment {


    private static final String TAG = "TFlashCardTestFragment";
    private TextView tFlash;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tflash_card_layout, container, false);
        tFlash = (TextView) v.findViewById(R.id.t_falshcard);
        ((TestItemActivity)getActivity()).disableButton(R.id.btn_next);
        showTFlashInfo();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkSDCard();
    }

    private void checkSDCard() {
        try {

            Runtime runtime = Runtime.getRuntime();

            Process proc = runtime.exec("mount");

            InputStream is = proc.getInputStream();

            InputStreamReader isr = new InputStreamReader(is);

            String line;

            String mount = new String();

            BufferedReader br = new BufferedReader(isr);

            while ((line = br.readLine()) != null) {

                if (line.contains("secure")) continue;

                if (line.contains("asec")) continue;



                if (line.contains("fat")) {

                    String columns[] = line.split(" ");

                    if (columns != null && columns.length > 1) {

                        mount = mount.concat("*" + columns[1] + "\n");

                    }

                } else if (line.contains("fuse")) {

                    String columns[] = line.split(" ");

                    if (columns != null && columns.length > 1) {

                        mount = mount.concat(columns[1] + "\n");

                    }

                }

            }

            Log.i(TAG, "checkSDCard: " + mount);

        } catch (FileNotFoundException e) {

            // TODO Auto-generated catch block

            e.printStackTrace();

        } catch (IOException e) {

            // TODO Auto-generated catch block

            e.printStackTrace();

        }
    }

    private void showTFlashInfo() {
//        if(!checkSDCard()){
//            // disable button
//            ((TestItemActivity)getActivity()).disableButton(R.id.btn_next);
//            tFlash.setText(getResources().getString(R.string.t_flash_card_test_fail));
//        }else {
//            // show sdcard info
//            String availableSize = String.valueOf((int)getAvailableExternalMemorySize() / 1024 / 1024 );
//            String totalSize = String.valueOf((int)getTotalExternalMemorySize() / 1024 /1024);
//            tFlash.setText(getResources().getString(R.string.t_flash_card_test_detail,availableSize,totalSize));
//        }
    }

     public long getAvailableExternalMemorySize() {
        if (isExternalStorageAvailable()) {
            File path = Environment.getExternalStorageDirectory();//获取SDCard根目录
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } else {
            return -1;
        }
    }


    public long getTotalExternalMemorySize() {
        if (isExternalStorageAvailable()) {
            File path = Environment.getExternalStorageDirectory(); //获取SDCard根目录
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        } else {
            return -1;
        }
    }

    public boolean isExternalStorageAvailable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

}
