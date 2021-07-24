package com.siju.acexplorer.common

import android.content.SharedPreferences
import android.util.Log

class SortModeData(val preferences: SharedPreferences) {

    fun getSortMode(): SortMode {
        val sortMode =
            preferences.getInt(
                PreferenceConstants.KEY_SORT_MODE,
                PreferenceConstants.DEFAULT_VALUE_SORT_MODE
            )
        return SortMode.getSortModeFromValue(sortMode)
    }

    fun saveSortMode(sortMode: SortMode) {
        Log.d(SortMode::class.java.simpleName, "saveSortMode: value:$sortMode")
        preferences.edit().apply {
            putInt(PreferenceConstants.KEY_SORT_MODE, sortMode.value)
            apply()
        }
    }
}