package com.siju.acexplorer.filesystem.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.Date;

public class PremiumUtils {
    private static final String TAG = PremiumUtils.class.getSimpleName();

    private static final boolean DEBUG = false;
    public static final String PREF_NAME = "PremiumUtils";
    private static final String KEY_INSTALL_DATE = "install_date";
    private static final String KEY_LAUNCH_TIMES = "launch_times";
    private static final String KEY_ASK_LATER_DATE = "ask_later_date";
    public static final String KEY_OPT_OUT = "opt_out";

    private static boolean mOptOut = false;
    private static Date mInstallDate = new Date();
    private static int mLaunchTimes = 0;
    private static Date mAskLaterDate = new Date();
    private static final int CRITERIA_LAUNCH_TIME = 15;
    private static final int CRITERIA_DAYS = 3;

    /**
     * @param context Context
     */
    public static void onStart(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        // If it is the first launch, save the date in shared preference.
        if (pref.getLong(KEY_INSTALL_DATE, 0) == 0L) {
            storeInstallDate(context, editor);
        }
        // Increment launch times
        int launchTimes = pref.getInt(KEY_LAUNCH_TIMES, 0);
        launchTimes++;
        editor.putInt(KEY_LAUNCH_TIMES, launchTimes);
        log("Launch times; " + launchTimes);

        editor.apply();

        mInstallDate = new Date(pref.getLong(KEY_INSTALL_DATE, 0));
        mLaunchTimes = pref.getInt(KEY_LAUNCH_TIMES, 0);
        mAskLaterDate = new Date(pref.getLong(KEY_ASK_LATER_DATE, 0));
        mOptOut = pref.getBoolean(KEY_OPT_OUT, false);

        printStatus(context);
    }

    /**
     * Print values in SharedPreferences (used for debug)
     *
     * @param context
     */
    private static void printStatus(final Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        log("*** Premium Status ***");
        log("Install Date: " + new Date(pref.getLong(KEY_INSTALL_DATE, 0)));
        log("Launch Times: " + pref.getInt(KEY_LAUNCH_TIMES, 0));
    }

    /**
     * Store install date.
     * Install date is retrieved from package manager if possible.
     *
     * @param context
     * @param editor
     */
    private static void storeInstallDate(final Context context, SharedPreferences.Editor editor) {
        Date installDate = new Date();
        PackageManager packMan = context.getPackageManager();
        try {
            PackageInfo pkgInfo = packMan.getPackageInfo(context.getPackageName(), 0);
            installDate = new Date(pkgInfo.firstInstallTime);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        editor.putLong(KEY_INSTALL_DATE, installDate.getTime());
        log("First install: " + installDate.toString());
    }

    /**
     * Print log if enabled
     *
     * @param message
     */
    private static void log(String message) {
        if (DEBUG) {
            Log.v(TAG, message);
        }
    }

    public static boolean shouldShowPremiumDialog() {
        if (mOptOut) {
            return false;
        } else {
            if (mLaunchTimes >= CRITERIA_LAUNCH_TIME) {
                return true;
            }
            long threshold = CRITERIA_DAYS * 24 * 60 * 60 * 1000L;    // msec
            return new Date().getTime() - mInstallDate.getTime() >= threshold &&
                    new Date().getTime() - mAskLaterDate.getTime() >= threshold;
        }
    }
}
