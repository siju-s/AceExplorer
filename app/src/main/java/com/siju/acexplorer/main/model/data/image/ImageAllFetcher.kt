package com.siju.acexplorer.main.model.data.image

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.HiddenFileHelper
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.FileUtils
import java.io.File
import java.util.*

class ImageAllFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {
        val cursor = fetchImages(context)
        return getImageAllDataFromCursor(cursor, category, canShowHiddenFiles(context))
    }

    override fun fetchCount(context: Context, path: String?): Int {
        val cursor = fetchImages(context)
        return getCursorCount(cursor)
    }

    private fun fetchImages(context: Context): Cursor? {
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val sortOrder = MediaStore.Images.Media.DEFAULT_SORT_ORDER
        return context.contentResolver.query(uri, null, null, null, sortOrder)
    }

    private fun getImageAllDataFromCursor(cursor: Cursor?, category: Category,
                                        showHidden: Boolean): ArrayList<FileInfo> {
        val fileInfoList = ArrayList<FileInfo>()
        if (cursor == null) {
            return fileInfoList
        }
        val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE)
        val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
        val dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
        val imageIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
        val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        if (cursor.moveToFirst()) {
            do {
                val path = cursor.getString(pathIndex)
                val file = File(path)
                if (HiddenFileHelper.shouldSkipHiddenFiles(file, showHidden)) {
                    continue
                }
                val size = cursor.getLong(sizeIndex)
                val date = cursor.getLong(dateIndex)
                val imageId = cursor.getLong(imageIdIndex)
                val bucketId = cursor.getLong(bucketIdIndex)
                val fileName = cursor.getString(titleIndex)
                val extension = FileUtils.getExtension(path)
                val nameWithExt = FileUtils.constructFileNameWithExtension(fileName, extension)
                fileInfoList.add(
                        FileInfo(category, imageId, bucketId, nameWithExt, path, date, size,
                                extension))
            }
            while (cursor.moveToNext())
        }
        cursor.close()
        return fileInfoList
    }
}