package com.cit.test.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cit.test.R;
import com.cit.test.TestItemActivity;
import com.cit.test.Utils;
import com.cit.test.view.CameraPreview;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

/**
 *
 */
public class CameraTestFragment extends Fragment implements View.OnTouchListener {

    private static final String TAG = "CameraTestFragment";
    private View v;
    private TextView openCamera;
    private SurfaceHolder mSurfaceViewHolder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.camera_test,container,false);
        mSurfaceView = (SurfaceView) v.findViewById(R.id.surfaceView);
        mSurfaceViewHolder = mSurfaceView.getHolder();
        mSurfaceViewHolder.addCallback(new MySurfaceViewCallback());
        mSurfaceViewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceViewHolder.setKeepScreenOn(true);
        openCamera = (TextView) v.findViewById(R.id.open_camera);
        focusIndex = v.findViewById(R.id.focus_index);
        v.setOnTouchListener(this);
        ((TestItemActivity)getActivity()).disableNextButton();
        return v;
    }
    public static final String CAMERA_PATH_VALUE1 = "PHOTO_PATH";
    public static final String CAMERA_PATH_VALUE2 = "PATH";
    public static final String CAMERA_TYPE = "CAMERA_TYPE";
    public static final String CAMERA_RETURN_PATH = "return_path";

    private int PHOTO_SIZE_W = 2000;
    private int PHOTO_SIZE_H = 2000;
    public static final int CAMERA_TYPE_1 = 1;
    public static final int CAMERA_TYPE_2 = 2;
    private final int PROCESS = 1;
    private CameraPreview preview;
    private Camera camera;
    private View focusIndex;
    private int mCurrentCameraId = 0; // 1是前置 0是后置
    private SurfaceView mSurfaceView;

    private Camera optimizeCameraConfig(Camera camera) {
        // 设置camera预览的角度，因为默认图片是倾斜90度的
        // mCamera.setDisplayOrientation(90);

        int previewWidth = 0;
        int previewHeight = 0;
        WindowManager wm = (WindowManager)getActivity().getSystemService(Context.WINDOW_SERVICE);// 获取窗口的管理器
        Display display = wm.getDefaultDisplay();// 获得窗口里面的屏幕
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        Camera.Parameters parameters = camera.getParameters();
        // parameters.setFlashMode(Parameters.FLASH_MODE_TORCH); //开启闪光灯,支持
        setDisplay(parameters, camera);
        // parameters.setRotation(90);
        // parameters.setPreviewFrameRate(3);// 每秒3帧 每秒从摄像头里面获得3个画面,
        // 某些机型（红米note2）不支持
        parameters.setPictureFormat(PixelFormat.JPEG);// 设置照片输出的格式
        parameters.set("jpeg-quality", 100);// 设置照片质量
        try {
            // 选择合适的预览尺寸
            List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
            Log.i(TAG, "optimizeCameraConfig: 合适的预览尺寸个数为：" + sizeList.size());
            for (int i = 0; i < sizeList.size(); i++) {
                Log.i(TAG, "合适的预览尺寸" + i + " ： width = " + sizeList.get(i).width
                + " height = " + sizeList.get(i).height);
            }

            if(sizeList.size() > 1){
                for (int i = 0; i < sizeList.size(); i++) {
                    Camera.Size cur = sizeList.get(i);
                    int w = cur.width;
                    int h = cur.height;
                    if (cur.width >= previewWidth
                            && cur.height >= previewHeight) {
                        previewWidth = cur.width;
                        previewHeight = cur.height;
                        break;
                    }
                }
            }
            parameters.setPreviewSize(previewWidth, previewHeight); // 获得摄像区域的大小
            parameters.setPictureSize(previewWidth, previewHeight); // 获得保存图片的大小
            // parameters.setPreviewSize(display.getWidth(),
            // display.getWidth()); // 获得摄像区域的大小
            // parameters.setPictureSize(display.getWidth(),
            // display.getWidth());// 设置拍出来的屏幕大小

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        try {
            camera.setPreviewDisplay(mSurfaceViewHolder);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        camera.setParameters(parameters);// 把上面的设置 赋给摄像头
        camera.startPreview();// 开始预览
//        camera.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上
        inPreviewMode = true;
        return camera;
    }
    // 控制图像的正确显示方向
    private void setDisplay(Camera.Parameters parameters, Camera camera) {
        if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
            camera.setDisplayOrientation(90);
        } else {
            parameters.setRotation(90);
        }

    }

    private class MySurfaceViewCallback implements SurfaceHolder.Callback{

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if(camera != null){
                camera.stopPreview();
                camera.release();
                camera = null;
            }
            try {
                camera = Camera.open();
            }catch (Exception e){
                Log.e(TAG, "surfaceCreated: ", e);
            }
            //设置camera参数并且开启预览模式
            camera = optimizeCameraConfig(camera);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.i(TAG, "surfaceChanged: ");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if(camera != null){
                if(inPreviewMode){
                    camera.stopPreview();
                    inPreviewMode = false;
                }
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        }
    }



    private static final int SHOW_DIALOG = 1010;
    private static final int DISMISS_DIALOG = 1011;


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SHOW_DIALOG:
                    showDialog();
                    break;
                case DISMISS_DIALOG:
                    dismissDialog();
            }
        }
    };

    private void showDialog() {
        if(mAlertDialog == null){
            AlertDialog.Builder  builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getString(R.string.loading));
            mAlertDialog = builder.create();
            mAlertDialog.setCanceledOnTouchOutside(false);
            mAlertDialog.show();
        }
    }

    private void dismissDialog() {
        if(mAlertDialog != null){
            mAlertDialog.dismiss();
        }
    }

    public void takePhoto() {
        try {
            camera.takePicture(shutterCallback, rawCallback, jpegCallback);
        } catch (Throwable t) {
            t.printStackTrace();
            Toast.makeText(getActivity(), getString(R.string.takephoto_error), Toast.LENGTH_LONG)
                    .show();
            try {
                camera.startPreview();
            } catch (Throwable e) {
                Log.e(TAG, "takePhoto: ", e);
            }
        }
    }


    public static final String ACTION_GLXSS_DEVICE_ATTACHED = UsbManager.ACTION_MRDEVICE_ATTACHED;
    public static final String ACTION_GLXSS_DEVICE_DETACHED = UsbManager.ACTION_MRDEVICE_DETACHED;
    @Override
    public void onResume() {
        super.onResume();
        ((TestItemActivity)getActivity()).hideStatusBarAndNavigationBar();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_GLXSS_DEVICE_ATTACHED);
        filter.addAction(ACTION_GLXSS_DEVICE_DETACHED);
        mReceiver = new GlxssReceiver();
        getActivity().registerReceiver(mReceiver,filter);
        if(!Utils.isGlxssConnect(getActivity())){
            openCamera.setText(getString(R.string.glxss_not_found));
            Toast.makeText(getActivity(), getResources().getString(R.string.glxss_not_found), Toast.LENGTH_LONG).show();
            return;
        }
        startCamera();
    }

    private boolean inPreviewMode;
    private void startCamera() {
        Log.i(TAG, "startCamera: ");
        mSurfaceView.setVisibility(View.VISIBLE);
        openCamera.setVisibility(View.GONE);
        ((TestItemActivity)getActivity()).resetButton();
    }


    private GlxssReceiver mReceiver;
    private class GlxssReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(ACTION_GLXSS_DEVICE_ATTACHED)){
                Log.i(TAG, "onReceive: " + " attach ");
                Toast.makeText(getActivity(), getResources().getString(R.string.glxss_connected), Toast.LENGTH_LONG).show();
                startCamera();
            }else if(action.equals(ACTION_GLXSS_DEVICE_DETACHED)){
                Log.i(TAG, "onReceive: detach");
                stopCamera();
                Toast.makeText(getActivity(), getResources().getString(R.string.insert_glxss), Toast.LENGTH_LONG).show();
                mSurfaceView.setVisibility(View.GONE);
                openCamera.setVisibility(View.VISIBLE);
                ((TestItemActivity)getActivity()).disableNextButton();
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        stopCamera();
        getActivity().unregisterReceiver(mReceiver);
    }

    private void stopCamera() {
        mSurfaceView.setVisibility(View.GONE);
        openCamera.setVisibility(View.VISIBLE);
        Log.i(TAG, "stopCamera: ");
    }


    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
        }
    };


    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
        }
    };


    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {

            new SaveImageTask(data).execute();
        }
    };


    @Override
    public boolean onTouch(View v, MotionEvent event) {
//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//                preview.pointFocus(event);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if(inPreviewMode) {
//            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(
//                    focusIndex.getLayoutParams());
//            layout.setMargins((int) event.getX() - 60, (int) event.getY() - 60, 0, 0);
//
//            focusIndex.setLayoutParams(layout);
//            focusIndex.setVisibility(View.VISIBLE);
//
//            ScaleAnimation sa = new ScaleAnimation(3f, 1f, 3f, 1f,
//                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
//                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
//            sa.setDuration(800);
//            focusIndex.startAnimation(sa);
//            handler.postAtTime(new Runnable() {
//                @Override
//                public void run() {
//                    focusIndex.setVisibility(View.INVISIBLE);
//                }
//            }, 800);
//        }
        return false;
    }


    private static String getCameraPath() {
        Calendar calendar = Calendar.getInstance();
        StringBuilder sb = new StringBuilder();
        sb.append("IMG");
        sb.append(calendar.get(Calendar.YEAR));
        int month = calendar.get(Calendar.MONTH) + 1; // 0~11
        sb.append(month < 10 ? "0" + month : month);
        int day = calendar.get(Calendar.DATE);
        sb.append(day < 10 ? "0" + day : day);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        sb.append(hour < 10 ? "0" + hour : hour);
        int minute = calendar.get(Calendar.MINUTE);
        sb.append(minute < 10 ? "0" + minute : minute);
        int second = calendar.get(Calendar.SECOND);
        sb.append(second < 10 ? "0" + second : second);
        if (!new File(sb.toString() + ".jpg").exists()) {
            return sb.toString() + ".jpg";
        }

        StringBuilder tmpSb = new StringBuilder(sb);
        int indexStart = sb.length();
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            tmpSb.append('(');
            tmpSb.append(i);
            tmpSb.append(')');
            tmpSb.append(".jpg");
            if (!new File(tmpSb.toString()).exists()) {
                break;
            }

            tmpSb.delete(indexStart, tmpSb.length());
        }

        return tmpSb.toString();
    }



    //处理拍摄的照片
    private class SaveImageTask extends AsyncTask<Void, Void, String> {
        private byte[] data;

        SaveImageTask(byte[] data) {
            this.data = data;
        }

        @Override
        protected String doInBackground(Void... params) {
            // Write to SD Card
            String path = "";
            try {

//                showProgressDialog("处理中");
                handler.sendEmptyMessage(SHOW_DIALOG);
                path = saveToSDCard(data);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            return path;
        }


        @Override
        protected void onPostExecute(String path) {
            super.onPostExecute(path);

            if (!TextUtils.isEmpty(path)) {
                Log.d("DemoLog", "path=" + path);
                handler.sendEmptyMessage(DISMISS_DIALOG);
                ((TestItemActivity)getActivity()).displayCameraPhoto(path);
//                dismissProgressDialog();
//                Intent intent = new Intent();
//                intent.setClass(CameraActivity.this, PhotoProcessActivity.class);
//                intent.putExtra(CAMERA_PATH_VALUE1, path);
//                startActivityForResult(intent, PROCESS);
            } else {
                Toast.makeText(getActivity(), getString(R.string.takephoto_error),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private AlertDialog mAlertDialog;

//    private void dismissProgressDialog() {
//        this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (mAlertDialog != null && mAlertDialog.isShowing()
//                        && !CameraActivity.this.isFinishing()) {
//                    mAlertDialog.dismiss();
//                    mAlertDialog = null;
//                }
//            }
//        });
//    }
//
//    private void showProgressDialog(final String msg) {
//        this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (mAlertDialog == null) {
//                    mAlertDialog = new GenericProgressDialog(
//                            CameraActivity.this);
//                }
//                mAlertDialog.setMessage(msg);
//                ((GenericProgressDialog) mAlertDialog)
//                        .setProgressVisiable(true);
//                mAlertDialog.setCancelable(false);
//                mAlertDialog.setOnCancelListener(null);
//                mAlertDialog.show();
//                mAlertDialog.setCanceledOnTouchOutside(false);
//            }
//        });
//    }


    /**
     * 将拍下来的照片存放在SD卡中
     */
    public String saveToSDCard(byte[] data) throws IOException {
        Bitmap croppedImage;
        // 获得图片大小
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        // PHOTO_SIZE = options.outHeight > options.outWidth ? options.outWidth
        // : options.outHeight;
        PHOTO_SIZE_W = options.outWidth;
        PHOTO_SIZE_H = options.outHeight;
        options.inJustDecodeBounds = false;
        Rect r = new Rect(0, 0, PHOTO_SIZE_W, PHOTO_SIZE_H);
        try {
            croppedImage = decodeRegionCrop(data, r);
        } catch (Exception e) {
            return null;
        }
        String imagePath = "";
        try {
            imagePath = saveToFile(croppedImage);
        } catch (Exception e) {

        }
        croppedImage.recycle();
        return imagePath;
    }



    private Bitmap decodeRegionCrop(byte[] data, Rect rect) {
        InputStream is = null;
        System.gc();
        Bitmap croppedImage = null;
        try {
            is = new ByteArrayInputStream(data);
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(is,false);
            try {
                croppedImage = decoder.decodeRegion(rect,
                        new BitmapFactory.Options());
            } catch (IllegalArgumentException e) {
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {

        }
        Matrix m = new Matrix();
        m.setRotate(90, PHOTO_SIZE_W / 2, PHOTO_SIZE_H / 2);
        if (mCurrentCameraId == 1) {
            m.postScale(1, -1);
        }
        Bitmap rotatedImage = Bitmap.createBitmap(croppedImage, 0, 0,
                PHOTO_SIZE_W, PHOTO_SIZE_H, m, true);
        if (rotatedImage != croppedImage)
            croppedImage.recycle();
        return rotatedImage;
    }



    // 保存图片文件
    public static String saveToFile(Bitmap croppedImage)
            throws FileNotFoundException, IOException {
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/DCIM/Camera/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = getCameraPath();
        File outFile = new File(dir, fileName);
        FileOutputStream outputStream = new FileOutputStream(outFile); // 文件输出流
        croppedImage.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
        outputStream.flush();
        outputStream.close();
        return outFile.getAbsolutePath();
    }


    /**
     * 闪光灯开关 开->关->自动
     *
     * @param mCamera
     */
    private void turnLight(Camera mCamera) {
        if (mCamera == null || mCamera.getParameters() == null
                || mCamera.getParameters().getSupportedFlashModes() == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        String flashMode = mCamera.getParameters().getFlashMode();
        List<String> supportedModes = mCamera.getParameters()
                .getSupportedFlashModes();
//        if (Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)
//                && supportedModes.contains(Camera.Parameters.FLASH_MODE_ON)) {// 关闭状态
//            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
//            mCamera.setParameters(parameters);
//            flashBtn.setImageResource(R.drawable.camera_flash_on);
//        } else if (Camera.Parameters.FLASH_MODE_ON.equals(flashMode)) {// 开启状态
//            if (supportedModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
//                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
//                flashBtn.setImageResource(R.drawable.camera_flash_auto);
//                mCamera.setParameters(parameters);
//            } else if (supportedModes
//                    .contains(Camera.Parameters.FLASH_MODE_OFF)) {
//                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
//                flashBtn.setImageResource(R.drawable.camera_flash_off);
//                mCamera.setParameters(parameters);
//            }
//        } else if (Camera.Parameters.FLASH_MODE_AUTO.equals(flashMode)
//                && supportedModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
//            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
//            mCamera.setParameters(parameters);
//            flashBtn.setImageResource(R.drawable.camera_flash_off);
//        }
    }


    // 切换前后置摄像头
    private void switchCamera() {
        mCurrentCameraId = (mCurrentCameraId + 1) % Camera.getNumberOfCameras();
        if (camera != null) {
            camera.stopPreview();
            preview.setCamera(null);
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
        try {
            camera = Camera.open(mCurrentCameraId);
            camera.setPreviewDisplay(mSurfaceView.getHolder());
            preview.setCamera(camera);
            camera.startPreview();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "未发现相机", Toast.LENGTH_LONG).show();
        }

    }

//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == PROCESS) {
//            if (resultCode == RESULT_OK) {
//                Intent intent = new Intent();
//                if (data != null) {
//                    intent.putExtra(CAMERA_RETURN_PATH,
//                            data.getStringExtra(CAMERA_PATH_VALUE2));
//                }
//                setResult(RESULT_OK, intent);
//            } else {
//                if (data != null) {
//                    File dir = new File(data.getStringExtra(CAMERA_PATH_VALUE2));
//                    if (dir != null) {
//                        dir.delete();
//                    }
//                }
//            }
//        }
//    }
}
