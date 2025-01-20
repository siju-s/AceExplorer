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
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import com.siju.acexplorer.common.coil.AppIconFetcher
import com.siju.acexplorer.common.theme.Theme
import dagger.hilt.android.HiltAndroidApp

private const val RATE_APP_CRITERIA_INSTALL_DAYS = 7
private const val RATE_APP_CRITERIA_LAUNCH_TIMES = 25

@HiltAndroidApp
class AceApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        Theme.setTheme(Theme.getTheme(applicationContext))
        appContext = this
//        initRateApp()
    }

//    private fun initRateApp() {
//        val config = RateThisApp.Config(RATE_APP_CRITERIA_INSTALL_DAYS, RATE_APP_CRITERIA_LAUNCH_TIMES)
//        RateThisApp.init(config)
//    }

    companion object {
        lateinit var appContext: AceApplication
            private set
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .components {
                add(AppIconFetcher.Factory(context))
            }
            .build()
    }
}
