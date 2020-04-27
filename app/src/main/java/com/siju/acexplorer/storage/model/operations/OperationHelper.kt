package com.siju.acexplorer.storage.model.operations

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.helper.MediaScannerHelper
import com.siju.acexplorer.home.model.FavoriteHelper
import com.siju.acexplorer.main.model.helper.FileOperations
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.SdkHelper
import com.siju.acexplorer.main.model.root.RootDeniedException
import com.siju.acexplorer.main.model.root.RootOperations
import com.siju.acexplorer.main.model.root.RootOperations.renameRoot
import com.siju.acexplorer.main.model.root.RootUtils
import com.siju.acexplorer.storage.model.PasteActionInfo
import com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_OP_REFRESH
import com.siju.acexplorer.storage.model.task.CopyService
import com.siju.acexplorer.storage.model.task.CreateZipService
import com.siju.acexplorer.storage.model.task.ExtractService
import com.stericson.RootTools.RootTools
import java.io.File

private const val EXT_TXT = ".txt"

class OperationHelper(val context: Context) {

    private val operationList = arrayListOf<OperationInfo>()
    private var operationId = 0
    private var fileOperationCallback: FileOperationCallback? = null
    private var zipOperationCallback: ZipOperationCallback? = null
    private val operationResultReceiver = OperationResultReceiver(this)
    private var receiverRegistered = false
    private var pasteActionInfo = arrayListOf<PasteActionInfo>()
    private var pasteOperationCallback : PasteOperationCallback? = null

    init {
        registerReceiver()
    }

    private fun addOperation(operations: Operations, operationData: OperationData) {
        Log.d("OperationHelper", "addOperation: $operationData")
        operationId++
        operationList.add(OperationInfo(operationId, operations, operationData))
    }

    private fun registerReceiver() {
        val intentFilter = IntentFilter(ACTION_OP_REFRESH)
        receiverRegistered = true
        context.registerReceiver(operationResultReceiver, intentFilter)
    }

    private fun unregisterReceiver() {
        if (receiverRegistered) {
            context.unregisterReceiver(operationResultReceiver)
        }
        receiverRegistered = false
    }


