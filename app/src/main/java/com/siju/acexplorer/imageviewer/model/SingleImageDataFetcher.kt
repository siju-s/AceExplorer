package com.siju.acexplorer.imageviewer.model

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.helper.MediaStoreColumnHelper
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.FileUtils

object SingleImageDataFetcher {

    fun fetchData(context: Context, uri: Uri): FileInfo? {
        Log.d(this.javaClass.simpleName, "fetchData:uri=$uri, authority:${uri.authority}, schem:${uri.scheme}")
        val cursor = fetchImageDetail(context, uri)
        return getImageData(cursor)
    }

    fun getImageInfo(context: Context, path: String): FileInfo? {
        val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Images.Media.DATA + " =?"
        val selectionArgs = arrayOf(path)

        val cursor = context.contentResolver.query(imageUri, null, selection, selectionArgs, null, null)
        return getImageDataFromCursor(cursor)
    }

    private fun getImageDataFromCursor(cursor: Cursor?, category: Category = Category.IMAGE): FileInfo? {
        if (cursor == null) {
            return null
        }
        val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE)
        val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
        val dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
        val imageIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStoreColumnHelper.getBucketIdColumn())
        val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val widthIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
        val heightIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

        var info : FileInfo? = null
        if (cursor.moveToFirst()) {

            val path = cursor.getString(pathIndex)

            val size = cursor.getLong(sizeIndex)
            val date = cursor.getLong(dateIndex)
            val imageId = cursor.getLong(imageIdIndex)
            val bucketId = cursor.getLong(bucketIdIndex)
            val fileName = cursor.getString(titleIndex)
            val extension = FileUtils.getExtension(path)
            val width = cursor.getLong(widthIndex)
            val height = cursor.getLong(heightIndex)

            info = FileInfo.createImagePropertiesInfo(category, imageId, bucketId, fileName, path, date,
                    size, extension, width, height)
        }
        cursor.close()
        return info
    }


    private fun fetchImageDetail(context: Context, uri: Uri): Cursor? {
        return context.contentResolver.query(uri, null, null, null, null)
    }

    private fun getImageData(cursor: Cursor?): FileInfo? {
        if (cursor == null) {
            return null
        }
        val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
        if (cursor.moveToFirst()) {
            val size = cursor.getLong(sizeIndex)
            val fileName = cursor.getString(titleIndex)
            cursor.close()
            return FileInfo(Category.IMAGE, -1, -1, fileName, null, 0, size,
                    null)
        }
        return null
    }
}
