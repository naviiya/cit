package com.cit.test.runintest.pcba;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cit.test.R;
import com.cit.test.runintest.ControlButtonUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

//import org.apache.http.util.EncodingUtils;

public class StorageTest_eMMCActivity extends Activity {
	private static final String TAG = "StorageTest_eMMCActivity";
//	private static final String filePAth = "/data/media/eMMC.test";
//	private File testFile;
	private boolean flag = true;
	private TextView resultView;
	private Handler mTimeOutHandler = new Handler();
    
	private String strFilePath;
	private File fileTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        
        getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.emmctest);
        
        //strFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/eMMC_test.txt";
        strFilePath = "/mnt/sdcard/eMMC_test.txt";

        writeTestFile();
        resultView = (TextView) findViewById(R.id.resultText);
        ControlButtonUtil.initControlButtonView(this);
        findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
        findViewById(R.id.btn_Fail).setVisibility(View.INVISIBLE);
        findViewById(R.id.btn_Skip).setVisibility(View.INVISIBLE);
        readTestFile();
        mTimeOutHandler.postDelayed(mRunner, 2000);
        
    }

    public void writeTestFile() {
    	fileTest = new File( strFilePath );
		if( !fileTest.exists() ) {
			try {
				fileTest.createNewFile();
				FileWriter wr = new FileWriter( fileTest, true );
				wr.write( strFilePath );
				wr.close();
				Log.i(TAG, "write \"" + strFilePath + "\" to eMMC ---> OK");
			}
			catch ( IOException e ) {
				e.printStackTrace();
				flag =false;
			}
		}
    }
    
    public void readTestFile() {

    	try {
			FileInputStream fin = new FileInputStream(fileTest);
			byte [] buf = new byte[fin.available()];
			fin.read(buf);
//			String strRead = EncodingUtils.getString( buf, "UTF-8" );
//			Log.i(TAG, "read from eMMC: \"" + strRead + "\" ---> OK");
//			compare(EncodingUtils.getString(buf, "UTF-8"));
			fin.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			flag =false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			flag =false;
		}
    	
    }
    
    public void compare(String str) {
    	if(str.equals(strFilePath)) {
    		resultView.setText("eMMC test pass!");
    		flag =true;
    	} else {
    		resultView.setText("eMMC test fail!");
    		flag =false;
    	}
    }
    
    private Runnable mRunner = new Runnable() {
		public void run() {
			// TODO Auto-generated method stub
			TestResult(flag);
			
	}};

	public void TestResult(boolean result) {
	    if (result == true) {
	        ((Button) findViewById(R.id.btn_Pass)).performClick();
	    } else if (result == false) {
	        ((Button) findViewById(R.id.btn_Fail)).performClick();
	    }
	}    

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(fileTest.exists()) {
        	fileTest.delete();
        }
        
    }   
    
    public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}

}
