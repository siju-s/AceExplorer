package com.siju.acexplorer.main.model.data.doc

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.FileConstants
import com.siju.acexplorer.main.model.HiddenFileHelper.constructionNoHiddenFilesArgs
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.data.doc.DocumentUtils.getMediaTypeNone
import com.siju.acexplorer.main.model.groups.Category
import java.util.*


class PdfFetcher : DataFetcher {
    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {
        val showHidden = canShowHiddenFiles(context)
        val cursor = fetchPdf(context, showHidden)
        return DocumentCursorData.getDataFromCursor(cursor, category, showHidden)
    }

    override fun fetchCount(context: Context, path: String?): Int {
        val cursor = fetchPdf(context, canShowHiddenFiles(context))
        return getCursorCount(cursor)
    }

    private fun fetchPdf(context: Context, showHidden: Boolean): Cursor? {
        val uri = MediaStore.Files.getContentUri("external")
        val pdf1 = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants
                .EXT_PDF)
        var selection: String? = null
        if (!showHidden) {
            selection = constructionNoHiddenFilesArgs() + " AND "
        }
        selection += getMediaTypeNone() + " AND " + MediaStore.Files.FileColumns.MIME_TYPE + " =?"
        val selectionArgs = arrayOf(pdf1)
        return context.contentResolver.query(uri, null, selection, selectionArgs,
                null)
    }
}