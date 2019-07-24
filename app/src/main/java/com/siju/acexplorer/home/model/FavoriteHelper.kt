package com.siju.acexplorer.home.model

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.siju.acexplorer.main.model.StorageUtils
import java.util.*
import kotlin.collections.ArrayList

private const val FAVORITES = "Favorites"
private const val FAVORITES_OLD = "Product_Favorite"

//TODO Use DB for storing favorites later when moving to Android Q SDK
object FavoriteHelper {

    private fun saveFavorites(context: Context, favPaths: ArrayList<String>) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        saveToPreference(preferences, Gson().toJson(favPaths))
    }

    private fun saveToPreference(preferences: SharedPreferences, favorites: String) {
        with(preferences.edit()) {
            putString(FAVORITES, favorites)
            apply()
        }
    }

    fun addFavorites(context: Context, favList: ArrayList<String>): Int {
        val favorites = getFavorites(context)
        var count = 0
        for (favorite in favList) {
            if (!favorites.contains(favorite)) {
                favorites.add(favorite)
                count++
            }
        }
        if (count > 0) {
            saveFavorites(context, favorites)
        }
        return count
    }

    fun getFavorites(context: Context): ArrayList<String> {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val favorites = ArrayList<String>()

        removeOldFavPref(sharedPreferences)
        if (sharedPreferences.contains(FAVORITES)) {
            val jsonFavorites = sharedPreferences.getString(FAVORITES, null)
            val favoriteItems = Gson().fromJson(jsonFavorites,
                    Array<String>::class.java)
            favorites.addAll(Arrays.asList(*favoriteItems))
        }
        return favorites
    }

    private fun removeOldFavPref(preferences: SharedPreferences): Boolean {

        if (preferences.contains(FAVORITES_OLD)) {
            preferences.edit().remove(FAVORITES_OLD).apply()
            return true
        }
        return false
    }

    private fun removeFavorite(favorites: ArrayList<String>, favorite: String): Boolean {
        return favorites.remove(favorite)
    }

    fun removeFavorites(context: Context, favList: ArrayList<String>): Int {
        val favorites = getFavorites(context)
        var deletedCount = 0
        for (favToDelete in favList) {
            val deleted = removeFavorite(favorites, favToDelete)
            if (deleted) {
                deletedCount++
            }
        }
        if (deletedCount > 0) {
            saveFavorites(context, favorites)
        }
        return deletedCount
    }

    fun resetFavorites(context: Context?) {
        if (context == null) {
            return
        }
        val favorites = getFavorites(context)
        for (i in favorites.indices.reversed()) {
            if (isNotDownloadDirectory(favorites, i)) {
                favorites.removeAt(i)
            }
        }
        saveFavorites(context, favorites)
    }

    private fun isNotDownloadDirectory(favorites: ArrayList<String>, i: Int) =
            !favorites[i].equals(StorageUtils.downloadsDirectory, ignoreCase = true)
}