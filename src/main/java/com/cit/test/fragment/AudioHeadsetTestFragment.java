package com.cit.test.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cit.test.R;
import com.cit.test.TestItemActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

/**
 *
 */

public class AudioHeadsetTestFragment extends Fragment {

    private static final String TAG = "AudioHeadsetTestFragment";
    private HeadsetPlugThread thread;
    private TextView tip;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().addFlags(FLAG_KEEP_SCREEN_ON);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.audio_headset_test_layout, container, false);
        tip = (TextView) v.findViewById(R.id.test_tip);
        ((TestItemActivity)getActivity()).disableButton(R.id.btn_next);
        return v;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        mPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor fd = getActivity().getAssets().openFd("test_music.mp3");
            mPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(),
                    fd.getDeclaredLength());

            mPlayer.prepare();
            mPlayer.setLooping(true);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void startPlay() {
        stopMediaPlayBack();
        this.mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, true);
        int i = this.mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mOldVolume = i;
        int j = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, j, 0);
        this.mSpeakerOn = this.mAudioManager.isSpeakerphoneOn();
        if (!this.mSpeakerOn) {
            this.mAudioManager.setSpeakerphoneOn(true);
        }
        this.mPlayer.start();
    }

    private AudioManager mAudioManager;
    private MediaPlayer mPlayer;
    private int mOldVolume;
    private boolean mSpeakerOn;
    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        getActivity().registerReceiver(mHeadSetReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, false);
        this.mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                this.mOldVolume, 0);
        if (this.mSpeakerOn)
            return;
        this.mAudioManager.setSpeakerphoneOn(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mHeadSetReceiver);
        mPlayer.stop();
        if (this.mPlayer == null) {
            return;
        }
        this.mPlayer.release();
        this.mPlayer = null;
    }

    private void stopMediaPlayBack() {
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        getActivity().sendBroadcast(i);

    }
    private boolean mflagHeadsetPlugThreadRunning = true;
    private class HeadsetPlugThread extends Thread {
        public void exit() {
            mflagHeadsetPlugThreadRunning = false;
        }
        public void run() {

            while ( mflagHeadsetPlugThreadRunning ) {
                String HEADSET_STATE_PATH = "/sys/class/switch/h2w/state";
                File file;
                FileInputStream fis;
                byte[] buff = new byte[1024];
                try {
                    file = new File( HEADSET_STATE_PATH );
                    fis = new FileInputStream( file );
                    int len = fis.read(buff, 0, 1024);
                    int state = Integer.valueOf( (new String(buff, 0, len)).trim() );
                    //Log.i(TAG, "/sys/class/switch/h2w/state: " + state );
                    if ( state == 1 || state == 2) { // has Headset
                        mHandler.sendEmptyMessage( MSG_HAS_HEADSET );
                    }
                    else if ( state == 0 ) { // no Headset
                        mHandler.sendEmptyMessage( MSG_NO_HEADSET );
                    }
                }
                catch( FileNotFoundException e ) {
                    e.printStackTrace();
                }
                catch (IOException e ) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static final int MSG_NO_HEADSET = 0;
    private static final int MSG_HAS_HEADSET = 1;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_HAS_HEADSET:
                    thread.exit();
                    tip.setText(getResources().getString(R.string.test_headset));
                    startPlay();
                    break;
                case MSG_NO_HEADSET:
                    Toast.makeText(getActivity(),getResources().getString(R.string.plug_headset),Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
    private final BroadcastReceiver mHeadSetReceiver =  new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case MSG_NO_HEADSET:
                        Toast.makeText(context,getResources().getString(R.string.plug_headset),Toast.LENGTH_LONG).show();
                        tip.setText(getResources().getString(R.string.plug_headset));
                        ((TestItemActivity)getActivity()).disableButton(R.id.btn_next);
                        stopMediaPlayBack();
                        break;
                    case MSG_HAS_HEADSET:
                        tip.setText(getResources().getString(R.string.test_headset));
                        ((TestItemActivity)getActivity()).resetButton();
                        startPlay();
                        break;
                    default:
                        Log.d(TAG, "unknow");
                        break;
                }

            }
        }

    };
}
