package com.siju.acexplorer.main.model.data

import android.content.Context
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import java.util.*

object DataLoader {

    fun fetchDataByCategory(context: Context, dataFetcher: DataFetcher, category: Category, currentDir: String? = null): ArrayList<FileInfo> {
        return dataFetcher.fetchData(context, currentDir, category)
    }

    fun fetchDataCount(context: Context, dataFetcher: DataFetcher, path: String? = null): Int {
        return dataFetcher.fetchCount(context, path)
    }
}
