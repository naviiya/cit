package com.cit.test.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cit.test.R;
import com.cit.test.TestItemActivity;
import com.cit.test.Utils;

import java.io.File;

/**
 *
 */

public class TFlashCardTestFragment extends Fragment {


    private static final String TAG = "TFlashCardTestFragment";
    private TextView tFlash;
    private StorageManager storageManager;

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
        if(storageManager == null){
            storageManager = (StorageManager) getActivity().getSystemService(Context.STORAGE_SERVICE);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        mReceiver = new TFReceiver();
        getActivity().registerReceiver(mReceiver, filter);
        checkSdCardMountInfo();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onDestroy() {
        storageManager = null;
        super.onDestroy();
    }

    private class TFReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: " + intent);
            if(intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)){
                Log.i(TAG, "onReceive: " + "mounted");
                checkSdCardMountInfo();
            }else if(intent.getAction().equals(Intent.ACTION_MEDIA_EJECT)){
                Log.i(TAG, "onReceive: eject");
                checkSdCardMountInfo();

            }else if(intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)){
                Log.i(TAG, "onReceive: unmounted");
                checkSdCardMountInfo();

            }else if(intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED)){
                Log.i(TAG, "onReceive: removed");
                checkSdCardMountInfo();

            }else if(intent.getAction().equals(Intent.ACTION_MEDIA_BAD_REMOVAL)){
                Log.i(TAG, "onReceive: bad removed");
                checkSdCardMountInfo();

            }
        }
    }
    private TFReceiver mReceiver;

    private String tFlashPath;

    private void checkSdCardMountInfo(){
        try {
           /* Class<?> clazz = Class.forName("android.os.storage.StorageManager");
            Class<?> lClazz = Class.forName("android.os.storage.StorageEventListener");
            Method methodPath = clazz.getDeclaredMethod("getVolumePaths");
            Method methodVol = clazz.getDeclaredMethod("getVolumeList");
            String[] ss = (String[]) methodPath.invoke(storageManager);
            for (int i = 0; i < ss.length; i++) {
                Log.i(TAG, "checkSdCardMountInfo: path  = " + ss[i]);
            }
            tFlashPath = ss[1];
            StorageVolume[] list = (StorageVolume[]) methodVol.invoke(storageManager);
            for (int i = 0; i < list.length; i++) {
                Log.i(TAG, "waitForInfo: " + list[i].getDescription(getActivity()) + "  " + list[i].getState() + " id = " + list[i].getUuid());
            }

            if(list[1].getState().equals("mounted")){
                Log.i(TAG, "checkSdCardMountInfo: sdcard is mounted!");
                showTFlashInfo();
                ((TestItemActivity)getActivity()).resetButton();
            }else {
                Toast.makeText(getActivity(),getResources().getString(R.string.tflash_card_mount_error),Toast.LENGTH_SHORT).show();
                ((TestItemActivity)getActivity()).disableButton(R.id.btn_next);
            }*/

            StorageVolume[] storageVolumes = storageManager.getVolumeList();
            String[] paths = storageManager.getVolumePaths();

            for (int i = 0; i < paths.length; i++) {
                Log.i(TAG, "path: " + paths[i]);
                if(paths[i].contains("sdcard")){
                    tFlashPath = paths[i];
                }
            }
            if(TextUtils.isEmpty(tFlashPath)){
                Log.e(TAG, "path error! ");
            }
            Log.i(TAG, "checkSdCardMountInfo: ------------------------------------------------");
            for (int i = 0; i < storageVolumes.length; i++) {
                Log.i(TAG, "storageVolumes: " + storageVolumes[i].getDescription(getActivity()) + "  " + storageVolumes[i].getState()
                        + "  id =  " + storageVolumes[i].getUuid());
            }
            if(storageVolumes[1].getState().equals("mounted")){
                Log.i(TAG, "checkSdCardMountInfo: sdcard is mounted!");
                showTFlashInfo();
                ((TestItemActivity)getActivity()).resetButton();
            }else {
                Toast.makeText(getActivity(),getResources().getString(R.string.tflash_card_mount_error),Toast.LENGTH_SHORT).show();
                tFlash.setText(getString(R.string.tflash_card_mount_error));
                ((TestItemActivity)getActivity()).disableButton(R.id.btn_next);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showTFlashInfo() {
        try {
            File file = new File(tFlashPath);
            long usableSpace = file.getUsableSpace();
            long totalSpace = file.getTotalSpace();
            long freeSpace = file.getFreeSpace();
            Log.i(TAG, "showTFlashInfo: usableSpace = " + Utils.formatStorage(usableSpace) + " totalSpace = " + Utils.formatStorage(totalSpace) + " freeSpace " + Utils.formatStorage(freeSpace));
            tFlash.setText(getResources().getString(R.string.t_flash_card_test_detail,
                    String.valueOf(Utils.formatStorage(freeSpace)), String.valueOf(Utils.formatStorage(totalSpace))));
        } catch (Exception e) {
            Log.e(TAG, "showTFlashInfo: ",e );
        }

    }



}
