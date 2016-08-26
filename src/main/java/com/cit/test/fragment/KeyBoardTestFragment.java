package com.cit.test.fragment;

import android.app.Fragment;
import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.cit.test.R;
import com.cit.test.TestItemActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static android.content.Context.WINDOW_SERVICE;

/**
 *
 */

public class KeyBoardTestFragment extends Fragment implements View.OnKeyListener {
    static final int Key_Status_Down = 1;
    static final int Key_Status_Null = 0;
    static final int Key_Status_Up = 2;
    static int upcolorflag = 0;
    static int downcolorflag = 0;
    static final String TAG = "KeyBoardTestFragment";

    private int[] mButtonIds;
    private HashMap<Integer, Integer> mButtonMaps = new HashMap();
    private HashMap<Integer, Integer> mButtonStatus = new HashMap();
    private int[] mKeyCodes;
    private View v = null;
    private View windowV;
    private WindowManager wm = null;
    KeyguardManager.KeyguardLock kl = null;

    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        //steven end
        wm = (WindowManager) getActivity().getSystemService(WINDOW_SERVICE);
        KeyguardManager km = (KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE);
        kl = km.newKeyguardLock("unLock");
    }


    public void onResume() {
        super.onResume();
        kl.disableKeyguard();
        windowV = new View(getActivity());
        addWindow();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeWindow();
        kl.reenableKeyguard();
    }

    private void initButtonsMaps() {
        Log.d(TAG, "===========initButtonsMaps======");
        int[] resId = {R.id.bt_sounddown, R.id.bt_soundup/*, R.id.bt_home,
                R.id.bt_menu, R.id.bt_back */};
        mButtonIds = resId;
        int[] keycode = {25, 24/*, 3, 82, 4*/};
        mKeyCodes = keycode;

        int i = 0;
        int j = mButtonIds.length;

        Log.d(TAG, "I=" + i + " j = " + j);

        for (i = 0; i < j; i++) {
            Integer key = Integer.valueOf(mKeyCodes[i]);
            Integer value = Integer.valueOf(mButtonIds[i]);
            mButtonMaps.put(key, value);
        }
        resetButtonBackground();
    }

    private boolean isTestKey(int keycode) {
        int j = mKeyCodes.length;
        int i = 0;
        while (i < j) {
            if (keycode == mKeyCodes[i]) {
                return true;
            } else {
                i++;
            }
        }
        return false;
    }

    private void resetButtonBackground() {
        Log.d(TAG, "resetButtonBackground()... ...");
        int i = mButtonIds.length;
        int j = 0;
        while (true) {
            if (j >= i)
                return;
            int k = mButtonIds[j];
            v.findViewById(k).setBackgroundColor(Color.rgb(255, 255, 255));
            ((TextView) v.findViewById(k)).setTextColor(Color.BLACK);
            j += 1;
        }
    }

    private void setButtonBackgroundDown(int resId) {
        Log.d(TAG, "=====613========setButtonBackgroundDown");
        v.findViewById(resId).setBackgroundColor(Color.BLUE);
    }

    private void setButtonBackgroundUp(int resId) {
        Log.d(TAG, "======setButtonBackgroundUp");
        v.findViewById(resId).setBackgroundColor(Color.GREEN);
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();
        int actionCode = keyEvent.getAction();
        Log.d(TAG, "KeyCode = " + keyCode);
        Log.d(TAG, "actionCode = " + actionCode);
        if (!isTestKey(keyCode)) {

        }
        int value = 0;
        Integer key = Integer.valueOf(keyCode);
        value = mButtonMaps.get(key).intValue();
        Log.d(TAG, "==================   value = " + value);
        switch (actionCode) {
            case 0:
                setButtonBackgroundDown(value);
                mButtonStatus.put(key, Integer.valueOf(1));
                break;

            case 1:
                setButtonBackgroundUp(value);
                mButtonStatus.put(key, Integer.valueOf(1));
                if (keyCode == 24) {
                    if (upcolorflag == 0 && downcolorflag == 0) {
                        upcolorflag = 1;
                        Log.d(TAG, "=========1111111111======== 00");
                    } else if (upcolorflag == 0 && downcolorflag == 1) {
                        ((Button) v.findViewById(R.id.btn_Fail)).performClick();
                        Log.d(TAG, "=========1111111111======== 01");
                    } else if (upcolorflag == 0 && downcolorflag == 2) {
                        upcolorflag = 1;
                        Log.d(TAG, "=========1111111111======== 02");
                    } else if (upcolorflag == 1 && downcolorflag == 0) {
                        upcolorflag = 2;
                        Log.d(TAG, "=========1111111111======== 10");
                    } else if (upcolorflag == 1 && downcolorflag == 1) {
                        ((Button) v.findViewById(R.id.btn_Fail)).performClick();
                        Log.d(TAG, "=========1111111111======== 11");
                    } else if (upcolorflag == 1 && downcolorflag == 2) {
                        upcolorflag = 2;
                        Log.d(TAG, "=========1111111111======== 12");
                    } else if (upcolorflag == 2 && downcolorflag == 1) {
                        downcolorflag = 0;
                        upcolorflag = 0;
                        ((Button) v.findViewById(R.id.btn_Fail)).performClick();
                        Log.d(TAG, "=========1111111111======== 11");
                    }
                } else {
                    if (upcolorflag == 0 && downcolorflag == 0) {
                        downcolorflag = 1;
                        Log.d(TAG, "=========1111111111======== 00");
                    } else if (upcolorflag == 1 && downcolorflag == 0) {
                        ((Button) v.findViewById(R.id.btn_Fail)).performClick();
                        Log.d(TAG, "=========1111111111======== 10");
                    } else if (upcolorflag == 2 && downcolorflag == 0) {
                        downcolorflag = 1;
                        Log.d(TAG, "=========1111111111======== 20");
                    } else if (upcolorflag == 0 && downcolorflag == 1) {
                        downcolorflag = 2;
                        Log.d(TAG, "=========1111111111======== 01");
                    } else if (upcolorflag == 1 && downcolorflag == 1) {
                        ((Button) v.findViewById(R.id.btn_Fail)).performClick();
                        Log.d(TAG, "=========1111111111======== 11");
                    } else if (upcolorflag == 2 && downcolorflag == 1) {
                        downcolorflag = 2;
                        Log.d(TAG, "=========1111111111======== 21");
                    } else if (upcolorflag == 1 && downcolorflag == 2) {
                        downcolorflag = 0;
                        upcolorflag = 0;
                        ((Button) v.findViewById(R.id.btn_Fail)).performClick();
                        Log.d(TAG, "=========1111111111======== 11");
                    }
                }
                if (downcolorflag == 2 && upcolorflag == 2) {
                    downcolorflag = 0;
                    upcolorflag = 0;
                    Log.d(TAG, "=========1111111111======== pass");
                    ((Button) v.findViewById(R.id.btn_Pass)).performClick();
                }
                break;

            default:
                break;
        }
        return true;
    }

    private void addWindow() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
