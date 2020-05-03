package com.siju.acexplorer.imageviewer.model

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.siju.acexplorer.BuildConfig
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.helper.MediaScannerHelper
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.SdkHelper
import com.siju.acexplorer.main.model.helper.ShareHelper
import com.siju.acexplorer.main.model.helper.UriHelper
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
        catch (exception : UnsupportedOperationException) {
            0
        }
    }

    override fun deleteFile(uri: Uri, treeUri: Uri): Int {
        val documentFile = DocumentFile.fromTreeUri(context, treeUri) ?: return 0
        if (documentFile.isDirectory) {
            val files = documentFile.listFiles()
            if (files.isEmpty()) {
                return 0
            }
            else {
                for (file in files) {
                    val result = file.delete()
                    return if (result) {
                        1
                    } else {
                        0
                    }
                }
            }
        }
        return 0
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
            try {
                count = context.contentResolver.delete(uri, null, null)
            }
            catch (exception : UnsupportedOperationException) {
                if (UriHelper.isExternalStorageDocument(uri)) {
                    return deletePath(UriHelper.getUriPath(uri))
                }
                return if (UriHelper.hasWritePermission(context, uri) && isOwnFileProvider(uri)) {
                    deletePath(UriHelper.getUriPath(uri))
                } else {
                    0
                }
            }
            if (count > 0 && isOwnFileProvider(uri)) {
                val encodedPath = UriHelper.getUriPath(uri)
                encodedPath ?: return 0
                MediaScannerHelper.scanFiles(context, arrayOf(encodedPath))
            }
            else if (UriHelper.hasWritePermission(context, uri) && isOwnFileProvider(uri)) {
                return deletePath(UriHelper.getUriPath(uri))
            }
            else if (isOwnFileProvider(uri) && context.contentResolver.persistedUriPermissions.isNotEmpty()) {
                val list = context.contentResolver.persistedUriPermissions
                for (permission in list) {
                        var documentFile = DocumentFile.fromTreeUri(context, permission.uri)
                        val uriPath = UriHelper.getUriPath(uri) ?: return 0
                        val parts = uriPath.split("/")

                    for (i in 3 until parts.size) {
                        documentFile = documentFile?.findFile(parts[i])
                    }
                    if (documentFile != null) {
                        val result =  documentFile.delete()
                        if (result) {
                            MediaScannerHelper.scanFiles(context, arrayOf(uriPath))
                            return  1
                        }
                    }
                }
                return 0
            }
            else {
                throw SecurityException(uri.toString())
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