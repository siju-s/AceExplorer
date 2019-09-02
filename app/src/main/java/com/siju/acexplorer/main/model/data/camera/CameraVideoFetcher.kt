package com.siju.acexplorer.main.model.data.camera

import android.content.Context
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.HiddenFileHelper
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.RootHelper
import java.io.File
import java.util.*

class CameraVideoFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {
        val file = File(path)
        return getFileList(file, canShowHiddenFiles(context))
    }

    override fun fetchCount(context: Context, path: String?): Int {
        return 0
    }

    private fun getFileList(sourceFile: File, showHidden: Boolean): ArrayList<FileInfo> {
        val filesList = ArrayList<FileInfo>()
        val listFiles = sourceFile.listFiles { _, name ->
            name?.toLowerCase(Locale.ROOT)?.endsWith(".mp4") == true ||
                    name?.toLowerCase(Locale.ROOT)?.endsWith(".ts") == true ||
                    name?.toLowerCase(Locale.ROOT)?.endsWith(".mkv") == true
        } ?: return filesList

        for (file in listFiles) {
            val filePath = file.absolutePath
            val size = file.length()
            var extension: String?
            var category: Category

            // Don't show hidden files by default
            if (HiddenFileHelper.shouldSkipHiddenFiles(file, showHidden)) {
                continue
            }
            extension = FileUtils.getExtension(filePath)
            category = FileUtils.getCategoryFromExtension(extension)
            val date = file.lastModified()

            val fileInfo = FileInfo(category, file.name, filePath, date, size,
                    false, extension, RootHelper.parseFilePermission(file), false)
            filesList.add(fileInfo)
        }
        return filesList
    }
}