package com.siju.acexplorer.home.model

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.home.types.HomeLibraryInfo
import com.siju.acexplorer.main.model.StorageItem
import com.siju.acexplorer.main.model.data.DataFetcherFactory
import com.siju.acexplorer.main.model.data.DataLoader
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.StorageFetcher
import com.siju.acexplorer.utils.ConfigurationHelper

private const val TAG = "HomeModelImpl"
class HomeModelImpl(val context: Context) : HomeModel {
    lateinit var libraries: LiveData<ArrayList<HomeLibraryInfo>>

    override fun getCategories(): ArrayList<HomeLibraryInfo> {
        return CategoryCreator.getCategories(context)
    }

    override fun getStorage(): ArrayList<StorageItem> {
        return StorageFetcher(context).getStorageList()
    }

    override fun saveCategories(categories: ArrayList<Category>) {
        CategorySaver.saveCategories(context, ArrayList(categories.map { it.value }))
    }

    override fun getCategoryGridCols(): Int {
        return ConfigurationHelper.getHomeGridCols(context.resources.configuration)
    }

    override fun loadCountForCategory(category: Category): FileInfo {
        val count = DataLoader.fetchDataCount(context, DataFetcherFactory.createDataFetcher(category), null)
        Log.e(TAG, "loadCountForCategory $category = $count")
        return FileInfo(category, count)
    }
}