//        params.type = WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        params.width = 1;//WindowManager.LayoutParams.FILL_PARENT;
        params.height = 1;//WindowManager.LayoutParams.FILL_PARENT;
        params.format = PixelFormat.TRANSLUCENT;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        //
        params.x = 0;
        params.y = 0;
        wm.addView(windowV, params);
        windowV.requestFocus();
        windowV.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCodee, KeyEvent event) {
                Log.d(TAG, " _____________---- onKey(),   " + event.getKeyCode());
                int keyCode = event.getKeyCode();
                int actionCode = event.getAction();
                Log.d(TAG, "KeyCode = " + keyCode);
                Log.d(TAG, "actionCode = " + actionCode);
                if (!isTestKey(keyCode))
                    return false;
                int value = 0;
                Integer key = Integer.valueOf(keyCode);
                value = mButtonMaps.get(key).intValue();
                Log.d(TAG, "==================   value = " + value);
                switch (actionCode) {
                    case 0:
                        setButtonBackgroundDown(value);
                        mButtonStatus.put(key, Integer.valueOf(1));
                        break;

                    case 1:
                        setButtonBackgroundUp(value);
                        checkKeyTestOver();
                        mButtonStatus.put(key, Integer.valueOf(1));
                        if (keyCode == 24) {
                            if (upcolorflag == 0 && downcolorflag == 0) {
                                upcolorflag = 1;
                                Log.d(TAG, "=========1111111111======== 00");
                            } else if (upcolorflag == 0 && downcolorflag == 1) {
                                downcolorflag = 0;
                                upcolorflag = 0;
                            } else if (upcolorflag == 0 && downcolorflag == 2) {
                                upcolorflag = 1;
                                Log.d(TAG, "=========1111111111======== 02");
                            } else if (upcolorflag == 1 && downcolorflag == 0) {
                                upcolorflag = 2;
                                Log.d(TAG, "=========1111111111======== 10");
                            } else if (upcolorflag == 1 && downcolorflag == 1) {
                                downcolorflag = 0;
                                upcolorflag = 0;
                                Log.d(TAG, "=========1111111111======== 11");
                            } else if (upcolorflag == 1 && downcolorflag == 2) {
                                upcolorflag = 2;
                                Log.d(TAG, "=========1111111111======== 12");
                            } else if (upcolorflag == 2 && downcolorflag == 1) {
                                downcolorflag = 0;
                                upcolorflag = 0;
                                Log.d(TAG, "=========1111111111======== 11");
                            }
                        } else {
                            if (upcolorflag == 0 && downcolorflag == 0) {
                                downcolorflag = 1;
                                Log.d(TAG, "=========1111111111======== 00");
                            } else if (upcolorflag == 1 && downcolorflag == 0) {
                                downcolorflag = 0;
                                upcolorflag = 0;
                                Log.d(TAG, "=========1111111111======== 10");
                            } else if (upcolorflag == 2 && downcolorflag == 0) {
                                downcolorflag = 1;
                                Log.d(TAG, "=========1111111111======== 20");
                            } else if (upcolorflag == 0 && downcolorflag == 1) {
                                downcolorflag = 2;
                                Log.d(TAG, "=========1111111111======== 01");
                            } else if (upcolorflag == 1 && downcolorflag == 1) {
                                downcolorflag = 0;
                                upcolorflag = 0;
                                Log.d(TAG, "=========1111111111======== 11");
                            } else if (upcolorflag == 2 && downcolorflag == 1) {
                                downcolorflag = 2;
                                Log.d(TAG, "=========1111111111======== 21");
                            } else if (upcolorflag == 1 && downcolorflag == 2) {
                                downcolorflag = 0;
                                upcolorflag = 0;
                                Log.d(TAG, "=========1111111111======== 11");
                            }
                        }
                        if (downcolorflag == 2 && upcolorflag == 2) {
                            downcolorflag = 0;
                            upcolorflag = 0;
                        }
                        break;
                    default:
                        break;
                }
                return true;

            }
        });
    }

    private void checkKeyTestOver() {
        if(mButtonStatus.size() != mButtonMaps.size())return;
        Set<Map.Entry<Integer, Integer>> entrySet = mButtonStatus.entrySet();
        boolean success = true;
        for(Map.Entry<Integer, Integer> entry : entrySet){
            if(entry.getValue() != 1){
                success = false;
            }
        }
        if(success){
            ((TestItemActivity)getActivity()).onNext();
        }
    }

    private void removeWindow() {
        wm.removeView(windowV);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.key_test, container, false);
        //steven
        v.findViewById(R.id.bt_menu).setVisibility(View.INVISIBLE);
        v.findViewById(R.id.bt_home).setVisibility(View.INVISIBLE);
        v.findViewById(R.id.bt_back).setVisibility(View.INVISIBLE);
        v.setOnKeyListener(this);
        initButtonsMaps();
        ((TestItemActivity) getActivity()).disableButton(R.id.btn_next);
        return v;
    }


}
