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
package com.siju.acexplorer.picker

import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.siju.acexplorer.R
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.settings.SettingsPreferenceFragment
import com.siju.acexplorer.storage.modules.picker.types.PickerType
import com.siju.acexplorer.storage.modules.picker.view.PickerFragment.Companion.newInstance
import com.siju.acexplorer.theme.Theme

private const val FRAGMENT_TAG = "Browse_Frag"

class TransparentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sendAnalytics = PreferenceManager.getDefaultSharedPreferences(this).
                getBoolean(SettingsPreferenceFragment.PREFS_ANALYTICS, true)
        Analytics.logger.sendAnalytics(sendAnalytics)
        Analytics.logger.register(this)
        Analytics.logger.reportDeviceName()
        val intent = intent
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action != null) {
            when (intent.action) {
                RingtoneManager.ACTION_RINGTONE_PICKER -> showPickerDialog(intent, PickerType.RINGTONE)
                Intent.ACTION_GET_CONTENT -> showPickerDialog(intent, PickerType.FILE)
            }
        }
    }

    private fun showPickerDialog(intent: Intent, pickerType: PickerType) {
        val dialogFragment = newInstance(this, checkTheme(),
                pickerType,
                intent.getIntExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, 0))
        dialogFragment.show(supportFragmentManager, FRAGMENT_TAG)
    }

    private fun checkTheme(): Int {
        return if (Theme.isDarkColoredTheme(baseContext.resources, Theme.getTheme(baseContext))) {
            R.style.TransparentTheme_DarkAppTheme_NoActionBar
        } else {
            R.style.TransparentTheme_AppTheme_NoActionBar
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        finish()
    }
}