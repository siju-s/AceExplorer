package com.siju.acexplorer.imageviewer.presenter

import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.imageviewer.model.ImageViewerModel
import com.siju.acexplorer.imageviewer.view.ImageViewerView

class ImageViewerPresenterImpl(private val view : ImageViewerView,
                               private val imageViewerModel: ImageViewerModel) : ImageViewerPresenter
{

    override fun inflateView() {
        view.inflate()
    }

    override fun loadData(uri: Any): FileInfo? {
        return imageViewerModel.loadInfo(uri)
    }

    override fun deleteFile(uri: Any) = imageViewerModel.deleteFile(uri)

    override fun shareClicked(fileInfo: FileInfo) {
    }

    override fun infoClicked(fileInfo: FileInfo) {
    }
}