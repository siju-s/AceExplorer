package com.siju.acexplorer.main.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siju.acexplorer.common.types.FileInfo

class InfoSharedViewModel : ViewModel() {

    val fileInfo = MutableLiveData<FileInfo>()
    val uri = MutableLiveData<Uri?>()

    fun setFileInfo(fileInfo: FileInfo) {
        this.fileInfo.value = fileInfo
    }

    fun setUri(uri: Uri?) {
        this.uri.value = uri
    }
}