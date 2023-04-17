/*
 * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.siju.acexplorer.storage.model.task

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.helper.MediaScannerHelper
import com.siju.acexplorer.helper.MediaScannerHelper.isMediaScanningRequired
import com.siju.acexplorer.logging.Logger
import com.siju.acexplorer.main.model.data.FileDataFetcher
import com.siju.acexplorer.main.model.helper.FileOperations
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.RootHelper
import com.siju.acexplorer.main.model.helper.SdkHelper.isAtleastOreo
import com.siju.acexplorer.main.model.root.RootDeniedException
import com.siju.acexplorer.main.model.root.RootUtils
import com.siju.acexplorer.storage.model.PasteActionInfo
import com.siju.acexplorer.storage.model.operations.*
import com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_OP_REFRESH
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_CONFLICT_DATA
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_END
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES_COUNT
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_IS_MOVE
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_OPERATION
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_RESULT
import com.siju.acexplorer.storage.model.operations.Operations.COPY
import java.io.*

private const val NOTIFICATION_ID = 1000
private const val SEPARATOR = "/"
private const val CHANNEL_ID = "operation"
private const val THREAD_NAME = "CopyService"
private const val TAG = "CopyService"

class CopyService : Service() {
    private val filesToMediaIndex = ArrayList<String>()
    private val failedFiles = ArrayList<FileInfo>()

    private lateinit var context: Context
    private lateinit var serviceHandler: ServiceHandler
    private lateinit var files: ArrayList<FileInfo>
    private var pasteActionInfo: List<PasteActionInfo>? = null

    private var notificationManager: NotificationManager? = null
    private var notifBuilder: NotificationCompat.Builder? = null

    private var totalBytes = 0L
    private var copiedBytes = 0L
    private var count = 0
    private var calculatingTotalSize = false
    private var stopService = false
    private var isCompleted = false
    private var filesCopied = 0
    private var time = System.nanoTime() / 500000000
    private var isMove = false

    override fun onCreate() {
        super.onCreate()
        Logger.log("CopyService", "onCreate: ")
        context = applicationContext
        createNotification()
        startThread()
    }

    private fun createNotification() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.cancelAll()

        createChannelId()
        val notification = buildNotificationBuilder()
        startForeground(NOTIFICATION_ID, notification)
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotificationBuilder(): Notification? {
        val pendingCancelIntent = createCancelIntent()
        notifBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
        notifBuilder?.apply {
            val icon = if (isMove) {
                R.drawable.ic_cut_white
            }
            else {
                R.drawable.ic_copy_white
            }
            setContentTitle(getTitle(isMove))
            setSmallIcon(icon)
            setOnlyAlertOnce(true)
            setDefaults(0)
            addAction(NotificationCompat.Action(R.drawable.ic_cancel,
                    getString(com.siju.acexplorer.common.R.string.dialog_cancel),
                    pendingCancelIntent))
        }

        return notifBuilder?.build()
    }

    private fun createCancelIntent(): PendingIntent? {
        val cancelIntent = Intent(context, CopyService::class.java)
        cancelIntent.action = OperationProgress.ACTION_STOP
        return PendingIntent.getService(context, NOTIFICATION_ID, cancelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannelId() {
        if (isAtleastOreo) {
            val name = getString(R.string.operation)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun startThread() {
        val thread = HandlerThread(THREAD_NAME,
                Process.THREAD_PRIORITY_BACKGROUND)
        thread.start()
        val serviceLooper = thread.looper
        serviceHandler = ServiceHandler(serviceLooper)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.log(TAG, "onStartCommand: " + intent + "starId:" + startId)
        if (intent == null) {
            Logger.log(this.javaClass.simpleName, "Null intent")
            stopService()
            return START_NOT_STICKY
        }
        val action = intent.action
        if (action == OperationProgress.ACTION_STOP) {
            stopService = true
            stopSelf()
            return START_NOT_STICKY
        }
        if (hasArgs(intent, startId)) return START_NOT_STICKY
        return START_STICKY
    }

    private fun hasArgs(intent: Intent, startId: Int): Boolean {
        val list : java.util.ArrayList<FileInfo> = intent.getParcelableArrayListExtra(KEY_FILES)
                ?: return false
        files = list
        if (files.isNullOrEmpty()) {
            stopService()
            return true
        }
        pasteActionInfo = intent.getParcelableArrayListExtra(KEY_CONFLICT_DATA)

        val currentDir = intent.getStringExtra(KEY_FILEPATH)
        isMove = intent.getBooleanExtra(KEY_IS_MOVE, false)

        val msg = serviceHandler.obtainMessage()
        msg.arg1 = startId
        msg.obj = currentDir
        serviceHandler.sendMessage(msg)
        return false
    }

    private fun stopService() {
        stopSelf()
    }

    private inner class ServiceHandler (looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            Logger.log(TAG, "handleMessage: " + msg.arg1)
            val currentDir = msg.obj as String
            checkWriteMode(currentDir)
            stopSelf()
        }
    }


    private fun checkWriteMode(currentDir: String) {
        when (OperationUtils.getWriteMode(currentDir)) {
            OperationUtils.WriteMode.INTERNAL -> {
                copyOnInternalStorage(currentDir)
                publishCompletionResult()
            }

            OperationUtils.WriteMode.ROOT -> {
                onRoot(currentDir)
                publishCompletionResult()
            }
            else -> {
            }
        }
    }

    private fun onRoot(currentDir: String) {
        totalBytes = files.size.toLong()
        for (i in files.indices) {
            val path = files[i].filePath
            val name = files[i].fileName
            if (path != null && name != null) {
                copyRoot(path, name, currentDir)
            }
            val newFileInfo = files[i]
            newFileInfo.filePath = currentDir + SEPARATOR + newFileInfo.fileName
            if (isFileCopied(files[i], newFileInfo)) {
                failedFiles.add(files[i])
            }
        }
    }

    private fun copyOnInternalStorage(destinationDir: String) {
        getTotalBytes(files)
        for (index in files.indices) {
            val sourceFile = files[index]
            if (stopService) {
                publishCompletionResult()
                break
            }
            try {
                if (isNonReadable(index)) {
                    val filePath = files[index].filePath
                    val fileName = files[index].fileName
                    if (filePath != null && fileName != null) {
                        copyRoot(filePath, fileName, destinationDir)
                    }
                    continue
                }

                val destFile = createDestFile(sourceFile)
                val action = getAction(sourceFile)
                val path = getNewPathForAction(destinationDir, index, action)
                destFile.filePath = path
                Logger.log("CopyService", "Execute-Dest file path=" + destFile
                        .filePath)
                startCopy(sourceFile, destFile)
            } catch (e: Exception) {
                e.printStackTrace()
                populateFailedFiles(index)
                break
            }
        }
    }

    private fun createDestFile(
            sourceFile: FileInfo): FileInfo {
        return FileInfo(sourceFile.category, sourceFile.fileName,
                sourceFile.filePath, sourceFile.date, sourceFile
                .size, sourceFile.isDirectory,
                sourceFile.extension, sourceFile.permissions, false)
    }

    private fun getNewPathForAction(destDir: String, index: Int, action: Int): String? {
        val fileName = files[index].fileName ?: return null

        var path = destDir + SEPARATOR + fileName

        if (action == FileUtils.ACTION_KEEP) {
            val isDirectory = File(path).isDirectory
            path = if (isDirectory) {
                getNewName(destDir, fileName, true)
            } else {
                val fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf("."))
                val extension = files[index].extension
                if (extension == null) {
                    return null
                } else {
                    destDir + SEPARATOR + getNewName(destDir, fileNameWithoutExt,
                            false, extension)
                }
            }
        }
        return path
    }

    private fun getAction(sourceFile: FileInfo): Int {
        pasteActionInfo ?: return FileUtils.ACTION_NONE
        pasteActionInfo?.let {
            for (pasteAction in it) {
                if (pasteAction.filePath == sourceFile.filePath) {
                    return pasteAction.action
                }
            }
        }
        return FileUtils.ACTION_NONE
    }

    private fun populateFailedFiles(index: Int) {
        failedFiles.add(files[index])
        for (j in index + 1 until files.size) {
            failedFiles.add(files[j])
        }
    }

    private fun isNonReadable(i: Int): Boolean {
        val filePath = files[i].filePath
        filePath ?: return true
        return !File(filePath).canRead()
    }

    private fun getNewName(destDir: String, fileName: String, isDirectory: Boolean,
                           extension: String = ""): String {
        val file = File(destDir)
        val list = file.list() ?: return ""
        val files = list.toList()
        val suffix = 1
        val newFileName = "$fileName ($suffix)"
        return if (isDirectory) {
            getNewDirectoryName(files, suffix, newFileName, fileName)
        } else {
            getNewFileName(files, suffix, newFileName, fileName, extension)
        }
    }

    private fun getNewFileName(files: List<String>, suffix: Int, fileName: String,
                               originalFileName: String, extension: String): String {
        var suffix1 = suffix
        return if (files.contains("$fileName.$extension")) {
            suffix1++
            val newFileName = "$originalFileName ($suffix1)"
            getNewFileName(files, suffix1, newFileName, originalFileName, extension)
        } else {
            "$fileName.$extension"
        }
    }

    private fun getNewDirectoryName(files: List<String>, suffix: Int, fileName: String,
                                    originalFileName: String): String {
        var suffix1 = suffix
        return if (files.contains(fileName)) {
            suffix1++
            val newFileName = "$originalFileName ($suffix1)"
            getNewDirectoryName(files, suffix1, newFileName, originalFileName)
        } else {
            fileName
        }
    }


    private fun getTotalBytes(files: List<FileInfo>) {
        calculatingTotalSize = true
        var totalBytes = 0L
        for (i in files.indices) {
            val file = files[i]
            val filePath = file.filePath
            if (filePath == null) {
                totalBytes += 0
                continue
            }
            totalBytes += if (file.isDirectory) {
                FileUtils.getFolderSize(File(filePath))
            } else {
                File(filePath).length()
            }
        }
        this.totalBytes = totalBytes
        calculatingTotalSize = false
    }


    private fun dismissProgressDialog() {
        val intent = Intent(getOperationProgress())
        intent.putExtra(KEY_END, true)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun getOperationProgress(): String {
        return if (isMove) {
            MOVE_PROGRESS
        }
        else {
            COPY_PROGRESS
        }
    }

    private fun publishCompletionResult() {
        if (isCompleted) {
            return
        }
        Log.d(TAG, "publishCompletionResult: filesCopied:$filesCopied, failedfiles:${failedFiles.size}")
        isCompleted = true
        cancelNotification()
        if (stopService) {
            dismissProgressDialog()
        }
        val operation = if (isMove) {
            Operations.CUT
        }
        else {
            COPY
        }
        val intent = Intent(ACTION_OP_REFRESH)
        intent.putExtra(KEY_FILES_COUNT, filesCopied)
        intent.putExtra(KEY_RESULT, failedFiles.isEmpty())
        intent.putExtra(KEY_OPERATION, operation)
        MediaScannerHelper.scanFiles(AceApplication.appContext, filesToMediaIndex.toTypedArray())
        sendBroadcast(intent)
    }

    private fun copyRoot(path: String, name: String, destinationPath: String) {
        val targetPath = if (destinationPath == File.separator) {
            destinationPath + name
        } else {
            destinationPath + File.separator + name
        }
        try {
            RootUtils.mountRW(destinationPath)
            RootUtils.copy(path, targetPath)
            if (isMediaScanningRequired(File(targetPath))) {
                filesToMediaIndex.add(targetPath)
            }
            copiedBytes++
            calculateProgress(name)
            filesCopied++
        } catch (e: RootDeniedException) {
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    private fun startCopy(sourceFile: FileInfo, targetFile: FileInfo) {
        if (sourceFile.isDirectory) {
            copyDirectory(sourceFile, targetFile)
        } else {
            copyFiles(sourceFile, targetFile)
        }
    }

    @Throws(IOException::class)
    private fun copyDirectory(sourceFile: FileInfo, targetFile: FileInfo) {
        val targetPath = targetFile.filePath
        if (targetPath == null) {
            failedFiles.add(sourceFile)
            return
        }
        val destinationDir = File(targetPath)
        var isExists = true
        if (!destinationDir.exists()) {
            isExists = FileOperations.mkdir(destinationDir)
        }
        if (!isExists) {
            failedFiles.add(sourceFile)
            return
        }

        val path = sourceFile.filePath
        val fileList = if (path == null) {
            arrayListOf()
        } else {
            FileDataFetcher.getFilesList(path, root = false, showHidden = true)
        }

        if (fileList.isEmpty()) {
            sendBroadcast(100, 0, totalBytes, 1)
            filesCopied++
            if (isMove) {
                deleteFilePath(path)
            }
            return
        }

        for (file in fileList) {
            if (stopService) {
                publishCompletionResult()
                break
            }

            val destFile = createDestFile(sourceFile)
            destFile.filePath = targetPath + SEPARATOR + file.fileName
            startCopy(file, destFile)
        }
        if (isMove) {
            val sourceFileCount = fileList.size
            val destDirList = destinationDir.list()
            destDirList?.let {
                if (sourceFileCount == destDirList.size && failedFiles.isEmpty()) {
                    deleteFilePath(sourceFile.filePath)
                }
            }
        }
    }

    private fun deleteFilePath(path: String?) {
        path?.let {
            DeleteOperation().delete(File(path))
        }
    }

    private fun getTitle(isMove : Boolean) : String {
        return if (isMove) {
            resources.getString(R.string.moving)
        }
        else {
            resources.getString(R.string.copying)
        }
    }

    private fun sendBroadcast(progress: Int = 0, completed: Long, total: Long, count: Int = 0,
                              totalProgress: Int = 0) {
        val intent = Intent(getOperationProgress())
        intent.putExtra(KEY_PROGRESS, progress)
        intent.putExtra(KEY_COMPLETED, completed)
        intent.putExtra(KEY_TOTAL, total)
        intent.putExtra(KEY_COUNT, count)
        intent.putExtra(KEY_TOTAL_PROGRESS, totalProgress)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }


    @Throws(IOException::class)
    private fun copyFiles(sourceFile: FileInfo, targetFile: FileInfo) {
        val sourcePath = sourceFile.filePath
        val targetPath = targetFile.filePath

        if (sourcePath == null || targetPath == null) {
            failedFiles.add(sourceFile)
            return
        }

        val size = File(sourcePath).length()
        Logger.log("Copy", "target file=$targetPath")
        var out: BufferedOutputStream? = null
        try {
            val target = File(targetPath)
            val outputStream = FileUtils.getOutputStream(target, context)
            if (outputStream != null) {
                out = BufferedOutputStream(outputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val inputStream = BufferedInputStream(FileInputStream(sourcePath))
        if (out == null) {
            failedFiles.add(sourceFile)
            return
        }
        copy(inputStream, out, size, sourceFile, sourcePath, targetPath)
    }


    @Throws(IOException::class)
    private fun copy(inputStream: BufferedInputStream, out: BufferedOutputStream, size: Long,
                     sourceFile: FileInfo, sourceFilePath : String,
                     targetPath: String) {
        val name = sourceFile.fileName
        var fileBytes = 0L
        val buffer = 2048 //2 KB
        val data = ByteArray(2048)
        var length = inputStream.read(data, 0, buffer)
        //copy the file content inputStream bytes
        while (length != -1) {
            if (stopService) {
                failedFiles.add(sourceFile)
                val targetFile = File(targetPath)
                targetFile.delete()
                publishCompletionResult()
                break
            }
            out.write(data, 0, length)
            copiedBytes += length.toLong()
            fileBytes += length.toLong()
            val time1 = System.nanoTime() / 500000000
            if (time1.toInt() > time.toInt()) {
                name?.let { calculateProgress(it) }
                time = System.nanoTime() / 500000000
            }
            length = inputStream.read(data, 0, buffer)
        }

        if (fileBytes == size) {
            count++
            val targetFile = File(targetPath)
            filesCopied++

            if (isMove) {
                DeleteOperation().delete(File(sourceFilePath))
            }
            if (isMediaScanningRequired(targetFile)) {
                filesToMediaIndex.add(targetPath)
            }
            Logger.log("CopyService", "Completed $name KEY_COUNT=$count")

            val totalProgress = (copiedBytes / totalBytes.toFloat() * 100).toInt()
            sendBroadcast(100, copiedBytes, totalBytes, count, totalProgress)
        }
        inputStream.close()
        out.close()
    }

    private fun calculateProgress(name: String) {
        var p1 = (copiedBytes / totalBytes.toFloat() * 100).toInt()
        Logger.log("CopyService", "Copied=$copiedBytes Totalbytes=$totalBytes")
        if (calculatingTotalSize) {
            p1 = 0
        }
        publishResults(name, p1, totalBytes, copiedBytes)
    }

    private fun publishResults(fileName: String, totalProgress: Int, total: Long, done: Long) {
        Logger.log("CopyService", "Total bytes=" + totalBytes + "Copied=" + copiedBytes +
                "Progress = " + totalProgress)
        val title = getTitle(isMove)
        notifBuilder?.apply {
            setProgress(100, totalProgress, false)
            setOngoing(true)
            setContentTitle(title)
            setContentText(File(fileName).name + " " + FileUtils.formatSize(context,
                    done) + SEPARATOR + FileUtils
                    .formatSize(context, total))
        }
        notificationManager?.notify(NOTIFICATION_ID, notifBuilder?.build())
        sendBroadcast(completed = copiedBytes, total = totalBytes, totalProgress = totalProgress)

        if (totalProgress == 100 || total == 0L || totalBytes == copiedBytes) {
            cancelNotification()
        }
    }

    private fun cancelNotification() {
        notificationManager?.cancel(NOTIFICATION_ID)
    }

    //check if copy is successful
    private fun isFileCopied(oldFileInfo: FileInfo, newFileInfo: FileInfo): Boolean {
        return if (oldFileInfo.isDirectory) {
            isRootDirectoryCopied(newFileInfo, oldFileInfo)
        } else {
            isRootFileCopied(oldFileInfo)
        }
    }

    private fun isRootDirectoryCopied(newFileInfo: FileInfo,
                                      oldFileInfo: FileInfo): Boolean {
        if (RootHelper.fileExists(newFileInfo.filePath)) {
            return false
        }
        val filesList = FileDataFetcher.getFilesList(oldFileInfo.filePath, root = true,
                showHidden = true)
        if (filesList.isNotEmpty()) {
            var copied = true
            for (file in filesList) {
                file.filePath = newFileInfo.filePath + SEPARATOR + file.fileName
                if (!isFileCopied(file, file)) {
                    copied = false
                }
            }
            return copied
        }
        return RootHelper.fileExists(newFileInfo.filePath)
    }

    private fun isRootFileCopied(oldFileInfo: FileInfo): Boolean {
        val filePath = oldFileInfo.filePath ?: return true
        val parent = File(filePath).parent
        val fileList = FileDataFetcher.getFilesList(parent, root = true, showHidden = true)
        var i = -1
        var index = -1
        for (file in fileList) {
            i++
            if (file.filePath == filePath) {
                index = i
                break
            }
        }
        return index == -1
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
