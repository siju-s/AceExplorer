package com.siju.acexplorer.storage.presenter

import androidx.lifecycle.LiveData
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.storage.model.operations.OperationHelper
import com.siju.acexplorer.storage.model.operations.Operations
import com.siju.acexplorer.storage.modules.zipviewer.ZipViewerCallback

interface ZipPresenter {
    val getShowZipDialogLiveData: LiveData<Triple<Operations, String, String>>
    val showCompressDialog: LiveData<Triple<Operations, String, ArrayList<FileInfo>>>
    val zipOperationCallback: OperationHelper.ZipOperationCallback
    val zipCallback: ZipViewerCallback
    var isZipMode: Boolean

}