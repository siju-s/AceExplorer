package com.siju.acexplorer.helper

import android.content.Context
import android.media.MediaScannerConnection
import android.util.Log
import com.siju.acexplorer.extensions.getMimeType
import java.io.File

object MediaScannerHelper {

    fun scanFiles(context: Context?, pathList: Array<String>) {
        Log.e("MediaScannerHelper", "scanFiles : ${pathList.size}")
        context?.let {
            if (pathList.isNotEmpty()) {
                MediaScannerConnection.scanFile(context, pathList, null,
                                                mediaScannerConnectionCallback)
            }
        }
    }

    private val mediaScannerConnectionCallback = MediaScannerConnection.OnScanCompletedListener { path, _ ->
        Log.e("MediaScannerHelper", "scanned : $path")
    }

    fun isMediaScanningRequired(file: File): Boolean {
        val isDirectory = file.isDirectory
        if (isDirectory) {
            return false
        }
        val mimeType = file.getMimeType()
        Log.e("MediaScannerHelper", "isMediaScanningRequired: mimeType : $mimeType")
        return mimeType.startsWith("audio") ||
                mimeType.startsWith("video") || mimeType.startsWith("image")
    }
}