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

package com.siju.acexplorer.common.theme

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager


const val PREFS_THEME = "prefThemes"
const val CURRENT_THEME = "theme"


enum class Theme(val value: Int) {

    LIGHT(0),
    DARK(1),
    DEVICE(2);


    companion object {

        fun getThemeValue(position: Int): Theme {
            when (position) {
                0 -> return LIGHT
                1 -> return DARK
                2 -> return DEVICE
            }
            return DARK
        }

        fun getTheme(context: Context): Theme {
            return getThemeValue(getUserThemeValue(context))
        }

        fun getUserThemeValue(context: Context?): Int {
            context ?: return DARK.value
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(CURRENT_THEME, DARK.value)
        }

        fun setTheme(theme: Theme?) {
            Log.d("Theme", "setTheme() called with: theme = $theme")
            when (theme) {
                DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                DEVICE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                else -> {
                }
            }
        }
    }

}




