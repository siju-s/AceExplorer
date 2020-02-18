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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.*
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
import com.siju.acexplorer.main.viewmodel.MainViewModel
import com.siju.acexplorer.premium.Premium
import com.siju.acexplorer.theme.CURRENT_THEME
import com.siju.acexplorer.theme.PREFS_THEME
import com.siju.acexplorer.theme.Theme
import com.siju.acexplorer.utils.LocaleHelper
import com.siju.acexplorer.utils.NetworkHelper

const val PREFS_UPDATE = "prefsUpdate"
const val PREFS_LANGUAGE = "prefLanguage"
private const val PREFS_FULL_VERSION = "prefsUnlockFull"

class SettingsPreferenceFragment : PreferenceFragmentCompat() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var mainCommunicator: MainCommunicator
    private var preferences: SharedPreferences? = null
    private var updatePreference: Preference? = null
    private var currentLanguage: String? = null
    private var updateChecker: UpdateChecker? = null
    private var theme = 0
    private var connectivityManager : ConnectivityManager? = null
    private var networkHelper : NetworkHelper? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainCommunicator) {
            updateChecker = context.getUpdateChecker()
            mainCommunicator = context
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_settings, rootKey)
        setupUpdatePref()
        setupUnlockFullVersionPref()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        preferences = PreferenceManager.getDefaultSharedPreferences(context)

        setupViewModels()
        setupRootPref()
        setupLanguagePreference()
        setupThemePref()
        setupAnalyticsPref()
        setupResetFavPref()
    }

    private fun setupViewModels() {
        val activity = requireNotNull(activity)
        mainViewModel = ViewModelProvider(activity).get(MainViewModel::class.java)
        mainViewModel.premiumLiveData.observe(this, Observer {
            it?.apply {
                if (it.entitled) {
                    findPreference<Preference>(PREFS_FULL_VERSION)?.isVisible = false
                }
            }
        })
    }

    private fun setupUnlockFullVersionPref() {
        val preference = findPreference<Preference>(PREFS_FULL_VERSION)
        preference?.isVisible = !mainCommunicator.isPremiumVersion()
        preference?.setOnPreferenceClickListener {
            val activity = activity as AppCompatActivity?
            activity?.let {
                val premium = Premium(it, mainViewModel)
                premium.showPremiumDialog(it)
            }
            true
        }
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
        val value = LocaleHelper.getLanguage(activity)
        languagePreference?.value = value
        currentLanguage = value
        bindPreferenceSummaryToValue(languagePreference)
    }

    private fun setupThemePref() {
        val themePreference = findPreference<ListPreference>(PREFS_THEME)
        theme = Theme.getUserThemeValue(activity!!)
        bindPreferenceSummaryToValue(themePreference)
    }

    private fun setupAnalyticsPref() {
        val analyticsPreference = findPreference(PREFS_ANALYTICS) as CheckBoxPreference?
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
        updatePreference = findPreference<Preference?>(PREFS_UPDATE)
        val updateAvailable = updateChecker?.isUpdateAvailable()
        if (updateAvailable == false) {
            return
        }
        networkHelper = NetworkHelper(networkChangeCallback)
        setupUpdatePrefText(NetworkHelper.isConnectedToInternet(context))
        connectivityManager = networkHelper?.getConnectivityManager(context)
        networkHelper?.registerNetworkRequest(connectivityManager)
        updatePreference?.isVisible = true
        initUpdatePrefListener()
    }

    private fun setupUpdatePrefText(networkAvailable : Boolean = true) {
        handler.post {
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
        updateChecker?.setUpdateCallback(updateCallback)
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
        updatePreference?.title = getString(R.string.update_downloaded)
        updatePreference?.summary = getString(R.string.install_update)
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
            } else if (preference.key == PREFS_THEME) {
                onThemeChanged(stringValue)
            }

        } else {
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
        val enterAnim = android.R.anim.fade_in
        val exitAnim = android.R.anim.fade_out
        activity.overridePendingTransition(enterAnim, exitAnim)
        activity.finish()
        activity.overridePendingTransition(enterAnim, exitAnim)
        activity.startActivity(activity.intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        networkHelper?.unregisterNetworkRequest(connectivityManager)
    }

    companion object {
        const val PREFS_ANALYTICS = "prefsAnalytics"
        const val PREF_ROOT = "prefRooted"

    }
}