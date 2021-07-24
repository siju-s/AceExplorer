package com.siju.acexplorer.storage.helper

import android.content.SharedPreferences
import com.siju.acexplorer.common.SortMode
import com.siju.acexplorer.common.SortMode.*
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.preferences.PreferenceConstants
import java.security.InvalidParameterException

object SortModeHelper {
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
                        "${DATE.value} or ${DATE_DESC.value}"
            )
        }
    }


    fun getSortMode(preferences: SharedPreferences, category: Category?): SortMode {
        val sortMode: Int = if (CategoryHelper.isCategorySortDifferent(category)) {
            PreferenceConstants.SORT_MODE_DATE_DESC
        }
        else {
            preferences.getInt(
                PreferenceConstants.KEY_SORT_MODE,
                PreferenceConstants.DEFAULT_VALUE_SORT_MODE
            )
        }
        return getSortModeFromValue(sortMode)
    }
}