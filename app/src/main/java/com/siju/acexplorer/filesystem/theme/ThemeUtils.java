package com.siju.acexplorer.filesystem.theme;

import android.content.Context;
import android.preference.PreferenceManager;

public class ThemeUtils {

    public static final String PREFS_THEME = "prefThemes";
    public static final String CURRENT_THEME = "theme";
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;

    public static boolean isDarkTheme(Context context) {

        int theme = getTheme(context);
        return theme == Themes.DARK.getValue();
    }

    public static int getTheme(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(CURRENT_THEME, THEME_DARK);
    }
}
