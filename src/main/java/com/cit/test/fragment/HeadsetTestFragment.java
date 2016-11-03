package com.cit.test.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.cit.test.R;
import com.cit.test.TestItemActivity;
import java.util.LinkedList;


/**
 *
 */

public class HeadsetTestFragment extends Fragment implements TestItemActivity.MyKeyListener {

    private static final String TAG = "HeadsetTestFragment";

    private TextView tip;
    private AudioManager mAudioManager;
    private int mOriginVolume;
    private TextView mHeadsetUp;
    private TextView mHeadsetDown;
    private TextView mHeadsetHook;
    private View mKeyGroupView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.audio_headset_test_layout, container, false);
        tip = (TextView) v.findViewById(R.id.test_tip);
        mKeyGroupView = v.findViewById(R.id.test_key_group);
        mHeadsetUp = (TextView) v.findViewById(R.id.headset_up);
        mHeadsetDown = (TextView) v.findViewById(R.id.headset_down);
        mHeadsetHook = (TextView) v.findViewById(R.id.headset_hook);
        ((TestItemActivity) getActivity()).disableButton(R.id.btn_next);
        mKeyGroupView.setVisibility(View.GONE);
        return v;
    }

    private Thread mRecorderThread;
    private Thread mPlayThread;

    private void startTest() {
        flag = true;
        // make sure headset was connected before init()
        init();
        Log.i(TAG, "start test .....");
        mRecorderThread.start();
        mPlayThread.start();
    }

    private void init() {
        Log.i(TAG, "init start..... ");
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
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        }
        mOriginVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                AudioManager.FLAG_PLAY_SOUND);
        if (mRecorderThread == null) {
            mRecorderThread = new Thread(new RecordSound());
        }
        if (mPlayThread == null) {
            mPlayThread = new Thread(new PlayRecord());
        }
        Log.i(TAG, "init end....");
    }

    public void stopTest() {
        flag = false;
        if (m_in_rec != null) {
            m_in_rec.stop();
            m_in_rec.release();
            m_in_rec = null;
        }
        if (m_out_trk != null) {
            m_out_trk.stop();
            m_out_trk.release();
            m_out_trk = null;
        }
        mPlayThread = null;
        mRecorderThread = null;
        if (mAudioManager != null) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    mOriginVolume,
                    AudioManager.FLAG_PLAY_SOUND);
        }
        Log.i(TAG, "stopTest .....");
    }
    protected int m_in_buf_size;
    private AudioRecord m_in_rec;
    private LinkedList<byte[]> m_in_q;
    private AudioTrack m_out_trk;
    private boolean flag = false;
    private static final int TEST_HEADSET_UP = 11;
    private static final int TEST_HEADSET_DOWN = 12;
    private static final int TEST_HEADSET_HOOK = 13;
    private SparseBooleanArray mTestKeyResult;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(!inTestKeyMode){
                return;
            }
            if (mTestKeyResult.get(msg.what)) {
                return;
            }
            switch (msg.what) {
                case TEST_HEADSET_UP:
                    mHeadsetUp.setVisibility(View.GONE);
                    mTestKeyResult.put(TEST_HEADSET_UP,true);
                    break;
                case TEST_HEADSET_DOWN:
                    mHeadsetDown.setVisibility(View.GONE);
                    mTestKeyResult.put(TEST_HEADSET_DOWN,true);
                    break;
                case TEST_HEADSET_HOOK:
                    mHeadsetHook.setVisibility(View.GONE);
                    mTestKeyResult.put(TEST_HEADSET_HOOK,true);
                    inTestKeyMode = false;
                    break;
            }
        }
    };

    @Override
    public void onKey(KeyEvent event) {
        int keyCode = event.getKeyCode();
        Log.i(TAG, "onKey: " + keyCode);
        if (!inTestKeyMode) {
            Log.i(TAG, "onKey: not in key test!");
            return;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                    if (!isAdded()) {
                        Log.i(TAG, "onKey: fragment is not attached!");
                        return;
                    }
                    if (flag) {
                        Log.i(TAG, "onKey: already in test!");
                        return;
                    }
                    if(!mTestKeyResult.get(TEST_HEADSET_UP) || !mTestKeyResult.get(TEST_HEADSET_DOWN)){
                        return;
                    }
                    mHandler.sendEmptyMessage(TEST_HEADSET_HOOK);
                    // down should start test
                    Toast.makeText(getActivity(), getString(R.string.press_hook), Toast.LENGTH_LONG).show();
                    tip.setText(getResources().getString(R.string.test_headset));
                    ((TestItemActivity) getActivity()).resetButton();
                    startTest();
                    break;
                case KeyEvent.KEYCODE_VOLUME_UP:
                    mHandler.sendEmptyMessage(TEST_HEADSET_UP);
                    break;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if(mTestKeyResult.get(TEST_HEADSET_UP)) {
                        mHandler.sendEmptyMessage(TEST_HEADSET_DOWN);
                    }
                    break;
            }
        }
    }

    class RecordSound implements Runnable {
        @Override
        public void run() {
            Log.i(TAG, "startup recorder....");
            Log.i(TAG, "thread id is " + Thread.currentThread().getId());
            m_in_rec.startRecording();
            byte[] m_in_bytes;
            while (flag) {
                try {
                    m_in_bytes = new byte[m_in_buf_size];
                    m_in_rec.read(m_in_bytes, 0, m_in_buf_size);
                    m_in_q.add(m_in_bytes);
                } catch (Exception e) {
                    Log.e(TAG, "run: ", e);
                }
            }
            Log.i(TAG, "start stop record... recorder");
            Log.i(TAG, "thread is end, id is " + Thread.currentThread().getId());
            m_in_bytes = null;
        }
    }


    class PlayRecord implements Runnable {
        @Override
        public void run() {
            Log.i(TAG, "startup player...");
            Log.i(TAG, "thread id is " + Thread.currentThread().getId());
            m_out_trk.play();
            while (flag) {
                try {
                    if (!m_in_q.isEmpty()) {
                        byte[] arrayOfByte = (byte[]) m_in_q.removeFirst();
                        if (arrayOfByte != null) {
                            m_out_trk.write(arrayOfByte, 0, arrayOfByte.length);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "run: ", e);
                }
            }
            Log.i(TAG, "start stop play... ");
            Log.i(TAG, "thread is end, id is " + Thread.currentThread().getId());
        }
    }

    private HeadsetReceiver mHeadSetReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mHeadSetReceiver == null) {
            mHeadSetReceiver = new HeadsetReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_HEADSET_PLUG);
            filter.addAction(Intent.ACTION_MEDIA_BUTTON);
            getActivity().registerReceiver(mHeadSetReceiver, filter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mAudioManager = (AudioManager) getActivity()
                .getSystemService(Context.AUDIO_SERVICE);
        name = new ComponentName(getActivity().getPackageName(),
                HeadsetReceiver.class.getName());
        if (isAdded()) {
            ((TestItemActivity) getActivity()).registerKeyListener(this);
        }
    }

    private ComponentName name;

    @Override
    public void onPause() {
        super.onPause();
        if (isAdded()) {
            ((TestItemActivity) getActivity()).unRegisterKeyListener(this);
        }
    }

    @Override
    public void onDestroy() {
        stopTest();
        if (mHeadSetReceiver != null) {
            getActivity().unregisterReceiver(mHeadSetReceiver);
            mHeadSetReceiver = null;
        }
        super.onDestroy();
    }

    private static final int MSG_NO_HEADSET = 0;
    private static final int MSG_HAS_HEADSET = 1;

    public class HeadsetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case MSG_NO_HEADSET:
                        onHeadsetNotFound(context);
                        break;
                    case MSG_HAS_HEADSET:
                        onHeadsetFound();
                        break;
                    default:
                        Log.d(TAG, "unknow");
                        break;
                }
            }
        }
    }

    private void onHeadsetFound() {
        tip.setText(getResources().getString(R.string.ready_test_headset_key));
        mKeyGroupView.setVisibility(View.VISIBLE);
        resetKeyGroup();
        inTestKeyMode = true;
        if (mTestKeyResult == null) {
            mTestKeyResult = new SparseBooleanArray();
        }
        mTestKeyResult.clear();
    }

    private void resetKeyGroup() {
        mHeadsetDown.setVisibility(View.VISIBLE);
        mHeadsetUp.setVisibility(View.VISIBLE);
        mHeadsetHook.setVisibility(View.VISIBLE);
    }

    private boolean inTestKeyMode = false;

    private void onHeadsetNotFound(Context context) {
        Toast.makeText(context, getResources().getString(R.string.plug_headset), Toast.LENGTH_LONG).show();
        tip.setText(getResources().getString(R.string.plug_headset));
        mKeyGroupView.setVisibility(View.GONE);
        ((TestItemActivity) getActivity()).disableButton(R.id.btn_next);
        stopTest();
        inTestKeyMode = false;
    }
}
