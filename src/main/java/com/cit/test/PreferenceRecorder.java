package com.cit.test;

import android.util.Log;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 */

public class PreferenceRecorder {

    private static final Map<Integer,PreferenceWrapper> mPreferenceWrappers
            = new TreeMap<>(new Comparator<Integer>() {
        @Override
        public int compare(Integer integer, Integer t1) {
            return integer - t1;
        }
    });
    private static final String TAG = "PreferenceRecorder";

    public static void addPreference(PreferenceWrapper wrapper){
        mPreferenceWrappers.put(wrapper.getIndex(),wrapper);
    }

    public static PreferenceWrapper findPreference(int index){
        return mPreferenceWrappers.get(index);
    }

    public static boolean isEnd(int index){
        return index == mPreferenceWrappers.size() - 1;
    }

    public static PreferenceWrapper findNextPreference(int index){
        if(index >= mPreferenceWrappers.size() - 1)throw new RuntimeException("error:");
        return mPreferenceWrappers.get(index + 1);
    }

    public static void reset() {
        mPreferenceWrappers.clear();
    }

    public static int findPreferenceIndex(String key){
        Set<Map.Entry<Integer, PreferenceWrapper>> entries = mPreferenceWrappers.entrySet();
        for ( Map.Entry<Integer,PreferenceWrapper> entry : entries){
            if(entry.getValue().getPreference().getKey().equals(key)){
                return entry.getKey();
            }
        }
        return -1;
    }

    public static void saveResult(boolean result, int index){
        if(mPreferenceWrappers.get(index) != null){
            mPreferenceWrappers.get(index).setSuccess(result);
        }
    }

    public static boolean isAllPass(){
        Set<Map.Entry<Integer, PreferenceWrapper>> entrySet = mPreferenceWrappers.entrySet();
        for (Map.Entry<Integer, PreferenceWrapper> entry : entrySet){
            Log.i(TAG, "isAllPass: " + entry.getKey() + "------------->" + entry.getValue().isSuccess());
        }
        Collection<PreferenceWrapper> values = mPreferenceWrappers.values();
        for(PreferenceWrapper pw : values){
            if(!pw.isSuccess()){
                return false;
            }
        }
        return true;
    }
}
