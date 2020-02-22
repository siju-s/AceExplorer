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

package com.siju.acexplorer.theme

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.preference.PreferenceManager


const val PREFS_THEME = "prefThemes"
const val CURRENT_THEME = "theme"

enum class Theme constructor(val value: Int) {

    LIGHT(0),
    DARK(1),
    DEVICE(2);


    companion object {

        private fun getThemeValue(position: Int): Theme {
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

        fun getUserThemeValue(context: Context) =
            PreferenceManager.getDefaultSharedPreferences(context)
                    .getInt(CURRENT_THEME, DARK.value)

        fun isDarkColoredTheme(resources: Resources?, currentTheme: Theme?): Boolean {
            if (resources == null) {
                return true
            }
            val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val isNightMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
            return when {
                currentTheme == DARK || (currentTheme == DEVICE && isNightMode) -> {
                    true
                }
                currentTheme == LIGHT || (currentTheme == DEVICE && currentNightMode == Configuration.UI_MODE_NIGHT_NO) -> {
                    false
                }
                else -> true
            }
        }
    }

}
