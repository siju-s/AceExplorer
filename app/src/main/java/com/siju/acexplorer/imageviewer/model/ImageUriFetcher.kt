package com.siju.acexplorer.imageviewer.model

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category

object ImageUriFetcher {

    fun fetchData(context: Context, uri: Uri): FileInfo? {
        val cursor = fetchImageDetail(context, uri)
        return getImageData(cursor)
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
