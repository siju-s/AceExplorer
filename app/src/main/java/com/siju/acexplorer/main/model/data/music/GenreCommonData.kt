package com.siju.acexplorer.main.model.data.music

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import java.util.*

object GenreCommonData {

    fun queryGenres(context: Context): Cursor? {
        val uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI
        return context.contentResolver.query(uri, null, null, null,
                                             MediaStore.Audio.Genres.DEFAULT_SORT_ORDER)
    }

    fun getGenreCursorData(cursor: Cursor?, category: Category): ArrayList<FileInfo> {
        val fileInfoList = ArrayList<FileInfo>()

        if (cursor == null) {
            return fileInfoList
        }
        if (cursor.moveToFirst()) {
            val genreNameIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)
            val genreIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID)
            do {
                val genreId = cursor.getLong(genreIdIdx)
                val genre = cursor.getString(genreNameIdx)
                fileInfoList.add(FileInfo(category, genreId, genre, INVALID_ID.toLong()))
            }
            while (cursor.moveToNext())
        }
        cursor.close()
        return fileInfoList
    }
}