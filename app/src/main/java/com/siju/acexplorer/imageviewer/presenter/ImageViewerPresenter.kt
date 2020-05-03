package com.siju.acexplorer.imageviewer.presenter

import android.net.Uri
import com.siju.acexplorer.common.types.FileInfo

interface ImageViewerPresenter {

    fun inflateView()
    fun loadInfo(uri : Any) : FileInfo?
    fun deleteFile(uri: Any): Int
    fun shareClicked(uri: Any?)
    fun deleteFile(uri: Uri, treeUri: Uri): Int
}