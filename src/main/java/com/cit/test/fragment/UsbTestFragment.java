package com.cit.test.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cit.test.R;
import com.cit.test.TestItemActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 *
 */

public class UsbTestFragment extends Fragment {

    private static final String TAG = "UsbTestFragment";
    private TextView usbTip;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((TestItemActivity)getActivity()).disableNextButton();
        View v = inflater.inflate(R.layout.usb_test,container,false);
        usbTip = (TextView) v.findViewById(R.id.usb_tip);
        return v;
    }
    private StorageManager storageManager;

    @Override
    public void onResume() {
        super.onResume();
        if(storageManager == null){
            storageManager = (StorageManager) getActivity().getSystemService(Context.STORAGE_SERVICE);
        }
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        iFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        iFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        iFilter.addDataScheme("file");
        mBroadcastReceiver = new USBBroadCastReceiver();
        getActivity().registerReceiver(mBroadcastReceiver, iFilter);
        checkUsbMountInfo();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mBroadcastReceiver != null) {
            getActivity().unregisterReceiver(mBroadcastReceiver);
        }
    }

    private USBBroadCastReceiver mBroadcastReceiver;


    private class USBBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: " + intent);
            if(intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)){
                Log.i(TAG, "onReceive: " + "mounted");
                checkUsbMountInfo();
            }else if(intent.getAction().equals(Intent.ACTION_MEDIA_EJECT)){
                Log.i(TAG, "onReceive: eject");
                checkUsbMountInfo();

            }else if(intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)){
                Log.i(TAG, "onReceive: unmounted");
                checkUsbMountInfo();

            }else if(intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED)){
                Log.i(TAG, "onReceive: removed");
                checkUsbMountInfo();

            }else if(intent.getAction().equals(Intent.ACTION_MEDIA_BAD_REMOVAL)){
                Log.i(TAG, "onReceive: bad removed");
                checkUsbMountInfo();

            }
        }
    }

    private final ArrayList<StorageVolume> mUsbs = new ArrayList<>();
    private void checkUsbMountInfo() {
        try {
           /* Class<?> clazz = Class.forName("android.os.storage.StorageManager");
            Class<?> lClazz = Class.forName("android.os.storage.StorageEventListener");
            Method methodPath = clazz.getDeclaredMethod("getVolumePaths");
            Method methodVol = clazz.getDeclaredMethod("getVolumeList");
            String[] ss = (String[]) methodPath.invoke(storageManager);
            for (int i = 0; i < ss.length; i++) {
                Log.i(TAG, "checkUsbMountInfo: " + ss[i]);
            }
            Log.i(TAG, "checkUsbMountInfo: ------------------------------------------------");
            StorageVolume[] list = (StorageVolume[]) methodVol.invoke(storageManager);
            for (int i = 0; i < list.length; i++) {
                Log.i(TAG, "checkUsbMountInfo: " + list[i].getDescription(getActivity()) + "  " + list[i].getState());
            }
            if (list[2].getState().equals("mounted")) {
                Log.i(TAG, "checkUsbMountInfo: usb is mounted!");
                ((TestItemActivity) getActivity()).resetButton();
                usbTip.setText(getString(R.string.usb_connected));
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.usb_mount_error), Toast.LENGTH_SHORT).show();
                ((TestItemActivity) getActivity()).disableButton(R.id.btn_next);
                usbTip.setText(getString(R.string.usb_test_tip));
            }*/

            StorageVolume[] storageVolumes = storageManager.getVolumeList();
            String[] paths = storageManager.getVolumePaths();

            for (int i = 0; i < paths.length; i++) {
                Log.i(TAG, "path: " + paths[i]);
                if (paths[i].contains("usb")) {
                    Log.i(TAG, "have usb hardware");
                }
            }
            Log.i(TAG, "checkUsbMountInfo: ------------------------------------------------");
            mUsbs.clear();
            for (int i = 0; i < storageVolumes.length; i++) {
                Log.i(TAG, "storageVolumes: " + storageVolumes[i].getDescription(getActivity()) + "  " + storageVolumes[i].getState()
                        + "  id =  " + storageVolumes[i].getUuid());
                if(storageVolumes[i].getDescription(getActivity()).contains("USB")){
                    mUsbs.add(storageVolumes[i]);
                }
            }

            if(mUsbs.size() == 0) {
                Toast.makeText(getActivity(), getResources().getString(R.string.usb_mount_error), Toast.LENGTH_SHORT).show();
                ((TestItemActivity) getActivity()).disableButton(R.id.btn_next);
                usbTip.setText(getString(R.string.usb_test_tip));
            }

            for (int i = 0; i < mUsbs.size(); i++) {
                if(mUsbs.get(i).getState().equals("mounted")){
                    Log.i(TAG, "checkUsbMountInfo: usb is mounted!");
                    Toast.makeText(getActivity(), getResources().getString(R.string.usb_connected), Toast.LENGTH_SHORT).show();
                    ((TestItemActivity) getActivity()).resetButton();
                    usbTip.setText(getString(R.string.usb_connected));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
