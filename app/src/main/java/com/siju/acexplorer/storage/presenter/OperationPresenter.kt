package com.siju.acexplorer.storage.presenter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.view.dialog.DialogHelper
import com.siju.acexplorer.storage.model.PasteOpData
import com.siju.acexplorer.storage.model.operations.Operations
import com.siju.acexplorer.storage.model.operations.PasteConflictCheckData

interface OperationPresenter {

    val showDragDialog: LiveData<Triple<String?, ArrayList<FileInfo>, DialogHelper.DragDialogListener>>
    val getPasteConflictListener: DialogHelper.PasteConflictListener
    var currentDir: String?
    var category: Category?

    fun handleMenuItemClick(itemId : Int)
    fun getMultiSelectionOpData() : LiveData<Pair<Operations, ArrayList<FileInfo>>>
    fun getPasteOpData(): LiveData<PasteOpData>
    fun getSingleOpData(): LiveData<Pair<Operations, FileInfo>>
    fun getNoOpData(): MutableLiveData<Pair<Operations, String>>
    fun onFabClicked(operation: Operations, path: String?)
    fun onPasteAction(operation: Operations, filesToPaste: ArrayList<FileInfo>, destinationDir: String?)
    fun onOperation(operation: Operations?, newFileName: String?)
    fun checkPasteConflict(path: String, operationData: Pair<Operations, ArrayList<FileInfo>>)
    fun showPasteDialog(): LiveData<Triple<Operations, String, ArrayList<FileInfo>>>
    fun getPasteData(): LiveData<PasteConflictCheckData>
    fun getDragEvent(): LiveData<Triple<Category, Int, ArrayList<FileInfo>>>
    fun clearDraggedData()
    fun toggleDragData(fileInfo: FileInfo)
    fun onUpTouchEvent()
    fun onMoveTouchEvent(category: Category)
    fun isDragNotStarted(): Boolean
    fun onDragDropEvent(pos: Int)
    fun setLongPressedTime(longPressedTime: Long)
    fun onDragStarted()
    fun onDragEnded()
}