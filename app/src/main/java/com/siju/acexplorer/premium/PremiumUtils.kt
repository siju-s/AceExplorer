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
package com.siju.acexplorer.premium

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.siju.acexplorer.BuildConfig
import com.siju.acexplorer.main.viewmodel.MainViewModel
import java.util.*

class PremiumUtils {
    private var optOut = false
    private var installDate = Date()
    private var askLaterDate = Date()
    private var launchTimes = 0

    fun onStart(context: Context) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = pref.edit()
        // If it is the first launch, save the date in shared preference.
        if (isFirstLaunch(pref)) {
            storeInstallDate(context, editor)
        }
        launchTimes = pref.getInt(KEY_LAUNCH_TIMES, 0)
        launchTimes++
        storeLaunchTime(editor, launchTimes)
        installDate = Date(pref.getLong(KEY_INSTALL_DATE, 0))
        askLaterDate = Date(pref.getLong(KEY_ASK_LATER_DATE, 0))
        optOut = pref.getBoolean(KEY_OPT_OUT, false)
        printStatus(context)
    }

    private fun isFirstLaunch(pref: SharedPreferences) = pref.getLong(KEY_INSTALL_DATE, 0) == 0L

    private fun storeLaunchTime(editor: SharedPreferences.Editor, launchTimes: Int) {
        editor.putInt(KEY_LAUNCH_TIMES, launchTimes)
        log("Launch times; $launchTimes")
        editor.apply()
    }

    fun showPremiumDialogIfNeeded(activity: AppCompatActivity, mainViewModel: MainViewModel): Boolean {
        return if (shouldShowPremiumDialog() && mainViewModel.premiumLiveData.value?.entitled == false) {
            val premium = Premium(activity, mainViewModel)
            premium.showPremiumDialog(activity)
            val pref = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = pref.edit()
            storeLaunchTime(editor, 0)
            true
        } else {
            false
        }
    }

    private fun shouldShowPremiumDialog(): Boolean {
        return if (optOut) {
            false
        } else {
            val time = Date().time
            time - installDate.time >= MAX_DAYS_MILLIS && launchTimes >= MAX_LAUNCH_TIMES
        }
    }

    companion object {
        private val TAG = PremiumUtils::class.java.simpleName
        const val PREF_NAME = "PremiumUtils"
        private const val KEY_INSTALL_DATE = "install_date"
        private const val KEY_LAUNCH_TIMES = "launch_times"
        private const val KEY_ASK_LATER_DATE = "ask_later_date"
        private const val MAX_LAUNCH_TIMES = 15
        private const val MAX_DAYS_MILLIS = 7 * 24 * 60 * 60 * 1000L
        const val KEY_OPT_OUT = "opt_out"

        private fun printStatus(context: Context) {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            log("*** Premium Status ***")
            log("Install Date: " + Date(pref.getLong(KEY_INSTALL_DATE, 0)))
            log("Launch Times: " + pref.getInt(KEY_LAUNCH_TIMES, 0))
        }

        /**
         * Store install date.
         * Install date is retrieved from package manager if possible.
         *
         * @param context
         * @param editor
         */
        private fun storeInstallDate(context: Context, editor: SharedPreferences.Editor) {
            var installDate = Date()
            val packMan = context.packageManager
            try {
                val pkgInfo = packMan.getPackageInfo(context.packageName, 0)
                installDate = Date(pkgInfo.firstInstallTime)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            editor.putLong(KEY_INSTALL_DATE, installDate.time)
            log("First install: $installDate")
        }

        /**
         * Print log if enabled
         *
         * @param message
         */
        private fun log(message: String) {
            if (BuildConfig.DEBUG) {
                Log.v(TAG, message)
            }
        }
    }
}