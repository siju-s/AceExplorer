package com.siju.acexplorer.main.model.data.camera

import android.content.Context
import android.util.Log
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.data.FileDataFetcher
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.SortHelper
import com.siju.acexplorer.storage.model.SortMode
import java.io.File

private const val TAG = "FileDataFetcher"

class CameraGenericFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?,
                           category: Category): ArrayList<FileInfo> {
        path?.let {
            val data = FileDataFetcher.getFilesList(it, false, canShowHiddenFiles(context))
            return getCategoryList(data)
        }
        return ArrayList()
    }

    private fun getCategoryList(fileList: java.util.ArrayList<FileInfo>): java.util.ArrayList<FileInfo> {
        val categories = java.util.ArrayList<Category>()
        SortHelper.sortFiles(fileList, SortMode.TYPE.value)
        var count = 0
        val fileInfoList = java.util.ArrayList<FileInfo>()
        for (fileInfo in fileList) {
            val category = fileInfo.category
            Log.e(TAG, "Category:$category, count : $count")
            if (!categories.contains(category)) {
                count = 1
                val itemFileInfo = FileInfo.createCameraGenericInfo(getGenericCategoryForType(category), category, fileInfo.filePath, count)
                fileInfoList.add(itemFileInfo)
                categories.add(category)
            } else {
                count++
                fileInfoList[categories.indexOf(category)].count = count
            }
        }
        return fileInfoList
    }

    private fun getGenericCategoryForType(category: Category): Category {
        return if (category == Category.IMAGE) {
            Category.CAMERA_IMAGES
        } else {
            Category.CAMERA_VIDEO
        }
    }

    override fun fetchCount(context: Context, path: String?): Int {
        Log.e(TAG, "fetchCount:$path")
        return if (path == null) {
            0
        } else {
            getFileCount(path)
        }
    }

    private fun getFileCount(path: String): Int {
        val file = File(path)
        val list: Array<out String>? = file.list()
        return list?.size ?: 0
    }

}
