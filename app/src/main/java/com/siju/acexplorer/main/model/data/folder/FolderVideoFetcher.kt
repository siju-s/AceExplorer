package com.siju.acexplorer.main.model.data.folder

import android.content.Context
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.data.folder.VideoUtils.getFolderVideoFileList
import com.siju.acexplorer.main.model.groups.Category
import java.io.File
import java.util.*

class FolderVideoFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {
        val file = File(path)
        return getFolderVideoFileList(file, canShowHiddenFiles(context))
    }

    override fun fetchCount(context: Context, path: String?): Int {
        return 0
    }
}