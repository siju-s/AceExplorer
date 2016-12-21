package com.siju.acexplorer.filesystem.utils;

import android.content.Context;
import android.preference.PreferenceManager;

import com.siju.acexplorer.filesystem.FileConstants;


public class RootUtils {
    public static boolean isRooted(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(FileConstants.PREFS_ROOTED,
                false);
    }
}
