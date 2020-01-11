package com.siju.acexplorer.imageviewer.model

import android.content.Context
import android.net.Uri
import android.util.Log
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.ShareHelper

class ImageViewerModelImpl(val context: Context) : ImageViewerModel {

    override fun loadInfo(uri: Any): FileInfo? {
        Log.e("ImageViewerModelImpl", "info:$uri")
        return if (uri is Uri) {
            SingleImageDataFetcher.fetchData(context, uri)
        }
        else {
            SingleImageDataFetcher.getImageInfo(context, uri as String)
        }
    }

    override fun deleteFile(uri: Any): Int {
        val count = context.contentResolver.delete(uri as Uri, null, null)
        if (count > 0) {
            context.contentResolver.notifyChange(uri, null)
        }
        return count
    }

    override fun shareClicked(uri: Any) {
        ShareHelper.shareMedia(context, Category.IMAGE, uri as Uri)
    }
}