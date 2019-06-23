package com.siju.acexplorer.main.model.data.music

import android.content.Context
import android.database.Cursor
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.data.music.AlbumCommonData.getAlbumCursorData
import com.siju.acexplorer.main.model.data.music.AlbumCommonData.queryAlbums
import com.siju.acexplorer.main.model.groups.Category
import java.util.*

class AlbumDataFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {
        val cursor = queryAlbums(context)
        return getAlbumCursorData(cursor, category)
    }

    override fun fetchCount(context: Context, path: String?): Int {
        val cursor = queryAlbums(context)
        return getAlbumCount(cursor)
    }

    private fun getAlbumCount(cursor: Cursor?): Int {
        val count = cursor?.count
        cursor?.close()
        return count ?: 0
    }
}
