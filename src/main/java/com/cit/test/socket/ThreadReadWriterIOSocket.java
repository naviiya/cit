package com.cit.test.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import android.content.Context;
import android.os.Build;
import android.util.Log;

public class ThreadReadWriterIOSocket implements Runnable{
    private static Socket mClient;

    ThreadReadWriterIOSocket(Context context, Socket client){
        mClient = client;
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
                if("100401".equals(currCMD)){
                    out.write("Read PSN".getBytes());
                    out.flush();
                }else if(currCMD.startsWith("100400+")){
                    out.write("set PSN".getBytes());
                    out.flush();
                }else if("004401".equals(currCMD)){
                    out.write("Read WIFI_MAC_ADDR".getBytes());
                    out.flush();
                }else if(currCMD.startsWith("004400+")){
                    out.write("write WIFI_MAC_ADDR".getBytes());
                    out.flush();
                }else if("003301".equals(currCMD)){
                    out.write("Read BT_MAC_ADDR".getBytes());
                    out.flush();
                }else if(currCMD.startsWith("003300+")){
                    out.write("write BT_MAC_ADDR".getBytes());
                    out.flush();
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

    public static String readCMDFromSocket(InputStream in) {
        int MAX_BUFFER_BYTES = 2048;
        String msg = "";
        byte[] tempBuffer = new byte[MAX_BUFFER_BYTES];
        try{
            Log.d(AndroidService.TAG, "wait for reading cmd...");
            int numReadBytes = in.read(tempBuffer);
            msg = new String(tempBuffer, 0, numReadBytes);//, "utf-8"
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
