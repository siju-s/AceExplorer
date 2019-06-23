package com.siju.acexplorer.main.model.data.music

import android.content.Context
import android.database.Cursor
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.data.music.GenreCommonData.getGenreCursorData
import com.siju.acexplorer.main.model.data.music.GenreCommonData.queryGenres
import com.siju.acexplorer.main.model.groups.Category
import java.util.*

const val INVALID_ID = -1

class GenreDataFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?,
                           category: Category): ArrayList<FileInfo> {
        val cursor = queryGenres(context)
        return getGenreCursorData(cursor, category)
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
}
