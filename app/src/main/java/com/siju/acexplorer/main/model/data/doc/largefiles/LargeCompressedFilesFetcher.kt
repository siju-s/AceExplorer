package com.siju.acexplorer.main.model.data.doc.largefiles

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.HiddenFileHelper.constructionNoHiddenFilesArgs
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.data.DataFetcher.Companion.canShowHiddenFiles
import com.siju.acexplorer.main.model.data.doc.DocumentCursorData
import com.siju.acexplorer.main.model.data.doc.DocumentUtils
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.SortHelper
import com.siju.acexplorer.storage.model.SortMode
import java.util.*

private const val LARGE_FILES_MIN_SIZE_MB = 104857600 //100 MB

class LargeCompressedFilesFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {
        val showHidden = canShowHiddenFiles(context)
        val cursor = fetchLargeCompressedFiles(context, showHidden)
        val data = DocumentCursorData.getDataFromCursor(cursor, category, showHidden)
        return SortHelper.sortFiles(data, SortMode.SIZE_DESC.value)
    }

    override fun fetchCount(context: Context, path: String?): Int {
        val cursor = fetchLargeCompressedFiles(context, canShowHiddenFiles(context))
        return getCursorCount(cursor)
    }

    private fun fetchLargeCompressedFiles(context: Context, showHidden: Boolean): Cursor? {
        val uri = MediaStore.Files.getContentUri("external")
        var selection = DocumentUtils.getMediaTypeNone() + " AND " + DocumentUtils.getCompressedMimeType()
        if (!showHidden) {
            selection = selection + " AND " + constructionNoHiddenFilesArgs()
        }
        selection += " AND " + MediaStore.Files.FileColumns.SIZE + " >?"
        val selectionArgs = arrayOf(LARGE_FILES_MIN_SIZE_MB.toString())
        return context.contentResolver.query(uri, null, selection, selectionArgs,
                null)
    }
}