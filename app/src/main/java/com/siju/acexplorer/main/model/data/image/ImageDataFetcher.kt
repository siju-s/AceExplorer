package com.siju.acexplorer.main.model.data.image


import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.SortHelper
import java.util.*

class ImageDataFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {
        val cursor = fetchImages(context)
        val data = ImageCursorData.getImageDataFromCursor(cursor, category)
        return SortHelper.sortFiles(data, getSortMode(context))
    }

    override fun fetchCount(context: Context, path: String?): Int {
        val cursor = fetchImages(context)
        return getCursorCount(cursor)
    }

    private fun fetchImages(context: Context): Cursor? {
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA)
        val sortOrder = MediaStore.Images.Media.BUCKET_ID
        return context.contentResolver.query(uri, projection, null, null, sortOrder)
    }
}
