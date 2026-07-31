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
package com.siju.acexplorer.welcome

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.siju.acexplorer.common.theme.MyApplicationTheme
import com.siju.acexplorer.common.theme.Theme
import com.siju.acexplorer.main.AceActivity
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "WelcomeActivity"
private const val SCHEMA_PACKAGE = "package"

@AndroidEntryPoint
class WelcomeActivity : ComponentActivity() {

    private val viewModel: WelcomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (!viewModel.isFirstLaunch) {
            launchHomeScreen()
            return
        }

        setContent {
            MyApplicationTheme(appTheme = Theme.getTheme(this)) {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(uiState.welcomeFinished) {
                    if (uiState.welcomeFinished) {
                        launchHomeScreen()
                    }
                }

                WelcomeScreen(
                    hasStorageAccess = uiState.hasStorageAccess,
                    onGrantStorageAccess = ::requestAllFilesAccess,
                    onWelcomeComplete = viewModel::onWelcomeCompleted
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // All Files Access is granted in system settings, so the result has to be re-read here
        // rather than trusting an activity result.
        viewModel.refreshStorageAccess()
    }

    private fun requestAllFilesAccess() {
        val appIntent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = Uri.fromParts(SCHEMA_PACKAGE, packageName, null)
        }
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            // Some OEM builds do not expose the per-app screen. Welcome cannot be dismissed without
            // the permission, so fall back to the all-apps list rather than stranding the user.
            Log.w(TAG, "Per-app all files access screen unavailable", e)
            try {
                startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
            } catch (e: ActivityNotFoundException) {
                Log.e(TAG, "No all files access settings screen available", e)
            }
        }
    }

    private fun launchHomeScreen() {
        startActivity(Intent(this, AceActivity::class.java))
        finish()
    }
}
