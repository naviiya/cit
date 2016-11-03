package com.cit.test;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.cit.test.fragment.BlueToothFragment;
import com.cit.test.fragment.BrightnessTestFragment;
import com.cit.test.fragment.CameraTestFragment;
import com.cit.test.fragment.ChargingTestFragment;
import com.cit.test.fragment.CheckVersionFragment;
import com.cit.test.fragment.EMMCTestFragment;
import com.cit.test.fragment.GpsTestFragment;
import com.cit.test.fragment.HeadsetTestFragment;
import com.cit.test.fragment.KeyBoardTestFragment;
import com.cit.test.fragment.LcdTestFragment;
import com.cit.test.fragment.MainAudioTestFragment;
import com.cit.test.fragment.RGBTestFragment;
import com.cit.test.fragment.SpeakerTestFragment;
import com.cit.test.fragment.TFlashCardTestFragment;
import com.cit.test.fragment.TmpKeyTestFragment;
import com.cit.test.fragment.TouchTestFragment;
import com.cit.test.fragment.UsbTestFragment;
import com.cit.test.fragment.WIFITestFragment;

/**
 *
 */

public class CitTest extends PreferenceActivity implements Preference.OnPreferenceClickListener {

    private static final String TAG = "CitTest";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.cit_test);
        bindPreference();
    }




    private void bindPreference() {
        Preference checkVersion = findPreference("check_version");
        Preference lcdTest = findPreference("lcd_test");
        Preference screenTest = findPreference("screen_test");
        Preference speakerTest = findPreference("speaker_test");
        Preference backlightTest = findPreference("backlight_test");
        Preference f12Test = findPreference("F12_test");
        Preference usbTest = findPreference("USB_test");
        Preference mainAudioTest = findPreference("main_audio_test");
        Preference earphoneTest = findPreference("earphone_test");
        Preference keyTest = findPreference("key_test");
        Preference flashTest = findPreference("T-FLASH_test");
        Preference emmcTest = findPreference("emmc_test");
        Preference chargingTest = findPreference("charging_test");
        Preference cameraTest = findPreference("camera_test");
        Preference wifiTest = findPreference("WIFI_test");
        Preference bluetoothTest = findPreference("bluetooth_test");
        Preference gpsTest = findPreference("gps_test");
        Preference rgbTest = findPreference("rgb_test");

        checkVersion.setOnPreferenceClickListener(this);
        lcdTest.setOnPreferenceClickListener(this);
        backlightTest.setOnPreferenceClickListener(this);
        screenTest.setOnPreferenceClickListener(this);
        speakerTest.setOnPreferenceClickListener(this);
        f12Test.setOnPreferenceClickListener(this);
        usbTest.setOnPreferenceClickListener(this);
        mainAudioTest.setOnPreferenceClickListener(this);
        earphoneTest.setOnPreferenceClickListener(this);
        keyTest.setOnPreferenceClickListener(this);
        flashTest.setOnPreferenceClickListener(this);
        emmcTest.setOnPreferenceClickListener(this);
        cameraTest.setOnPreferenceClickListener(this);
        wifiTest.setOnPreferenceClickListener(this);
        bluetoothTest.setOnPreferenceClickListener(this);
        chargingTest.setOnPreferenceClickListener(this);
        gpsTest.setOnPreferenceClickListener(this);
        rgbTest.setOnPreferenceClickListener(this);
        // reset
        int count = 0;
        PreferenceRecorder.reset();
        // add preference
        PreferenceWrapper wrapper = new PreferenceWrapper();
        wrapper.setPreference(checkVersion);
        wrapper.setClazz(CheckVersionFragment.class);
        wrapper.setSuccess(false);
        wrapper.setIndex(count++);
        PreferenceRecorder.addPreference(wrapper);

        wrapper = new PreferenceWrapper();
        wrapper.setPreference(lcdTest);
        wrapper.setClazz(LcdTestFragment.class);
        wrapper.setSuccess(false);
        wrapper.setIndex(count++);
        PreferenceRecorder.addPreference(wrapper);

        wrapper = new PreferenceWrapper();
        wrapper.setPreference(screenTest);
        wrapper.setClazz(TouchTestFragment.class);
        wrapper.setSuccess(false);
        wrapper.setIndex(count++);
        PreferenceRecorder.addPreference(wrapper);

        wrapper = new PreferenceWrapper();
        wrapper.setPreference(speakerTest);
        wrapper.setClazz(SpeakerTestFragment.class);
        wrapper.setSuccess(false);
        wrapper.setIndex(count++);
        PreferenceRecorder.addPreference(wrapper);

        wrapper = new PreferenceWrapper();
        wrapper.setPreference(backlightTest);
        wrapper.setClazz(BrightnessTestFragment.class);
        wrapper.setSuccess(false);
        wrapper.setIndex(count++);
        PreferenceRecorder.addPreference(wrapper);



        wrapper = new PreferenceWrapper();
        wrapper.setPreference(usbTest);
        wrapper.setClazz(UsbTestFragment.class);
        wrapper.setSuccess(false);
        wrapper.setIndex(count++);
        PreferenceRecorder.addPreference(wrapper);



        wrapper = new PreferenceWrapper();
        wrapper.setPreference(earphoneTest);
        wrapper.setClazz(HeadsetTestFragment.class);
        wrapper.setSuccess(false);
        wrapper.setIndex(count++);
        PreferenceRecorder.addPreference(wrapper);


        wrapper = new PreferenceWrapper();
        wrapper.setPreference(keyTest);
        wrapper.setClazz(KeyBoardTestFragment.class);
        wrapper.setSuccess(false);
        wrapper.setIndex(count++);
        PreferenceRecorder.addPreference(wrapper);


        wrapper = new PreferenceWrapper();
        wrapper.setPreference(flashTest);
        wrapper.setClazz(TFlashCardTestFragment.class);
        wrapper.setSuccess(false);
        wrapper.setIndex(count++);
        PreferenceRecorder.addPreference(wrapper);

        wrapper = new PreferenceWrapper();
        wrapper.setPreference(emmcTest);
        wrapper.setClazz(EMMCTestFragment.class);
        wrapper.setSuccess(false);
        wrapper.setIndex(count++);
        PreferenceRecorder.addPreference(wrapper);

        wrapper = new PreferenceWrapper();
        wrapper.setPreference(chargingTest);
        wrapper.setClazz(ChargingTestFragment.class);
        wrapper.setSuccess(false);
        wrapper.setIndex(count++);
        PreferenceRecorder.addPreference(wrapper);



        wrapper = new PreferenceWrapper();
        wrapper.setPreference(wifiTest);
        wrapper.setClazz(WIFITestFragment.class);
        wrapper.setSuccess(false);
        wrapper.setIndex(count++);
        PreferenceRecorder.addPreference(wrapper);

        wrapper = new PreferenceWrapper();
        wrapper.setPreference(bluetoothTest);
        wrapper.setClazz(BlueToothFragment.class);
        wrapper.setSuccess(false);
        wrapper.setIndex(count++);
        PreferenceRecorder.addPreference(wrapper);

        wrapper = new PreferenceWrapper();
        wrapper.setPreference(gpsTest);
        wrapper.setClazz(GpsTestFragment.class);
        wrapper.setSuccess(false);
        wrapper.setIndex(count++);
        PreferenceRecorder.addPreference(wrapper);

        wrapper = new PreferenceWrapper();
        wrapper.setPreference(rgbTest);
        wrapper.setClazz(RGBTestFragment.class);
        wrapper.setSuccess(false);
        wrapper.setIndex(count++);
        PreferenceRecorder.addPreference(wrapper);

        wrapper = new PreferenceWrapper();
        wrapper.setPreference(f12Test);
        wrapper.setClazz(TmpKeyTestFragment.class);
        wrapper.setSuccess(false);
        wrapper.setIndex(count++);
        PreferenceRecorder.addPreference(wrapper);

        wrapper = new PreferenceWrapper();
        wrapper.setPreference(mainAudioTest);
        wrapper.setClazz(MainAudioTestFragment.class);
        wrapper.setSuccess(false);
        wrapper.setIndex(count++);
        PreferenceRecorder.addPreference(wrapper);

        wrapper = new PreferenceWrapper();
        wrapper.setPreference(cameraTest);
        wrapper.setClazz(CameraTestFragment.class);
        wrapper.setSuccess(false);
        wrapper.setIndex(count);
        PreferenceRecorder.addPreference(wrapper);

    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        Log.i(TAG, "onPreferenceClick: ");
        Intent i = new Intent(this, TestItemActivity.class);
        i.putExtra("key", preference.getKey());
        i.putExtra("title", preference.getSummary());
        // clear previous
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        return true;
    }
}
