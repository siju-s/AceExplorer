package com.siju.acexplorer.imageviewer.model

import android.content.Context
import android.net.Uri
import android.util.Log
import com.siju.acexplorer.common.types.FileInfo

class ImageViewerModelImpl(val context: Context) : ImageViewerModel {

    override fun loadInfo(uri: Any): FileInfo? {
        Log.e("ImageViewerModelImpl", "info:$uri")
        return ImageUriFetcher.fetchData(context, uri as Uri)
    }

    override fun deleteFile(uri: Any): Int {
        val count = context.contentResolver.delete(uri as Uri, null, null)
        if (count > 0) {
            context.contentResolver.notifyChange(uri, null)
        }
        return count
    }
}