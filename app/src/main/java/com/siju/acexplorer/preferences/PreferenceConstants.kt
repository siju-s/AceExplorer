package com.siju.acexplorer.preferences

import com.siju.acexplorer.common.SortMode

object PreferenceConstants {
    const val PREFS_HIDDEN = "prefHidden"
    const val KEY_SORT_MODE = "sort_mode"
    val DEFAULT_VALUE_SORT_MODE = SortMode.NAME.value
    val SORT_MODE_DATE_DESC = SortMode.DATE_DESC.value

}