package com.siju.acexplorer.common

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val PREFS_VIEW_MODE = "view-mode"
class ViewModeData(val preferences: SharedPreferences) {
    private val _viewMode = MutableStateFlow(ViewMode.LIST)

    fun getViewMode(): StateFlow<ViewMode> {
        val mode =  if (preferences.contains(PREFS_VIEW_MODE)) {
            ViewMode.getViewModeFromValue(
                preferences.getInt(PREFS_VIEW_MODE, ViewMode.LIST.value))
        } else {
            ViewMode.LIST
        }
        _viewMode.value = mode
        return _viewMode
    }

    fun saveViewMode(viewMode: ViewMode?) {
        viewMode?.let {
            preferences.edit().apply {
                putInt(PREFS_VIEW_MODE, viewMode.value)
                apply()
            }
            _viewMode.value = viewMode
        }
    }
}