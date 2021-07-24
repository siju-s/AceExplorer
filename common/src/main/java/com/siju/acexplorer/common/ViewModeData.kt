package com.siju.acexplorer.common

import android.content.SharedPreferences

private const val PREFS_VIEW_MODE = "view-mode"
class ViewModeData(val preferences: SharedPreferences) {

    fun getViewMode(): ViewMode {
        return if (preferences.contains(PREFS_VIEW_MODE)) {
            ViewMode.getViewModeFromValue(
                preferences.getInt(PREFS_VIEW_MODE, ViewMode.LIST.value))
        } else {
            ViewMode.LIST
        }
    }

    fun saveViewMode(viewMode: ViewMode?) {
        viewMode?.let {
            preferences.edit().apply {
                putInt(PREFS_VIEW_MODE, viewMode.value)
                apply()
            }
        }
    }
}