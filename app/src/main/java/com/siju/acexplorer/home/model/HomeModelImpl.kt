package com.siju.acexplorer.home.model

import android.content.Context
import android.util.Log
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.home.types.HomeLibraryInfo
import com.siju.acexplorer.main.model.StorageItem
import com.siju.acexplorer.main.model.data.DataFetcherFactory
import com.siju.acexplorer.main.model.data.DataLoader
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.StorageFetcher
import com.siju.acexplorer.common.utils.ConfigurationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val TAG = "HomeModelImpl"
class HomeModelImpl @Inject constructor(@ApplicationContext val context: Context) : HomeModel {

    override fun getCategories(): ArrayList<HomeLibraryInfo> {
        return CategoryListFetcher.getCategories(context)
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

    override fun loadCountForCategory(category: Category, path : String?): FileInfo {
        val count = DataLoader.fetchDataCount(context, DataFetcherFactory.createDataFetcher(category), path)
        Log.d(TAG, "loadCountForCategory $category = $count")
        return FileInfo(category, count)
    }
}