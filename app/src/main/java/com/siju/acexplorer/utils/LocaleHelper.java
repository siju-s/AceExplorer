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

package com.siju.acexplorer.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.LocaleList;

import androidx.preference.PreferenceManager;

import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.main.model.helper.SdkHelper;

import java.util.Locale;


public class LocaleHelper extends ContextWrapper {


    private static final String SELECTED_LANGUAGE = "prefs_lang";
    private static final String TAG                  = "LocaleHelper";


    public LocaleHelper(Context base) {
        super(base);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }

    public static Context setLanguage(Context context) {
        String currentLanguage = getLanguage(context);
        Logger.log(TAG, "setLanguage: current:"+currentLanguage + " default:"+Locale.getDefault().getLanguage());
        if (!currentLanguage.equals(Locale.getDefault().getLanguage())) {
            persist(context, currentLanguage);
        }
        context = updateResources(context, currentLanguage);
        return context;
    }


    public static void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        Logger.log(TAG, "persist: "+language);
        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Logger.log(TAG, "updateResources: "+language);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        if (SdkHelper.INSTANCE.isAtleastNougat()) {
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            config.setLocales(localeList);
            Logger.log(TAG, "updateResources: new config:"+config);
            context = context.createConfigurationContext(config);
        }
        else {
            config.setLocale(locale);
            config.setLayoutDirection(locale);
            context = context.createConfigurationContext(config);
        }
        return context;

    }


}