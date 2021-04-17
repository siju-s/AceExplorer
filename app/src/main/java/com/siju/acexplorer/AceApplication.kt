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

package com.siju.acexplorer

import android.app.Application
import android.os.StrictMode
import com.kobakei.ratethisapp.RateThisApp
import com.siju.acexplorer.theme.Theme
import dagger.hilt.android.HiltAndroidApp

private const val RATE_APP_CRITERIA_INSTALL_DAYS = 7
private const val RATE_APP_CRITERIA_LAUNCH_TIMES = 25

@HiltAndroidApp
class AceApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Theme.setTheme(Theme.getTheme(applicationContext))
        appContext = this
        initRateApp()
        setupPerformanceCheckers()
    }

    private fun initRateApp() {
        val config = RateThisApp.Config(RATE_APP_CRITERIA_INSTALL_DAYS, RATE_APP_CRITERIA_LAUNCH_TIMES)
        RateThisApp.init(config)
    }

    private fun setupPerformanceCheckers() {
        if (BuildConfig.DEBUG) {
//            setupStrictMode()
        }
    }

    private fun setupStrictMode() {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll()
                .penaltyLog().build())
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build())
    }

    companion object {
        lateinit var appContext: AceApplication
            private set
    }

}
