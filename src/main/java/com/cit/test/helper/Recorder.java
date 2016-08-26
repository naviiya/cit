package com.cit.test.helper;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.cit.test.DeviceTest;

import java.io.File;
import java.io.FileInputStream;

public class Recorder implements MediaPlayer.OnCompletionListener,MediaPlayer.OnErrorListener {
    private static final String TAG = "Recorder";
    public static final int IDLE_STATE = 0;
    public static final int INTERNAL_ERROR = 2;
    public static final int NO_ERROR = 0;
    public static final int PLAYING_STATE = 2;
    public static final int RECORDING_STATE = 1;
    static final String SAMPLE_LENGTH_KEY = "sample_length";
    static final String SAMPLE_PATH_KEY = "sample_path";
    static final String SAMPLE_PREFIX = "recording";
    private static final String TEST_FILE_PATH = DeviceTest.DATA_PATH + "test";
    public static final int SDCARD_ACCESS_ERROR = 1;
    OnStateChangedListener mOnStateChangedListener = null;
    MediaPlayer mPlayer = null;
    MediaRecorder mRecorder = null;
    File mSampleFile = null;
    int mSampleLength;
    long mSampleStart = 0L;
    int mState;

    private boolean flag = true;
    private double db = 0;
    private myThread thread = null;

    private void setError(int paramInt) {
        if (this.mOnStateChangedListener == null) {
            return;
        }

        this.mOnStateChangedListener.onError(paramInt);
    }

    private void setState(int state) {
        if (this.state() == state) {
            return;

        }

        this.mState = state;

        signalStateChanged(this.mState);
    }

    private void signalStateChanged(int paramInt) {
        if (this.mOnStateChangedListener == null)
            return;
        this.mOnStateChangedListener.onStateChanged(paramInt);
    }

    public void clear() {
        stop();
        this.mSampleLength = 0;
        signalStateChanged(0);
    }

    public void delete() {
        int i = 0;
        stop();
        if (this.mSampleFile != null)
            this.mSampleFile.delete();
        this.mSampleFile = null;
        this.mSampleLength = i;
        signalStateChanged(i);
    }


    public int getMaxAmplitude()
    {
        if (this.mState == Recorder.RECORDING_STATE) {
            return this.mRecorder.getMaxAmplitude();
        }
        return 0;
    }
    public void onCompletion(MediaPlayer paramMediaPlayer) {
        stop();
    }

    public boolean onError(MediaPlayer paramMediaPlayer, int paramInt1,
                           int paramInt2) {
        stop();
        setError(1);
        return true;
    }

    public int progress() {
        if (this.state() != Recorder.PLAYING_STATE) {
            return 0;
        }
        long currentTime = System.currentTimeMillis();

        return (int) ((currentTime - this.mSampleStart) / 1000L);

    }

    public File sampleFile() {
        return this.mSampleFile;
    }

    public int sampleLength() {
        return this.mSampleLength;
    }

    public void setOnStateChangedListener(
            OnStateChangedListener paramOnStateChangedListener) {
        this.mOnStateChangedListener = paramOnStateChangedListener;
    }

    public void startPlayback() {
        stop();
        if(db < 20) {
            Log.i(TAG, "the record less than 20db!");
            return;
        }
        this.mPlayer = new MediaPlayer();
        try {
            Log.i(TAG,"11111111111HWHWHHWHW1111111111111");
            FileInputStream fis = new FileInputStream(mSampleFile);
            this.mPlayer.setDataSource(fis.getFD());
            Log.i(TAG,"888888888888888::" + fis.getFD());
            Log.i(TAG,"2222222222222HWHWHWHWWHWH222222222222");
            this.mPlayer.setOnCompletionListener(this);
            Log.i(TAG,"3333333333333333HWHWHWH3333333333333333");
            this.mPlayer.setOnErrorListener(this);
            this.mPlayer.prepare();
            Log.i(TAG,"4444444444444444HWHWHHWHHWH444444444444444444444");
            this.mPlayer.start();
            Log.i(TAG,"555555555555HWHHWHWH55555555555555555555555555");

            this.mSampleStart = System.currentTimeMillis();
            setState(Recorder.PLAYING_STATE);

        } catch (Exception e) {
            setError(Recorder.PLAYING_STATE);
            setState(Recorder.IDLE_STATE);

        }
    }

    public void startRecording(int outputfileformat, String extension) {

        Log.i(TAG,"start record stop  recorder first");
        stop();
        Log.i(TAG,"start record stop  recorder first ok");
        try {
            Log.i(TAG,"Create new file:" + TEST_FILE_PATH);
            mSampleFile = new File( Environment.getExternalStorageDirectory().getPath() + "/audiotest_mic.3gp" );
            if( !mSampleFile.exists() ) {
                mSampleFile.createNewFile();
            }

            if(this.mSampleFile.isFile())
            {
                Log.i(TAG,"new file created,now create recorder");
                this.mRecorder = new MediaRecorder();
                this.mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                this.mRecorder.setOutputFormat(outputfileformat);
                this.mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                String str = this.mSampleFile.getAbsolutePath();
                Log.i("TAG","--path is ---:"+str);
                mRecorder.setOutputFile(str);
                Log.i("TAG","---------File is ---:"+str);
                Log.i(TAG,"start to record");

                this.mRecorder.prepare();
                this.mRecorder.start();
                thread=new myThread();
                thread.start();

                this.mSampleStart = System.currentTimeMillis();
                setState(Recorder.RECORDING_STATE);}
        } catch (Exception e) {
            Log.i("TAG","123456789789456123123456789");
            e.printStackTrace();
            setError(Recorder.IDLE_STATE);
            if(mRecorder != null) {
                this.mRecorder.reset();
                this.mRecorder.release();
                this.mRecorder = null;
            }

        }

    }

    public int state() {
        return this.mState;
    }

    public void stop() {
        stopRecording();
        stopPlayback();
    }

    public void stopPlayback() {
        if (this.mPlayer == null) {
            return;
        }
        this.mPlayer.stop();
        this.mPlayer.release();
        this.mPlayer = null;
        setState(Recorder.IDLE_STATE);

    }

    public void stopRecording() {
        if (this.mRecorder == null) {
            return;
        }

        if(thread!=null) {
            thread.exit();
        }

        Log.i(TAG,"call stop recorder");
        this.mRecorder.stop();
        this.mRecorder.release();
        this.mRecorder = null;

        this.mSampleLength = (int) ((System.currentTimeMillis() - mSampleStart) / 1000L);
        setState(Recorder.IDLE_STATE);
    }

    public double getDB() {
        return db;
    }

    private class myThread extends Thread {
        myThread() {

        }

        public void exit() {
            flag = false;
        }

        public void run() {
            while (flag) {
                int x = mRecorder.getMaxAmplitude();
                if (x != 0) {
                    double f = 10 * Math.log(x) / Math.log(10); // t7h

                    int dB = (int) f;
                    if (f > db) {
                        db = f;
                    }
                    flag = true;
                }

            }
        }
    }

 interface OnStateChangedListener {
     void onError(int paramInt);

     void onStateChanged(int paramInt);
 }
}
