package com.siju.acexplorer.main.model.data.video

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.HiddenFileHelper
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.SortHelper
import java.io.File
import java.util.*


class VideoDetailFetcher : DataFetcher {
    override fun fetchData(context: Context, path: String?,
                           category: Category): ArrayList<FileInfo> {
        val data = fetchBucketDetail(context, path, category, canShowHiddenFiles(context))
        return SortHelper.sortFiles(data, getSortMode(context))
    }

    override fun fetchCount(context: Context, path: String?): Int {
        return 0
    }

    private fun fetchBucketDetail(context: Context, bucketId: String?, category: Category,
                                  showHidden: Boolean): ArrayList<FileInfo> {

        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Video.Media.BUCKET_ID + " =?"
        val selectionArgs = arrayOf(bucketId)

        val cursor = context.contentResolver.query(uri, null, selection, selectionArgs,
                                                   MediaStore.Video.Media.DEFAULT_SORT_ORDER)

        return getBucketDetailCursorData(cursor, category, showHidden)
    }

    private fun getBucketDetailCursorData(cursor: Cursor?, category: Category,
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
            }
            while (cursor.moveToNext())
        }
        cursor.close()
        return fileInfoList
    }
}