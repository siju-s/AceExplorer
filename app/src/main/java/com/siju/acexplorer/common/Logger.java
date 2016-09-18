package com.siju.acexplorer.common;

import android.util.Log;

import com.siju.acexplorer.BuildConfig;

/**
 * Created by Siju on 16-06-2016.
 */

public class Logger {

    /**
     * Logging only for Debug mode
     *
     * @param tag
     * @param msg
     */
    public static void log(String tag, String msg) {
        if (BuildConfig.DEBUG) Log.d(tag, msg);
    }
}
