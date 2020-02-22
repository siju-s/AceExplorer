package com.siju.acexplorer.storage.presenter

import androidx.lifecycle.MutableLiveData
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.view.dialog.DialogHelper
import com.siju.acexplorer.storage.model.PasteActionInfo
import com.siju.acexplorer.storage.model.PasteOpData
import com.siju.acexplorer.storage.model.StorageModel
import com.siju.acexplorer.storage.model.operations.OperationHelper
import com.siju.acexplorer.storage.model.operations.Operations
import com.siju.acexplorer.storage.model.operations.PasteConflictCheckData
import com.siju.acexplorer.storage.model.task.PasteConflictChecker

class PasteOpPresenter(val storageModel: StorageModel) {

    private val pasteActionInfo = arrayListOf<PasteActionInfo>()

    val _pasteData = MutableLiveData<PasteConflictCheckData>()
    val _showPasteDialog = MutableLiveData<Triple<Operations, String, ArrayList<FileInfo>>>()
    val _pasteOpData = MutableLiveData<PasteOpData>()

    fun checkPasteConflict(path: String, operationData: Pair<Operations, ArrayList<FileInfo>>) {
        val pasteConflictChecker = PasteConflictChecker(path, false,
                operationData.first,
                operationData.second)
        pasteConflictChecker.setListener(pasteResultListener)
        pasteConflictChecker.run()
    }

    fun createPasteOpData(operation: Operations, operations: Operations, list: ArrayList<FileInfo>, currentDir: String?) {
        _pasteOpData.value = PasteOpData(operation, operations, list, currentDir)
    }


    val pasteConflictListener = object : DialogHelper.PasteConflictListener {

        override fun onSkipClicked(pasteConflictCheckData: PasteConflictCheckData, isChecked: Boolean) {
            var end = false
            val filesToPaste = pasteConflictCheckData.files
            if (isChecked) {
                filesToPaste.removeAll(pasteConflictCheckData.conflictFiles)
                end = true
            }
            else {
                filesToPaste.remove(pasteConflictCheckData.conflictFiles[0])
                pasteConflictCheckData.destFiles.removeAt(0)
                pasteConflictCheckData.conflictFiles.removeAt(0)
                if (pasteConflictCheckData.conflictFiles.isEmpty()) {
                    end = true
                }
            }

            if (end) {
                if (filesToPaste.isNotEmpty()) {
                    storageModel.checkPasteWriteMode(pasteConflictCheckData.destinationDir, filesToPaste,
                            pasteActionInfo,
                            pasteConflictCheckData.operations,
                            pasteOperationCallback)
                }
            }
            else {
                _pasteData.postValue(
                        PasteConflictCheckData(filesToPaste, pasteConflictCheckData.conflictFiles, pasteConflictCheckData.destFiles,
                                pasteConflictCheckData.destinationDir, pasteConflictCheckData.operations))
            }
        }

        override fun onReplaceClicked(pasteConflictCheckData: PasteConflictCheckData, isChecked: Boolean) {
            var end = false
            val filesToPaste = pasteConflictCheckData.files
            if (isChecked) {
                end = true
            }
            else {
                pasteConflictCheckData.destFiles.removeAt(0)
                pasteConflictCheckData.conflictFiles.removeAt(0)
                if (pasteConflictCheckData.conflictFiles.isEmpty()) {
                    end = true
                }
            }

            if (end) {
                storageModel.checkPasteWriteMode(pasteConflictCheckData.destinationDir, filesToPaste,
                        pasteActionInfo,
                        pasteConflictCheckData.operations, pasteOperationCallback)
            }
            else {
                _pasteData.postValue(
                        PasteConflictCheckData(filesToPaste, pasteConflictCheckData.conflictFiles, pasteConflictCheckData.destFiles,
                                pasteConflictCheckData.destinationDir, pasteConflictCheckData.operations))
            }
        }

        override fun onKeepBothClicked(pasteConflictCheckData: PasteConflictCheckData, isChecked: Boolean) {
            var end = false
            val filesToPaste = pasteConflictCheckData.files
            if (isChecked) {
                for (fileInfo in pasteConflictCheckData.conflictFiles) {
                    pasteActionInfo.add(PasteActionInfo(fileInfo.filePath))
                }
                end = true
            }
            else {
                pasteActionInfo.add(PasteActionInfo(pasteConflictCheckData.conflictFiles[0].filePath))
                pasteConflictCheckData.destFiles.removeAt(0)
                pasteConflictCheckData.conflictFiles.removeAt(0)
                if (pasteConflictCheckData.conflictFiles.isEmpty()) {
                    end = true
                }
            }

            if (end) {
                storageModel.checkPasteWriteMode(pasteConflictCheckData.destinationDir, filesToPaste,
                        pasteActionInfo, pasteConflictCheckData.operations,
                        pasteOperationCallback)
            }
            else {
                _pasteData.postValue(
                        PasteConflictCheckData(filesToPaste, pasteConflictCheckData.conflictFiles, pasteConflictCheckData.destFiles,
                                pasteConflictCheckData.destinationDir, pasteConflictCheckData.operations))
            }
        }

    }

    val pasteOperationCallback = object : OperationHelper.PasteOperationCallback {

        override fun onPasteActionStarted(operation: Operations, destinationDir: String,
                                          files: ArrayList<FileInfo>) {
            if (operation == Operations.COPY) {
                _showPasteDialog.postValue(Triple(operation, destinationDir, files))
            }
        }
    }

    private val pasteResultListener = object : PasteConflictChecker.PasteResultCallback {
        override fun showConflictDialog(files: java.util.ArrayList<FileInfo>,
                                        conflictFiles: java.util.ArrayList<FileInfo>,
                                        destFiles: java.util.ArrayList<FileInfo>,
                                        destinationDir: String, operation: Operations) {
            Analytics.logger.conflictDialogShown()
            _pasteData.postValue(
                    PasteConflictCheckData(files, conflictFiles, destFiles, destinationDir, operation))
        }

        override fun checkWriteMode(destinationDir: String, files: java.util.ArrayList<FileInfo>,
                                    operation: Operations) {
            storageModel.checkPasteWriteMode(destinationDir, files, pasteActionInfo, operation,
                    pasteOperationCallback)
        }


        override fun onLowSpace() {
        }

    }
}