    private fun removeOperation() {
        Log.d("OperationHelper", "removeOperation: ")
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
            OperationUtils.WriteMode.ROOT -> {
                renameFileRoot(operation, filePath, newName, fileOperationCallback)
            }
            OperationUtils.WriteMode.EXTERNAL -> {
                fileOperationCallback.onOperationResult(
                        operation,
                        getOperationAction(OperationResult(OperationResultCode.SAF, 0)))
            }

            OperationUtils.WriteMode.INTERNAL -> {
                parent?.let {
                    renameFile(operation, filePath, parent, newName, fileOperationCallback)
                }
            }
        }
    }

    private fun renameFileRoot(operation: Operations, filePath: String, newName: String, fileOperationCallback: FileOperationCallback) {
        val oldFile = File(filePath)
        var extension: String? = null
        var isFile = false
        if (File(filePath).isFile) {
            extension = FileUtils.getExtensionWithDot(filePath)
            isFile = true
        }
        val parent = File(filePath).parent
        if (parent == null) {
            fileOperationCallback.onOperationResult(operation, getOperationAction(
                    OperationResult(OperationResultCode.FAIL, 1)))
            removeOperation()
            return
        }
        val newFilePath: String
        newFilePath = if (isFile) {
            parent + File.separator + newName + extension
        }
        else {
            parent + File.separator + newName
        }
        val newFile = File(newFilePath)
        var result = FileOperations.renameFile(oldFile, newFile)
        val fileCreated = !oldFile.exists() && newFile.exists()
        if (!result) {
            if (!fileCreated && RootUtils.isRooted(context)) {
                try {
                    renameRoot(oldFile, newFile.name)
                } catch (e: RootDeniedException) {
                }

                result = true
            }
        }
        val resultCode = if (result) OperationResultCode.SUCCESS else OperationResultCode.FAIL
        fileOperationCallback.onOperationResult(operation, getOperationAction(
                OperationResult(resultCode, 1)))
        removeOperation()
    }

    private fun renameFile(operation: Operations,
                           filePath: String, parent: String,
                           newName: String,
                           fileOperationCallback: FileOperationCallback) {
        var extension: String? = null
        var isFile = false
        if (File(filePath).isFile && operation == Operations.RENAME) {
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
        Log.d(this.javaClass.simpleName, "renameFile : parent $parent newName $newName newpath : $newFilePath")
        if (FileUtils.isFileExisting(parent, newName)) {
            Log.d(this.javaClass.simpleName, "renameFile : existing")
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
            val result = FileOperations.renameFile(oldFile, newFile)
            if (result && MediaScannerHelper.isMediaScanningRequired(newFile)) {
                MediaScannerHelper.scanFiles(context, arrayOf(filePath))
                MediaScannerHelper.scanFiles(context, arrayOf(newFilePath))
            }
            Log.d("OperationHelper", "renameFile: result : $result")
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
        when (OperationUtils.getWriteMode(path)) {
            OperationUtils.WriteMode.ROOT -> {
                createFolderInRoot(file, fileOperationCallback, operation)
            }
            OperationUtils.WriteMode.EXTERNAL -> {
                if (checkIfFileExists(file, fileOperationCallback, operation)) return
                fileOperationCallback.onOperationResult(
                        operation,
                        getOperationAction(OperationResult(OperationResultCode.SAF, 0)))
            }
            OperationUtils.WriteMode.INTERNAL -> {
                if (checkIfFileExists(file, fileOperationCallback, operation)) return
                createFolder(operation, file, fileOperationCallback)
            }
        }
    }

    private fun createFolderInRoot(file: File, fileOperationCallback: FileOperationCallback, operation: Operations) {
        RootTools.debugMode = true
        val exists: Boolean
        try {
            exists = RootOperations.fileExists(file.absolutePath, true)
        } catch (e: RootDeniedException) {
            fileOperationCallback.onOperationResult(
                    operation,
                    getOperationAction(OperationResult(OperationResultCode.FAIL, 0)))
            removeOperation()
            return
        }
        if (exists) {
            fileOperationCallback.onOperationResult(operation,
                    getOperationAction(
                            OperationResult(
                                    OperationResultCode.FILE_EXISTS,
                                    0)))
            removeOperation()
        } else {
            var result = FileOperations.mkdir(file)
            if (!result && RootTools.isAccessGiven()) {
                result = try {
                    RootUtils.mkDir(file.absolutePath)
                    true
                } catch (e: RootDeniedException) {
                    false
                }

            }
            val resultCode = if (result) OperationResultCode.SUCCESS else OperationResultCode.FAIL
            fileOperationCallback.onOperationResult(operation, getOperationAction(
                    OperationResult(resultCode, 1)))
            removeOperation()
        }
    }

    private fun createFileInRoot(file: File, fileOperationCallback: FileOperationCallback, operation: Operations) {
        val exists: Boolean
        try {
            exists = RootOperations.fileExists(file.absolutePath, false)
        } catch (e: RootDeniedException) {
            fileOperationCallback.onOperationResult(
                    operation,
                    getOperationAction(OperationResult(OperationResultCode.FAIL, 0)))
            removeOperation()
            return
        }
        if (exists) {
            fileOperationCallback.onOperationResult(operation,
                    getOperationAction(
                            OperationResult(
                                    OperationResultCode.FILE_EXISTS,
                                    0)))
            removeOperation()
        } else {
            var result = FileOperations.mkfile(file)
            Log.d("OpHelper", "Result createFileInRoot:$result")
            if (!result && RootTools.isAccessGiven()) {
                result = try {
                    RootUtils.mkFile(file.absolutePath)
                    true
                } catch (e: RootDeniedException) {
                    false
                }
            }
            val resultCode = if (result) OperationResultCode.SUCCESS else OperationResultCode.FAIL
            fileOperationCallback.onOperationResult(operation, getOperationAction(
                    OperationResult(resultCode, 1)))
            removeOperation()
        }
    }

    private fun checkIfFileExists(file: File, fileOperationCallback: FileOperationCallback, operation: Operations): Boolean {
        if (file.exists()) {
            fileOperationCallback.onOperationResult(operation,
                    getOperationAction(
                            OperationResult(
                                    OperationResultCode.FILE_EXISTS,
                                    0)))
            removeOperation()
            return true
        }
        return false
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
            OperationUtils.WriteMode.ROOT -> {
                createFileInRoot(file, fileOperationCallback, operation)
            }
            OperationUtils.WriteMode.EXTERNAL -> {
                if (checkIfFileExists(file, fileOperationCallback, operation)) return
                fileOperationCallback.onOperationResult(
                        operation,
                        getOperationAction(OperationResult(OperationResultCode.SAF, 0)))
            }
            OperationUtils.WriteMode.INTERNAL -> {
                if (checkIfFileExists(file, fileOperationCallback, operation)) return
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
            OperationUtils.WriteMode.INTERNAL, OperationUtils.WriteMode.ROOT -> {
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
        Log.d("OperationHelper", "getOperationAction: data:$operationData")
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
                    val parent = File(path).parent
                    parent?.let {
                        renameFile(operationInfo.operation, path, parent, operationInfo.operationData.arg2,
                                fileOperationCallback)
                    }
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

                Operations.EXTRACT -> {
                    extract(context, operationInfo.operationData.arg1, operationInfo.operationData.arg2, zipOperationCallback)
                }
                Operations.COMPRESS -> {
                    compress(context, operationInfo.operationData.arg1, operationInfo.operationData.filesList, zipOperationCallback)
                }
                Operations.COPY -> {
                    copyFiles(context, operationInfo.operationData.arg1, operationInfo.operationData.filesList, pasteActionInfo, pasteOperationCallback)
                }
                Operations.CUT -> {
                    moveFiles(context, operationInfo.operationData.arg1, operationInfo.operationData.filesList, pasteActionInfo, pasteOperationCallback)
                }
                else -> {}
            }
        }
    }

    private fun deleteWriteableFiles(filesList: ArrayList<String>,
                                     fileOperationCallback: FileOperationCallback,
                                     operation: Operations) {
        val count = DeleteOperation().deleteFiles(filesList)
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
        addOperation(Operations.COPY,
                OperationData.createCopyOperation(destinationDir, files))

        when (val writeMode = OperationUtils.getWriteMode(destinationDir)) {
            OperationUtils.WriteMode.INTERNAL, OperationUtils.WriteMode.ROOT -> {
                if (writeMode == OperationUtils.WriteMode.ROOT && RootUtils.isRooted(context) && !RootUtils.hasRootAccess()) {
                    fileOperationCallback.onOperationResult(Operations.COPY, getOperationAction(
                            OperationResult(OperationResultCode.FAIL, 0)))
                    removeOperation()
                }
                else {
                    copyFiles(context, destinationDir, files, pasteActionInfo, pasteOperationCallback)
                }
            }
            OperationUtils.WriteMode.EXTERNAL -> {
                this.pasteActionInfo = pasteActionInfo
                fileOperationCallback.onOperationResult(
                        Operations.COPY,
                        getOperationAction(OperationResult(OperationResultCode.SAF, 0)))
            }
        }
    }


    private fun copyFiles(context: Context, destinationDir: String, files: ArrayList<FileInfo>,
                          pasteActionInfo: ArrayList<PasteActionInfo>,
                          pasteOperationCallback: PasteOperationCallback?) {
        pasteOperationCallback?.onPasteActionStarted(Operations.COPY, destinationDir, files)

        val intent = Intent(context, CopyService::class.java)
        intent.apply {
            putParcelableArrayListExtra(OperationUtils.KEY_CONFLICT_DATA, pasteActionInfo)
            putParcelableArrayListExtra(OperationUtils.KEY_FILES, files)
            putExtra(OperationUtils.KEY_FILEPATH, destinationDir)
            putExtra(OperationUtils.KEY_IS_MOVE, false)
        }
        if (SdkHelper.isAtleastOreo) {
            context.startForegroundService(intent)
        }
        else {
            context.startService(intent)
        }
    }

    fun moveFiles(context: Context, destinationDir: String, files: ArrayList<FileInfo>,
                  pasteActionInfo: ArrayList<PasteActionInfo>,
                  pasteOperationCallback: PasteOperationCallback,
                  fileOperationCallback: FileOperationCallback) {
        setFileOperationCallback(fileOperationCallback)
        addOperation(Operations.CUT,
                OperationData.createCopyOperation(destinationDir, files))
        when (val writeMode = OperationUtils.getWriteMode(destinationDir)) {
            OperationUtils.WriteMode.INTERNAL, OperationUtils.WriteMode.ROOT -> {
                if (writeMode == OperationUtils.WriteMode.ROOT && RootUtils.isRooted(context) && !RootUtils.hasRootAccess()) {
                    fileOperationCallback.onOperationResult(Operations.CUT, getOperationAction(
                            OperationResult(OperationResultCode.FAIL, 0)))
                    removeOperation()
                }
                else {
                    moveFiles(context, destinationDir, files, pasteActionInfo, pasteOperationCallback)
                }
            }
            OperationUtils.WriteMode.EXTERNAL -> {
                this.pasteActionInfo = pasteActionInfo
                fileOperationCallback.onOperationResult(
                        Operations.CUT,
                        getOperationAction(OperationResult(OperationResultCode.SAF, 0)))
            }
        }
    }

    private fun moveFiles(context: Context, destinationDir: String, files: ArrayList<FileInfo>,
                          pasteActionInfo: ArrayList<PasteActionInfo>,
                          pasteOperationCallback: PasteOperationCallback?) {
        pasteOperationCallback?.onPasteActionStarted(Operations.CUT, destinationDir, files)

        val intent = Intent(context, CopyService::class.java)
        intent.apply {
            putParcelableArrayListExtra(OperationUtils.KEY_CONFLICT_DATA, pasteActionInfo)
            putParcelableArrayListExtra(OperationUtils.KEY_FILES, files)
            putExtra(OperationUtils.KEY_FILEPATH, destinationDir)
            putExtra(OperationUtils.KEY_IS_MOVE, true)
        }
        if (SdkHelper.isAtleastOreo) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun extractFile(context: Context, sourceFilePath : String, destinationDir: String, newName: String,
                    zipOperationCallback: ZipOperationCallback,
                    fileOperationCallback: FileOperationCallback) {
        setFileOperationCallback(fileOperationCallback)
        setZipOperationCallback(zipOperationCallback)

        val newPath = "$destinationDir/$newName"
        addOperation(Operations.EXTRACT, OperationData.createExtractOperation(sourceFilePath, newPath))

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
                extract(context, sourceFilePath, newPath, zipOperationCallback)
            }

            OperationUtils.WriteMode.EXTERNAL -> {
                fileOperationCallback.onOperationResult(
                        Operations.EXTRACT,
                        getOperationAction(OperationResult(OperationResultCode.SAF, 0)))

            }
            OperationUtils.WriteMode.ROOT -> {
                fileOperationCallback.onOperationResult(
                        Operations.EXTRACT,
                        getOperationAction(OperationResult(OperationResultCode.FAIL, 0)))
            }
        }
    }

    private fun extract(context: Context, sourceFilePath: String,
                        newPath: String,
                        zipOperationCallback: ZipOperationCallback?) {
        zipOperationCallback?.onZipOperationStarted(Operations.EXTRACT, sourceFilePath, newPath)
        val intent = Intent(context, ExtractService::class.java)
        intent.apply {
            putExtra(OperationUtils.KEY_FILEPATH, sourceFilePath)
            putExtra(OperationUtils.KEY_FILEPATH2, newPath)
        }
        if (SdkHelper.isAtleastOreo) {
            context.startForegroundService(intent)
        }
        else {
            context.startService(intent)
        }
    }

    fun compressFile(context: Context, destinationDir: String, filesToArchive : ArrayList<FileInfo>,
                    zipOperationCallback: ZipOperationCallback,
                    fileOperationCallback: FileOperationCallback) {
        setFileOperationCallback(fileOperationCallback)
        setZipOperationCallback(zipOperationCallback)
        val newFile = File(destinationDir)
        addOperation(Operations.COMPRESS, OperationData.createArchiveOperation(destinationDir, filesToArchive))

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
                compress(context, destinationDir, filesToArchive, zipOperationCallback)
            }

            OperationUtils.WriteMode.EXTERNAL -> {
                fileOperationCallback.onOperationResult(
                        Operations.COMPRESS,
                        getOperationAction(OperationResult(OperationResultCode.SAF, 0)))

            }
            OperationUtils.WriteMode.ROOT -> {
                fileOperationCallback.onOperationResult(
                        Operations.COMPRESS,
                        getOperationAction(OperationResult(OperationResultCode.FAIL, 0)))
            }
        }
    }

    private fun compress(context: Context, destinationDir: String,
                         filesToArchive: ArrayList<FileInfo>,
                         zipOperationCallback: ZipOperationCallback?) {
        zipOperationCallback?.onZipOperationStarted(Operations.COMPRESS, destinationDir,
                                                   filesToArchive)
        val intent = Intent(context, CreateZipService::class.java)
        intent.apply {
            putExtra(OperationUtils.KEY_FILEPATH, destinationDir)
            putParcelableArrayListExtra(OperationUtils.KEY_FILES, filesToArchive)
        }
        if (SdkHelper.isAtleastOreo) {
            context.startForegroundService(intent)
        }
        else {
            context.startService(intent)
        }
    }


    private fun setFileOperationCallback(fileOperationCallback: FileOperationCallback) {
        this.fileOperationCallback = fileOperationCallback
    }

    private fun setZipOperationCallback(zipOperationCallback: ZipOperationCallback?) {
        this.zipOperationCallback = zipOperationCallback
    }


    fun onOperationCompleted(operation: Operations, count: Int) {
        val resultCode =
                if (count > 0) {
                    OperationResultCode.SUCCESS
                }
                else if (count == 0 && operation == Operations.FAVORITE) {
                    OperationResultCode.FAVORITE_EXISTS
                }
                else {
                    OperationResultCode.FAIL
                }
        fileOperationCallback?.onOperationResult(operation, getOperationAction(
                OperationResult(resultCode, count)))
        removeOperation()
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

    fun deleteFavorite(context: Context, favList: ArrayList<String>, fileOperationCallback: FileOperationCallback) {
        setFileOperationCallback(fileOperationCallback)
        addOperation(Operations.DELETE_FAVORITE, OperationData.createFavoriteOperation(favList))
        val count = FavoriteHelper.removeFavorites(context, favList)
        onOperationCompleted(Operations.DELETE_FAVORITE, count)
    }

    fun setPermissions(operation: Operations, path: String, permissions: String, dir: Boolean, fileOperationCallback: FileOperationCallback) {
        addOperation(operation, OperationData.createPermissionOperation(path))
        val result = RootOperations.setPermissions(path, dir, permissions)
        val resultCode = if (result) {
            OperationResultCode.SUCCESS
        }
        else {
            OperationResultCode.FAIL
        }
        fileOperationCallback.onOperationResult(
                Operations.PERMISSIONS,
                getOperationAction(OperationResult(resultCode, 1)))
        removeOperation()
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