package com.siju.acexplorer.main.model.data

import android.content.Context
import android.database.Cursor
import androidx.preference.PreferenceManager
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.FileConstants
import com.siju.acexplorer.main.model.groups.Category
import java.util.*

interface DataFetcher {

    fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo>
    fun fetchCount(context: Context, path: String? = null): Int

    fun getSortMode(context: Context): Int {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(
                FileConstants.KEY_SORT_MODE, FileConstants.KEY_SORT_NAME)
    }

    fun canShowHiddenFiles(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(FileConstants.PREFS_HIDDEN, false)
    }

    fun getCursorCount(cursor: Cursor?): Int {
        val count = cursor?.count
        cursor?.close()
        return count ?: 0
    }
}