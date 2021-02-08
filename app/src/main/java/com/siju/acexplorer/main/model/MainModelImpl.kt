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
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.main.model.FileConstants.PREFS_FIRST_RUN
import com.siju.acexplorer.main.model.helper.SdkHelper
import com.siju.acexplorer.preferences.PreferenceConstants
import com.siju.acexplorer.preferences.SharedPreferenceBooleanLiveData
import com.siju.acexplorer.preferences.SharedPreferenceIntLiveData
import com.siju.acexplorer.settings.SettingsPreferenceFragment
import com.siju.acexplorer.theme.Theme
import com.siju.acexplorer.utils.Utils


private const val PREF_UPDATE_CANCELLED = "update_cancelled_"
class MainModelImpl : MainModel {

    private val context: Context = AceApplication.appContext
    val theme   = MutableLiveData<Theme>()
    val preferences : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val dualMode = SharedPreferenceBooleanLiveData(preferences, FileConstants.PREFS_DUAL_PANE, false)
    val sortMode = SharedPreferenceIntLiveData(preferences, FileConstants.KEY_SORT_MODE, PreferenceConstants.DEFAULT_VALUE_SORT_MODE)

    init {
        setupFirstRunSettings(preferences)
        setupAnalytics(preferences)
        setupTheme()
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

        Analytics.logger.sendAnalytics(sendAnalytics)
        Analytics.logger.register(context)
        Analytics.logger.reportDeviceName()
    }

    private fun setupTheme() {
        theme.postValue(Theme.getTheme(context))
    }

    @Suppress("DEPRECATION")
    override fun saveUserCancelledUpdate() {
        val versionCode = getVersionCode()
        preferences.edit().putBoolean(PREF_UPDATE_CANCELLED + versionCode, true).apply()
    }

    @Suppress("DEPRECATION")
    private fun getVersionCode(): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (SdkHelper.isAtleastPie) {
                packageInfo.longVersionCode.toInt()
            } else {
                packageInfo.versionCode
            }
        } catch (e: PackageManager.NameNotFoundException) {
            0
        }
    }


    override fun hasUserCancelledUpdate() : Boolean {
        return preferences.getBoolean(PREF_UPDATE_CANCELLED + getVersionCode(), false)
    }

    override fun onUpdateComplete() {
        if (hasUserCancelledUpdate()) {
            preferences.edit().remove(PREF_UPDATE_CANCELLED + getVersionCode()).apply()
        }
    }


}
