package com.siju.acexplorer.main.model.data.music

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.groups.Category
import java.util.*

class ArtistDataFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {
        val cursor = queryArtist(context)
        return getArtistCursorData(cursor)
    }

    override fun fetchCount(context: Context, path: String?): Int {
        val cursor = queryArtist(context)
        return getArtistCount(cursor)
    }

    private fun getArtistCount(cursor: Cursor?): Int {
        val count = cursor?.count
        cursor?.close()
        return count ?: 0
    }

    private fun queryArtist(context: Context): Cursor? {
        val uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
        return context.contentResolver.query(uri, null, null, null,
                MediaStore.Audio.Artists.DEFAULT_SORT_ORDER)
    }

    private fun getArtistCursorData(cursor: Cursor?): ArrayList<FileInfo> {
        val fileInfoList = ArrayList<FileInfo>()
        if (cursor == null) {
            return fileInfoList
        }

        if (cursor.moveToFirst()) {
            val artistNameIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)
            val artistIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
            val numTracksIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS)
            do {
                val artist = cursor.getString(artistNameIdx)
                val artistId = cursor.getLong(artistIdIdx)
                val numTracks = cursor.getLong(numTracksIndex)

                fileInfoList.add(FileInfo(Category.ARTISTS, artistId, artist, numTracks))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return fileInfoList
    }
}
