package com.siju.acexplorer.home.model

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import androidx.preference.PreferenceManager

private const val CATEGORIES = "Categories"
private const val DELIMITER_CATEGORIES = ";"


object CategorySaver {

    fun saveCategories(context: Context, categoryIds: ArrayList<Int>) {
        val categories = TextUtils.join(DELIMITER_CATEGORIES, categoryIds)
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        saveToPreference(preferences, CATEGORIES, categories)
    }

    @Suppress("SameParameterValue")
    //Suppressing to allow caller to provide name of the key
    private fun saveToPreference(preferences: SharedPreferences, key: String, categories: String?) {
        with(preferences.edit()) {
            putString(key, categories)
            apply()
        }
    }

    fun getSavedCategories(context: Context): ArrayList<Int> {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val categories = preferences.getString(CATEGORIES, null)
        val categoryIds = arrayListOf<Int>()
        val splitCategories = categories?.split(DELIMITER_CATEGORIES)
        splitCategories?.let {
            for (category in splitCategories) {
                categoryIds.add(category.toInt())
            }
        }
        return categoryIds
    }
}