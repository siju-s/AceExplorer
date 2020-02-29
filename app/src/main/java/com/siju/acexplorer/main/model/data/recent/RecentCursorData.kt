package com.siju.acexplorer.main.model.data.recent

import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.HiddenFileHelper
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.main.model.helper.FileUtils
import java.io.File
import java.util.*

object RecentCursorData {

    fun getDataFromCursor(cursor: Cursor?, category: Category, showHidden: Boolean): ArrayList<FileInfo> {
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
                if (HiddenFileHelper.shouldSkipHiddenFiles(file, showHidden) || File(path).isDirectory) {
                    continue
                }
                val fileName = cursor.getString(titleIndex)
                val size = cursor.getLong(sizeIndex)
                val date = cursor.getLong(dateIndex)
                val fileId = cursor.getLong(fileIdIndex)
                val extension = FileUtils.getExtension(path)

                if (RecentUtils.shouldSkipApk(category, extension)) {
                    continue
                }
                val nameWithExt = FileUtils.constructFileNameWithExtension(fileName, extension)
                fileInfoList.add(FileInfo(CategoryHelper.getSubCategoryForRecentFromExtension(extension), fileId,
                        nameWithExt, path, date, size,
                        extension))

            } while (cursor.moveToNext())
        }
        cursor.close()
        return if (CategoryHelper.isRecentGenericCategory(category)) {
            RecentUtils.getRecentCategoryList(fileInfoList)
        } else fileInfoList
    }
}