package com.siju.acexplorer.main.model.data.recent

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.HiddenFileHelper.constructionNoHiddenFilesArgs
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.groups.Category
import java.util.*

class RecentAppFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {
        val showHidden = canShowHiddenFiles(context)
        val cursor = fetchRecentApps(context, showHidden)
        return RecentCursorData.getDataFromCursor(cursor, category, showHidden)
    }

    override fun fetchCount(context: Context, path: String?): Int {
        val cursor = fetchRecentApps(context, canShowHiddenFiles(context))
        return getCursorCount(cursor)
    }

    private fun fetchRecentApps(context: Context, showHidden: Boolean): Cursor? {
        val uri = MediaStore.Files.getContentUri("external")
        val where = MediaStore.Files.FileColumns.DATA + " LIKE ?"
        var selection = ""
        if (!showHidden) {
            selection = constructionNoHiddenFilesArgs() + " AND "
        }
        selection += RecentUtils.getRecentTimeSelectionArgument() + " AND " + where
        val filter = ".apk"
        val selectionArgs = arrayOf("%$filter")

        val sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"
        return context.contentResolver.query(uri, null, selection, selectionArgs,
                sortOrder)
    }
}