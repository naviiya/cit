package com.cit.test.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cit.test.R;
import com.cit.test.TestItemActivity;

/**
 *
 */
public class ChargingTestFragment extends Fragment {

    private static final String TAG = "ChargingTestFragment";
    TextView mChargeStatus;
    TextView mVoltage;
    TextView mCurrent;
    TextView mCapacity;
    TextView mPlug;
    boolean stop = false;
    private boolean batteryflag = true;
    public boolean chargeflag = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.charging_layout, container, false);
        this.mChargeStatus = (TextView) v.findViewById(R.id.chargeStatusText);
        this.mVoltage = (TextView) v.findViewById(R.id.voltageText);
        this.mCurrent = (TextView) v.findViewById(R.id.currentText);
        this.mCapacity = (TextView) v.findViewById(R.id.capacityText);
        this.mPlug = (TextView) v.findViewById(R.id.plugText);
        ((TestItemActivity)getActivity()).disableButton(R.id.btn_next);
        return v;
    }

    public void onResume() {
        super.onResume();
        stop = false;
        IntentFilter localIntentFilter = new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED);
        getActivity().registerReceiver(this.mBatteryInfoReceiver, localIntentFilter);
    }

    public void onPause() {
        super.onPause();
        stop = true;
        BroadcastReceiver localBroadcastReceiver = this.mBatteryInfoReceiver;
        getActivity().unregisterReceiver(localBroadcastReceiver);

    }

    private final BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context paramContext, Intent intent) {
            if (stop) {
                return;
            }
            String action = intent.getAction();

            if (!Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                return;

            }

            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);

            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
            Log.e("Jeffy", "plugged:" + plugged);

            int current = -1;
            String statusString = "";

            switch (status) {
                case BatteryManager.BATTERY_STATUS_UNKNOWN:
                    statusString = getResources().getString(R.string.status_unknown);
                    break;
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    statusString = getResources().getString(R.string.status_charging);
                    break;
                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    statusString = getResources().getString(R.string.status_not_charging);
                    break;
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                    statusString = getResources().getString(R.string.status_not_charging);
                    break;
                case BatteryManager.BATTERY_STATUS_FULL:
                    statusString = getResources().getString(R.string.status_full);
                    break;
            }
            if (statusString != "Charging" && batteryflag) {
                dialog_headphone();
            }
            if (statusString == "Charging") {
//                mTimeOutHandler.removeCallbacks(mmRunner);
//                mTimeOutHandler.postDelayed(mRunner, 1000);
                chargeflag = true;
            }

            mChargeStatus.setText(getResources().getString(R.string.ChargeState,statusString));

            mVoltage.setText(getString(R.string.Voltage,voltage + ""));

            if (current != -1) {
                mCurrent.setText(getResources().getString(R.string.remain_capacity,current + ""));
            } else {
                mCurrent.setVisibility(View.GONE);
            }

            mCapacity.setText(getString(R.string.Capacity,(level * 100 / scale) + "%"));

            boolean acPlugin = false;
            String pluggedStr = "";
            switch (plugged) {
                case BatteryManager.BATTERY_PLUGGED_AC:
                    acPlugin = true;
                    pluggedStr = getResources().getString(R.string.Plug_status_ac);
                    ((TestItemActivity)getActivity()).resetButton();
                    break;
                case BatteryManager.BATTERY_PLUGGED_USB:
                    pluggedStr = getResources().getString(R.string.Plug_status_usb);
                    ((TestItemActivity)getActivity()).resetButton();
                    break;
                default:
                    pluggedStr = getResources().getString(R.string.Plug_status_none);
                    Toast.makeText(getActivity(),getResources().getString(R.string.charging_tip),Toast.LENGTH_SHORT).show();
                    ((TestItemActivity)getActivity()).disableButton(R.id.btn_next);
                    break;
            }
            mPlug.setText(getString(R.string.Plug,pluggedStr));

        }

    };

    private void dialog_headphone() {
//        AlertDialog dialog;
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setMessage("Please connect the charging adapter ");
//        dialog = builder.create();
//        dialog.setCancelable(false);
//        dialog.show();
//        batteryflag = false;
    }
}
