package com.siju.acexplorer.storage.model.operations

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.helper.FileOperations
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.SdkHelper
import com.siju.acexplorer.storage.model.PasteActionInfo
import com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_OP_REFRESH
import com.siju.acexplorer.storage.model.task.CopyService
import java.io.File

private const val EXT_TXT = ".txt"

class OperationHelper(val context: Context) {

    private val operationList = arrayListOf<OperationInfo>()
    private var operationId = 0
    private var fileOperationCallback: FileOperationCallback? = null
    private val operationResultReceiver = OperationResultReceiver(this)

    init {
        registerReceiver()
    }

    private fun addOperation(operations: Operations, operationData: OperationData) {
        Log.e("OperationHelper", "addOperation: ${operationData.arg1}")
        operationId++
        operationList.add(OperationInfo(operationId, operations, operationData))
    }

    private fun registerReceiver() {
        val intentFilter = IntentFilter(ACTION_OP_REFRESH)
        context.registerReceiver(operationResultReceiver, intentFilter)
    }

    private fun unregisterReceiver() {
        context.unregisterReceiver(operationResultReceiver)
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

    private fun getOperationInfo(): OperationInfo? {
        if (hasOperations()) {
            return operationList[0]
        }
        return null
    }

    private fun hasOperations() = operationList.size > 0

    fun renameFile(operation: Operations, filePath: String, newName: String,
                   fileOperationCallback: FileOperationCallback) {
        addOperation(operation, OperationData.createRenameOperation(filePath, newName))

        if (FileUtils.isFileNameInvalid(newName)) {
            fileOperationCallback.onOperationResult(operation, getOperationAction(
                    OperationResult(OperationResultCode.INVALID_FILE, 0)))
            removeOperation()
            return
        }
        val parent = File(filePath).parent
        when (OperationUtils.checkFolder(parent)) {

            OperationUtils.WriteMode.EXTERNAL -> {
                fileOperationCallback.onOperationResult(
                        operation,
                        getOperationAction(OperationResult(OperationResultCode.SAF, 0)))
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
                    fileOperationCallback.onOperationResult(operation,
                                                            getOperationAction(
                                                                    OperationResult(
                                                                            OperationResultCode.FILE_EXISTS,
                                                                            0)))
                    removeOperation()
                }
                else {
                    val oldFile = File(filePath)
                    val newFile = File(newFilePath)
                    FileOperations.renameFolder(oldFile, newFile)
                    val success = !oldFile.exists() && newFile.exists()
                    Log.e("OperationHelper", "renameFile: success:$success")
                    val resultCode = if (success) OperationResultCode.SUCCESS else OperationResultCode.FAIL
                    fileOperationCallback.onOperationResult(operation, getOperationAction(
                            OperationResult(resultCode, 1)))
                    removeOperation()
                }
            }
        }
    }

    fun createFolder(operation: Operations, path: String, name: String,
                     fileOperationCallback: FileOperationCallback) {

        addOperation(operation, OperationData.createNewFolderOperation(path, name))
        if (FileUtils.isFileNameInvalid(name)) {
            fileOperationCallback.onOperationResult(operation, getOperationAction(
                    OperationResult(OperationResultCode.INVALID_FILE, 0)))
            removeOperation()
            return
        }
        val file = File(path + File.separator + name)
        if (file.exists()) {
            fileOperationCallback.onOperationResult(operation,
                                                    getOperationAction(
                                                            OperationResult(
                                                                    OperationResultCode.FILE_EXISTS,
                                                                    0)))
            removeOperation()
            return
        }
        when (OperationUtils.checkFolder(path)) {
            OperationUtils.WriteMode.EXTERNAL -> {
                fileOperationCallback.onOperationResult(
                        operation,
                        getOperationAction(OperationResult(OperationResultCode.SAF, 0)))
                removeOperation()
            }
            OperationUtils.WriteMode.INTERNAL -> {
                val success = FileOperations.mkdir(file)
                val resultCode = if (success) OperationResultCode.SUCCESS else OperationResultCode.FAIL
                fileOperationCallback.onOperationResult(operation, getOperationAction(
                        OperationResult(resultCode, 1)))
                removeOperation()
            }
        }
    }

