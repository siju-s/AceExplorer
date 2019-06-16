package com.siju.acexplorer.main.model.data.music

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.groups.Category
import java.util.*

class AlbumDataFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {
        val cursor = queryAlbums(context)
        return getAlbumCursorData(cursor)
    }

    override fun fetchCount(context: Context, path: String?): Int {
        val cursor = queryAlbums(context)
        return getAlbumCount(cursor)
    }

    private fun queryAlbums(context: Context): Cursor? {
        val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        return context.contentResolver.query(uri, null, null, null,
                MediaStore.Audio.Albums.DEFAULT_SORT_ORDER)
    }

    private fun getAlbumCount(cursor: Cursor?): Int {
        val count = cursor?.count
        cursor?.close()
        return count ?: 0
    }

    private fun getAlbumCursorData(cursor: Cursor?): ArrayList<FileInfo> {
        val fileInfoList = ArrayList<FileInfo>()

        if (cursor == null) {
            return fileInfoList
        }

        if (cursor.moveToFirst()) {
            val albumNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
            val albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
            val numTracksIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)
            do {
                val album = cursor.getString(albumNameIndex)
                val albumId = cursor.getLong(albumIdIndex)
                val numTracks = cursor.getLong(numTracksIndex)

                fileInfoList.add(FileInfo(Category.ALBUMS, albumId, album, numTracks))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return fileInfoList
    }
}
