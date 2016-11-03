package com.cit.test.runintest.pcba;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cit.test.R;
import com.cit.test.runintest.ControlButtonUtil;
import com.cit.test.runintest.DeviceTest;
import com.cit.test.runintest.FileUtility;
import com.cit.test.runintest.TestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

/* CameraTest_FrontActivity
 * 1. Request
 *    1) Photograph and check  picture resolution (5MP) 2048x1536
 *    2) automatic judgement
 *    3) auto generation log
 * 2. Program
 * 
 * 3. Ref:
 *   http://developer.android.com/reference/android/hardware/Camera.html
 */


public class CameraTest_RearActivity extends Activity implements Callback {
	private final static String TAG = "CameraTest_RearActivity";
	private SurfaceView surfaceView;
	private ImageView imageView;
	private TextView resultView;
	private Camera camera;
	private SurfaceHolder mSurfaceHolder;
//	private String picturePath = "/data/data/com.FtTest/picture_rear.jpg";
	private String picturePath = "/mnt/sdcard/camera_rear.jpg";
	private File pictureFile;
	private final static int pictureWidth = 1600; ///(T10TC) Modify by lsg 
	private final static int pictureHeight = 1200; //(T10TC) Modify by lsg 
	private boolean flag = true;
	private Handler mTimeOutHandler = new Handler();
	private int cameraNum = 0;
	private int testType = 1;

	private boolean mResult = false;
	private int m_iVideo = -1;
	
	private Button mBtnSnapshot;
	private Button mBtnSnapshotPic;
	private Button mBtnPreview;
	
	public static final int TYPE_AUTOTEST[] = {DeviceTest.TESTCASE_TYPE_PCBA, DeviceTest.TESTCASE_TYPE_RUNIN};
	public static final int TYPE_SNAPSHOT[] = {DeviceTest.TESTCASE_TYPE_FT, DeviceTest.TESTCASE_TYPE_OTHER};
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.cameratest_rear);
		ControlButtonUtil.initControlButtonView(this);
		
		testType = getIntent().getIntExtra("testType", 1);
		surfaceView = (SurfaceView) findViewById(R.id.SurfaceView);
		resultView = (TextView) findViewById(R.id.resultView);
		
