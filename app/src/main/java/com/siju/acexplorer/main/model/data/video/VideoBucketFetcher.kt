package com.siju.acexplorer.main.model.data.video


import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.SortHelper
import java.util.*

class VideoBucketFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {
        val cursor = queryVideos(context)
        val data = getVideoCursorData(cursor)
        return SortHelper.sortFiles(data, getSortMode(context))
    }

    override fun fetchCount(context: Context, path: String?): Int {
        val cursor = queryVideos(context)
        return getCursorCount(cursor)
    }

    private fun queryVideos(context: Context): Cursor? {
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Video.Media.BUCKET_ID, MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.DATA)
        return context.contentResolver.query(uri, projection, null, null, MediaStore.Video.VideoColumns.BUCKET_ID)
    }

    private fun getVideoCursorData(cursor: Cursor?): ArrayList<FileInfo> {
        val fileInfoList = ArrayList<FileInfo>()
        if (cursor == null) {
            return fileInfoList
        }
        if (cursor.moveToFirst()) {
            val bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)
            val bucketNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val ids = ArrayList<Long>()
            var count = 0
            do {
                val bucketId = cursor.getLong(bucketIdIndex)
                val path = cursor.getString(pathIndex)
                val bucketName = cursor.getString(bucketNameIndex)
                if (!ids.contains(bucketId)) {
                    count = 1
                    val fileInfo = FileInfo(Category.VIDEO, bucketId, bucketName, path, count)
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
