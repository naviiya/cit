package com.cit.test.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cit.test.R;
import com.cit.test.TestItemActivity;

import java.io.IOException;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

/**
 *
 */

public class SpeakerTestFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().addFlags(FLAG_KEEP_SCREEN_ON);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((TestItemActivity)getActivity()).disableButton(R.id.btn_next);
        return inflater.inflate(R.layout.speaker_test_layout,container,false);
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

    private AudioManager mAudioManager;
    private MediaPlayer mPlayer;
    private int mOldVolume;
    private boolean mSpeakerOn;
    @Override
    public void onResume() {
        super.onResume();
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
        mHandler.sendEmptyMessageDelayed(PASS,500);
    }


    private static final int PASS = 19890;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case PASS:
                    ((TestItemActivity)getActivity()).resetButton();
                    break;
            }
        }
    };

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
        mPlayer.stop();
        if (this.mPlayer == null) {
            return;
        }
        this.mPlayer.release();
        this.mPlayer = null;
        mHandler.removeCallbacksAndMessages(null);
    }

    private void stopMediaPlayBack() {
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        getActivity().sendBroadcast(i);

    }
}
