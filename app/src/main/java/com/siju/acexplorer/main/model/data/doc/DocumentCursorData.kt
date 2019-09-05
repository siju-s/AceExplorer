package com.siju.acexplorer.main.model.data.doc

import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.HiddenFileHelper
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.FileUtils.getCategoryFromExtension
import java.io.File
import java.util.*

object DocumentCursorData {

    fun getDataFromCursor(cursor: Cursor?, category: Category,
                          showHidden: Boolean): ArrayList<FileInfo> {
        val fileInfoList = ArrayList<FileInfo>()
        if (cursor == null) {
            return fileInfoList
        }
        if (cursor.moveToFirst()) {
            val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE)
            val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val fileIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)

            do {
                val path = cursor.getString(pathIndex)
                val file = File(path)
                if (HiddenFileHelper.shouldSkipHiddenFiles(file, showHidden)) {
                    continue
                }
                val fileName = cursor.getString(titleIndex)
                val extension = FileUtils.getExtension(path)
                val nameWithExt = FileUtils.constructFileNameWithExtension(fileName, extension)
                val size = cursor.getLong(sizeIndex)
                val date = cursor.getLong(dateIndex)
                val fileId = cursor.getLong(fileIdIndex)
                fileInfoList.add(FileInfo(getCategoryFromExtension(extension), fileId, nameWithExt, path, date, size, extension))
            } while (cursor.moveToNext())
        }
        cursor.close()

        return if (CategoryHelper.isLargeFilesOrganisedCategory(category)) {
            DocumentUtils.getLargeFilesCategoryList(fileInfoList)
        } else fileInfoList
    }

}