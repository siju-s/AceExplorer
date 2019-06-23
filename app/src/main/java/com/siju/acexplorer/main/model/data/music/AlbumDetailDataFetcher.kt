package com.siju.acexplorer.main.model.data.music

import android.content.Context
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.groups.Category
import java.util.*

class AlbumDetailDataFetcher : DataFetcher {
    override fun fetchData(context: Context, path: String?,
                           category: Category): ArrayList<FileInfo> {
        return fetchAlbumDetail(context, path, category, canShowHiddenFiles(context))
    }

    override fun fetchCount(context: Context, path: String?): Int {
        return 0
    }

    private fun fetchAlbumDetail(context: Context, bucketId: String?, category: Category,
                                 showHidden: Boolean): ArrayList<FileInfo> {

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.ALBUM_ID + " =?"
        val selectionArgs = arrayOf(bucketId)
        val projection = arrayOf(MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media._ID,
                                 MediaStore.Audio.Media.ALBUM_ID,
                                 MediaStore.Audio.Media.DATE_MODIFIED, MediaStore.Audio.Media.SIZE,
                                 MediaStore.Audio.Media.DATA)

        val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)

        return AudioDetailCursorData.getDetailCursorData(cursor, category, showHidden)
    }
}