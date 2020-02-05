package com.siju.acexplorer.main.model.data.recent

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.HiddenFileHelper.constructionNoHiddenFilesArgs
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.data.DataFetcher.Companion.canShowHiddenFiles
import com.siju.acexplorer.main.model.groups.Category
import java.util.*

class RecentFolderAudioFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {
        val showHidden = canShowHiddenFiles(context)
        val cursor = fetchRecentAudio(context, path, showHidden)
        return RecentCursorData.getDataFromCursor(cursor, category, showHidden)
    }

    override fun fetchCount(context: Context, path: String?): Int {
        val cursor = fetchRecentAudio(context, path, canShowHiddenFiles(context))
        return getCursorCount(cursor)
    }

    private fun fetchRecentAudio(context: Context, path: String?, showHidden: Boolean): Cursor? {
        if (path == null) {
            return null
        }
        val uri = MediaStore.Files.getContentUri("external")
        var selection = MediaStore.Files.FileColumns.DATA + " LIKE " + "'" + path + "'" + " AND " +
                RecentUtils.getRecentTimeSelectionArgument() + " AND " + RecentUtils.getAudioMediaType()
        if (!showHidden) {
            selection += " AND " + constructionNoHiddenFilesArgs()
        }
        val sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"
        return context.contentResolver.query(uri, null, selection, null,
                sortOrder)
    }
}