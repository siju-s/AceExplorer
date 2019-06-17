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
import com.siju.acexplorer.home.model.LibrarySortModel;
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




    private void saveFavorites(Context context, List<FavInfo> favorites) {
        if (context == null) {
            return;
        }
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(favorites);
        editor.putString(FAVORITES, jsonFavorites);
        editor.apply();
    }

    public void addFavorite(Context context, FavInfo favInfo) {
        List<FavInfo> favorites = getFavorites(context);

        if (!favorites.contains(favInfo)) {
            favorites.add(favInfo);
            saveFavorites(context, favorites);
        }
    }

    public int addFavorites(Context context, ArrayList<FavInfo> favInfoArrayList) {
        if (context == null) {
            return 0;
        }
        List<FavInfo> favorites = getFavorites(context);

        int count = 0;
        for (FavInfo favInfo : favInfoArrayList) {
            if (!favorites.contains(favInfo)) {
                favorites.add(favInfo);
                count++;
            }
        }
        if (count != 0) {
            saveFavorites(context, favorites);
        }
        return count;
    }

    public boolean removeFavorite(Context context, FavInfo favInfo) {
        if (context == null) {
            return false;
        }
        ArrayList<FavInfo> favorites = getFavorites(context);
        boolean isDeleted = false;
        if (favorites != null) {
            if (favorites.remove(favInfo)) {
                isDeleted = true;
                saveFavorites(context, favorites);
            }
        }
        return isDeleted;
    }


    public ArrayList<FavInfo> getFavorites(Context context) {
        SharedPreferences sharedPreferences;
        ArrayList<FavInfo> favorites = new ArrayList<>();

        if (context == null) {
            return favorites;
        }

        sharedPreferences = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        removeOldFavPref(sharedPreferences);
        if (sharedPreferences.contains(FAVORITES)) {
            String jsonFavorites = sharedPreferences.getString(FAVORITES, null);
            Gson gson = new Gson();
            FavInfo[] favoriteItems = gson.fromJson(jsonFavorites,
                    FavInfo[].class);
            favorites.addAll(Arrays.asList(favoriteItems));
        }

        return favorites;
    }

    public void addLibrary(Context context, LibrarySortModel librarySortModel) {
        List<LibrarySortModel> libraries = getLibraries(context);
        if (!libraries.contains(librarySortModel)) {
            libraries.add(librarySortModel);
            saveLibrary(context, libraries);
        }
        Logger.log("SharedWrapper", "addLibrary=" + libraries.size());

    }


    public ArrayList<LibrarySortModel> getLibraries(Context context) {
        SharedPreferences preferences;
        ArrayList<LibrarySortModel> libraries = new ArrayList<>();

        preferences = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        if (preferences.contains(LIBRARIES)) {
            String jsonFavorites = preferences.getString(LIBRARIES, null);
            Gson gson = new Gson();
            LibrarySortModel[] libItems = gson.fromJson(jsonFavorites,
                    LibrarySortModel[].class);
            libraries.addAll(Arrays.asList(libItems));
        }

        return libraries;
    }

    public void saveLibrary(Context context, List<LibrarySortModel> librarySortModel) {
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




    public void updateFavoritePath(Context context, String oldFile, String newFile) {
        ArrayList<FavInfo> favList = getFavorites(context);
        Logger.log("SharedWrapper", "updateFavoritePath: " + favList.size());
        FavInfo fav = null;
        FavInfo newFavInfo = null;
        for (FavInfo favInfo : favList) {
            if (favInfo.getFilePath().equals(oldFile)) {
                fav = favInfo;
                newFavInfo = favInfo;
                newFavInfo.setFilePath(newFile);
                break;
            }
        }
        if (fav != null) {
            favList.remove(fav);
            favList.add(newFavInfo);
            Logger.log("SharedWrapper", "updateFavoritePath NEW: " + favList.size() + " newFavInfo:" + newFavInfo.getFilePath());
            saveFavorites(context, favList);
        }

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