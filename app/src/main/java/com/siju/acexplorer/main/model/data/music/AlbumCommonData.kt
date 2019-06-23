package com.siju.acexplorer.main.model.data.music

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import java.util.*

object AlbumCommonData {

    fun queryAlbums(context: Context): Cursor? {
        val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        return context.contentResolver.query(uri, null, null, null,
                                             MediaStore.Audio.Albums.DEFAULT_SORT_ORDER)
    }

    fun getAlbumCursorData(cursor: Cursor?, category: Category): ArrayList<FileInfo> {
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

                fileInfoList.add(FileInfo(category, albumId, album, numTracks))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return fileInfoList
    }


}