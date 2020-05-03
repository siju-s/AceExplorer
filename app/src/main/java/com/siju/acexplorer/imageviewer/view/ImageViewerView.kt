package com.siju.acexplorer.imageviewer.view

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.imageviewer.viewmodel.ImageViewerViewModel

interface ImageViewerView {

    fun setActivity(activity : AppCompatActivity)
    fun setPosition(pos : Int)
    fun setUriList(list : ArrayList<Uri?>)
    fun inflate()
    fun onFileInfoFetched(fileInfo: FileInfo?)
    fun shareClicked()
    fun infoClicked()
    fun deleteClicked()
    fun setViewModel(viewModel: ImageViewerViewModel)
    fun onDeleteSuccess()
    fun onDeleteFailed()
    fun setPathList(pathList: ArrayList<String?>)
    fun setNoWriteAccess()
    fun handleSafResult(uri: Uri, flags: Int)
}