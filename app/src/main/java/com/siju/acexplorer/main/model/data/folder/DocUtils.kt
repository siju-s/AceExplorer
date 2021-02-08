package com.siju.acexplorer.main.model.data.folder

import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.FileConstants
import com.siju.acexplorer.main.model.HiddenFileHelper
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.RootHelper
import java.io.File
import java.util.*

object DocUtils {

    fun getFolderDocFileList(sourceFile: File, showHidden: Boolean): ArrayList<FileInfo> {
        val filesList = ArrayList<FileInfo>()
        val listFiles = sourceFile.listFiles { _, name ->
            isDocFile(name)

        } ?: return filesList

        for (file in listFiles) {
            val filePath = file.absolutePath
            val size = file.length()

            // Don't show hidden files by default
            if (HiddenFileHelper.shouldSkipHiddenFiles(file, showHidden)) {
                continue
            }
            val extension: String? = FileUtils.getExtension(filePath)
            val category = FileUtils.getCategoryFromExtension(extension)
            val date = file.lastModified()

            val fileInfo = FileInfo(category, file.name, filePath, date, size,
                    false, extension, RootHelper.parseFilePermission(file), false)
            filesList.add(fileInfo)
        }
        return filesList
    }

    private fun isDocFile(name: String?): Boolean {
        return name?.endsWith(FileConstants.EXT_PDF, true) == true ||
                name?.endsWith(FileConstants.EXT_DOC, true) == true ||
                name?.endsWith(FileConstants.EXT_DOCX, true) == true ||
                name?.endsWith(FileConstants.EXT_ZIP, true) == true ||
                name?.endsWith(FileConstants.EXT_CSV, true) == true ||
                name?.endsWith(FileConstants.EXT_XLS, true) == true ||
                name?.endsWith(FileConstants.EXT_XLXS, true) == true ||
                name?.endsWith(FileConstants.EXT_PPT, true) == true ||
                name?.endsWith(FileConstants.EXT_PPTX, true) == true ||
                name?.endsWith(FileConstants.EXT_TEXT, true) == true ||
                name?.endsWith(FileConstants.EXT_HTML, true) == true
    }
}