package com.siju.acexplorer.main.model.data.music

import android.content.Context
import android.database.Cursor
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.data.music.ArtistCommonData.getArtistCursorData
import com.siju.acexplorer.main.model.data.music.ArtistCommonData.queryArtist
import com.siju.acexplorer.main.model.groups.Category
import java.util.*

class ArtistDataFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {
        val cursor = queryArtist(context)
        return getArtistCursorData(cursor, category)
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
}
