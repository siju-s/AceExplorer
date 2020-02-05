package com.siju.acexplorer.main.model.data.music

import android.content.Context
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.data.DataFetcher.Companion.canShowHiddenFiles
import com.siju.acexplorer.main.model.data.music.PodcastCommonData.getPodcastCursorData
import com.siju.acexplorer.main.model.data.music.PodcastCommonData.queryPodcasts
import com.siju.acexplorer.main.model.groups.Category
import java.util.*

class PodcastDataFetcher : DataFetcher {
    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {
        val cursor = queryPodcasts(context)
        return getPodcastCursorData(cursor, category, canShowHiddenFiles(context))
    }

    override fun fetchCount(context: Context, path: String?): Int {
        val cursor = queryPodcasts(context)
        return getCursorCount(cursor)
    }
}