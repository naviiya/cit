package com.cit.test;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.util.Log;

import com.cit.test.socket.AndroidService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 */

public class Utils {

    private static final String TAG = "Utils";
    private static final String PSN_PATH = "config/psn";
    private static final String CIT_FLAG_NAME = "citflag";
    private static final String RUNIN_FLAG_NAME = "runinflag";

    public static void writeCitResult(boolean result){
        File citFile = makeFile(PSN_PATH, CIT_FLAG_NAME);
        writeResultToDisk(result,citFile);
    }
    public static void writeRuninResult(boolean result){
        File runinFile = makeFile(PSN_PATH, RUNIN_FLAG_NAME);
        writeResultToDisk(result,runinFile);
    }
    public static void writeResultToDisk(final boolean testResult, final File file) {
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                boolean writeResult = writeFile(file, testResult ? "pass" : "fail");
                Log.i(TAG, "write cit tst result " + writeResult);
                return null;
            }
        };
        task.execute();
    }
    private static File makeFile(String path, String name){
        File filePath = null;
        try{
            filePath = new File(path);
            if(!filePath.exists()){
                filePath.mkdir();
            }
        }catch(Exception e){
            android.util.Log.d(AndroidService.TAG, "file path error:" + e.toString());
        }
        File file = null;
        if(!filePath.exists()){
            android.util.Log.d(AndroidService.TAG, "dir " + filePath.getAbsolutePath() + " fail!");
            return null;
        }
        try{
            file = new File(filePath, name);
            if(!file.exists()){
                file.createNewFile();
            }
        }catch(Exception e){
            android.util.Log.d(AndroidService.TAG, "file error:" + e.toString());
        }
        return file;
    }

    private static boolean writeFile(File file, String content){
        boolean result = false;
        if(!file.exists()){
            return result;
        }
        FileOutputStream fos = null;
        try{
            Runtime.getRuntime().exec("chmod 664 " + file);
            fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.flush();
            result = true;
        }catch(Exception e){
            result = false;
            android.util.Log.d(AndroidService.TAG, "writeFile " + file.getAbsolutePath()
                    + " error:" + e.toString());
        }finally{
            if(fos != null){
                try{
                    fos.close();
                    fos = null;
                }catch(Exception e1){
                    android.util.Log.d(AndroidService.TAG, "" + e1.toString());
                }
            }
        }
        return result;
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
        return usbManager.isMRDeviceAttached();
    }
}
