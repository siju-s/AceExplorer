package com.siju.acexplorer.imageviewer.model

import com.siju.acexplorer.common.types.FileInfo

interface ImageViewerModel {

    fun loadInfo(uri : Any): FileInfo?
    fun deleteFile(uri: Any): Int
}