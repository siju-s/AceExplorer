package com.siju.acexplorer.main.model.data.music

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import java.util.*

object ArtistCommonData {

    fun queryArtist(context: Context): Cursor? {
        val uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
        return context.contentResolver.query(uri, null, null, null,
                                             MediaStore.Audio.Artists.DEFAULT_SORT_ORDER)
    }

    fun getArtistCursorData(cursor: Cursor?, category: Category): ArrayList<FileInfo> {
        val fileInfoList = ArrayList<FileInfo>()
        if (cursor == null) {
            return fileInfoList
        }

        if (cursor.moveToFirst()) {
            val artistNameIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)
            val artistIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
            val numTracksIndex = cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS)
            do {
                val artist = cursor.getString(artistNameIdx)
                val artistId = cursor.getLong(artistIdIdx)
                val numTracks = cursor.getLong(numTracksIndex)

                fileInfoList.add(FileInfo(category, artistId, artist, numTracks))
            }
            while (cursor.moveToNext())
        }
        cursor.close()
        return fileInfoList
    }
}