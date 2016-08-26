package com.cit.test;

import android.preference.Preference;

/**
 *
 */

public class PreferenceWrapper {

    private Preference preference;

    private boolean isSuccess;

    private Class clazz;

    private int index;

    public Preference getPreference() {
        return preference;
    }

    public void setPreference(Preference preference) {
        this.preference = preference;
    }


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }
}
