package com.siju.acexplorer.imageviewer.model

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.siju.acexplorer.BuildConfig
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.helper.MediaScannerHelper
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.SdkHelper
import com.siju.acexplorer.main.model.helper.ShareHelper
import com.siju.acexplorer.storage.model.operations.DeleteOperation
import java.io.File

class ImageViewerModelImpl(val context: Context) : ImageViewerModel {

    override fun loadInfo(uri: Any): FileInfo? {
        return if (uri is Uri) {
            SingleImageDataFetcher.fetchData(context, uri)
        }
        else {
            SingleImageDataFetcher.getImageInfo(context, uri as String)
        }
    }

    override fun deleteFile(uri: Any): Int {
        val uri1 = uri as Uri
        Log.e(this.javaClass.simpleName, "deleteFile:$uri1")
        return try {
            val path = uri.path
            if (path != null && File(path).exists()) {
                deletePath(path)
            } else {
                deleteUri(uri)
            }
        } catch (exception: SecurityException) {
            throw exception
        }
    }

    private fun getUriPath(uri: Uri): String? {
        var path = uri.path
        path ?: return null
        val pathSegments: List<String> = uri.pathSegments
        val firstSegment = pathSegments[0]
        val index = path.indexOf(firstSegment)
        path = path.substring(index + firstSegment.length)
        return path
    }

    private fun isOwnFileProvider(uri: Uri): Boolean {
        return uri.authority.equals(BuildConfig.APPLICATION_ID + ".fileprovider")
    }

    private fun deletePath(path: String?): Int {
        path ?: return 0
        return DeleteOperation().deleteFiles(arrayListOf(path))
    }

    private fun deleteUri(uri: Uri): Int {
        val imageId = uri.lastPathSegment ?: return 0
        val count: Int
        try {
            imageId.toLong()
        } catch (exception: NumberFormatException) {
            count = context.contentResolver.delete(uri, null, null)
            if (count > 0 && isOwnFileProvider(uri)) {
                val encodedPath = getUriPath(uri)
                encodedPath ?: return 0
                MediaScannerHelper.scanFiles(context, arrayOf(encodedPath))
            }
            return count
        }
        val contentUri = if (SdkHelper.isAtleastAndroid10) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val imageUri = ContentUris.withAppendedId(contentUri, imageId.toLong())
        count = context.contentResolver.delete(imageUri, null, null)
        return count
    }

    override fun shareClicked(uri: Any) {
        ShareHelper.shareMedia(context, Category.IMAGE, uri as Uri)
    }
}