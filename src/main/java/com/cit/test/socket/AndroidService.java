package com.cit.test.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AndroidService extends Service {
    public static final String TAG = "adbsocket";
    private static boolean mMainThreadFlag = true;
    public static boolean mIoThreadFlag = true;
    private ServerSocket mServerSocket = null;
    private final int SERVER_PORT = 10086;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "AndroidService onCreate");
        new Thread(){
            public void run(){
                doListen();
            }
        }.start();
    }

    private void doListen(){
        Log.d(TAG, Thread.currentThread().getName() + "----> doListen START");
        mServerSocket = null;
        try {
            mServerSocket = new ServerSocket(SERVER_PORT);
            Socket client = null;
            while(mMainThreadFlag){
                Log.d(TAG, "doListen----mMainThreadFlag=" + mMainThreadFlag);
                client = mServerSocket.accept();
                Log.d(TAG, "accept successed!!!");
                new Thread(new ThreadReadWriterIOSocket(this, client)).start();
//                mMainThreadFlag = false;
            }
            if(client != null){
                try{
                    client.close();
                }catch(Exception e){
                    e.printStackTrace();
                    Log.d(TAG, "client close exception: " + e.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "" + e.toString());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "AndroidService onDestroy");
        mMainThreadFlag = false;
        mIoThreadFlag = false;
        try{
            mServerSocket.close();
        }catch(Exception e){
            e.printStackTrace();
            Log.d(TAG, "ServerSocket close exception: " + e.toString());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "AndroidService onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}
