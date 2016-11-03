package com.cit.test.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import org.apache.http.util.EncodingUtils;

public class ThreadReadWriterIOSocket implements Runnable{
    private static Socket mClient;
    private Context mContext;
    private WifiManager mWifiManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;

    private String psnFilePath = "config/psn";
    private String psnFileName = "psn.txt";
    private String wifiFilePath = "/config/wifi/";
    private String wifiFileName = "mac.txt";
    private String btFilePath = "/config/bt/";
    private String btFileName = "bd_addr.conf";
    private String citFileName = "citflag";
    private String runinFileName = "runinflag";
    private String snFilePath = "config/sn";
    private String snFileName = "sn.txt";

    private static final String READ_FAIL = "read fail";

    ThreadReadWriterIOSocket(Context context, Socket client){
        mClient = client;
        mContext = context;
    }

    @Override
    public void run() {
        Log.d(AndroidService.TAG, Thread.currentThread().getName() + "----> run read write io socket.");
        BufferedOutputStream out;
        BufferedInputStream in;
        try{
            String currCMD = "";
            out = new BufferedOutputStream(mClient.getOutputStream());
            in = new BufferedInputStream(mClient.getInputStream());

            while(AndroidService.mIoThreadFlag && (mClient != null && mClient.isConnected())){
                currCMD = readCMDFromSocket(in);
                Log.d(AndroidService.TAG, "currCMD=" + currCMD);
                if("PC".equals(currCMD)){//check socket connection
                    out.write("061".getBytes());
                    out.flush();
                }else if("0039000".equals(currCMD)){//read SW_Version
                    String version = "061" + Build.DISPLAY;
                    out.write(version.getBytes());
                    out.flush();
                }else if("100401".equals(currCMD)){//read psn
                    String serial = readPSN();
                    String result = "060";
                    if(!READ_FAIL.equals(serial)){
                        result = "061" + serial;
                    }
                    out.write(result.getBytes());
                    out.flush();
                }else if(currCMD.startsWith("100400")){//write psn
                    boolean success = false;
                    if(currCMD.length() < 7){
                        success = false;
                        android.util.Log.d(AndroidService.TAG, "write psn error: no psn number set by command!");
                    }else{
                        success = writePsn(currCMD.substring(6));
                    }
                    out.write((success?"061":"060").getBytes());
                    out.flush();
                }else if("100501".equals(currCMD)){//read sn
                    String sn = readSN();
                    String result = "060";
                    if(!READ_FAIL.equals(sn)){
                        result = "061" + sn;
                    }
                    out.write(result.getBytes());
                    out.flush();
                }else if(currCMD.startsWith("100500")){//write sn
                    boolean success = false;
                    if(currCMD.length() < 7){
                        success = false;
                        android.util.Log.d(AndroidService.TAG, "write psn error: no psn number set by command!");
                    }else{
                        success = writeSN(currCMD.substring(6));
                    }
                    out.write((success?"061":"060").getBytes());
                    out.flush();
                }else if("004401".equals(currCMD)){//read wifi ...
                    String wifiAddr = readWifiMacAddr();
                    String wifiresult = "060";
                    if(!READ_FAIL.equals(wifiAddr)){
                        wifiresult = "061" + wifiAddr.replace(":", "");
                    }
                    out.write(wifiresult.getBytes());
                    out.flush();
                }else if(currCMD.startsWith("004400")){//write wifi ...
                    //change XX:XX:XX:XX:XX:XX to XXXXXXXXXXXX
                    currCMD.replace(":", "");
                    boolean success = false;
                    if(currCMD.length() < 7){//no input addr
                        success = false;
                        android.util.Log.d(AndroidService.TAG, "write wifi error: no address set by command!");
                    }else if(currCMD.length() > 18){
                        success = false;
                        android.util.Log.d(AndroidService.TAG, "write wifi error:address length error!");
                    }else{
                        success = writeWifiMacAddr(currCMD.substring(6));
                    }
                    out.write((success?"061":"060").getBytes());
                    out.flush();
                }else if("003301".equals(currCMD)){//read bt ...
                    String btAddr = readBtMacAddr().replace(":", "");
                    String btresult = "060";
                    if(!READ_FAIL.equals(btAddr)){
                        btresult = "061" + readBtMacAddr().replace(":", "");
                    }
                    out.write(btresult.getBytes());
                    out.flush();
                }else if(currCMD.startsWith("003300")){//write bt ...(0033010017CDE0F018)
                    //change XX:XX:XX:XX:XX:XX to XXXXXXXXXXXX
                    currCMD.replace(":", "");
                    boolean success = false;
                    if(currCMD.length() < 7){//no input addr
                        success = false;
                        android.util.Log.d(AndroidService.TAG, "write bt error: no address set by command!");
                    }else if(currCMD.length() > 18){
                        success = false;
                        android.util.Log.d(AndroidService.TAG, "write bt error:address length error!");
                    }else{
                        success = writeBtMacAddr(currCMD.substring(6));
                    }
                    out.write((success?"061":"060").getBytes());
                    out.flush();
                }else if("006600".equals(currCMD)){//read citflag
                    String citFlag = readCitFlag();
                    String citresult = "060";
                    if(!READ_FAIL.equals(citFlag)){
                        citresult = "061" + citFlag;
                    }
                    out.write(citresult.getBytes());
                    out.flush();
                }else if("006605".equals(currCMD)){//read runinflag
                    String runinFlag = readRuninFlag();
                    String runinresult = "060";
                    if(!READ_FAIL.equals(runinFlag)){
                        runinresult = "061" + runinFlag;
                    }
                    out.write(runinresult.getBytes());
                    out.flush();
                }else if("CLEAR".equals(currCMD)){
                    out.write("061".getBytes());
                    out.flush();
                    Intent intent = new Intent(Intent.ACTION_MASTER_CLEAR);
                    intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                    intent.putExtra(Intent.EXTRA_REASON, "MasterClearConfirm");
                    mContext.sendBroadcast(intent);
                }else if("SHUTDOWN".equals(currCMD)){
                    out.write("061".getBytes());
                    out.flush();
                    Intent intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
                    intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }else{
                    out.write("Command ERROR!".getBytes());
                    out.flush();
                }
            }
            try{
                out.close();
                in.close();
            }catch(Exception e){
                 e.printStackTrace();
                 Log.d(AndroidService.TAG, "" + e.toString());
            }
        }catch(Exception e){
            e.printStackTrace();
            Log.d(AndroidService.TAG, "" + e.toString());
        }finally{
            try {
                if(mClient != null){
                    mClient.close();
                    mClient = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(AndroidService.TAG, "" + e.toString());
            }
        }
    }

    private File makeFile(String path, String name){
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

    private boolean writeFile(File file, String content){
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

    private boolean writeSN(String sn){
        android.util.Log.d(AndroidService.TAG, "set SN:" + sn);
        File snFile = makeFile(snFilePath, snFileName);
        if(null == snFile || null == sn){
            return false;
        }else{
            return writeFile(snFile, sn);
        }
    }

    private boolean writePsn(String psn){
        android.util.Log.d(AndroidService.TAG, "set psn:" + psn);
        File psnFile = makeFile(psnFilePath, psnFileName);
        if(null == psnFile || null == psn){
            return false;
        }else{
            return writeFile(psnFile, psn);
        }
    }

    private boolean writeWifiMacAddr(String addr){
        boolean result = false;
        android.util.Log.d(AndroidService.TAG, "set wifi addr:" + addr);
        File addrFile = makeFile(wifiFilePath, wifiFileName);
        if(null == addrFile || null == addr){
            return false;
        }else{
            //turn off wifi before write mac addr
            mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
            if(mWifiManager != null){
                mWifiManager.setWifiEnabled(false);
            }
            result = writeFile(addrFile, addr);
            mWifiManager.setWifiEnabled(true);
            return result;
        }
    }

    private boolean writeBtMacAddr(String addr){
        boolean result = false;
        android.util.Log.d(AndroidService.TAG, "set bt addr:" + addr);
        File addrFile = makeFile(btFilePath, btFileName);
        if(null == addrFile || null == addr || addr.length() != 12){
            return false;
        }else{
            //turn off bt before write mac addr
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null) {
                mBluetoothAdapter.disable();
            }

            StringBuffer sb = new StringBuffer(addr);
            for(int i = 1; i < 6; i++){
                sb = sb.insert(3 * i - 1, ":");
            }
            addr = sb.toString();
            android.util.Log.d(AndroidService.TAG, "writeBtMacAddr----" + addr);
            result =  writeFile(addrFile, addr);
//            mBluetoothAdapter.enable();
            return result;
        }
    }

    private String readSN(){
        File snFile = null;
        try{
            snFile = new File(snFilePath, snFileName);
            if(snFile.exists()){
                return readFile(snFile);
            }
        }catch(Exception e){
            android.util.Log.d(AndroidService.TAG, e.toString());
            return READ_FAIL;
        }
        return READ_FAIL;
    }


    private String readPSN(){
        File psnFile = null;
        try{
            psnFile = new File(psnFilePath, psnFileName);
            if(psnFile.exists()){
                return readFile(psnFile);
            }
        }catch(Exception e){
            android.util.Log.d(AndroidService.TAG, e.toString());
            return READ_FAIL;
        }
        return READ_FAIL;
    }

    private String readWifiMacAddr(){
        File wifiFile = null;
        try{
            wifiFile = new File(wifiFilePath, wifiFileName);
            if(wifiFile.exists()){
                return readFile(wifiFile);
            }
        }catch(Exception e){
            android.util.Log.d(AndroidService.TAG, e.toString());
            return READ_FAIL;
        }
        return READ_FAIL;
        /*mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        if(null == mWifiManager){
            return READ_FAIL;
        }
        int i = 0;
        while(!mWifiManager.isWifiEnabled() && i < 20){
            mWifiManager.setWifiEnabled(true);
            i++;
            try{
                Thread.sleep(1000);
            }catch(Exception e){
                android.util.Log.d(AndroidService.TAG, e.toString());
            }
        }
        android.util.Log.d(AndroidService.TAG, "i=" + i);
        if(20 == i){
            return READ_FAIL;
        }
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        String macAddress = wifiInfo == null ? "unavailable" : wifiInfo.getMacAddress() + "";
        return macAddress;*/
    }

    private String readBtMacAddr(){
        File btFile = null;
        try{
            btFile = new File(btFilePath, btFileName);
            if(btFile.exists()){
                return readFile(btFile);
            }
        }catch(Exception e){
            android.util.Log.d(AndroidService.TAG, e.toString());
            return READ_FAIL;
        }
        return READ_FAIL;
        /*
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (null == mBluetoothAdapter ) {
            return READ_FAIL;
        }
        int i = 0;
        while(!mBluetoothAdapter.isEnabled() && i < 20){
            mBluetoothAdapter.enable();
            i++;
            try{
                Thread.sleep(1000);
            }catch(Exception e){
                android.util.Log.d(AndroidService.TAG, e.toString());
            }
        }
        android.util.Log.d(AndroidService.TAG, "i=" + i);
        if(20 == i){
            return READ_FAIL;
        }
        String address = mBluetoothAdapter.isEnabled() ? mBluetoothAdapter.getAddress() + "" : "unavailable";
        return address;*/
    }

    private String readFile(File file){
        String result = "";
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(file);
            byte[] buffer = new byte[fis.available()];
            int conut = fis.read(buffer);
            result = EncodingUtils.getString(buffer, "UTF-8");
        }catch(Exception e){
            result = READ_FAIL;
            android.util.Log.d(AndroidService.TAG, "read file error: " + e.toString());
        }finally{
            if(fis != null){
                try{
                    fis.close();
                    fis = null;
                }catch(Exception e1){
                    android.util.Log.d(AndroidService.TAG, "" + e1.toString());
                }
            }
        }
        return result;
    }
    private String readCitFlag(){
        String result = "";
        File citFile = new File(psnFilePath,citFileName);
        if(citFile.exists()){
            result = readFile(citFile);
        }
        return result;
//        FileInputStream fis = null;
//        try{
//            fis = mContext.openFileInput(citFileName);
//            byte[] buffer = new byte[1024];
//            int conut = fis.read(buffer);
//            result = EncodingUtils.getString(buffer, "UTF-8");
//        }catch(Exception e){
//            result = READ_FAIL;
//            android.util.Log.d(AndroidService.TAG, "readCitFlag error: " + e.toString());
//        }finally{
//            if(fis != null){
//                try{
//                    fis.close();
//                    fis = null;
//                }catch(Exception e1){
//                    android.util.Log.d(AndroidService.TAG, "" + e1.toString());
//                }
//            }
//        }
//        android.util.Log.d(AndroidService.TAG, "CitFlag=" + result);
//        return result;
    }

    private String readRuninFlag(){
        String result = "";
        FileInputStream fis = null;
        try{
            fis = mContext.openFileInput(runinFileName);
            byte[] buffer = new byte[1024];
            int conut = fis.read(buffer);
            result = EncodingUtils.getString(buffer, "UTF-8");
        }catch(Exception e){
            result = READ_FAIL;
            android.util.Log.d(AndroidService.TAG, "readRuninFlag error: " + e.toString());
        }finally{
            if(fis != null){
                try{
                    fis.close();
                    fis = null;
                }catch(Exception e1){
                    android.util.Log.d(AndroidService.TAG, "" + e1.toString());
                }
            }
        }
        android.util.Log.d(AndroidService.TAG, "RuninFlag=" + result);
        return result;
    }

    public static String readCMDFromSocket(InputStream in) {
        int MAX_BUFFER_BYTES = 2048;
        String msg = "";
        byte[] tempBuffer = new byte[MAX_BUFFER_BYTES];
        try{
            Log.d(AndroidService.TAG, "wait for reading cmd...");
            int numReadBytes = in.read(tempBuffer);
            msg = new String(tempBuffer, 0, numReadBytes);//, "utf-8"
            msg = msg.replace("\r\n", "");
            msg = msg.replace("\n", "");
            msg = msg.replace("\r", "");
            tempBuffer = null;
        }catch(Exception e){
            e.printStackTrace();
            Log.d(AndroidService.TAG, "read cmd exception:" + e.toString());
            try{
                if(mClient != null){
                    mClient.close();
                    mClient = null;
                }
            }catch(Exception e1){
                e.printStackTrace();
                Log.d(AndroidService.TAG, "client close exception:" + e1.toString());
            }
        }
        return msg;
    }

}
