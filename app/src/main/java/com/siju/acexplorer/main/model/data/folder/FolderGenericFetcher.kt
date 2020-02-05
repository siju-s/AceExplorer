package com.siju.acexplorer.main.model.data.folder

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.HiddenFileHelper
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.data.DataFetcher.Companion.canShowHiddenFiles
import com.siju.acexplorer.main.model.data.FileDataFetcher
import com.siju.acexplorer.main.model.groups.Category

private const val TAG = "FolderGenericFetcher"

class FolderGenericFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?,
                           category: Category): ArrayList<FileInfo> {
        path?.let {
            return FileDataFetcher.getFilesList(it, false, canShowHiddenFiles(context))
        }
        return ArrayList()
    }


    override fun fetchCount(context: Context, path: String?): Int {
        return if (path == null) {
            0
        } else {
            getFileCount(context, path, canShowHiddenFiles(context))
        }
    }

    private fun getFileCount(context: Context, path: String, showHidden: Boolean): Int {
        val cursor = getFolderCursor(context, path, showHidden)
        return getCursorCount(cursor)
    }

    private fun getFolderCursor(context: Context, path: String, showHidden: Boolean): Cursor? {
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
        var selection = ""
        if (!showHidden) {
            selection += HiddenFileHelper.constructionNoHiddenFilesArgs() + " AND "
        }
        selection +=
                MediaStore.Files.FileColumns.MEDIA_TYPE + " IN " +
                        "(" + MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO + "," +
                        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + "," +
                        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + ")" + " AND "
        selection += MediaStore.Files.FileColumns.DATA + " LIKE ? "
        val selectionArgs = arrayOf("$path%")
        return context.contentResolver.query(uri, projection, selection, selectionArgs, null)
    }

}
