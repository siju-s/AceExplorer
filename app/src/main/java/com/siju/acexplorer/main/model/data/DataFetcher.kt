package com.siju.acexplorer.main.model.data

import android.content.Context
import android.database.Cursor
import android.util.Log
import androidx.preference.PreferenceManager
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.preferences.PreferenceConstants
import com.siju.acexplorer.storage.model.SortMode
import java.util.*

interface DataFetcher {

    fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo>
    fun fetchCount(context: Context, path: String? = null): Int

    fun fetchData(context: Context, path: String?, category: Category, ringtonePicker: Boolean) : ArrayList<FileInfo>{
        return arrayListOf()
    }

    fun getSortMode(context: Context, category: Category = Category.FILES): Int {
        Log.d(this.javaClass.simpleName, "getSortMode() called with: category = $category")
        return SortMode.getSortMode(PreferenceManager.getDefaultSharedPreferences(context), category).value
    }

    companion object {

        fun canShowHiddenFiles(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(PreferenceConstants.PREFS_HIDDEN, false)
        }
    }

    fun getCursorCount(cursor: Cursor?): Int {
        val count = cursor?.count
        cursor?.close()
        return count ?: 0
    }
}