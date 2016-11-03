package com.cit.test.runintest.pcba;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cit.test.R;
import com.cit.test.runintest.ControlButtonUtil;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

public class StorageTest_DDRActivity extends Activity {
	private static final String TAG = "StorageTest_DDRActivity";
	private boolean flag = true;
	private TextView resultView;
	private char[] test;
	private Handler mTimeOutHandler = new Handler();
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.ddrtest);
        
        resultView = (TextView) findViewById(R.id.resultText);
        ControlButtonUtil.initControlButtonView(this);
        
        test = new char[512*1024];
        for(int i = 0; i < 512*1024; i++) {
        	test[i] = 'a';
         }
        for(int i = 0; i < 512*1024; i++) {
        	if(test[i] != 'a') {
        		flag = false;
        		resultView.setText("DDR test fail!");
        		break;
        	}
        	if(i == 512*1024-1) {
        		flag = true;
        		resultView.setText("DDR test pass!");
        	}
         }
        mTimeOutHandler.postDelayed(mRunner, 2000);
        
        findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
        findViewById(R.id.btn_Fail).setVisibility(View.INVISIBLE);
        findViewById(R.id.btn_Skip).setVisibility(View.INVISIBLE);
        
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
	
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
	
}
