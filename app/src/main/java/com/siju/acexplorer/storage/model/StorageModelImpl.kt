package com.siju.acexplorer.storage.model

import android.content.Context
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.data.DataFetcherFactory
import com.siju.acexplorer.main.model.data.DataLoader
import com.siju.acexplorer.main.model.groups.Category
import java.util.*

private const val PREFS_NAME = "PREFS"
private const val PREFS_VIEW_MODE = "view-mode"

class StorageModelImpl(val context: Context) : StorageModel {


    override fun loadData(path: String?, category: Category): ArrayList<FileInfo> {
        return DataLoader.fetchDataByCategory(context,
                                              DataFetcherFactory.createDataFetcher(category),
                                              category, path)
    }

    override fun getViewMode(): Int {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (sharedPreferences.contains(PREFS_VIEW_MODE)) {
            sharedPreferences.getInt(PREFS_VIEW_MODE, ViewMode.LIST)
        }
        else {
            ViewMode.LIST
        }
    }
}