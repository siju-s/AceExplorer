package com.siju.acexplorer.main.model.data.music

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.SortHelper
import java.util.*

class GenreDetailFetcher : DataFetcher {
    override fun fetchData(context: Context, path: String?,
                           category: Category): ArrayList<FileInfo> {
        if (path == null) {
            return arrayListOf()
        }
        val uri = MediaStore.Audio.Genres.Members.getContentUri("external", path.toLong())
        val cursor = context.contentResolver.query(uri,
                                                   null, null, null, null)
        val data = getGenreDetailCursorData(cursor)
        return SortHelper.sortFiles(data, getSortMode(context))
    }

    override fun fetchCount(context: Context, path: String?): Int {
        return 0
    }

    // DATA field is required to check path. Works fine till Android 12 even though deprecated
    @Suppress("Deprecation")
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