package com.cit.test.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cit.test.R;
import com.cit.test.helper.Recorder;
import com.cit.test.helper.VUMeter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 */

public class MainAudioTestFragment extends Fragment {

    public static final String DATA_PATH = "/data/data/com.FtTest/";
    public static final String RECORD_DATAFILE = DATA_PATH + "recordmic";
    private static final String TAG = "MainAudioTestFragment";
    // hehg
    private int mIndex = 0;
//    private int[] mBtnID = new int[] {R.id.b1, R.id.b2,
//            R.id.b3, R.id.b4,
//            R.id.b5, R.id.b6};
    private int[] mMusic = new int[] {R.raw.s1, R.raw.s2,
            R.raw.s3, R.raw.s4,
            R.raw.s5, R.raw.s6};
    /*	private int mBtnID[] = {R.id.b1, R.id.b2,
                R.id.b3, R.id.b4,
                R.id.b5, R.id.b6};
        private int mMusic[] = {R.raw.s1, R.raw.s2,
                R.raw.s3, R.raw.s4,
                R.raw.s5, R.raw.s6};
    */
    private final static int RECORD_SECOND = 6;
    private int mSec = 0;
    private TextView mtextviewSec;

    private int mflagPluginDlgShow = 0;
    private HeadsetPlugThread mheadsetPlugThread = null;
    private boolean mflagHeadsetPlugThreadRunning = true;

    //	private BroadcastReceiver mHeadsetPlugReceiver;
    private boolean mflagMicRecordStart = false;

    private AlertDialog mAlertDlg = null;


    private final static String ERRMSG = "Record error";

    private static final int MSG_TEST_MIC_START = 1;
    private static final int MSG_TEST_MIC_ING = 2;
    private static final int MSG_TEST_MIC_OVER = 3;
    private static final int MSG_TEST_MIC_HEADSET_PLUGIN = 4;
    private static final int R_PASS = 0;

    private boolean isSDcardTestOk = false;

    private AudioManager mAudioManager;
    private Handler mHandler;

    private int mOldVolume;
    private Recorder mRecorder;
    //	private TextView mResult;
    boolean mSpeakerOn = false;
    private TextView mText;

    TextView mTitle;
    private VUMeter mVUMeter;
    private static boolean headphoneflag= true;
    private TextView mTextView01 = null;
    private TextView mTextView02 = null;
    AudioRecord audioRecord;
    AudioTrack audioTrack;
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private Button mSound1;
    private Button mSound2;
    private Button mSound3;
    private Button mSound4;
    private Button mSound5;
    private Button mSound6;
    private Button mSound7;
    private MediaPlayer mAudioPlayer;

    private Handler mTimeOutHandler = new Handler();

    private Runnable mRunner = new Runnable() {
        public void run() {
            return_result( true );
        }
    };

    private Runnable mmRunner = new Runnable() {
        public void run() {
//            ((Button)findViewById(R.id.btn_Fail)).performClick();
        }
    };
    private View v;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHandler = new MyHandler();
    }

    private void return_result(boolean status ) {
//        if ( status ) {
//            ((Button) findViewById(R.id.btn_Pass)).performClick();
//        }
//        else {
//            ((Button) findViewById(R.id.btn_Fail)).performClick();
//        }
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );


        mRecorder = new Recorder();

        mAudioManager = (AudioManager) getActivity().getSystemService( Context.AUDIO_SERVICE );
        mOldVolume = mAudioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
        int maxVolume = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
        mAudioManager.setStreamVolume( AudioManager.STREAM_MUSIC, maxVolume, 0 );
        double tmp = Math.random();
        mIndex = Integer.valueOf( (int)(tmp*100) ) % 6; // total 6 audio files.
        Log.v(TAG, "Audio index: " + mIndex );



//        TextView mmheadcheck =(TextView)findViewById(R.id.textSubTitle);
//        mmheadcheck.setText(getString(R.string.PhoneMicSubTitle));

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.main_audio_test,container,false);
//        mVUMeter = (VUMeter) v.findViewById(R.id.uvMeter);
//        mVUMeter.setRecorder(mRecorder);
//        mtextviewSec = (TextView) v.findViewById( R.id.record_second );

//        ControlButtonUtil.initControlButtonView(this);

//        v.findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
//        v.findViewById(R.id.btn_Fail).setVisibility(View.INVISIBLE);
//        v.findViewById(R.id.btn_Skip).setVisibility(View.INVISIBLE);

//        mTextView01 = (TextView)v.findViewById(R.id.show);

        //hehg
