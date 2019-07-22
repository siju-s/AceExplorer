package com.siju.acexplorer.storage.model.operations

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.home.model.FavoriteHelper
import com.siju.acexplorer.main.model.helper.FileOperations
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.SdkHelper
import com.siju.acexplorer.storage.model.PasteActionInfo
import com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_OP_REFRESH
import com.siju.acexplorer.storage.model.task.CopyService
import com.siju.acexplorer.storage.model.task.CreateZipService
import com.siju.acexplorer.storage.model.task.ExtractService
import com.siju.acexplorer.storage.model.task.MoveService
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
        Log.e("OperationHelper", "addOperation: ${operationData}")
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
        Log.d("OperationHelper", "getOperationResult: ${hasOperations()}")
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
        when (OperationUtils.getWriteMode(parent)) {

            OperationUtils.WriteMode.EXTERNAL -> {
                fileOperationCallback.onOperationResult(
                        operation,
                        getOperationAction(OperationResult(OperationResultCode.SAF, 0)))
            }

            OperationUtils.WriteMode.INTERNAL -> {
                renameFile(operation, filePath, parent, newName, fileOperationCallback)
            }
        }
    }

    private fun renameFile(operation: Operations,
                           filePath: String, parent: String,
                           newName: String,
                           fileOperationCallback: FileOperationCallback) {
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
        Log.e(this.javaClass.simpleName, "renameFile : parent $parent newName $newName newpath : $newFilePath")
        if (FileUtils.isFileExisting(parent, newName)) {
            Log.e(this.javaClass.simpleName, "renameFile : existing")
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
            val result = FileOperations.renameFolder(oldFile, newFile)
              Log.e("OperationHelper", "renameFile: result : $result")
            val resultCode = if (result) OperationResultCode.SUCCESS else OperationResultCode.FAIL
            fileOperationCallback.onOperationResult(operation, getOperationAction(
                    OperationResult(resultCode, 1)))
            removeOperation()
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
        when (OperationUtils.getWriteMode(path)) {
            OperationUtils.WriteMode.EXTERNAL -> {
                fileOperationCallback.onOperationResult(
                        operation,
                        getOperationAction(OperationResult(OperationResultCode.SAF, 0)))
            }
            OperationUtils.WriteMode.INTERNAL -> {
                createFolder(operation, file, fileOperationCallback)
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
        when (OperationUtils.getWriteMode(path)) {
            OperationUtils.WriteMode.EXTERNAL -> {
                fileOperationCallback.onOperationResult(
                        operation,
                        getOperationAction(OperationResult(OperationResultCode.SAF, 0)))
            }
            OperationUtils.WriteMode.INTERNAL -> {
                createFile(operation, file, fileOperationCallback)
            }
        }
    }

    private fun createFile(operation: Operations,
                           file: File,
                           fileOperationCallback: FileOperationCallback) {
        val success = FileOperations.mkfile(file)
        val resultCode = if (success) OperationResultCode.SUCCESS else OperationResultCode.FAIL
        fileOperationCallback.onOperationResult(operation,
                                                getOperationAction(OperationResult(
                                                        resultCode, 1)))
        removeOperation()
    }

    fun deleteFiles(operation: Operations, filesList: ArrayList<String>,
                    fileOperationCallback: FileOperationCallback) {
        addOperation(operation, OperationData.createDeleteOperation(filesList))
        when (OperationUtils.getWriteMode(File(filesList[0]).parent)) {
            OperationUtils.WriteMode.INTERNAL -> {
                deleteWriteableFiles(filesList, fileOperationCallback, operation)
            }
            OperationUtils.WriteMode.EXTERNAL -> {
                fileOperationCallback.onOperationResult(
                        operation,
                        getOperationAction(OperationResult(OperationResultCode.SAF, 0)))
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
                    val path = operationInfo.operationData.arg1
                    renameFile(operationInfo.operation, path, File(path).parent, operationInfo.operationData.arg2,
                               fileOperationCallback)
                }

                Operations.FOLDER_CREATION -> {
                    val file = File(operationInfo.operationData.arg1 + File.separator + operationInfo.operationData.arg2)
                    createFolder(operationInfo.operation, file, fileOperationCallback)
                }

                Operations.FILE_CREATION -> {
                    val file = File(operationInfo.operationData.arg1 + File.separator + operationInfo.operationData.arg2 + EXT_TXT)
                    createFile(operationInfo.operation, file, fileOperationCallback)
                }

                Operations.DELETE -> {
                    val filesToDelete = operationInfo.operationData.paths
                    deleteWriteableFiles(filesToDelete, fileOperationCallback,
                                         operationInfo.operation)
                }
            }
        }

    }

    private fun deleteWriteableFiles(filesList: ArrayList<String>,
                                     fileOperationCallback: FileOperationCallback,
                                     operation: Operations) {
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
        removeOperation()
    }

    private fun createFolder(operation: Operations,
                             file: File,
                             fileOperationCallback: FileOperationCallback) {
        val success = FileOperations.mkdir(file)
        val resultCode = if (success) OperationResultCode.SUCCESS else OperationResultCode.FAIL
        fileOperationCallback.onOperationResult(operation, getOperationAction(
                OperationResult(resultCode, 1)))
        removeOperation()
    }


    fun copyFiles(context: Context, destinationDir: String, files: ArrayList<FileInfo>,
                  pasteActionInfo: ArrayList<PasteActionInfo>,
                  pasteOperationCallback: PasteOperationCallback,
                  fileOperationCallback: FileOperationCallback) {
        setFileOperationCallback(fileOperationCallback)
        when (OperationUtils.getWriteMode(destinationDir)) {
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

    fun moveFiles(context: Context, destinationDir: String, files: ArrayList<FileInfo>,
                  pasteActionInfo: ArrayList<PasteActionInfo>,
                  pasteOperationCallback: PasteOperationCallback,
                  fileOperationCallback: FileOperationCallback) {
        setFileOperationCallback(fileOperationCallback)
        when (OperationUtils.getWriteMode(destinationDir)) {
            OperationUtils.WriteMode.INTERNAL -> {
                addOperation(Operations.CUT,
                             OperationData.createCopyOperation(destinationDir, files))
                pasteOperationCallback.onPasteActionStarted(Operations.CUT, destinationDir, files)

                val intent = Intent(context, MoveService::class.java)
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

    fun extractFile(context: Context, sourceFilePath : String, destinationDir: String, newName: String,
                    zipOperationCallback: ZipOperationCallback,
                    fileOperationCallback: FileOperationCallback) {
        setFileOperationCallback(fileOperationCallback)
        val newPath = "$destinationDir/$newName"

        if (FileUtils.isFileNameInvalid(newName)) {
            fileOperationCallback.onOperationResult(Operations.EXTRACT, getOperationAction(
                    OperationResult(OperationResultCode.INVALID_FILE, 0)))
            return
        }
        if (File(newPath).exists()) {
            fileOperationCallback.onOperationResult(Operations.EXTRACT,
                                                    getOperationAction(OperationResult(
                                                            OperationResultCode.FILE_EXISTS, 0)))
            return
        }
        when (OperationUtils.getWriteMode(destinationDir)) {
            OperationUtils.WriteMode.INTERNAL -> {
                addOperation(Operations.EXTRACT, OperationData.createExtractOperation(sourceFilePath, newPath))
                zipOperationCallback.onZipOperationStarted(Operations.EXTRACT, sourceFilePath, newPath)
                val intent = Intent(context, ExtractService::class.java)
                intent.apply {
                    putExtra(OperationUtils.KEY_FILEPATH, sourceFilePath)
                    putExtra(OperationUtils.KEY_FILEPATH2, newPath)
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

    fun compressFile(context: Context, destinationDir: String, filesToArchive : ArrayList<FileInfo>,
                    zipOperationCallback: ZipOperationCallback,
                    fileOperationCallback: FileOperationCallback) {
        setFileOperationCallback(fileOperationCallback)
        val newFile = File(destinationDir)

        if (FileUtils.isFileNameInvalid(newFile.name)) {
            fileOperationCallback.onOperationResult(Operations.COMPRESS, getOperationAction(
                    OperationResult(OperationResultCode.INVALID_FILE, 0)))
            return
        }
        if (FileUtils.isFileExisting(newFile.parent, newFile.name)) {
            fileOperationCallback.onOperationResult(Operations.COMPRESS,
                                                    getOperationAction(OperationResult(
                                                            OperationResultCode.FILE_EXISTS, 0)))
            return
        }
        when (OperationUtils.getWriteMode(newFile.parent)) {
            OperationUtils.WriteMode.INTERNAL -> {
                addOperation(Operations.EXTRACT, OperationData.createArchiveOperation(destinationDir, filesToArchive))
                zipOperationCallback.onZipOperationStarted(Operations.COMPRESS, destinationDir, filesToArchive)
                val intent = Intent(context, CreateZipService::class.java)
                intent.apply {
                    putExtra(OperationUtils.KEY_FILEPATH, destinationDir)
                    putParcelableArrayListExtra(OperationUtils.KEY_FILES, filesToArchive)
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

    fun onOperationCompleted(operation: Operations, count: Int) {
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

    fun addToFavorite(context: Context, favList: ArrayList<String>, fileOperationCallback: FileOperationCallback) {
        setFileOperationCallback(fileOperationCallback)
        addOperation(Operations.FAVORITE, OperationData.createFavoriteOperation(favList))
        val count = FavoriteHelper.addFavorites(context, favList)
        onOperationCompleted(Operations.FAVORITE, count)
    }


    interface FileOperationCallback {
        fun onOperationResult(operation: Operations, operationAction: OperationAction?)
    }

    interface PasteOperationCallback {
        fun onPasteActionStarted(operation: Operations, destinationDir: String,
                                 files: ArrayList<FileInfo>)
    }
    interface ZipOperationCallback {
        fun onZipOperationStarted(operation: Operations, sourceFilePath: String, destinationDir: String)

        fun onZipOperationStarted(operation: Operations, destinationDir: String, filesToArchive: ArrayList<FileInfo>)

    }

}