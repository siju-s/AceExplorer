package com.siju.filemanager.common;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.siju.filemanager.filesystem.model.FavInfo;
import com.siju.filemanager.filesystem.FileConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Siju on 27-06-2016.
 */

public class SharedPreference {

    public static final String PREFS_NAME = "PREFS";
    public static final String FAVORITES = "Product_Favorite";
    public static final String PREFS_VIEW_MODE = "view-mode";

    public SharedPreference() {
        super();
    }

    // This four methods are used for maintaining favorites.
    public void saveFavorites(Context context, List<FavInfo> favorites) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(favorites);

        editor.putString(FAVORITES, jsonFavorites);

        editor.apply();
    }

    public void addFavorite(Context context, FavInfo favInfo) {
        List<FavInfo> favorites = getFavorites(context);
        if (favorites == null)
            favorites = new ArrayList<FavInfo>();
        favorites.add(favInfo);
        saveFavorites(context, favorites);
    }

    public void removeFavorite(Context context, FavInfo favInfo) {
        ArrayList<FavInfo> favorites = getFavorites(context);
        if (favorites != null) {
            favorites.remove(favInfo);
            saveFavorites(context, favorites);
        }
    }

    public ArrayList<FavInfo> getFavorites(Context context) {
        SharedPreferences settings;
        List<FavInfo> favorites;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        if (settings.contains(FAVORITES)) {
            String jsonFavorites = settings.getString(FAVORITES, null);
            Gson gson = new Gson();
            FavInfo[] favoriteItems = gson.fromJson(jsonFavorites,
                    FavInfo[].class);

            favorites = Arrays.asList(favoriteItems);
            favorites = new ArrayList<FavInfo>(favorites);
        } else
            return null;

        return (ArrayList<FavInfo>) favorites;
    }

    public void savePrefs(Context context, int viewMode) {
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;

        sharedPreferences = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putInt(PREFS_VIEW_MODE, viewMode);
        editor.apply();
    }

    public int getViewMode(Context context) {
        SharedPreferences sharedPreferences;
        int mode;

        sharedPreferences = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        if (sharedPreferences.contains(PREFS_VIEW_MODE)) {
            mode = sharedPreferences.getInt(PREFS_VIEW_MODE, FileConstants.KEY_LISTVIEW);
        } else {
            return FileConstants.KEY_LISTVIEW;
        }
        return mode;
    }


}