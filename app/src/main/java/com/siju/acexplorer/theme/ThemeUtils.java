/*
 * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.siju.acexplorer.theme;

import android.content.Context;
import androidx.preference.PreferenceManager;

public class ThemeUtils {

    public static final String PREFS_THEME   = "prefThemes";
    public static final String CURRENT_THEME = "theme";
    public static final int    THEME_DARK    = Theme.DARK.getValue();

    public static boolean isDarkTheme(Context context) {

        int theme = getTheme(context);
        return theme == THEME_DARK;
    }

    public static int getTheme(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(CURRENT_THEME, THEME_DARK);
    }

}
