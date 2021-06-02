package com.siju.acexplorer.storage.model

import android.content.SharedPreferences
import android.util.Log
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.preferences.PreferenceConstants
import java.security.InvalidParameterException

enum class SortMode(val value: Int) {
    NAME(0),
    NAME_DESC(1),
    TYPE(2),
    TYPE_DESC(3),
    SIZE(4),
    SIZE_DESC(5),
    DATE(6),
    DATE_DESC(7);

    companion object {
        fun getSortModeFromValue(value: Int): SortMode {
            return when (value) {
                0    -> NAME
                1    -> NAME_DESC
                2    -> TYPE
                3    -> TYPE_DESC
                4    -> SIZE
                5    -> SIZE_DESC
                6    -> DATE
                7    -> DATE_DESC
                else -> throw InvalidParameterException(
                        "Invalid sortmode mode value should be either ${NAME.value} or ${NAME_DESC.value}" +
                                "or ${TYPE.value} or ${TYPE_DESC.value} or ${SIZE.value} or ${SIZE_DESC.value} + " +
                                "${DATE.value} or ${DATE_DESC.value}")
            }
        }

        fun isAscending(sortMode: SortMode) = sortMode.value % 2 == 0

        fun getSortMode(preferences : SharedPreferences, category: Category?) : SortMode {
            Log.d(SortMode::class.java.simpleName, "getSortMode() called with: category = $category")
            val sortMode : Int = if (CategoryHelper.isCategorySortDifferent(category)) {
                PreferenceConstants.SORT_MODE_DATE_DESC
            }
            else {
                preferences.getInt(
                    PreferenceConstants.KEY_SORT_MODE,
                    PreferenceConstants.DEFAULT_VALUE_SORT_MODE)
            }
            return getSortModeFromValue(sortMode)
        }

        fun saveSortMode(preferences: SharedPreferences, sortMode: SortMode) {
            Log.d(SortMode::class.java.simpleName, "saveSortMode: value:$sortMode")
            preferences.edit().apply {
                putInt(PreferenceConstants.KEY_SORT_MODE, sortMode.value)
                apply()
            }
        }

    }

}