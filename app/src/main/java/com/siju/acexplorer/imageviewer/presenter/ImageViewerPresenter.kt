package com.siju.acexplorer.imageviewer.presenter

import com.siju.acexplorer.common.types.FileInfo

interface ImageViewerPresenter {

    fun inflateView()
    fun shareClicked(fileInfo : FileInfo)
    fun infoClicked(fileInfo: FileInfo)
    fun loadData(uri : Any) : FileInfo?
    fun deleteFile(uri: Any): Int
}