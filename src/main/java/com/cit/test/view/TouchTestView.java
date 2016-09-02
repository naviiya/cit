package com.cit.test.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

/**
 * used for touch test
 */

public class TouchTestView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private static final String TAG = "TouchTestView";
    private Paint mPaint;
    private Rect mRect;
    private final Path mPath = new Path();
    private final Path mRectPath = new Path();
    private boolean allowDraw = true;// flag if allow to draw in this view
    public TouchTestView(Context context) {
        this(context, null);
    }

    public TouchTestView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TouchTestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initConfig(context);
    }

    public boolean isAllowDraw() {
        return allowDraw;
    }

    public void setAllowDraw(boolean allowDraw) {
        this.allowDraw = allowDraw;
    }

    private void initConfig(Context context) {
        mHolder = getHolder();
        mHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setKeepScreenOn(true);

        final float density = getResources().getDisplayMetrics().density;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(3f);
        mRect = new Rect();

        guideBoxWidth = (int) (density * 30);
    }

    private int mWidth, mHeight;

    private int guideBoxWidth;

    private SurfaceHolder mHolder;

    private boolean mLoop = true;

    private Canvas mCanvas;

    private float mX, mY;

    @Override
    public void run() {
        while (mLoop) {
            startDraw();
            try {
                Thread.sleep(80);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private void startDraw() {
        try {
            mCanvas = mHolder.lockCanvas();
            mWidth = getMeasuredWidth();
            mHeight = getMeasuredHeight();
            Log.i(TAG, "surfaceCreated: " + mWidth + ".." + mHeight);
            // clean canvas
            clear();
            if (!needHighLightLeftTopBox) {
                mPaint.setColor(Color.parseColor("#ffffff"));
            } else {
                mPaint.setColor(Color.parseColor("#00ff00"));
            }
            // draw left-top box
            mRect.set(0, 0, guideBoxWidth, guideBoxWidth);
            mCanvas.drawRect(mRect, mPaint);
            if(!allowDraw)return;
            if (!outOfBounds) {
                if (mStep == 1) {
                    // draw left box
                    if (needDisplayLeftBox) {
                        mPaint.setColor(needHighLightLeftBox ? Color.parseColor("#00ff00") :
                                Color.parseColor("#ffffff"));
                        mRect.set(0, guideBoxWidth, guideBoxWidth, mHeight - guideBoxWidth);
                        mCanvas.drawRect(mRect, mPaint);
                    }
                    // draw left-bottom box
                    if (needDisplayLBBox) {
                        mPaint.setColor(needHighLightLBBox ? Color.parseColor("#00ff00") :
                                Color.parseColor("#ffffff"));
                        mRect.set(0, mHeight - guideBoxWidth, guideBoxWidth, mHeight);
                        mCanvas.drawRect(mRect, mPaint);
                    }

                    // draw bottom box
                    if (needDisplayBottomBox) {
                        mPaint.setColor(needHighLightBottomBox ? Color.parseColor("#00ff00") :
                                Color.parseColor("#ffffff"));
                        mRect.set(guideBoxWidth, mHeight - guideBoxWidth, mWidth - guideBoxWidth, mHeight);
                        mCanvas.drawRect(mRect, mPaint);
                    }
                    // draw bottom-right box
                    if (needDisplayBRBox) {
                        mPaint.setColor(needHighLightBRBox ? Color.parseColor("#00ff00") :
                                Color.parseColor("#ffffff"));
                        mRect.set(mWidth - guideBoxWidth, mHeight - guideBoxWidth, mWidth, mHeight);
                        mCanvas.drawRect(mRect, mPaint);
                    }
                    // draw right box
                    if (needDisplayRightBox) {
                        mPaint.setColor(needHighLightRightBox ? Color.parseColor("#00ff00") :
                                Color.parseColor("#ffffff"));
                        mRect.set(mWidth - guideBoxWidth, guideBoxWidth, mWidth, mHeight - guideBoxWidth);
                        mCanvas.drawRect(mRect, mPaint);
                    }
                    // draw right-top box
                    if (needDisplayRTBox) {
                        mPaint.setColor(needHighLightRTBox ? Color.parseColor("#00ff00") :
                                Color.parseColor("#ffffff"));
                        mRect.set(mWidth - guideBoxWidth, 0, mWidth, guideBoxWidth);
                        mCanvas.drawRect(mRect, mPaint);
                    }
                    // draw top box
                    if (needDisplayTopBox) {
                        mPaint.setColor(needHighLightTopBox ? Color.parseColor("#00ff00") :
                                Color.parseColor("#ffffff"));
                        mRect.set(guideBoxWidth, 0, mWidth - guideBoxWidth, guideBoxWidth);
                        mCanvas.drawRect(mRect, mPaint);
                    }
                    // draw top box
                    if (needDisplayTLBox) {
                        mPaint.setColor(needHighLightTLBox ? Color.parseColor("#00ff00") :
                                Color.parseColor("#ffffff"));
                        mRect.set(0, 0, guideBoxWidth, guideBoxWidth);
                        mCanvas.drawRect(mRect, mPaint);
                    }
                } else if (mStep == 2) {
                    if (needDisplayLTtoRBBox) {
                        mPaint.setColor(needHighLightLTtoRBBox ? Color.parseColor("#00ff00") :
                                Color.parseColor("#ffffff"));
                        mRectPath.moveTo(guideBoxWidth, 0);
                        mRectPath.lineTo(mWidth, mHeight - guideBoxWidth);
                        mRectPath.lineTo(mWidth - guideBoxWidth, mHeight);
                        mRectPath.lineTo(0, guideBoxWidth);
                        mRectPath.close();
                        mCanvas.drawPath(mRectPath, mPaint);
                    }
                    if (needDisplayRB2Box) {
                        mPaint.setColor(needHighLightRB2Box ? Color.parseColor("#00ff00") :
                                Color.parseColor("#ffffff"));
                        mRect.set(mWidth - guideBoxWidth, mHeight - guideBoxWidth, mWidth, mHeight);
                        mCanvas.drawRect(mRect, mPaint);
                    }
                    if (needDisplayRBtoRLBox) {
                        mPaint.setColor(needHighLightRBtoRLBox ? Color.parseColor("#00ff00") :
                                Color.parseColor("#ffffff"));
                        mRect.set(guideBoxWidth, mHeight - guideBoxWidth, mWidth - guideBoxWidth, mHeight);
                        mCanvas.drawRect(mRect, mPaint);
                    }
                    if (needDisplayLB2Box) {
                        mPaint.setColor(needHighLightLB2Box ? Color.parseColor("#00ff00") :
                                Color.parseColor("#ffffff"));
                        mRect.set(0, mHeight - guideBoxWidth, guideBoxWidth, mHeight);
                        mCanvas.drawRect(mRect, mPaint);
                    }
                    if (needDisplayLBtoRTBox) {
                        mPaint.setColor(needHighLightLBtoRTBox ? Color.parseColor("#00ff00") :
                                Color.parseColor("#ffffff"));
                        mRectPath.moveTo(guideBoxWidth, mHeight);
                        mRectPath.lineTo(0, mHeight - guideBoxWidth);
                        mRectPath.lineTo(mWidth - guideBoxWidth, 0);
                        mRectPath.lineTo(mWidth, guideBoxWidth);
                        mRectPath.close();
                        mCanvas.drawPath(mRectPath, mPaint);
                    }
                    if (needDisplayRT2Box) {
                        mPaint.setColor(needHighLightRT2Box ? Color.parseColor("#00ff00") :
                                Color.parseColor("#ffffff"));
                        mRect.set(mWidth - guideBoxWidth, 0, mWidth, guideBoxWidth);
                        mCanvas.drawRect(mRect, mPaint);
                    }
                }
            }

            // draw finger path
            mPaint.setColor(Color.parseColor("#ffffff"));
            mCanvas.drawPath(mPath, mPaint);

        } catch (Exception e) {
        } finally {
            if (mCanvas != null) {
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // recorder x y
                touchDown(event);
                // set touch status
                setStatus();
                break;
            case MotionEvent.ACTION_MOVE:
                // update status
                checkFingerPath();
                // draw line and check out of bounds
                drawFingerPath(event);
                break;
            case MotionEvent.ACTION_UP:
                clean();
                break;
        }
        invalidate();
        return true;
    }

    private int mStep = 1;// 1 , 2 two type test

    private void checkFingerPath() {
        if (outOfBounds) return;
        if (mStep == 1) {
            if (wantToStartDrawLeft() && !needDisplayTLBox) return;
            if (needDisplayLeftBox && (drawLeft() || needDisplayLBBox)) {
                needHighLightLeftBox = true;
                needDisplayLBBox = true;
            } else {
                needHighLightLeftBox = false;
                needDisplayLeftBox = false;
                needDisplayLBBox = false;
            }
            if (needDisplayLBBox && wantToStartDrawBottom() || needDisplayBottomBox) {
                needHighLightLBBox = true;
                needDisplayBottomBox = true;
            } else {
                needDisplayBottomBox = false;
                needHighLightLBBox = false;
            }
            if (needDisplayBottomBox && drawBottom() || needHighLightBottomBox) {
                needHighLightBottomBox = true;
                needDisplayBRBox = true;
            } else {
                needHighLightBottomBox = false;
                needDisplayBRBox = false;
            }
            if (needDisplayBRBox && wantToStartDrawRight() || needDisplayRightBox) {
                needDisplayRightBox = true;
                needHighLightBRBox = true;
            } else {
                needDisplayRightBox = false;
                needHighLightBRBox = false;
            }
            if (needDisplayRightBox && drawRight() || needHighLightRightBox) {
                needDisplayRTBox = true;
                needHighLightRightBox = true;
            } else {
                needDisplayRTBox = false;
                needHighLightRightBox = false;
            }
            if (needDisplayRTBox && wantToStartDrawTop() || needHighLightRTBox) {
                needHighLightRTBox = true;
                needDisplayTopBox = true;
            } else {
                needHighLightRTBox = false;
                needDisplayTopBox = false;
            }
            if (needDisplayTopBox && drawTop() || needHighLightTopBox) {
                needHighLightTopBox = true;
                needDisplayTLBox = true;
            } else {
                needHighLightTopBox = false;
                needDisplayTLBox = false;
            }
            if (needDisplayTLBox && drawOver()) {
                needHighLightTLBox = true;
                mStep = 2;
                clean();
            } else {
                needHighLightTLBox = false;
            }
        } else if (mStep == 2) {
            if (needDisplayLTtoRBBox && drawRB() || needDisplayRB2Box) {
                needHighLightLTtoRBBox = true;
                needDisplayRB2Box = true;
            } else {
                needHighLightLTtoRBBox = false;
                needDisplayRB2Box = false;
            }
            if (needDisplayRB2Box && wantToStartDrawRight() || needHighLightRB2Box) {
                needHighLightRB2Box = true;
                needDisplayRBtoRLBox = true;
            } else {
                needHighLightRB2Box = false;
                needDisplayRBtoRLBox = false;
            }
            if (needDisplayRBtoRLBox && drawBottom() || needHighLightRBtoRLBox) {
                needHighLightRBtoRLBox = true;
                needDisplayLB2Box = true;
            } else {
                needDisplayLB2Box = false;
                needHighLightRBtoRLBox = false;
            }
            if (needDisplayLB2Box && wantToStartDrawBottom() || needHighLightLB2Box) {
                needHighLightLB2Box = true;
                needDisplayLBtoRTBox = true;
            } else {
                needHighLightLB2Box = false;
                needDisplayLBtoRTBox = false;
            }
            if (needDisplayLBtoRTBox && drawRT() || needHighLightLBtoRTBox) {
                needHighLightLBtoRTBox = true;
                needDisplayRT2Box = true;
            } else {
                needHighLightLBtoRTBox = false;
                needDisplayRT2Box = false;
            }
            if (needDisplayRT2Box && wantToStartDrawTop() || needHighLightRT2Box) {
                needHighLightRT2Box = true;
                if (mListener != null) mListener.complete();
            } else {
                needHighLightRT2Box = false;
            }
        }
    }


    private boolean needHighLightLeftTopBox;
    private boolean needDisplayLeftBox;
    private boolean needHighLightLeftBox;
    private boolean needDisplayBottomBox;
    private boolean needHighLightBottomBox;
    private boolean needDisplayRightBox;
    private boolean needHighLightRightBox;
    private boolean needDisplayTopBox;
    private boolean needHighLightTopBox;
    private boolean needDisplayLBBox;
    private boolean needHighLightLBBox;
    private boolean needDisplayBRBox;
    private boolean needHighLightBRBox;
    private boolean needDisplayRTBox;
    private boolean needHighLightRTBox;
    private boolean needDisplayTLBox;
    private boolean needHighLightTLBox;

    private boolean needDisplayLTtoRBBox;
    private boolean needHighLightLTtoRBBox;
    private boolean needDisplayRBtoRLBox;
    private boolean needHighLightRBtoRLBox;
    private boolean needDisplayLBtoRTBox;
    private boolean needHighLightLBtoRTBox;
    private boolean needDisplayRB2Box;
    private boolean needHighLightRB2Box;
    private boolean needDisplayLB2Box;
    private boolean needHighLightLB2Box;
    private boolean needDisplayRT2Box;
    private boolean needHighLightRT2Box;

    private void setStatus() {
        if (wantToStartDrawLeft() && mStep == 1) {
            needHighLightLeftTopBox = true;
            needDisplayLeftBox = true;
        }
        if (wantToStartDrawRB() && mStep == 2) {
            if ((mX + mY) > guideBoxWidth) {
                needDisplayLTtoRBBox = true;
                needHighLightLeftTopBox = true;
                needHighLightLTtoRBBox = true;
                needDisplayRB2Box = true;
            } else {
                needDisplayLTtoRBBox = true;
                needHighLightLeftTopBox = true;
            }
            mIndex = DRAW_FROM_LT_TO_RB;
        }
    }

    private boolean drawRB() {
        return (mHeight - guideBoxWidth) * mX + (guideBoxWidth - mWidth) * mY <=
                guideBoxWidth * (mHeight - guideBoxWidth) &&
                (mHeight - guideBoxWidth) * mX + (guideBoxWidth - mWidth) * mY >=
                        guideBoxWidth * (guideBoxWidth - mWidth);
    }

    private boolean wantToStartDrawRB() {
        return wantToStartDrawLeft();
    }

    private boolean wantToStartDrawLeft() {
        return mX >= 0 && mX <= guideBoxWidth &&
                mY >= 0 && mY <= guideBoxWidth;
    }

    private boolean wantToStartDrawBottom() {
        return mX >= 0 && mX <= guideBoxWidth &&
                mY >= mHeight - guideBoxWidth && mY <= mHeight;
    }

    private boolean wantToStartDrawRight() {
        return mX >= mWidth - guideBoxWidth && mX <= mWidth &&
                mY >= mHeight - guideBoxWidth && mY <= mHeight;
    }

    private boolean wantToStartDrawTop() {
        return mX >= mWidth - guideBoxWidth && mX <= mWidth &&
                mY >= 0 && mY <= guideBoxWidth;
    }

    private boolean drawLeft() {
        return mX >= 0 && mX <= guideBoxWidth &&
                mY >= guideBoxWidth && mY <= mHeight - guideBoxWidth;
    }

    private boolean drawBottom() {
        return mX >= guideBoxWidth && mX <= mWidth - guideBoxWidth &&
                mY >= mHeight - guideBoxWidth && mY <= mHeight;
    }

    private boolean drawRight() {
        return mX >= mWidth - guideBoxWidth && mX <= mWidth &&
                mY >= guideBoxWidth && mY <= mHeight - guideBoxWidth;
    }

    private boolean drawTop() {
        return mX >= guideBoxWidth && mX <= mWidth - guideBoxWidth &&
                mY >= 0 && mY <= guideBoxWidth;
    }

    private boolean drawOver() {
        return wantToStartDrawLeft();
    }

    private boolean drawRT() {
        return (mHeight - guideBoxWidth) * mX + (mWidth - guideBoxWidth) * mY >=
                (mHeight - guideBoxWidth) * (mWidth - guideBoxWidth) &&
                (mHeight + guideBoxWidth) * mX + (mWidth + guideBoxWidth) * mY <=
                        (mHeight + guideBoxWidth) * (mWidth + guideBoxWidth);
    }


    private void touchDown(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        mX = x;
        mY = y;
        mPath.moveTo(x, y);
    }

    private boolean outOfBounds;
    private int mIndex;// draw step index for step 2
    private static final int DRAW_FROM_LT_TO_RB = 1001;
    private static final int DRAW_FROM_RB_TO_LB = 1002;
    private static final int DRAW_FROM_LB_TO_RT = 1003;

    private void drawFingerPath(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        outOfBounds = outOfBounds || !checkPointXY(x, y);

        final float previousX = mX;
        final float previousY = mY;

        final float dx = Math.abs(x - previousX);
        final float dy = Math.abs(y - previousY);

        if (dx >= 3 || dy >= 3) {
            float cX = (x + previousX) / 2;
            float cY = (y + previousY) / 2;

            mPath.quadTo(previousX, previousY, cX, cY);

            mX = x;
            mY = y;
        }
    }

    private boolean checkPointXY(float x, float y) {
        if (mStep == 1) {
            return x >= 0 && x <= mWidth && y >= 0 && y <= guideBoxWidth ||
                    x >= 0 && x <= guideBoxWidth && y >= 0 && y <= mHeight ||
                    x >= 0 && x <= mWidth && y >= mHeight - guideBoxWidth && y <= mHeight ||
                    x >= mWidth - guideBoxWidth && x <= mWidth && y >= 0 && y <= mHeight;
        } else if (mStep == 2) {
            // update status
            if(needHighLightRB2Box){
                mIndex = needHighLightLB2Box ? DRAW_FROM_LB_TO_RT : DRAW_FROM_RB_TO_LB;
            }else {
                mIndex = DRAW_FROM_LT_TO_RB;
            }
            boolean result = false;
            switch (mIndex) {
                case DRAW_FROM_LT_TO_RB:
                    result = wantToStartDrawRB() || drawRB();
                    break;
                case DRAW_FROM_RB_TO_LB:
                    result = drawRB() || x >= 0 && x <= mWidth &&
                            y >= mHeight - guideBoxWidth && y <= mHeight;
                    break;
                case DRAW_FROM_LB_TO_RT:
                    result = x >= 0 && x <= mWidth &&
                            y >= mHeight - guideBoxWidth && y <= mHeight || drawRT() || wantToStartDrawTop();
                    break;
            }
            return result;
        } else {
            throw new RuntimeException("error: invalid step index!");
        }

    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mLoop = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mLoop = false;
    }

    @Override
    public SurfaceHolder getHolder() {
        return super.getHolder();
    }


    /**
     * clear all tags
     */
    private void clean() {
        needHighLightLeftTopBox = false;
        needDisplayLeftBox = false;
        needHighLightLeftBox = false;
        needDisplayBottomBox = false;
        needHighLightBottomBox = false;
        needDisplayRightBox = false;
        needHighLightRightBox = false;
        needDisplayTopBox = false;
        needHighLightTopBox = false;
        needDisplayLBBox = false;
        needHighLightLBBox = false;
        needDisplayBRBox = false;
        needHighLightBRBox = false;
        needDisplayRTBox = false;
        needHighLightRTBox = false;
        needDisplayTLBox = false;
        needHighLightTLBox = false;
        needDisplayLTtoRBBox = false;
        needHighLightLTtoRBBox = false;
        needDisplayRBtoRLBox = false;
        needHighLightRBtoRLBox = false;
        needDisplayLBtoRTBox = false;
        needHighLightLBtoRTBox = false;
        needDisplayRB2Box = false;
        needHighLightRB2Box = false;
        needDisplayLB2Box = false;
        needHighLightLB2Box = false;
        needDisplayRT2Box = false;
        needHighLightRT2Box = false;
        mPath.rewind();
        mRectPath.rewind();
        outOfBounds = false;
    }

    /**
     * clear latest canvas
     */
    private void clear() {
        if (mPaint != null && mCanvas != null) {
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mCanvas.drawPaint(mPaint);
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
            invalidate();
        }
    }

    private TouchCompleteListener mListener;

    public void setListener(TouchCompleteListener mListener) {
        this.mListener = mListener;
    }

    public interface TouchCompleteListener {
        void complete();
    }
}
