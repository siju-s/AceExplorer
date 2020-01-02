package com.siju.acexplorer.imageviewer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.siju.acexplorer.imageviewer.presenter.ImageViewerPresenter
import com.siju.acexplorer.imageviewer.view.ImageViewerView

class ImageViewerViewModelFactory(private val view : ImageViewerView, private val presenter: ImageViewerPresenter) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImageViewerViewModel::class.java)) {
            return ImageViewerViewModel(view, presenter) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}