package com.cit.test.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;


public class InterceptTouchView extends RelativeLayout {
    public InterceptTouchView(Context context) {
        this(context, null);
    }

    public InterceptTouchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InterceptTouchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


}
