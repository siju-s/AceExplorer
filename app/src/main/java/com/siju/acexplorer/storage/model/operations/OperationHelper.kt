package com.siju.acexplorer.storage.model.operations

import android.util.Log
import com.siju.acexplorer.main.model.helper.FileOperations
import com.siju.acexplorer.main.model.helper.FileUtils
import java.io.File

class OperationHelper {

    private val operationList = arrayListOf<OperationInfo>()
    private var operationId = 0

    private fun addOperation(operations: Operations, operationData: OperationData) {
        Log.e("OperationHelper", "addOperation: ${operationData.arg1}")
        operationId++
        operationList.add(OperationInfo(operationId, operations, operationData))
    }

    private fun removeOperation() {
        Log.e("OperationHelper", "removeOperation: ")
        if (hasOperations()) {
            operationList.removeAt(0)
        }
    }

    private fun getOperationData(): OperationData? {
        Log.d("OperationHelper", "getOperationData: ${hasOperations()}")
        if (hasOperations()) {
            return operationList[0].operationData
        }
        return null
    }

    private fun getOperationInfo() : OperationInfo? {
        if (hasOperations()) {
            return operationList[0]
        }
        return null
    }

    private fun hasOperations() = operationList.size > 0

    fun renameFile(filePath: String, newName: String,
                   fileOperationCallback: FileOperationCallback) {
        addOperation(Operations.RENAME, OperationData.createRenameOperation(filePath, newName))

        if (FileUtils.isFileNameInvalid(newName)) {
            fileOperationCallback.onOperationResult(Operations.RENAME, getOperationAction(
                    OperationResultCode.INVALID_FILE))
            removeOperation()
            return
        }
        val parent = File(filePath).parent
        when (OperationUtils.checkFolder(parent)) {

            OperationUtils.WriteMode.EXTERNAL -> {
                fileOperationCallback.onOperationResult(
                        Operations.RENAME,
                        getOperationAction(OperationResultCode.SAF))
                removeOperation()
            }

            OperationUtils.WriteMode.INTERNAL -> {
                var extension: String? = null
                var isFile = false
                if (File(filePath).isFile) {
                    extension = FileUtils.getExtensionWithDot(filePath)
                    isFile = true
                }
                val newFilePath: String
                newFilePath = if (isFile) {
                    parent + File.separator + newName + extension
                }
                else {
                    parent + File.separator + newName
                }
                if (FileUtils.isFileExisting(parent, newName)) {
                    fileOperationCallback.onOperationResult(Operations.RENAME,
                                                            getOperationAction(
                                                                    OperationResultCode.FILE_EXISTS))
                    removeOperation()
                }
                else {
                    val oldFile = File(filePath)
                    val newFile = File(newFilePath)
                    FileOperations.renameFolder(oldFile, newFile)
                    val success = !oldFile.exists() && newFile.exists()
                    Log.e("OperationHelper", "renameFile: sucess:$success")
                    val resultCode = if (success) OperationResultCode.SUCCESS else OperationResultCode.FAIL
                    fileOperationCallback.onOperationResult(Operations.RENAME, getOperationAction(
                            resultCode))
                    removeOperation()
                }
            }
        }
    }

    private fun getOperationAction(operationResultCode: OperationResultCode): OperationAction? {
        val operationData = getOperationData()
        Log.e("OperationHelper", "getOperationAction: data:$operationData")
        operationData?.let {
            return OperationAction(operationResultCode, it)
        }
        return null
    }

    fun onSafSuccess(fileOperationCallback: FileOperationCallback) {
       val operation = getOperationInfo()
        operation?.let {operationInfo ->
            when(operationInfo.operation) {
                Operations.RENAME -> renameFile(operationInfo.operationData.arg1, operationInfo.operationData.arg2, fileOperationCallback)
            }
        }
    }


    interface FileOperationCallback {

        fun onOperationResult(operation: Operations, operationAction: OperationAction?)
    }

}