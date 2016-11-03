package com.cit.test.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cit.test.R;
import com.cit.test.TestItemActivity;
import com.cit.test.Utils;

import java.util.HashMap;
import java.util.Map;

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

    private UsbManager mUsbManager;
    @Override
    public void onResume() {
        super.onResume();
        if(mUsbManager == null){
            mUsbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
        }
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        iFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        iFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        iFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        mBroadcastReceiver = new USBBroadCastReceiver();
        getActivity().registerReceiver(mBroadcastReceiver, iFilter);
        checkUsbMountInfo();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mBroadcastReceiver != null) {
            getActivity().unregisterReceiver(mBroadcastReceiver);
        }
    }


    private USBBroadCastReceiver mBroadcastReceiver;


    private class USBBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: " + intent);
          if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){
                Log.i(TAG, "onReceive: usb attached!");
                usbConnect();
            }
            else if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)){
                Log.i(TAG, "onReceive: usb detached!");
                usbDisconnect();
            }
            else if(intent.getAction().equals(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)){
                Log.i(TAG, "onReceive: usb accessory attached!");
            }
            else if(intent.getAction().equals(UsbManager.ACTION_USB_ACCESSORY_DETACHED)){
                Log.i(TAG, "onReceive: usb accessory detached!");
            }
        }
    }

    private void checkUsbMountInfo() {
        if(mUsbManager != null){
            HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
            if(deviceList.size() > 1 ||
                    (deviceList.size() == 1 && !Utils.isGlxssConnect(getActivity()))){
                usbConnect();
            }else{
                usbDisconnect();
            }
        }
    }

    private void usbConnect() {
        Toast.makeText(getActivity(), getResources().getString(R.string.usb_connected), Toast.LENGTH_SHORT).show();
        ((TestItemActivity) getActivity()).resetButton();
        usbTip.setText(getString(R.string.usb_connected));
    }

    private void usbDisconnect() {
        Toast.makeText(getActivity(), getResources().getString(R.string.usb_mount_error), Toast.LENGTH_SHORT).show();
        ((TestItemActivity) getActivity()).disableButton(R.id.btn_next);
        usbTip.setText(getString(R.string.usb_test_tip));
    }

}
