package com.siju.acexplorer.main.model.data.doc

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.FileConstants
import com.siju.acexplorer.main.model.HiddenFileHelper.constructionNoHiddenFilesArgs
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.groups.Category
import java.util.*


class OtherDocFilesFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {
        val showHidden = canShowHiddenFiles(context)
        val cursor = fetchOtherDocFiles(context, showHidden)
        return DocumentCursorData.getDataFromCursor(cursor, category, showHidden)
    }

    override fun fetchCount(context: Context, path: String?): Int {
        val cursor = fetchOtherDocFiles(context, canShowHiddenFiles(context))
        return getCursorCount(cursor)
    }

    private fun fetchOtherDocFiles(context: Context, showHidden: Boolean): Cursor? {
        val uri = MediaStore.Files.getContentUri("external")
        var selection = ""
        if (!showHidden) {
            selection = constructionNoHiddenFilesArgs() + " AND "
        }

        val doc = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_DOC)
        val docx = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_DOCX)
        val txt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_TEXT)
        val html = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_HTML)
        val xls = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_XLS)
        val xlxs = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_XLXS)
        val ppt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_PPT)
        val pptx = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_PPTX)

        selection += MediaStore.Files.FileColumns.MIME_TYPE + " IN " + "(" + "'" + doc + "'" + "," +
                "'" + docx + "'" + "," +
                "'" + txt + "'" + "," +
                "'" + html + "'" + "," +
                "'" + xls + "'" + "," +
                "'" + xlxs + "'" + "," +
                "'" + ppt + "'" + "," +
                "'" + pptx + "'" + " )"
        return context.contentResolver.query(uri, null, selection, null,
                null)
    }
}