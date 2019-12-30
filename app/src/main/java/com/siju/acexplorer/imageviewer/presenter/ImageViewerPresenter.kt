package com.siju.acexplorer.imageviewer.presenter

import com.siju.acexplorer.common.types.FileInfo

interface ImageViewerPresenter {

    fun inflateView()
    fun shareClicked(fileInfo : FileInfo)
    fun deleteClicked(fileInfo: FileInfo)
    fun infoClicked(fileInfo: FileInfo)
}