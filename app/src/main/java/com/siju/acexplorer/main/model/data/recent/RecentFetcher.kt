package com.siju.acexplorer.main.model.data.recent

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.HiddenFileHelper.constructionNoHiddenFilesArgs
import com.siju.acexplorer.main.model.data.AppDataFetcher
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.data.doc.DocumentUtils
import com.siju.acexplorer.main.model.groups.Category
import java.util.*


class RecentFetcher : DataFetcher {


    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {
        val showHidden = canShowHiddenFiles(context)
        val cursor = fetchRecentMedia(context, showHidden)
        return RecentCursorData.getDataFromCursor(cursor, category, showHidden)
    }

    override fun fetchCount(context: Context, path: String?): Int {
        val cursor = fetchRecentMedia(context, canShowHiddenFiles(context))
        return getCursorCount(cursor)
    }

    private fun fetchRecentMedia(context: Context, showHidden: Boolean): Cursor? {
        val uri = MediaStore.Files.getContentUri("external")
        var selection = ""
        if (!showHidden) {
            selection = constructionNoHiddenFilesArgs() + " AND "
        }
        val where = MediaStore.Files.FileColumns.DATA + " LIKE ?"
        // Reason for such a big query is to avoid fetching directories in the count and also because we can't just
        // filter based on {@link MediaStore.Files.FileColumns.MIME_TYPE} since "apk" mime type is stored as null
        selection += (RecentUtils.getRecentTimeSelectionArgument() + " AND " +
                "(" + RecentUtils.getImagesMediaType() + " OR "
                + RecentUtils.getAudioMediaType() + " OR " +
                RecentUtils.getVideosMediaType() + " OR " +
                DocumentUtils.getCompressedMimeType() + " OR " +
                DocumentUtils.getDocMimeTypes() + " OR "
                + where + ")")
        val sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"

        val filter = AppDataFetcher.EXT_APK
        val selectionArgs = arrayOf("%$filter")

        return context.contentResolver.query(uri, null, selection, selectionArgs,
                sortOrder)
    }
}