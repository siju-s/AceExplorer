package com.siju.acexplorer.main.model.data.video

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.HiddenFileHelper
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.data.DataFetcher.Companion.canShowHiddenFiles
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.FileUtils
import java.io.File
import java.util.*


class VideoAllFetcher : DataFetcher {
    override fun fetchData(context: Context, path: String?,
                           category: Category): ArrayList<FileInfo> {
        val cursor = fetchAllVideos(context)
        return getVideoCursorData(cursor, category, canShowHiddenFiles(context))
    }

    override fun fetchCount(context: Context, path: String?): Int {
        val cursor = fetchAllVideos(context)
        return getCursorCount(cursor)
    }

    private fun fetchAllVideos(context: Context): Cursor? {
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        return context.contentResolver.query(uri, null, null, null,
                MediaStore.Video.Media.DEFAULT_SORT_ORDER)
    }

    private fun getVideoCursorData(cursor: Cursor?, category: Category,
                                   showHidden: Boolean): ArrayList<FileInfo> {
        val fileInfoList = ArrayList<FileInfo>()
        if (cursor == null) {
            return fileInfoList
        }
        val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
        val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
        val dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
        val imageIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val bucketIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)
        val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
        if (cursor.moveToFirst()) {
            do {
                val path = cursor.getString(pathIndex)
                val file = File(path)
                if (HiddenFileHelper.shouldSkipHiddenFiles(file, showHidden)) {
                    continue
                }
                val fileName = cursor.getString(titleIndex)
                val size = cursor.getLong(sizeIndex)
                val date = cursor.getLong(dateIndex)
                val videoId = cursor.getLong(imageIdIndex)
                val bucketId = cursor.getLong(bucketIdIndex)
                val extension = FileUtils.getExtension(path)
                val nameWithExt = FileUtils.constructFileNameWithExtension(fileName, extension)
                fileInfoList.add(
                        FileInfo(category, videoId, bucketId, nameWithExt, path, date, size,
                                extension))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return fileInfoList
    }
}