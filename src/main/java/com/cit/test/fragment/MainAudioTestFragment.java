package com.cit.test.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.cit.test.R;
import com.cit.test.TestItemActivity;
import com.cit.test.Utils;
import java.util.LinkedList;

public class MainAudioTestFragment extends Fragment {

    private static final String TAG = "MainAudioTestFragment";

    private void startMICToSPK() {
        flag = true;
        init();
        mRecord.start();
        mPlay.start();
        Log.i(TAG, "startMICToSPK");
    }

    private View v;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.main_audio_test,container,false);
        return v;
    }

    protected int m_in_buf_size;
    private AudioRecord m_in_rec;
    private LinkedList<byte[]> m_in_q;
    private AudioTrack m_out_trk;
    private boolean flag = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mReceiver == null){
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_GLXSS_DEVICE_ATTACHED);
            filter.addAction(ACTION_GLXSS_DEVICE_DETACHED);
            mReceiver = new GlxssReceiver();
            getActivity().registerReceiver(mReceiver,filter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((TestItemActivity)getActivity()).disableNextButton();
        if(!Utils.isGlxssConnect(getActivity())){
            Toast.makeText(getActivity(),getString(R.string.insert_glxss),Toast.LENGTH_LONG).show();
            return;
        }
        if(isAdded()) {
            ((TestItemActivity)getActivity()).resetButton();
            if(!flag) {
                startMICToSPK();
            }
        }
    }

    private GlxssReceiver mReceiver;
    private class GlxssReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(ACTION_GLXSS_DEVICE_ATTACHED)){
                Log.i(TAG, "onReceive: " + " attach ");
                Toast.makeText(getActivity(), getResources().getString(R.string.glxss_connected), Toast.LENGTH_LONG).show();
                ((TestItemActivity)getActivity()).resetButton();
                startMICToSPK();
            }else if(action.equals(ACTION_GLXSS_DEVICE_DETACHED)){
                Log.i(TAG, "onReceive: detach");
                Toast.makeText(getActivity(), getResources().getString(R.string.insert_glxss), Toast.LENGTH_LONG).show();
                ((TestItemActivity)getActivity()).disableNextButton();
                stopTest();
            }
        }
    }

    @Override
    public void onDestroy() {
        stopTest();
        if(mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        super.onDestroy();
    }

    private void init()
    {
        Log.i(TAG, "init start...... ");
        m_in_buf_size = AudioRecord.getMinBufferSize(8000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        m_in_rec = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, m_in_buf_size);
        m_in_q = new LinkedList<>();
        int m_out_buf_size = AudioTrack.getMinBufferSize(8000,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        m_out_trk = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, m_out_buf_size,
                AudioTrack.MODE_STREAM);
        if(mAudioManager == null) {
            mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        }
        mOriginVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                AudioManager.FLAG_PLAY_SOUND);
        if(mRecord == null) {
            mRecord = new RecordSound();
        }
        if(mPlay == null) {
            mPlay = new PlayRecord();
        }
        Log.i(TAG, "init end.......");
    }
    private RecordSound mRecord;
    private PlayRecord mPlay;
    public static final String ACTION_GLXSS_DEVICE_ATTACHED = UsbManager.ACTION_MRDEVICE_ATTACHED;
    public static final String ACTION_GLXSS_DEVICE_DETACHED = UsbManager.ACTION_MRDEVICE_DETACHED;

    public void stopTest(){
        flag = false;
        if(m_in_rec != null){
            m_in_rec.stop();
            m_in_rec.release();
            m_in_rec = null;
        }
        if(m_out_trk != null) {
            m_out_trk.stop();
            m_out_trk.release();
            m_out_trk = null;
        }
        mPlay = null;
        mRecord = null;
        if(mAudioManager != null) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    mOriginVolume,
                    AudioManager.FLAG_PLAY_SOUND);
        }
        Log.i(TAG, "stopTest .....");
    }
    private AudioManager mAudioManager;
    private int mOriginVolume;
    class RecordSound extends Thread
    {
        @Override
        public void run()
        {
            m_in_rec.startRecording();
            Log.i(TAG, "startup recorder....");
            Log.i(TAG, "thread id is " + Thread.currentThread().getId());
            byte[] m_in_bytes;
            while (flag) {
                try {
                    m_in_bytes = new byte[m_in_buf_size];
                    m_in_rec.read(m_in_bytes, 0, m_in_buf_size);
                    m_in_q.add(m_in_bytes);
                }catch (Exception e){
                    Log.e(TAG, "run: ", e);
                }
            }
            Log.i(TAG, "start stop record... recorder");
            Log.i(TAG, "thread is end, id is " + Thread.currentThread().getId());
            m_in_bytes = null;
        }
    }


    class PlayRecord extends Thread
    {
        @Override
        public void run()
        {
            m_out_trk.play();
            Log.i(TAG, "startup player...");
            Log.i(TAG, "thread id is " + Thread.currentThread().getId());
            while (flag) {
                try {
                    if (!m_in_q.isEmpty()) {
                        byte[] arrayOfByte = (byte[]) m_in_q.removeFirst();
                        if (arrayOfByte != null) {
                            m_out_trk.write(arrayOfByte, 0, arrayOfByte.length);
                            arrayOfByte = null;
//                            System.gc();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "run: ", e);
                }
            }
            Log.i(TAG, "start stop play... " );
            Log.i(TAG, "thread is end, id is " + Thread.currentThread().getId());
        }
    }
}
