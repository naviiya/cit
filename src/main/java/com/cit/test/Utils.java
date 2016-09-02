package com.cit.test;

import android.content.Context;

import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 */

public class Utils {

    private static final String TAG = "Utils";

    public static void writeResultToDisk(final boolean testResult, final File file) {
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                Log.i(TAG, "doInBackground: " + file.getAbsolutePath());
                FileWriter fw = null;
                try {
                    fw = new FileWriter(file);
                    fw.write(testResult ? "pass" : "fail");
                    fw.flush();
                } catch (Exception e) {
                    Log.e(TAG, "doInBackground: ", e);
                } finally {
                    if (fw != null) try {
                        fw.close();
                    } catch (IOException e) {
                        Log.e(TAG, "doInBackground: ", e);
                    }
                }
                return null;
            }
        };
        task.execute();
    }

    public static String formatStorage(long  res){
        if(res < 1024){
            return res + " B";
        }else if(res < 1024 * 1024 && res >= 1024){
            return res / 1024 + " KB";
        }else if(res < 1024 * 1024 * 1024 && res >= 1024 * 1024) {
            return Math.round(res * 1.0f / 1024 / 1024) + " MB";
        }else {
            return Math.round(res * 1.0f / 1024 / 1024 / 1024) + " GB";
        }
    }

    public static boolean isGlxssConnect(Context context){
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        return usbManager.isGlxssAttached();
    }
}
