package com.cit.test.fragment;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cit.test.R;
import com.cit.test.TestItemActivity;

import junit.framework.Test;

import java.io.File;
import java.io.FileNotFoundException;

/**
 *
 */
public class CameraTestFragment extends Fragment implements View.OnTouchListener {

    private static final String TAG = "CameraTestFragment";
    private ImageView cameraBg;

    private Bitmap bmp_selectedPhoto;
    private Uri uri;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.camera_test,container,false);
        cameraBg = (ImageView) v.findViewById(R.id.camera_img);
        v.setOnTouchListener(this);
        ((TestItemActivity)getActivity()).disableNextButton();
        return v;
    }

    private static final int CAMERA_WITH_DATA = 0;

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

            File appDir = new File(Environment.getExternalStorageDirectory()
                    + "/tmp");

            if (!appDir.exists()) {
                appDir.mkdir();
            }

            uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory()
                    + "/tmp/", "pic"
                    + String.valueOf(System.currentTimeMillis()) + ".jpg"));
            cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);

            startActivityForResult(cameraIntent, CAMERA_WITH_DATA);
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_WITH_DATA){
            cameraBg.setVisibility(View.VISIBLE);
            ContentResolver cr = getActivity().getContentResolver();
            try {
                if (bmp_selectedPhoto != null)
                    bmp_selectedPhoto.recycle();
                bmp_selectedPhoto = BitmapFactory.decodeStream(cr
                        .openInputStream(uri));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Log.i(TAG, "onActivityResult: " + uri);
            cameraBg.setBackground(new BitmapDrawable(getResources(),
                    bmp_selectedPhoto));
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        ((TestItemActivity)getActivity()). displayStatusBarAndNavigationBar();
    }
}