    fun createFile(operation: Operations, path: String, name: String,
                   fileOperationCallback: FileOperationCallback) {

        addOperation(operation, OperationData.createNewFileOperation(path, name))
        if (FileUtils.isFileNameInvalid(name)) {
            fileOperationCallback.onOperationResult(operation, getOperationAction(
                    OperationResult(OperationResultCode.INVALID_FILE, 0)))
            removeOperation()
            return
        }
        val file = File(path + File.separator + name + EXT_TXT)
        if (file.exists()) {
            fileOperationCallback.onOperationResult(operation,
                                                    getOperationAction(OperationResult(
                                                            OperationResultCode.FILE_EXISTS, 0)))
            removeOperation()
            return
        }
        when (OperationUtils.checkFolder(path)) {
            OperationUtils.WriteMode.EXTERNAL -> {
                fileOperationCallback.onOperationResult(
                        operation,
                        getOperationAction(OperationResult(OperationResultCode.SAF, 0)))
                removeOperation()
            }
            OperationUtils.WriteMode.INTERNAL -> {
                val success = FileOperations.mkfile(file)
                val resultCode = if (success) OperationResultCode.SUCCESS else OperationResultCode.FAIL
                fileOperationCallback.onOperationResult(operation,
                                                        getOperationAction(OperationResult(
                                                                resultCode, 1)))
                removeOperation()
            }
        }
    }

    fun deleteFiles(operation: Operations, filesList: ArrayList<String>,
                    fileOperationCallback: FileOperationCallback) {
        addOperation(operation, OperationData.createDeleteOperation(filesList))
        val mode = OperationUtils.checkFolder(File(filesList[0]).parent)
        when (mode) {
            OperationUtils.WriteMode.INTERNAL -> {
                val count = FileOperations.deleteFiles(filesList)
                val resultCode =
                        if (count > 0) {
                            OperationResultCode.SUCCESS
                        }
                        else {
                            OperationResultCode.FAIL
                        }
                fileOperationCallback.onOperationResult(operation, getOperationAction(
                        OperationResult(resultCode, count)))
            }
        }
    }


    private fun getOperationAction(operationResult: OperationResult): OperationAction? {
        val operationData = getOperationData()
        Log.e("OperationHelper", "getOperationAction: data:$operationData")
        operationData?.let {
            return OperationAction(operationResult, it)
        }
        return null
    }

    fun onSafSuccess(fileOperationCallback: FileOperationCallback) {
        val operation = getOperationInfo()
        operation?.let { operationInfo ->
            when (operationInfo.operation) {
                Operations.RENAME          -> {
                    renameFile(operationInfo.operation,
                               operationInfo.operationData.arg1,
                               operationInfo.operationData.arg2,
                               fileOperationCallback)
                }

                Operations.FOLDER_CREATION -> {
                    createFolder(operationInfo.operation,
                                 operationInfo.operationData.arg1,
                                 operationInfo.operationData.arg2,
                                 fileOperationCallback)
                }
            }
        }

    }

    fun copyFiles(context: Context, destinationDir: String, files: ArrayList<FileInfo>,
                  pasteActionInfo: ArrayList<PasteActionInfo>,
                  pasteOperationCallback: PasteOperationCallback,
                  fileOperationCallback: FileOperationCallback) {
        setFileOperationCallback(fileOperationCallback)
        when (OperationUtils.checkFolder(destinationDir)) {
            OperationUtils.WriteMode.INTERNAL -> {
                addOperation(Operations.COPY,
                             OperationData.createCopyOperation(destinationDir, files))
                pasteOperationCallback.onPasteActionStarted(Operations.COPY, destinationDir, files)

                val intent = Intent(context, CopyService::class.java)
                intent.apply {
                    putParcelableArrayListExtra(OperationUtils.KEY_CONFLICT_DATA, pasteActionInfo)
                    putParcelableArrayListExtra(OperationUtils.KEY_FILES, files)
                    putExtra(OperationUtils.KEY_FILEPATH, destinationDir)
                }
                if (SdkHelper.isAtleastOreo()) {
                    context.startForegroundService(intent)
                }
                else {
                    context.startService(intent)
                }
            }
        }
    }

    private fun setFileOperationCallback(fileOperationCallback: FileOperationCallback) {
        this.fileOperationCallback = fileOperationCallback
    }

    fun onCopyCompleted(operation: Operations, count: Int) {
        val resultCode =
                if (count > 0) {
                    OperationResultCode.SUCCESS
                }
                else {
                    OperationResultCode.FAIL
                }
        fileOperationCallback?.onOperationResult(operation, getOperationAction(
                OperationResult(resultCode, count)))
    }

    fun cleanup() {
        unregisterReceiver()
        fileOperationCallback = null
    }


    interface FileOperationCallback {
        fun onOperationResult(operation: Operations, operationAction: OperationAction?)
    }

    interface PasteOperationCallback {
        fun onPasteActionStarted(operation: Operations, destinationDir: String,
                                 files: ArrayList<FileInfo>)
    }

}