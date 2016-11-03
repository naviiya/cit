package com.cit.test.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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
        int screenHeight = dm.heightPixels;
        BitmapFactory.Options ops = new BitmapFactory.Options();
        ops.outWidth = screenWidth;
//        ops.outHeight = screenHeight;
        Bitmap bitmap = BitmapFactory.decodeFile(path, ops);
        bitmap = rotateBitmapByDegree(bitmap,-90);
        photo.setImageBitmap(bitmap);
    }
    private Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                    bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }
}