/*		cameraNum = Camera.getNumberOfCameras();
		if(cameraNum <= 0) {
			Log.e(TAG, cameraNum + " Cameras!");
			resultView.setText("Fail,No camera!");
			flag = false;
			mTimeOutHandler.postDelayed(mRunner, 1000);
		} else {
			Log.i(TAG, cameraNum + " Cameras!");
			mSurfaceHolder = surfaceView.getHolder();
			mSurfaceHolder.addCallback(this);
			mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
*/		
		mBtnSnapshot = (Button) findViewById(R.id.btnSnapshot);
		mBtnSnapshot.setOnClickListener( new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.i(TAG, "mBtnSnapshot.onClick");
				Log.i( TAG, "[Rear] onClick: camera.takePicture(null, null, null, jpegCallback);   ---> camera: " + camera );
				if ( camera != null ) {
					Camera.Parameters parameters = camera.getParameters();
					//parameters.setPictureSize(pictureWidth, pictureHeight);
					parameters.setPictureFormat(ImageFormat.JPEG);
					camera.setParameters(parameters);
					camera.takePicture(null, null, null, jpegCallback);
				}
			}
		});
		mBtnSnapshotPic = (Button) findViewById(R.id.btnSnapshotPic);
		mBtnSnapshotPic.setOnClickListener( new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.i(TAG, "mBtnSnapshotPic.onClick");
				Log.i( TAG, "[Rear] onClick: camera.takePicture(null, null, null, jpegCallbackNotPreview);   ---> camera: " + camera );
				if ( camera != null ) {
					Camera.Parameters parameters = camera.getParameters();
					//parameters.setPictureSize(pictureWidth, pictureHeight);
					parameters.setPictureFormat(ImageFormat.JPEG);
					camera.setParameters(parameters);
					camera.takePicture(null, null, null, jpegCallbackNotPreview);
				}
			}
		});
		mBtnPreview = (Button) findViewById(R.id.btnPreview);
		mBtnPreview.setOnClickListener( new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.i(TAG, "mBtnPreview.onClick");
				//Log.i( TAG, "[Front] onClick: camera.takePicture(null, null, null, jpegCallback);   ---> camera: " + camera );
				if ( camera != null ) {
					camera.startPreview();
				}
			}
		});
		
		findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
	    findViewById(R.id.btn_Fail).setVisibility(View.INVISIBLE);
	    findViewById(R.id.btn_Skip).setVisibility(View.INVISIBLE);
		//hehg
		if ( (m_iVideo = checkFrontCamera( BACK_CAMERA )) < 0 ) {
			flag = false;
			resultView.setText( "ERROR: Not found the Back Camera!!!" );
			Log.i(TAG, "Not found the back camera!");
			
			mResult = false;
			setResult( (mResult ? TestCase.RESULT.OK.ordinal() : TestCase.RESULT.NG.ordinal()) );
			mTimeOutHandler.postDelayed( new Runnable() {
				public void run() {
					// TODO Auto-generated method stub
					finish();
				}
			}, 1000 );
		}
		else {
			Log.i(TAG, "Found the back camera!");
			mSurfaceHolder = surfaceView.getHolder();
			mSurfaceHolder.addCallback(this);
			//mSurfaceHolder.setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS );
		}
		
		findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
	    findViewById(R.id.btn_Fail).setVisibility(View.INVISIBLE);
	    findViewById(R.id.btn_Skip).setVisibility(View.INVISIBLE);
	}		
	
	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, "onPause");
		if(camera != null) {
			camera.stopPreview();
			camera.release();
		}
		mSurfaceHolder = null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();		
		Log.i(TAG, "onDestroy");
		//if(pictureFile.exists()) {
		//	pictureFile.delete();
		//}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		Log.i(TAG, "surfaceChanged");
		if (holder.getSurface() == null) {
            Log.d(TAG, "---- surfaceChanged(),  holder.getSurface() == null");
            return;
        }
		//mSurfaceHolder = holder;
		
		try {
			//camera = Camera.open(0); // t7h
			camera = Camera.open( m_iVideo );
			Camera.Parameters parameters = camera.getParameters();
			List<String> colorEffects = parameters.getSupportedColorEffects();
			Iterator<String> colorItor = colorEffects.iterator();
			while(colorItor.hasNext())
			{  
				String currColor = colorItor.next();
				Log.i(TAG, "The supported color is "+currColor);
			 }  
			String strColor = parameters.getColorEffect();
			Log.i(TAG, "The current color is "+strColor);
			parameters.setPictureSize(pictureWidth, pictureHeight);
			parameters.setPictureFormat(ImageFormat.JPEG);
			camera.setParameters(parameters);
		} catch (Exception e) {
			Log.e(TAG, "Can't open the camera!");
			e.printStackTrace();
		}
		
		try {
			camera.setPreviewDisplay(mSurfaceHolder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		camera.startPreview();
		//if(testType == 1 || testType == 2) {
		if ( testType == DeviceTest.TESTCASE_TYPE_PCBA || testType == DeviceTest.TESTCASE_TYPE_RUNIN ) { //hehg
			resultView.setText("Camera is OK!");
			mTimeOutHandler.postDelayed(mRunner, 1000);
			return;
		}		
		
		//mTimeOutHandler.postDelayed(mRunner2, 1000);
	for ( int i = 0; i < TYPE_SNAPSHOT.length; i ++ ) {
		if ( testType == TYPE_SNAPSHOT[i] ) {
			mBtnSnapshot.performClick();
		}
	}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.i(TAG, "surfaceDestroyed");
	}

	public void pictureIsVGA(byte[] data) {
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		Log.i( TAG, "[Rear] picture size: " + bitmap.getWidth() + "x" + bitmap.getHeight() );
		if(bitmap.getWidth() == pictureWidth && bitmap.getHeight() == pictureHeight){
			resultView.setText("PASS! The size of picture is " + pictureWidth + "x" + pictureHeight);
			resultView.setTextColor( Color.GREEN );
			flag = true;
			//mTimeOutHandler.postDelayed(mRunner, 1000);
			findViewById(R.id.btn_Pass).setVisibility(View.VISIBLE);
		    findViewById(R.id.btn_Fail).setVisibility(View.VISIBLE);
		    //findViewById(R.id.btn_Skip).setVisibility(View.VISIBLE);
			
		} else {
			resultView.setText("FAIL! The size of picture isn't " + pictureWidth + "x" + pictureHeight);
			resultView.setTextColor( Color.RED );
			flag = false;
			//mTimeOutHandler.postDelayed(mRunner, 1000);
			findViewById(R.id.btn_Pass).setVisibility(View.VISIBLE);
		    findViewById(R.id.btn_Fail).setVisibility(View.VISIBLE);
		    //findViewById(R.id.btn_Skip).setVisibility(View.VISIBLE);
		}
		
	}
	
	private PictureCallback jpegCallbackNotPreview = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			pictureFile = new File(picturePath);

			if(!pictureFile.exists()) {
				try {
					pictureFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pictureIsVGA(data);

			//camera.startPreview(); 
		}
	};
	private PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			pictureFile = new File(picturePath);
/*			if(!pictureFile.exists()) {
				try {
					pictureFile.createNewFile();
					FileOutputStream fos = new FileOutputStream(pictureFile);
					fos.write(data);
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
*/
			if(!pictureFile.exists()) {
				try {
					pictureFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
				try {
					FileOutputStream fos = new FileOutputStream(pictureFile);
					fos.write(data);
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			pictureIsVGA(data);
			camera.startPreview(); 
		}
	};
	
	private Runnable mRunner = new Runnable() {
		public void run() {
			// TODO Auto-generated method stub
			Log.i(TAG, "mRunner");
			if(flag) {
				TestResult(true);
			} else {
				TestResult(false);
			}
			
	}};
	
	private Runnable mRunner2 = new Runnable() {
		public void run() {
			// TODO Auto-generated method stub
			camera.takePicture(null, null, null, jpegCallback);
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

	/* Intel BayTrail
	shell@byt_t_crv2:/sys/class/video4linux $ cat v4l-subdev0/name
	cat v4l-subdev0/name
	bf3a20 2-006f
	shell@byt_t_crv2:/sys/class/video4linux $ cat v4l-subdev1/name
	cat v4l-subdev1/name
	bf3905 2-006e
	shell@byt_t_crv2:/sys/class/video4linux $ cat v4l-subdev2/name
	cat v4l-subdev2/name
	ATOM ISP CSI2-port0
	shell@byt_t_crv2:/sys/class/video4linux $ cat v4l-subdev1/index
	cat v4l-subdev1/index
	10
	shell@byt_t_crv2:/sys/class/video4linux $ cat v4l-subdev0/index
	cat v4l-subdev0/index
	9
	 */
	public final static String SYSFS_V4L = "/sys/class/video4linux/v4l-subdev";
	public final static String FRONT_CAMERA = "ov2680";     //Modify by lsg
	public final static String BACK_CAMERA = "ov5693";
	
	public int checkFrontCamera( String cam ) {
		int ret = -1;
		String path = "";
		String fname = "";
		String name = "";
		
		for ( int i = 0; i < 16; i ++ ) {
			//fname = String.format( "%s%d", SYSFS_V4L, i );
			path = String.format( Locale.getDefault(), "%s%d", SYSFS_V4L, i );
			Log.i( TAG, "[" + i + "] " + path );
			File f = new File(path);
			if ( f.exists() ) {
				fname = path + "/name";
				name = FileUtility.getSysfsFile(fname);
				Log.i( TAG, "name: " + name );
				if ( name.contains( cam ) ) {
					ret = i;
					Log.i( TAG, "Found the camera \"" + cam + "\" (ret=" + i + ")" );
					break;
				}
			}
		}
		
		return ret;
	}

}














