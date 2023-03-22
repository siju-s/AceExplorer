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

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.*
import com.google.android.material.appbar.AppBarLayout
import com.google.android.play.core.appupdate.AppUpdateManager
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.home.model.FavoriteHelper
import com.siju.acexplorer.logging.Logger
import com.siju.acexplorer.main.MainCommunicator
import com.siju.acexplorer.main.helper.UpdateChecker
import com.siju.acexplorer.main.model.FileConstants
import com.siju.acexplorer.main.model.root.RootUtils
import com.siju.acexplorer.theme.CURRENT_THEME
import com.siju.acexplorer.theme.PREFS_THEME
import com.siju.acexplorer.theme.Theme
import com.siju.acexplorer.utils.LocaleHelper
import com.siju.acexplorer.utils.NetworkHelper

const val PREFS_UPDATE = "prefsUpdate"
const val PREFS_LANGUAGE = "prefLanguage"

class SettingsPreferenceFragment : PreferenceFragmentCompat() {

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var mainCommunicator: MainCommunicator

    private var preferences: SharedPreferences? = null
    private var updatePreference: Preference? = null
    private var currentLanguage: String? = null
    private var updateChecker: UpdateChecker? = null
    private var theme = 0
    private var connectivityManager : ConnectivityManager? = null
    private var networkHelper : NetworkHelper? = null
    private var registeredNetworkCallback = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainCommunicator) {
            updateChecker = context.getUpdateChecker()
            mainCommunicator = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root: View = super.onCreateView(inflater, container, savedInstanceState)
        val activity = activity as AppCompatActivity?
        val appbar = root.findViewById(R.id.appbar) as AppBarLayout
        val toolbar = appbar.findViewById<Toolbar>(R.id.toolbarContainer)
        activity?.setSupportActionBar(toolbar)
        val actionBar: ActionBar? = activity?.supportActionBar
        actionBar?.setTitle(R.string.action_settings)
        setHasOptionsMenu(true)
        return root
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_settings, rootKey)
        setupUpdatePref()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferences = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }

        setupRootPref()
        setupLanguagePreference()
        setupThemePref()
        setupAnalyticsPref()
        setupResetFavPref()
    }

    private fun setupRootPref() {
        val rootPreference = findPreference(PREF_ROOT) as CheckBoxPreference?
        rootPreference?.setOnPreferenceClickListener { _ ->
            onRootPrefClicked(rootPreference.isChecked, rootPreference)
            true
        }
    }

    private fun onRootPrefClicked(newValue: Boolean, rootPreference: CheckBoxPreference) {
        if (newValue) {
            val rooted = RootUtils.hasRootAccess()
            Log.d("Settings", " rooted:$rooted")
            rootPreference.isChecked = rooted
        } else {
            rootPreference.isChecked = false
        }
    }

    private fun setupLanguagePreference() {
        val languagePreference = findPreference(PREFS_LANGUAGE) as ListPreference?
        val value = LocaleHelper.getLanguage(context)
        languagePreference?.value = value
        currentLanguage = value
        bindPreferenceSummaryToValue(languagePreference)
    }

    private fun setupThemePref() {
        val themePreference = findPreference<ListPreference>(PREFS_THEME)
        theme = Theme.getUserThemeValue(context)
        bindPreferenceSummaryToValue(themePreference)
    }

    private fun setupAnalyticsPref() {
        val analyticsPreference = findPreference(PREFS_ANALYTICS) as CheckBoxPreference?
        analyticsPreference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            Analytics.logger.sendAnalytics(newValue as Boolean)
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
        updatePreference = findPreference(PREFS_UPDATE)
        val updateAvailable = updateChecker?.isUpdateAvailable()
        if (updateChecker == null || updateAvailable == false) {
            updatePreference?.isVisible = false
            return
        }
        updateChecker?.setUpdateCallback(updateCallback)
        networkHelper = NetworkHelper(networkChangeCallback)
        setupUpdatePrefText(NetworkHelper.isConnectedToInternet(context))
        connectivityManager = networkHelper?.getConnectivityManager(context)
        if (!registeredNetworkCallback) {
            networkHelper?.registerNetworkRequest(connectivityManager)
            registeredNetworkCallback = true
        }
        updatePreference?.isVisible = true
        initUpdatePrefListener()
    }

    private fun setupUpdatePrefText(networkAvailable : Boolean = true) {
        handler.post {
            context ?: return@post
            when {
                updateChecker?.isUpdateDownloaded() == true -> {
                    onUpdateDownloaded()
                }
                networkAvailable && updateChecker?.isUpdateDownloading() == true -> {
                    onUpdateDownloading()
                }
                else -> {
                    updatePreference?.title = getString(R.string.update_available)
                    handleInternetSetting(networkAvailable)
                }
            }
        }
    }

    private fun initUpdatePrefListener() {
        updatePreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            updateClicked()
            true
        }
    }

    private fun updateClicked() {
        updateChecker?.startUpdate()
    }

    private val networkChangeCallback = object : NetworkHelper.NetworkChangeCallback {
        override fun onNetworkAvailable() {
             if (updateChecker?.isUpdateAvailable() == true) {
                 setupUpdatePrefText(true)
             }
        }

        override fun onNetworkUnavailable() {
            if (updateChecker?.isUpdateAvailable() == true) {
                setupUpdatePrefText(false)
            }
        }
    }

    private fun handleInternetSetting(networkAvailable: Boolean = true) {
        if (networkAvailable) {
            updatePreference?.summary = ""
        }
        else {
            updatePreference?.summary = getString(R.string.connect_internet_download_update)
        }
    }

    private val updateCallback = object : UpdateChecker.UpdateCallback {
        override fun onUpdateDownloaded(appUpdateManager: AppUpdateManager) {
            onUpdateDownloaded()
        }

        override fun onUpdateInstalled() {
            updatePreference?.isVisible = false
        }

        override fun onUpdateDownloading() {
            this@SettingsPreferenceFragment.onUpdateDownloading()
        }

        override fun onUpdateCancelledByUser() {
            updatePreference?.title = getString(R.string.update_available)
            handleInternetSetting(false)
        }

        override fun onUpdateSnackbarDismissed() {
        }
    }

    private fun onUpdateDownloaded() {
        context?.let {
            updatePreference?.title = it.getString(R.string.update_downloaded)
            updatePreference?.summary = it.getString(R.string.install_update)
        }
    }

    private fun onUpdateDownloading() {
        updatePreference?.title = getString(R.string.update_downloading)
        updatePreference?.summary = ""
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
        preference ?: return
        // Set the listener to watch for value changes.
        preference.onPreferenceChangeListener = bindPreferenceSummaryToValueListener

        // Trigger the listener immediately with the preference's
        // current value.
        bindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(
                                preference.context)
                        .getString(preference.key,
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
            } else if (preference.key == PREFS_THEME) {
                onThemeChanged(stringValue)
            }

        } else {
            preference.summary = stringValue
        }
        true
    }

    private fun onThemeChanged(themeValue: String) {
        val theme = Integer.parseInt(themeValue)
        preferences?.edit()?.putInt(CURRENT_THEME, theme)?.apply()
        Logger.log("TAG", "Current theme=" + this@SettingsPreferenceFragment
                .theme + " new theme=" + theme)
        if (this@SettingsPreferenceFragment.theme != theme) {
           Theme.setTheme(Theme.getThemeValue(theme))
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
        val enterAnim = android.R.anim.fade_in
        val exitAnim = android.R.anim.fade_out
        activity.overridePendingTransition(enterAnim, exitAnim)
        activity.finish()
        activity.overridePendingTransition(enterAnim, exitAnim)
        activity.startActivity(activity.intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (registeredNetworkCallback) {
            registeredNetworkCallback = false
            networkHelper?.unregisterNetworkRequest(connectivityManager)
        }
    }

    companion object {
        const val PREFS_ANALYTICS = "prefsAnalytics"
        const val PREF_ROOT = "prefRooted"

    }
}