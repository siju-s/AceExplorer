package com.siju.acexplorer.main.model.data.doc

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.HiddenFileHelper.constructionNoHiddenFilesArgs
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.SortHelper
import java.util.*


class CompressedFileFetcher : DataFetcher {
    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {
        val showHidden = canShowHiddenFiles(context)
        val cursor = fetchCompressedFiles(context, showHidden)
        val data = DocumentCursorData.getDataFromCursor(cursor, category, showHidden)
        return SortHelper.sortFiles(data, getSortMode(context))
    }

    override fun fetchCount(context: Context, path: String?): Int {
        val cursor = fetchCompressedFiles(context, canShowHiddenFiles(context))
        return getCursorCount(cursor)
    }

    private fun fetchCompressedFiles(context: Context, showHidden: Boolean): Cursor? {
        val uri = MediaStore.Files.getContentUri("external")
        var selection: String? = null
        selection = DocumentUtils.getMediaTypeNone() + " AND " + DocumentUtils.getCompressedMimeType()
        if (!showHidden) {
            selection = selection + " AND " + constructionNoHiddenFilesArgs()
        }
        return context.contentResolver.query(uri, null, selection, null,
                null)
    }
}