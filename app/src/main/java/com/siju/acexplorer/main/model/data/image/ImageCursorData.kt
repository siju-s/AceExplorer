package com.siju.acexplorer.main.model.data.image

import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import java.util.*

object ImageCursorData {

    fun getImageDataFromCursor(cursor: Cursor?, category: Category): ArrayList<FileInfo> {
        val fileInfoList = ArrayList<FileInfo>()
        if (cursor == null) {
            return fileInfoList
        }
        if (cursor.moveToFirst()) {
            val bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val bucketNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val ids = ArrayList<Long>()
            var count = 0
            do {
                val path = cursor.getString(pathIndex)
                var bucketName = cursor.getString(bucketNameIndex)
                val bucketId = cursor.getLong(bucketIdIndex)
                if (!ids.contains(bucketId)) {
                    count = 1
                    if (bucketName == null) {
                        bucketName = 0.toString()
                    }
                    val fileInfo = FileInfo(category, bucketId, bucketName, path, count)
                    fileInfoList.add(fileInfo)
                    ids.add(bucketId)
                } else {
                    count++
                    fileInfoList[ids.indexOf(bucketId)].numTracks = count.toLong()
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return fileInfoList
    }
}