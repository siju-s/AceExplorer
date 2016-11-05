package com.siju.acexplorer.utils;

import android.content.Context;
import android.content.SharedPreferences;


public class PrefManager {
    private SharedPreferences pref;

    // Shared preferences file name
    private static final String PREF_NAME = "ace_prefs";

    private static final String IS_FIRST_TIME_LAUNCH = "first_launch"; //TODO change it later

    public PrefManager(Context context) {
        int PRIVATE_MODE = 0;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);

    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        pref.edit().putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime).apply();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

}
