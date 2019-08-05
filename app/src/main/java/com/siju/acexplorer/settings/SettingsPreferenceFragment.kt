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

package com.siju.acexplorer.settings

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.extensions.canHandleIntent
import com.siju.acexplorer.extensions.showToast
import com.siju.acexplorer.home.model.FavoriteHelper
import com.siju.acexplorer.logging.Logger
import com.siju.acexplorer.main.model.FileConstants
import com.siju.acexplorer.theme.CURRENT_THEME
import com.siju.acexplorer.theme.PREFS_THEME
import com.siju.acexplorer.theme.Theme
import com.siju.acexplorer.utils.LocaleHelper

const val PREFS_UPDATE = "prefsUpdate"
const val PREFS_LANGUAGE = "prefLanguage"
private const val URL_PLAYSTORE = "https://play.google.com/store/apps/details?id="
private const val URL_PLAYSTORE_MARKET = "market://details?id="

class SettingsPreferenceFragment : PreferenceFragmentCompat() {

    private var preferences: SharedPreferences? = null
    private var currentLanguage: String? = null
    private var theme = 0


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_settings, rootKey)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        preferences = PreferenceManager.getDefaultSharedPreferences(context)

        setupLanguagePreference()
        setupThemePref()
        setupAnalyticsPref()
        setupResetFavPref()
        setupUpdatePref()
    }

    private fun setupLanguagePreference() {
        val languagePreference = findPreference(PREFS_LANGUAGE) as ListPreference?
        val value = LocaleHelper.getLanguage(activity)
        languagePreference?.value = value
        currentLanguage = value
        bindPreferenceSummaryToValue(languagePreference)
    }

    private fun setupThemePref() {
        val themePreference = findPreference<ListPreference>(PREFS_THEME) as ListPreference
        theme = Theme.getUserThemeValue(activity!!)
        bindPreferenceSummaryToValue(themePreference)
    }

    private fun setupAnalyticsPref() {
        val analyticsPreference = findPreference(PREFS_ANALYTICS) as SwitchPreferenceCompat?
        analyticsPreference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            Analytics.getLogger().sendAnalytics(newValue as Boolean)
            true
        }
    }

    private fun setupResetFavPref() {
        val resetPreference = findPreference<Preference>(FileConstants.PREFS_RESET)
        resetPreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            resetFavorites()
            false
        }
    }

    private fun resetFavorites() {
        FavoriteHelper.resetFavorites(context)
        Toast.makeText(AceApplication.appContext, getString(R.string.msg_fav_reset), Toast
                .LENGTH_LONG).show()
    }

    private fun setupUpdatePref() {
        val updatePreference = findPreference(PREFS_UPDATE) as Preference?
        updatePreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            updateClicked(intent)

            true
        }
    }

    private fun updateClicked(intent: Intent) {
        // Try Google play
        intent.data = Uri.parse(URL_PLAYSTORE_MARKET + activity!!.packageName)

        if (context?.canHandleIntent(intent) == true) {
            startActivity(intent)
        }
        else {
            intent.data = Uri.parse(URL_PLAYSTORE + context?.packageName)
            if (context?.canHandleIntent(intent) == true) {
                startActivity(intent)
            }
            else {
                context.showToast(getString(R.string.msg_error_not_supported))
            }
        }
    }


    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see [bindPreferenceSummaryToValueListener]
     */
    private fun bindPreferenceSummaryToValue(preference: Preference?) {
        // Set the listener to watch for value changes.
        preference?.onPreferenceChangeListener = bindPreferenceSummaryToValueListener

        // Trigger the listener immediately with the preference's
        // current value.
        bindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                                                                PreferenceManager
                                                                        .getDefaultSharedPreferences(
                                                                                preference?.context)
                                                                        .getString(preference?.key,
                                                                                   ""))
    }

    private val bindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
        val stringValue = value.toString()

        if (preference is ListPreference) {
            val index = preference.findIndexOfValue(stringValue)

            // Set the summary to reflect the new value.
            preference.setSummary(
                    if (index >= 0)
                        preference.entries[index]
                    else
                        null)

            if (preference.key == PREFS_LANGUAGE) {
                onLanguagePrefChanged(stringValue)
            }
            else if (preference.key == PREFS_THEME) {
                onThemeChanged(stringValue)
            }

        }
        else {
            preference.summary = stringValue
        }
        true
    }

    private fun onThemeChanged(stringValue: String) {
        val theme = Integer.parseInt(stringValue)
        preferences?.edit()?.putInt(CURRENT_THEME, theme)?.apply()
        Logger.log("TAG", "Current theme=" + this@SettingsPreferenceFragment
                .theme + " new theme=" + theme)
        if (this@SettingsPreferenceFragment.theme != theme) {
            restartApp()
        }
    }

    private fun onLanguagePrefChanged(stringValue: String) {
        if (stringValue != currentLanguage) {
            LocaleHelper.persist(activity, stringValue)
            restartApp()
        }
    }

    private fun restartApp() {
        val activity = activity as AppCompatActivity? ?: return
        val enter_anim = android.R.anim.fade_in
        val exit_anim = android.R.anim.fade_out
        activity.overridePendingTransition(enter_anim, exit_anim)
        activity.finish()
        activity.overridePendingTransition(enter_anim, exit_anim)
        activity.startActivity(activity.intent)
    }

    companion object {
        const val PREFS_ANALYTICS = "prefsAnalytics"
    }
}