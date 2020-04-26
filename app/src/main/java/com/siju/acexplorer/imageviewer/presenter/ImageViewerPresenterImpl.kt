package com.siju.acexplorer.imageviewer.presenter

import android.app.RecoverableSecurityException
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.imageviewer.model.ImageViewerModel
import com.siju.acexplorer.imageviewer.view.ImageViewerView

class ImageViewerPresenterImpl(private val view : ImageViewerView,
                               private val imageViewerModel: ImageViewerModel) : ImageViewerPresenter
{

    override fun inflateView() {
        view.inflate()
    }

    override fun loadInfo(uri: Any): FileInfo? {
        return imageViewerModel.loadInfo(uri)
    }

    @Throws(RecoverableSecurityException::class)
    override fun deleteFile(uri: Any) = imageViewerModel.deleteFile(uri)

    override fun shareClicked(uri: Any?) {
        uri?.let {
            imageViewerModel.shareClicked(uri)
        }
    }
}