package com.cit.test.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
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
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 */
public class GpsTestFragment extends Fragment {

    private static final String TAG = "GpsTestFragment";
    private TextView gpsTip;
    private TextView gpsTimer;
    private TextView empty;
    private ListView gpsList;
    private GpsAdapter adapter;
    private int mLocationMode;
    private int mAssistedGps;
    private Timer mTimer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gps_test_layout, container, false);
        gpsTip = (TextView) v.findViewById(R.id.gps_tip);
        gpsList = (ListView) v.findViewById(R.id.gps_list);
        gpsTimer = (TextView) v.findViewById(R.id.gps_timer);
        gpsTip.setText(getResources().getString(R.string.open_gps));
        ((TestItemActivity) getActivity()).disableButton(R.id.btn_next);
        init();
        return v;
    }

    LocationManager locationManager;


    @Override
    public void onResume() {
        super.onResume();
    }

    private class GpsAdapter extends BaseAdapter {

        public void refresh() {
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return numSatelliteList.size();
        }

        @Override
        public Object getItem(int i) {
            return numSatelliteList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v;
            Holder holder;
            if (view == null) {
                v = LayoutInflater.from(getActivity()).inflate(R.layout.gps_item_layout, viewGroup, false);
                holder = new Holder();
                holder.satelliteIndex = (TextView) v.findViewById(R.id.satellite_index);
                holder.satelliteDegree = (TextView) v.findViewById(R.id.satellite_degree);
                v.setTag(holder);
            } else {
                v = view;
                holder = (Holder) v.getTag();
            }
            holder.satelliteIndex.setText(i + "");
            holder.satelliteDegree.setText(numSatelliteList.get(i).getSnr() + "");
            return v;
        }

        class Holder {
            TextView satelliteIndex;

            TextView satelliteDegree;
        }
    }


    private void init() {
        adapter = new GpsAdapter();
        gpsList.setAdapter(adapter);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        String provider = LocationManager.GPS_PROVIDER;
        Location location = locationManager.getLastKnownLocation(provider);
        locationManager.requestLocationUpdates(provider, 1000, 0,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {
                    }

                    @Override
                    public void onProviderEnabled(String s) {
                        if(isAdded()) {
                            Log.i(TAG, "onProviderEnabled: " + getResources().getString(R.string.gps_on));
                        }
                    }

                    @Override
                    public void onProviderDisabled(String s) {
                        if(isAdded()) {
                            Log.i(TAG, "onProviderDisabled: " + getResources().getString(R.string.gps_off));
                            mHandler.sendEmptyMessageDelayed(GO_TO_SET_GPS,0);
                        }
                    }
                });
        locationManager.addGpsStatusListener(statusListener);
        if(isGpsOn()){
            startTimer();
        }
    }

    private void startTimer() {
        if(startTimer){
            return;
        }
      TimerTask timerTask = new TimerTask() {
          @Override
          public void run() {
              mHandler.sendEmptyMessage(CHANGE_TIME);
          }
      };
        mTimer = new Timer();
        mTimer.schedule(timerTask,0,1000);
        startTimer = true;
    }
    private void stopTimer(){
        if(mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            startTimer = false;
        }
    }

    private static final int GO_TO_SET_GPS = 1110;
    private static final int CHANGE_TIME = 1111;
    private int time;
    private boolean startTimer;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == GO_TO_SET_GPS){
                startGPS();
            }else if(msg.what == CHANGE_TIME){
                refreshTimeView();
            }
        }
    };

    private void refreshTimeView() {
        if(isAdded()) {
            gpsTimer.setText(getString(R.string.current_search, time));
        }
        time ++;
    }

    private void resetTimeView(){
        time = 0;
    }
    private List<GpsSatellite> numSatelliteList = new ArrayList<>();

    private final GpsStatus.Listener statusListener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            Log.i(TAG, "onGpsStatusChanged: " + event);
            GpsStatus status = locationManager.getGpsStatus(null);
            String satelliteInfo = updateGpsStatus(event, status);
        }
    };

    private String updateGpsStatus(int event, GpsStatus status) {
        numSatelliteList.clear();
        StringBuilder sb2 = new StringBuilder("");
        if(event == GpsStatus.GPS_EVENT_STARTED){
            Log.i(TAG, "updateGpsStatus: start");
            startTimer();
        }else if(event == GpsStatus.GPS_EVENT_STOPPED){
            Log.i(TAG, "updateGpsStatus: stop");
            resetTimeView();
        }
        if (status == null) {
            findNoSatellite();
        } else if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            int maxSatellites = status.getMaxSatellites();
            Iterator<GpsSatellite> it = status.getSatellites().iterator();
            numSatelliteList.clear();
            int count = 0;
            while (it.hasNext() && count <= maxSatellites) {
                GpsSatellite s = it.next();
                numSatelliteList.add(s);
                count++;
            }
            Log.i(TAG, "updateGpsStatus: count = " + count);
            if (count != 0) {
                notifySatelliteFind();
            } else {
                findNoSatellite();
            }
        }

        return sb2.toString();
    }

    private void notifySatelliteFind() {
        if(isAdded()) {
            ((TestItemActivity) getActivity()).resetButton();
            adapter.refresh();
            gpsTip.setText(getResources().getString(R.string.search_gps_success, numSatelliteList.size() + ""));
        }
        gpsList.setVisibility(View.VISIBLE);
    }

    private void findNoSatellite() {
        if(isAdded()) {
            ((TestItemActivity) getActivity()).disableButton(R.id.btn_next);
            gpsTip.setText(getResources().getString(R.string.search_gps_fail));
        }
        gpsList.setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeGpsStatusListener(statusListener);
        }
        stopGpsIfNecessary();
        stopTimer();
    }

    private boolean isGpsOn(){
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void startGPS(){
        Log.i(TAG, "startGPS...");
      try{
          Intent intent = new Intent("com.android.settings.location.MODE_CHANGING");
          intent.putExtra("NEW_MODE", android.provider.Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
          getActivity().sendBroadcast(intent, android.Manifest.permission.WRITE_SECURE_SETTINGS);
          mLocationMode = Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Secure.LOCATION_MODE);
          mAssistedGps = Settings.Global.getInt(getActivity().getContentResolver(), Settings.Global.ASSISTED_GPS_ENABLED);
          Settings.Secure.putInt(getActivity().getContentResolver(), Settings.Secure.LOCATION_MODE, android.provider.Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
          Settings.Global.putInt(getActivity().getContentResolver(), Settings.Global.ASSISTED_GPS_ENABLED,1);
      }catch (Exception e){
          Log.e(TAG, "startGPS error! ", e);
      }
    }

    private void stopGpsIfNecessary(){
        Intent intent = new Intent("com.android.settings.location.MODE_CHANGING");
        intent.putExtra("NEW_MODE", android.provider.Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
        getActivity().sendBroadcast(intent, android.Manifest.permission.WRITE_SECURE_SETTINGS);
        Settings.Secure.putInt(getActivity().getContentResolver(), Settings.Secure.LOCATION_MODE, mLocationMode);
        Settings.Global.putInt(getActivity().getContentResolver(), Settings.Global.ASSISTED_GPS_ENABLED,mAssistedGps);
    }
}
