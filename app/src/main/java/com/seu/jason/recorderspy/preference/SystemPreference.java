package com.seu.jason.recorderspy.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Jason on 2015/4/12.
 */
public class SystemPreference {

    private static final String LOG_TAG = "SystemPrefernece";

    static private SystemPreference systemPreferenceInstance = null;
    SharedPreferences mSharedPreferences;

    private SystemPreference(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    static public SystemPreference getSystemPreferenceInstance(Context context) {
        Log.e(LOG_TAG, "getSystemPreferenceInstance(context)");
        if (systemPreferenceInstance == null) {
            systemPreferenceInstance = new SystemPreference(context);
        }
        return systemPreferenceInstance;
    }

    static public SystemPreference getSystemPreferenceInstance() {
        Log.e(LOG_TAG, "getSystemPreferenceInstance()");
        return systemPreferenceInstance;
    }

    public long getSharedPreferenceAlarm() {
        return mSharedPreferences.getLong("alarmDate", 0);
    }

    public void setSharedPreferenceAlarm(long date) {
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putLong("alarmDate", date);
        mEditor.commit();
    }

}
