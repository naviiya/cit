package com.cit.test.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cit.test.R;
import com.cit.test.TestItemActivity;

/**
 *
 */
public class CameraPhotoTestFragment extends Fragment{

    private static final String TAG = "CameraPhotoTestFragment";
    private ImageView photo;
    private String path;
    private View v;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        path = arguments.getString("path");
        Log.i(TAG, "onCreate: path = " + path);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.camera_photo_test,container,false);
        photo = (ImageView) v.findViewById(R.id.camera_photo);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((TestItemActivity)getActivity()).hideStatusBarAndNavigationBar();
        loadImg();
    }


    private void loadImg() {
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        BitmapFactory.Options ops = new BitmapFactory.Options();
        ops.outWidth = screenWidth;
        Bitmap bitmap = BitmapFactory.decodeFile(path, ops);
        photo.setImageBitmap(bitmap);
    }
}
