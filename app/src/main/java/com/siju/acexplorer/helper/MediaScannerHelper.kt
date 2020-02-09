package com.siju.acexplorer.helper

import android.content.Context
import android.media.MediaScannerConnection
import android.util.Log
import java.io.File

object MediaScannerHelper {

    fun scanFiles(context: Context?, pathList: Array<String>) {
        Log.d("MediaScannerHelper", "scanFiles : ${pathList.size}")
        context?.let {
            if (pathList.isNotEmpty()) {
                MediaScannerConnection.scanFile(context, pathList, null,
                                                mediaScannerConnectionCallback)
            }
        }
    }

    private val mediaScannerConnectionCallback = MediaScannerConnection.OnScanCompletedListener { path, _ ->
        Log.d("MediaScannerHelper", "scanned : $path")
    }

    fun isMediaScanningRequired(file: File): Boolean {
        return !file.isDirectory
    }
}