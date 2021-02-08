package com.siju.acexplorer.main.model.data.folder

import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.HiddenFileHelper
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.RootHelper
import java.io.File
import java.util.*

object ImageUtils {

    fun getFolderImageFileList(sourceFile: File, showHidden: Boolean): ArrayList<FileInfo> {
        val filesList = ArrayList<FileInfo>()
        val listFiles = sourceFile.listFiles { _, name ->
            isImageFile(name)
        } ?: return filesList

        for (file in listFiles) {
            val filePath = file.absolutePath
            val size = file.length()

            // Don't show hidden files by default
            if (HiddenFileHelper.shouldSkipHiddenFiles(file, showHidden)) {
                continue
            }
            val extension = FileUtils.getExtension(filePath)
            val category = FileUtils.getCategoryFromExtension(extension)
            val date = file.lastModified()

            val fileInfo = FileInfo(category, file.name, filePath, date, size,
                    false, extension, RootHelper.parseFilePermission(file), false)
            filesList.add(fileInfo)
        }
        return filesList
    }

    private fun isImageFile(name: String?): Boolean {
        return name?.toLowerCase(Locale.ROOT)?.endsWith(".jpg") == true ||
                name?.toLowerCase(Locale.ROOT)?.endsWith(".jpeg") == true ||
                name?.toLowerCase(Locale.ROOT)?.endsWith(".heif") == true ||
                name?.toLowerCase(Locale.ROOT)?.endsWith(".heic") == true ||
                name?.toLowerCase(Locale.ROOT)?.endsWith(".png") == true
    }
}