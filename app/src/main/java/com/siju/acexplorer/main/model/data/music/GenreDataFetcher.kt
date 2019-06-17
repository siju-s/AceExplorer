package com.siju.acexplorer.main.model.data.music

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.FileUtils
import java.util.*

const val INVALID_ID = -1

class GenreDataFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?,
                           category: Category): ArrayList<FileInfo> {
        val cursor = queryGenres(context)
        return getGenreCursorData(cursor)
    }

    override fun fetchCount(context: Context, path: String?): Int {
        val cursor = queryGenres(context)
        return getGenreCount(cursor)
    }

    private fun getGenreCount(cursor: Cursor?): Int {
        val count = cursor?.count
        cursor?.close()
        return count ?: 0
    }

    private fun queryGenres(context: Context): Cursor? {
        val uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI
        return context.contentResolver.query(uri, null, null, null,
                                             MediaStore.Audio.Genres.DEFAULT_SORT_ORDER)
    }

    private fun getGenreCursorData(cursor: Cursor?): ArrayList<FileInfo> {
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
                fileInfoList.add(FileInfo(Category.GENRES, genreId, genre, INVALID_ID.toLong()))
            }
            while (cursor.moveToNext())
        }
        cursor.close()
        return fileInfoList
    }


    internal fun fetchGenreDetails(context: Context, id: Long): ArrayList<FileInfo> {

        val uri = MediaStore.Audio.Genres.Members.getContentUri("external", id)

        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return getGenreDetailCursorData(cursor)
    }

    private fun getGenreDetailCursorData(cursor: Cursor?): ArrayList<FileInfo> {
        val projection = arrayOf(MediaStore.Audio.Genres.Members.TITLE,
                                 MediaStore.Audio.Genres.Members._ID,
                                 MediaStore.Audio.Genres.Members.ALBUM_ID,
                                 MediaStore.Audio.Genres.Members.DATE_MODIFIED,
                                 MediaStore.Audio.Genres.Members.SIZE,
                                 MediaStore.Audio.Genres.Members.DATA)
        val fileInfoList = ArrayList<FileInfo>()
        if (cursor == null) {
            return fileInfoList
        }
        val titleIndex = cursor.getColumnIndexOrThrow(projection[0])
        val audioIdIndex = cursor.getColumnIndexOrThrow(projection[1])
        val albumIdIndex = cursor.getColumnIndexOrThrow(projection[2])
        val dateIndex = cursor.getColumnIndexOrThrow(projection[3])
        val sizeIndex = cursor.getColumnIndexOrThrow(projection[4])
        val pathIndex = cursor.getColumnIndexOrThrow(projection[5])
        if (cursor.moveToFirst()) {
            do {
                val fileName = cursor.getString(titleIndex)
                val size1 = cursor.getLong(sizeIndex)
                val date1 = cursor.getLong(dateIndex)
                val path = cursor.getString(pathIndex)
                val audioId = cursor.getLong(audioIdIndex)
                val albumId = cursor.getLong(albumIdIndex)
                val extension = FileUtils.getExtension(path)
                val nameWithExt = FileUtils.constructFileNameWithExtension(fileName, extension)
                fileInfoList.add(
                        FileInfo(Category.AUDIO, audioId, albumId, nameWithExt, path, date1, size1,
                                 extension))
            }
            while (cursor.moveToNext())
        }
        cursor.close()
        return fileInfoList
    }
}
