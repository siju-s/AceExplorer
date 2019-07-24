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

package com.siju.acexplorer.main.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.home.model.CategoryEdit;
import com.siju.acexplorer.logging.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SharedPreferenceWrapper {

    private static final String PREFS_NAME = "PREFS";
    private static final String PREFS_NAME_TEMP = "PREFS_TEMP";

    private static final String FAVORITES_OLD = "Product_Favorite";
    private static final String LIBRARIES_OLD = "Library";

    private static final String FAVORITES = "Favorite";
    private static final String LIBRARIES = "Libs";
    private static final String TEMP_FILE_DATA = "File_data";
    private static final String TEMP_STRING_DATA = "String_data";


    public void addLibrary(Context context, CategoryEdit librarySortModel) {
        List<CategoryEdit> libraries = getLibraries(context);
        if (!libraries.contains(librarySortModel)) {
            libraries.add(librarySortModel);
            saveLibrary(context, libraries);
        }
        Logger.log("SharedWrapper", "addLibrary=" + libraries.size());

    }

    private ArrayList<CategoryEdit> getLibraries(Context context) {
        SharedPreferences preferences;
        ArrayList<CategoryEdit> libraries = new ArrayList<>();

        preferences = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        if (preferences.contains(LIBRARIES)) {
            String jsonFavorites = preferences.getString(LIBRARIES, null);
            Gson gson = new Gson();
            CategoryEdit[] libItems = gson.fromJson(jsonFavorites,
                    CategoryEdit[].class);
            libraries.addAll(Arrays.asList(libItems));
        }

        return libraries;
    }

    private void saveLibrary(Context context, List<CategoryEdit> librarySortModel) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(librarySortModel);
        Logger.log("SharedWrapper", "Save library=" + jsonFavorites);
        editor.putString(LIBRARIES, jsonFavorites);

        editor.apply();
    }
    public boolean removeOldPrefs(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        boolean hadOldPrefs = false;
        if (preferences.contains(LIBRARIES_OLD)) {
            hadOldPrefs = true;
            editor.remove(LIBRARIES_OLD).apply();
        }

        return hadOldPrefs;

    }

    private boolean removeOldFavPref(SharedPreferences preferences) {

        if (preferences.contains(FAVORITES_OLD)) {
            preferences.edit().remove(FAVORITES_OLD).apply();
            return true;
        }
        return false;
    }

    public void storeFileData(Context context, List<FileInfo> fileInfo) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME_TEMP,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        Gson gson = new Gson();
        String value = gson.toJson(fileInfo);
        Logger.log("SharedWrapper", "storeFileData=" + fileInfo.size());
        editor.putString(TEMP_FILE_DATA, value);
        editor.apply();
    }



    public List<FileInfo> getFileData(Context context) {
        SharedPreferences preferences;
        ArrayList<FileInfo> files = new ArrayList<>();

        preferences = context.getSharedPreferences(PREFS_NAME_TEMP,
                Context.MODE_PRIVATE);
        if (preferences.contains(TEMP_FILE_DATA)) {

            String jsonFavorites = preferences.getString(TEMP_FILE_DATA, null);
            Gson gson = new Gson();
            FileInfo[] libItems = gson.fromJson(jsonFavorites,
                    FileInfo[].class);
            files.addAll(Arrays.asList(libItems));
            Logger.log("SharedWrapper", "getFileData="+files.size());
        }
        return files;
    }

    public void removeFileData(Context context) {
        SharedPreferences preferences;
        preferences = context.getSharedPreferences(PREFS_NAME_TEMP,
                Context.MODE_PRIVATE);
        if (preferences.contains(TEMP_FILE_DATA)) {
            Logger.log("SharedWrapper", "removeFileData=");
            preferences.edit().remove(TEMP_FILE_DATA).apply();
        }
    }

    public void storeStringData(Context context, List<String> fileInfo) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME_TEMP,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        Gson gson = new Gson();
        String value = gson.toJson(fileInfo);
        Logger.log("SharedWrapper", "storeStringData=" + fileInfo.size());
        editor.putString(TEMP_STRING_DATA, value);
        editor.apply();
    }

    public void removeStringData(Context context) {
        SharedPreferences preferences;
        preferences = context.getSharedPreferences(PREFS_NAME_TEMP,
                Context.MODE_PRIVATE);
        if (preferences.contains(TEMP_STRING_DATA)) {
            Logger.log("SharedWrapper", "removeStringData=");
            preferences.edit().remove(TEMP_STRING_DATA).apply();
        }
    }

    public List<String> getStringData(Context context) {
        SharedPreferences preferences;
        ArrayList<String> files = new ArrayList<>();

        preferences = context.getSharedPreferences(PREFS_NAME_TEMP,
                Context.MODE_PRIVATE);
        if (preferences.contains(TEMP_STRING_DATA)) {

            String jsonFavorites = preferences.getString(TEMP_STRING_DATA, null);
            Gson gson = new Gson();
            String[] libItems = gson.fromJson(jsonFavorites,
                    String[].class);
            files.addAll(Arrays.asList(libItems));
            Logger.log("SharedWrapper", "getStringData="+files.size());
        }
        return files;
    }


}