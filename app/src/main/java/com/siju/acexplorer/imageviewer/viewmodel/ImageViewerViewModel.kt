package com.siju.acexplorer.imageviewer.viewmodel

import android.app.RecoverableSecurityException
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.imageviewer.presenter.ImageViewerPresenter
import com.siju.acexplorer.imageviewer.view.ImageViewerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ImageViewerViewModel(private val view : ImageViewerView, private val presenter : ImageViewerPresenter) : ViewModel() {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _fileData = MutableLiveData<FileInfo?>()

    val fileData: LiveData<FileInfo?>
        get() = _fileData

    fun infoClicked(uri : Any) {
        Log.d("ViewModel", "info:$uri")
        uiScope.launch {
            Log.d("ViewModel", "info inside:$uri")
            val data = presenter.loadInfo(uri)
            _fileData.postValue(data)
        }
    }

    @Throws(RecoverableSecurityException::class)
    fun deleteClicked(uri: Uri?) {
      uri?.let {
          val deleted = presenter.deleteFile(uri)
          if (deleted > 0) {
              view.onDeleteSuccess()
          }
          else {
              view.onDeleteFailed()
          }
      }
    }

    fun shareClicked(uri: Uri?) {
        presenter.shareClicked(uri as Any?)
    }
}