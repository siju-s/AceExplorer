package com.siju.acexplorer.main.model.data

import android.content.Context
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.home.model.FavoriteHelper
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.RootHelper
import com.siju.acexplorer.main.model.helper.SortHelper
import java.io.File

class FavoriteDataFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {
        val favoritePaths = getFavorites(context)
        return getFavoriteData(favoritePaths, getSortMode(context))
    }

    override fun fetchCount(context: Context, path: String?): Int {
        return getFavorites(context).size
    }

    private fun getFavorites(context: Context): ArrayList<String> {
        return FavoriteHelper.getFavorites(context)
    }

    private fun getFavoriteData(favoritePaths: ArrayList<String>, sortMode: Int): ArrayList<FileInfo> {
        val fileInfoList = ArrayList<FileInfo>()
        for (path in favoritePaths) {
            val file = File(path)
            val fileName = file.name
            var childFileListSize: Long = 0
            val filesList = file.list()
            if (filesList != null) {
                childFileListSize = filesList.size.toLong()
            }
            val date = file.lastModified()

            val fileInfo = FileInfo(Category.FILES, fileName, path, date, childFileListSize,
                    true, null, RootHelper.parseFilePermission(File(path)), false)
            fileInfoList.add(fileInfo)
        }
        return SortHelper.sortFiles(fileInfoList, sortMode)
    }

}