//        for ( int i = 0; i < mBtnID.length; i ++ ) {
//            Button b = (Button) v.findViewById( mBtnID[i] );
//            b.setEnabled(false);
//            b.setOnClickListener( new Button.OnClickListener() {
//                public void onClick( View v ) {
//                    if ( v.getId() == mBtnID[ mIndex ] ) {
//                        return_result( true );
//                    }
//                    else
//                        return_result( false );
//                }
//            });
//        }
//        mSound7 = (Button) v.findViewById(R.id.b7);
//        mSound7.setEnabled(false);
//
//        mSound7.setOnClickListener(new Button.OnClickListener(){
//
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                return_result(false);
//            }
//        });
//        mheadsetPlugThread = new HeadsetPlugThread();
//        mheadsetPlugThread.start();
        return v;
    }

    @Override
    public void onDestroy() {
//        mTimeOutHandler.removeCallbacks(mRunner);
        //mAudioPlayer.release();

//        mAudioManager.setStreamVolume( AudioManager.STREAM_MUSIC, mOldVolume, 0 );
//        if ( mheadsetPlugThread != null ) {
//            mheadsetPlugThread.exit();
//        }
        super.onDestroy();
    }


    class MyHandler extends Handler {
        @Override
        public void handleMessage( Message msg ) {
            switch ( msg.what ) {
                default:
                case MSG_TEST_MIC_START:
                    removeMessages( MSG_TEST_MIC_START );

                    if ( mflagPluginDlgShow == 1 ) {
                        mAlertDlg.getButton( DialogInterface.BUTTON_POSITIVE ).performClick();
                    }

                    mflagMicRecordStart = true;
                    mSec = RECORD_SECOND;
                    mtextviewSec.setText( "Recording " + mSec + " " );
                    mRecorder.startRecording( MediaRecorder.OutputFormat.THREE_GPP, ".3gpp" );
                    sendEmptyMessageDelayed( MSG_TEST_MIC_ING, 1000L );
                    break;

                case MSG_TEST_MIC_ING:
                    if ( mSec > 0 ) {
                        mtextviewSec.setText( "Recording " + mSec + " " );
                        mSec --;
                        sendEmptyMessageDelayed( MSG_TEST_MIC_ING, 1000L );
                    }
                    else {
                        removeMessages( MSG_TEST_MIC_ING );
                        sendEmptyMessage( MSG_TEST_MIC_OVER );
                    }
                    break;
                case MSG_TEST_MIC_OVER:
                    removeMessages( MSG_TEST_MIC_OVER );
                    mRecorder.stopRecording();
                    if ( mRecorder.sampleLength() > 0 ) {
                        if ( mRecorder.getDB() < 20 ) {
                            mtextviewSec.setText( R.string.audiotest_internalmic_record_err_lt_20db );
                            //TestResult( false );
                            return_result( false );
                            Log.v( TAG, "This record less than 20db!" );
                        }
                        else {
                            mtextviewSec.setText( R.string.audiotest_internalmic_record_ok );
                            mRecorder.startPlayback();

                            sendEmptyMessageDelayed( R_PASS, 8500L ); // delay 8.5s for playback.
                        }
                    }
                    else {
                        mtextviewSec.setText( R.string.audiotest_internalmic_record_err );
                        Log.e(TAG, "This record error!!!");

                        return_result( false );
                    }
                    break;

                case R_PASS:
                    mtextviewSec.setText( R.string.audiotest_internalmic_play_digit );
                    audioplayerInit();
                    audioplayerStart();
                    break;

                case MSG_TEST_MIC_HEADSET_PLUGIN:
                    promptHeadsetPlugin();
                    break;
            }

            mVUMeter.invalidate();
        }
    }

    //hehg
    private void audioplayerInit() {
//        mAudioPlayer = MediaPlayer.create(getActivity(), mMusic[mIndex]);
//        mAudioPlayer.setOnCompletionListener( new MediaPlayer.OnCompletionListener() {
//            public void onCompletion( MediaPlayer mediaplayer ) {
//                for ( int i = 0; i < mBtnID.length; i ++ ) {
//                    ((Button)v.findViewById( mBtnID[ i ] )).setEnabled(true);
//                }
//                mSound7.setEnabled(true);
//            }
//        });
    }
    private void audioplayerStart() {
        mAudioPlayer.start();
    }

    /**
     * Prompt the user if the headset with mic is plugin
     */
    private void promptHeadsetPlugin() {
        mAlertDlg = new AlertDialog.Builder(getActivity()).setTitle(
                R.string.audiotest_internalmic_dlg_headsetplugin_title).setMessage(
                R.string.audiotest_internalmic_dlg_headsetplugin_info).setPositiveButton(
                android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                        mflagPluginDlgShow = 0;
                    }
                }).create();
        mAlertDlg.show();

    }


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
                    if ( state == 1 ) { // Headset with Mic
                        // 提示用户拔出耳机
                        if ( mflagPluginDlgShow == 0 ) {
                            mflagPluginDlgShow = 1;

                            mHandler.sendEmptyMessage( MSG_TEST_MIC_HEADSET_PLUGIN );
                            Log.i(TAG, "Headset with Mic is pluged!!!");
                        }
                    }
                    else if ( state == 2 ) { // Headset without Mic
                        //if ( mflagPluginDlgShow == 0 && mflagMicRecordStart == false  )
                        if (!mflagMicRecordStart)
                            mHandler.sendEmptyMessage( MSG_TEST_MIC_START );

                    }
                    else if ( state == 0 ) { // no Headset
                        //if ( mflagPluginDlgShow == 0 && mflagMicRecordStart == false )
                        if (!mflagMicRecordStart)
                            mHandler.sendEmptyMessage( MSG_TEST_MIC_START );

                    }
                    Thread.sleep(100);
                }
                catch( FileNotFoundException e ) {
                    e.printStackTrace();
                }
                catch (IOException e ) {
                    e.printStackTrace();
                }
                catch ( InterruptedException e ) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void TestResult11(boolean result) {
        if (result == true) {
            ((Button) v.findViewById(R.id.btn_Pass)).performClick();
        } else if (result == false) {
            ((Button)v.findViewById(R.id.btn_Pass)).performClick();
        }
    }


}
