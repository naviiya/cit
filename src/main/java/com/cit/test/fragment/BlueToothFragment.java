package com.cit.test.fragment;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cit.test.R;
import com.cit.test.TestItemActivity;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class BlueToothFragment extends Fragment{

    private static final String TAG = "BlueToothFragment";
    private TextView blueToothTip;
    private TextView empty;
    private ListView blueToothList;
    private BluetoothAdapter mBluetoothAdapter;
    private List<String> mList;
    private MyAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bluetooth_test_layout,container,false);
        blueToothTip = (TextView) v.findViewById(R.id.bluetooth_tip);
        blueToothList = (ListView) v.findViewById(R.id.bluetooth_list);
        empty = (TextView) v.findViewById(R.id.empty_bluetooth);
        blueToothTip.setText(getResources().getString(R.string.open_bluetooth));
        ((TestItemActivity)getActivity()).disableButton(R.id.btn_next);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(ACTION_AUDIO_STATE_CHANGED);
        filter.addAction(ACTION_STATE_CHANGED);
        filter.addAction(ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(mReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        init();
    }

    private class MyAdapter extends BaseAdapter{

        public void refresh(List<String> list){
            mList.clear();
            mList.addAll(list);
            notifyDataSetChanged();
        }

        public void add(String dev){
            mList.add(dev);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int i) {
            return mList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v;
            if(view == null){
                v = new TextView(getActivity());
            }else {
                v = view;
            }
            ((TextView)v).setText(mList.get(i));
            return v;
        }
    }

    private void init() {
        mList = new ArrayList<>();
        adapter = new MyAdapter();
        blueToothList.setAdapter(adapter);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }else {
            mEnable = mBluetoothAdapter.isEnabled();
            openBluetooth();
        }
    }

    private boolean searchSuccess = false;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String dev = device.getName() + "\n" + device.getAddress();
                Log.i(TAG, "onReceive: " + dev);
                adapter.add(dev);
                searchSuccess = true;
                mHandler.sendEmptyMessage(SEARCH_BLUETOOTH_SUCCESS);
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                if(!searchSuccess) {
                    mHandler.sendEmptyMessage(SEARCH_BLUETOOTH_FAIL);
                }
            }
        }
    };
    private  static final String ACTION_STATE_CHANGED = "android.bluetooth.adapter.action.STATE_CHANGED";
    private  static final String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED";
    private  static final String ACTION_AUDIO_STATE_CHANGED = "android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED";

    private boolean mEnable;
    private void checkBlueToothIsStarted(){
        if(mBluetoothAdapter.isEnabled()){
            mHandler.sendEmptyMessageDelayed(SEARCH_BLUETOOTH,200);
        }else {
            mHandler.sendEmptyMessageDelayed(OPEN_BLUETOOTH,200);
        }
    }
   private void openBluetooth(){
       if (!mBluetoothAdapter.isEnabled()) {
           mBluetoothAdapter.enable();
           mHandler.sendEmptyMessageDelayed(OPEN_BLUETOOTH,3000);
       }else {
           mHandler.sendEmptyMessageDelayed(SEARCH_BLUETOOTH,1000);
       }
   }

    private void stopBlueToothIfNecessary(){
        if(!mEnable){
            mBluetoothAdapter.disable();
        }
    }

    private static final int OPEN_BLUETOOTH = 1;
    private static final int SEARCH_BLUETOOTH = 2;
    private static final int SEARCH_BLUETOOTH_SUCCESS = 3;
    private static final int SEARCH_BLUETOOTH_FAIL = 4;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case OPEN_BLUETOOTH:
                    blueToothTip.setText(getResources().getString(R.string.open_bluetooth));
                    ((TestItemActivity)getActivity()).disableButton(R.id.btn_next);
                    checkBlueToothIsStarted();
                    break;
                case SEARCH_BLUETOOTH:
                    blueToothTip.setText(getResources().getString(R.string.bluetooth_search));
                    startSearchBluetooth();
                    break;
                case SEARCH_BLUETOOTH_SUCCESS:
                    blueToothTip.setText(getResources().getString(R.string.bluetooth_result));
                    blueToothList.setVisibility(View.VISIBLE);
                    empty.setVisibility(View.GONE);
                    ((TestItemActivity)getActivity()).resetButton();
                    break;
                case SEARCH_BLUETOOTH_FAIL:
                    ((TestItemActivity)getActivity()).disableButton(R.id.btn_next);
                    blueToothTip.setText(getResources().getString(R.string.bluetooth_fail));
                    blueToothList.setVisibility(View.INVISIBLE);
                    empty.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    private void startSearchBluetooth() {
        if(!mBluetoothAdapter.startDiscovery()){
            mHandler.sendEmptyMessageDelayed(SEARCH_BLUETOOTH_FAIL,200);
        }else {
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopBlueToothIfNecessary();
        mHandler.removeCallbacksAndMessages(null);
    }
}
