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

package com.siju.acexplorer.main.model

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.main.model.FileConstants.PREFS_FIRST_RUN
import com.siju.acexplorer.preferences.SharedPreferenceBooleanLiveData
import com.siju.acexplorer.settings.SettingsPreferenceFragment
import com.siju.acexplorer.theme.Theme
import com.siju.acexplorer.utils.Utils

class MainModelImpl : MainModel {

    private val context: Context = AceApplication.appContext
    val theme   = MutableLiveData<Theme>()
    val preferences : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val dualMode = SharedPreferenceBooleanLiveData(preferences, FileConstants.PREFS_DUAL_PANE, false)

    init {
        setupFirstRunSettings(preferences)
        setupAnalytics(preferences)
        setupTheme()
        Log.e("MainModel", "Dualmode:${dualMode.value}")
    }

    private fun setupFirstRunSettings(preferences: SharedPreferences) {
        val isFirstRun = preferences.getBoolean(PREFS_FIRST_RUN, true)

        if (isFirstRun) {
            preferences.edit().apply {
                setDefaultSortMode()
                setDefaultDualPaneMode()
            }.apply()
        }
        PreferenceManager.setDefaultValues(context, R.xml.pref_settings, false)
    }

    private fun SharedPreferences.Editor.setDefaultDualPaneMode() {
        val isTablet = Utils.isTablet(context)
        if (isTablet) {
            putBoolean(FileConstants.PREFS_DUAL_PANE, true)
        }
    }

    private fun SharedPreferences.Editor.setDefaultSortMode() {
        putInt(FileConstants.KEY_SORT_MODE, FileConstants.KEY_SORT_NAME)
    }

    private fun setupAnalytics(preferences: SharedPreferences) {
        val sendAnalytics = preferences.getBoolean(SettingsPreferenceFragment.PREFS_ANALYTICS, true)

        Analytics.getLogger().sendAnalytics(sendAnalytics)
        Analytics.getLogger().register(context)
        Analytics.getLogger().reportDeviceName()
    }

    private fun setupTheme() {
        theme.postValue(Theme.getTheme(context))
    }
}
