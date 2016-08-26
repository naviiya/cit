package com.cit.test.fragment;

import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cit.test.R;
import com.cit.test.TestItemActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 */
public class GpsTestFragment extends Fragment{

    private static final String TAG = "GpsTestFragment";
    private TextView gpsTip;
    private TextView empty;
    private ListView gpsList;
    private GpsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gps_test_layout,container,false);
        gpsTip = (TextView) v.findViewById(R.id.gps_tip);
        gpsList = (ListView) v.findViewById(R.id.gps_list);
        empty = (TextView) v.findViewById(R.id.empty_gps);
        gpsTip.setText(getResources().getString(R.string.open_gps));
        ((TestItemActivity)getActivity()).disableButton(R.id.btn_next);
        return v;
    }

    LocationManager locationManager;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }



    private class GpsAdapter extends BaseAdapter{

        public void refresh(){
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
            if(view == null){
                v = LayoutInflater.from(getActivity()).inflate(R.layout.gps_item_layout,viewGroup,false);
                holder = new Holder();
                holder.satelliteIndex = (TextView) v.findViewById(R.id.satellite_index);
                holder.satelliteDegree = (TextView) v.findViewById(R.id.satellite_degree);
                v.setTag(holder);
            }else {
                v = view;
                holder = (Holder) v.getTag();
            }
            holder.satelliteIndex.setText(i + "");
            holder.satelliteDegree.setText(numSatelliteList.get(i).getSnr() + "");
            return v;
        }
        class Holder{
            TextView satelliteIndex;

            TextView satelliteDegree;
        }
    }


    private void init() {
        adapter = new GpsAdapter();
        gpsList.setAdapter(adapter);
        locationManager =  (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
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
                        Toast.makeText(getActivity(),getResources().getString(R.string.gps_on),Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProviderDisabled(String s) {
                        Toast.makeText(getActivity(),getResources().getString(R.string.gps_off),Toast.LENGTH_SHORT).show();
                    }
                });
        locationManager.addGpsStatusListener(statusListener);
    }


    private List<GpsSatellite> numSatelliteList = new ArrayList<>();

    private final GpsStatus.Listener statusListener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            GpsStatus status = locationManager.getGpsStatus(null);
            String satelliteInfo = updateGpsStatus(event, status);
        }
    };

    private String updateGpsStatus(int event, GpsStatus status) {
        numSatelliteList.clear();
        StringBuilder sb2 = new StringBuilder("");
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
            if(count != 0){
                notifySatelliteFind();
            }else {
                findNoSatellite();
            }
        }

        return sb2.toString();
    }

    private void notifySatelliteFind() {
        ((TestItemActivity)getActivity()).resetButton();
        adapter.refresh();
        gpsTip.setText(getResources().getString(R.string.search_gps_success,numSatelliteList.size() + ""));
        gpsList.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);
    }

    private void findNoSatellite() {
        ((TestItemActivity)getActivity()).disableButton(R.id.btn_next);
        gpsTip.setText(getResources().getString(R.string.search_gps_fail));
        gpsList.setVisibility(View.GONE);
        empty.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            locationManager.removeGpsStatusListener(statusListener);
        }
    }
}
