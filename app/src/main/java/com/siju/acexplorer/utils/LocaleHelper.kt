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
package com.siju.acexplorer.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.LocaleList
import androidx.preference.PreferenceManager
import com.siju.acexplorer.logging.Logger.log
import com.siju.acexplorer.main.model.helper.SdkHelper.isAtleastNougat
import java.util.*

private const val TAG = "LocaleHelper"
private const val SELECTED_LANGUAGE = "prefs_lang"

object LocaleHelper {

    fun getLanguage(context: Context?): String? {
        context ?: return null
        return getPersistedData(context, Locale.getDefault().language)
    }

    private fun getPersistedData(context: Context, defaultLanguage: String): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage)
    }

    fun setLanguage(context: Context): Context {
        val currentLanguage = getLanguage(context)
        log(TAG, "setLanguage: current:" + currentLanguage + " default:" + Locale.getDefault().language)
        if (currentLanguage != Locale.getDefault().language) {
            persist(context, currentLanguage)
        }
        return updateResources(context, currentLanguage)
    }

    fun persist(context: Context?, language: String?) {
        context ?: return
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        log(TAG, "persist: $language")
        editor.putString(SELECTED_LANGUAGE, language)
        editor.apply()
    }

    @SuppressLint("NewApi")
    private fun updateResources(context: Context, language: String?): Context {
        if (language == null) {
            return context
        }
        val locale = Locale(language)
        log(TAG, "updateResources: $language")
        val resources = context.resources
        val config = resources.configuration
        return if (isAtleastNougat) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocales(localeList)
            log(TAG, "updateResources: new config:$config")
            context.createConfigurationContext(config)
        } else {
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            context.createConfigurationContext(config)
        }
    }
}
