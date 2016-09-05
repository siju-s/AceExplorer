package com.siju.acexplorer.filesystem.utils;

import android.content.Context;
import android.preference.PreferenceManager;

import com.siju.acexplorer.filesystem.FileConstants;

/**
 * Created by Siju on 05-09-2016.
 */
public class ThemeUtils {

    public static boolean isDarkTheme(Context context) {
        int theme = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(FileConstants.CURRENT_THEME, FileConstants.THEME_LIGHT);

//        int  stringValue = PreferenceManager.getDefaultSharedPreferences(this).getInt(FileConstants.PREFS_THEME, 0);

        return theme == FileConstants.THEME_DARK;


    }
}
