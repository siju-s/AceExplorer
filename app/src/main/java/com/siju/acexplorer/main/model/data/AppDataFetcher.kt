package com.siju.acexplorer.main.model.data

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.HiddenFileHelper
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.SortHelper.sortFiles
import java.io.File
import java.util.*

private const val EXT_APK = ".apk"

class AppDataFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {
        val cursor = fetchApk(context)
        return getApkCursorData(cursor, getSortMode(context), canShowHiddenFiles(context))
    }

    override fun fetchCount(context: Context, path: String?): Int {
        val cursor = fetchApk(context)
        return getCursorCount(cursor)
    }

    private fun fetchApk(context: Context): Cursor? {
        val where = MediaStore.Files.FileColumns.DATA + " LIKE ?"
        val selectionArgs = arrayOf("%$EXT_APK")
        val uri = MediaStore.Files.getContentUri("external")

        return context.contentResolver.query(uri, null, where, selectionArgs, null)
    }

    private fun getApkCursorData(cursor: Cursor?, sortMode: Int,
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
                val size = cursor.getLong(sizeIndex)
                val date = cursor.getLong(dateIndex)
                val fileId = cursor.getLong(fileIdIndex)
                val extension = FileUtils.getExtension(path)
                val nameWithExt = FileUtils.constructFileNameWithExtension(fileName, extension)
                fileInfoList.add(FileInfo(Category.APPS, fileId, nameWithExt, path, date, size,
                        extension))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return sortFiles(fileInfoList, sortMode)
    }
}
