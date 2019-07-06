package com.siju.acexplorer.storage.model

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.data.DataFetcherFactory
import com.siju.acexplorer.main.model.data.DataLoader
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.preferences.PreferenceConstants
import java.util.*

private const val PREFS_NAME = "PREFS"
private const val PREFS_VIEW_MODE = "view-mode"

class StorageModelImpl(val context: Context) : StorageModel {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val globalPreference = PreferenceManager.getDefaultSharedPreferences(context)

    override fun loadData(path: String?, category: Category): ArrayList<FileInfo> {
        return DataLoader.fetchDataByCategory(context,
                                              DataFetcherFactory.createDataFetcher(category),
                                              category, path)
    }

    override fun getViewMode(): ViewMode {
        return if (sharedPreferences.contains(PREFS_VIEW_MODE)) {
            ViewMode.getViewModeFromValue(
                    sharedPreferences.getInt(PREFS_VIEW_MODE, ViewMode.LIST.value))
        }
        else {
            ViewMode.LIST
        }
    }

    override fun saveViewMode(viewMode: ViewMode?) {
        viewMode?.let {
            sharedPreferences.edit().apply {
                putInt(PREFS_VIEW_MODE, viewMode.value)
                apply()
            }
        }
    }

    override fun shouldShowHiddenFiles() = globalPreference.getBoolean(
            PreferenceConstants.PREFS_HIDDEN, false)

    override fun saveHiddenFileSetting(value: Boolean) {
        Log.e(this.javaClass.name, "saveHiddenFileSetting: value:$value")
        globalPreference.edit().apply {
            putBoolean(PreferenceConstants.PREFS_HIDDEN, value)
            apply()
        }
    }

    override fun getSortMode(): SortMode {
        return SortMode.getSortModeFromValue(
                globalPreference.getInt(
                        PreferenceConstants.KEY_SORT_MODE,
                        PreferenceConstants.DEFAULT_VALUE_SORT_MODE))
    }

    override fun saveSortMode(sortMode: SortMode) {
        Log.e(this.javaClass.name, "saveSortMode: value:$sortMode")
        globalPreference.edit().apply {
            putInt(PreferenceConstants.KEY_SORT_MODE, sortMode.value)
            apply()
        }
    }

    override fun renameFile(filePath: String?, newName: String?) {
        if (FileUtils.isFileNameInvalid(newName)) {
            return
        }
    }


}