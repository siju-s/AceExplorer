package com.siju.acexplorer.storage.presenter

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.logging.Logger
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.view.dialog.DialogHelper
import com.siju.acexplorer.storage.model.operations.Operations
import com.siju.acexplorer.storage.viewmodel.FileListViewModel
import java.io.File

private const val MIN_DRAG_TIME_MS = 1500
private const val TAG = "DragOperation"

class DragOperation(private val viewModel: FileListViewModel, private val operationPresenter: OperationPresenter) {
    val showDragDialog = MutableLiveData<Triple<String?, ArrayList<FileInfo>, DialogHelper.DragDialogListener>>()
    val dragEvent = MutableLiveData<Triple<Category, Int, ArrayList<FileInfo>>>()
    private val draggedData = arrayListOf<FileInfo>()

    private var longPressedTimeMs = 0L
    private var dragStarted = false

    fun onUpTouchEvent() {
        dragStarted = false
        longPressedTimeMs = 0
    }

    fun onMoveTouchEvent(selectedCount: Int, category: Category) {
        if (longPressedTimeMs == 0L) {
            return
        }
        val timeElapsed = System.currentTimeMillis() - longPressedTimeMs
        Log.d(TAG, "onMoveTouchEvent:timeElapsed:$timeElapsed, longpressTime:$longPressedTimeMs")
        if (timeElapsed > MIN_DRAG_TIME_MS) {
            longPressedTimeMs = 0
            dragStarted = false
            if (draggedData.isNotEmpty()) {
                dragEvent.value = Triple(category, selectedCount, draggedData)
            }
        }
    }

    fun toggleDragData(fileInfo: FileInfo) {
        if (draggedData.contains(fileInfo)) {
            draggedData.remove(fileInfo)
        } else {
            draggedData.add(fileInfo)
        }
    }

    fun clearDragData() {
        draggedData.clear()
    }

    fun isDragNotStarted() = !dragStarted

    fun onDragDropEvent(pos: Int, currentDir: String?) {
        val paths = java.util.ArrayList<String>()

        var destinationDir: String?
        destinationDir = if (pos != -1) {
            viewModel.fileData.value?.get(pos)?.filePath
        } else {
            currentDir
        }

        for (info in draggedData) {
            paths.add(info.filePath)
        }

        val sourceParent = File(draggedData[0].filePath).parent
        if (File(destinationDir).isFile) {
            destinationDir = File(destinationDir).parent
        }

        if (!paths.contains(destinationDir)) {
            if (destinationDir != sourceParent) {
                Logger.log(TAG, "Source parent=" + sourceParent + " Dest=" +
                        destinationDir + "draggedFiles:" + draggedData.size)
                showDragDialog.value = Triple(destinationDir, draggedData, dragDialogListener)
            } else {
                val info = ArrayList(draggedData)
                Logger.log(TAG, "Source=" + draggedData[0] + "Dest=" +
                        destinationDir)
                operationPresenter.onPasteAction(Operations.COPY, info, destinationDir)
            }
        }
    }

    fun setLongPressedTime(longPressedTime: Long) {
        this.longPressedTimeMs = longPressedTime
    }

    fun onDragStarted() {
        dragStarted = true
        Log.d(TAG, "onDragStarted")
    }

    fun dragEnded() {
        dragStarted = false
        Log.d(TAG, "dragEnded")
    }

    private val dragDialogListener = DialogHelper.DragDialogListener { filesToPaste, destinationDir, operation ->
        operationPresenter.onPasteAction(operation, filesToPaste, destinationDir)
    }
}