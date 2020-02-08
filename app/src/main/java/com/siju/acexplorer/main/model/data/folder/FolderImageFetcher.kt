package com.siju.acexplorer.main.model.data.folder

import android.content.Context
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.data.DataFetcher.Companion.canShowHiddenFiles
import com.siju.acexplorer.main.model.data.folder.ImageUtils.getFolderImageFileList
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.SortHelper
import java.io.File
import java.util.*

class FolderImageFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {
        if (path == null) {
            return arrayListOf()
        }
        val file = File(path)
        val data = getFolderImageFileList(file, canShowHiddenFiles(context))
        return SortHelper.sortFiles(data, getSortMode(context))
    }

    override fun fetchCount(context: Context, path: String?): Int {
        return 0
    }
}