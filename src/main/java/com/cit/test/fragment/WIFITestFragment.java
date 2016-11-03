package com.cit.test.fragment;

import android.app.Fragment;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
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
 *
 */
public class WIFITestFragment extends Fragment{

    private static final String TAG = "WIFITestFragment";
    private TextView wifiTip;
    private ListView wifiList;
    private WifiManager wifiManager;
    private List<ScanResult> list;
    private TextView empty;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.wifi_test_layout,container,false);
        wifiTip = (TextView) v.findViewById(R.id.wifi_tip);
        wifiList = (ListView) v.findViewById(R.id.wifi_list);
        empty = (TextView) v.findViewById(R.id.empty_wifi);
        ((TestItemActivity)getActivity()).disableButton(R.id.btn_next);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private boolean searchSuccess = false;

    private void startSearchWifi() {
        long startTime = System.currentTimeMillis();
        List<ScanResult> reduplicate = new ArrayList<>();
       while (!searchSuccess){
           list = wifiManager.getScanResults();
           reduplicate.clear();
           for (int i = 1; i < list.size(); i++) {
               for (int j = 0; j < i; j++) {
                   if(list.get(i).SSID.equals(list.get(j).SSID)){
                       reduplicate.add(list.get(i));
                   }
                   if(TextUtils.isEmpty(list.get(i).SSID)){
                       reduplicate.add(list.get(i));
                   }
               }
           }
           list.removeAll(reduplicate);

           if(list.size() != 0){
               mHandler.sendEmptyMessageDelayed(SEARCH_WIFI_SUCCESS,200);
               searchSuccess = true;
           }else {
               long curTime = System.currentTimeMillis();
               if(curTime - startTime > 30000){
                   mHandler.sendEmptyMessageDelayed(SEARCH_WIFI_Fail,200);
                   break;
               }
           }
       }
    }

    private void searchSuccess(){
        wifiList.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public Object getItem(int i) {
                return list.get(i);
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
                ((TextView)v).setText(list.get(i).SSID);
                return v;
            }
        });
    }
    private static final int OPEN_WIFI = 1;
    private static final int SEARCH_WIFI = 2;
    private static final int SEARCH_WIFI_SUCCESS = 3;
    private static final int SEARCH_WIFI_Fail = 4;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SEARCH_WIFI:
                    wifiTip.setText(getResources().getString(R.string.wifi_search));
                    ((TestItemActivity)getActivity()).disableButton(R.id.btn_next);
                    startSearchWifi();
                    break;
                case OPEN_WIFI:
                    wifiTip.setText(getResources().getString(R.string.open_wifi));
                    checkWifiStatus();
                    break;
                case SEARCH_WIFI_SUCCESS:
                    wifiTip.setText(getResources().getString(R.string.wifi_result));
                    empty.setVisibility(View.GONE);
                    ((TestItemActivity)getActivity()).resetButton();
                    searchSuccess();
                    break;
                case SEARCH_WIFI_Fail:
                    ((TestItemActivity)getActivity()).disableButton(R.id.btn_next);
                    wifiTip.setText(getResources().getString(R.string.wifi_fail));
                    wifiList.setVisibility(View.INVISIBLE);
                    empty.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };
    private void init() {
        wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        wifiTip.setText(getResources().getString(R.string.open_wifi));
        openWifi();
    }

    private void openWifi() {
        boolean enable = wifiManager.isWifiEnabled();
        mEnable = enable;
        if (!enable) {
            wifiManager.setWifiEnabled(true);
            mHandler.sendEmptyMessageDelayed(OPEN_WIFI,1500);
        }else {
            mHandler.sendEmptyMessageDelayed(OPEN_WIFI,200);
        }
    }

    private boolean mEnable;
    private void checkWifiStatus(){
        boolean enable = wifiManager.isWifiEnabled();
        if (enable) {
            mHandler.sendEmptyMessageDelayed(SEARCH_WIFI, 200);
        }else {
            mHandler.sendEmptyMessageDelayed(OPEN_WIFI,200);
        }
    }

    private void stopWifiImmediateIfNecessary(){
        wifiManager.setWifiEnabled(mEnable);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopWifiImmediateIfNecessary();
        mHandler.removeCallbacksAndMessages(null);
    }
}
