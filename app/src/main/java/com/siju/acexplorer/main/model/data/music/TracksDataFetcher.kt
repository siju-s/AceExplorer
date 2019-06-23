package com.siju.acexplorer.main.model.data.music

import android.content.Context
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.data.music.TracksCommonData.getTracksCursorData
import com.siju.acexplorer.main.model.data.music.TracksCommonData.queryTracks
import com.siju.acexplorer.main.model.groups.Category
import java.util.*

class TracksDataFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {
        val cursor = queryTracks(context)
        return getTracksCursorData(cursor, Category.AUDIO, canShowHiddenFiles(context))
    }

    override fun fetchCount(context: Context, path: String?): Int {
        val cursor = queryTracks(context)
        return getCursorCount(cursor)
    }
}