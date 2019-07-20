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
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.logging.Logger
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.FileUtils.getCategoryFromExtension
import com.siju.acexplorer.main.model.helper.MediaStoreHelper.scanFile
import com.siju.acexplorer.main.model.helper.SdkHelper.isAtleastOreo
import com.siju.acexplorer.main.model.root.RootDeniedException
import com.siju.acexplorer.main.model.root.RootUtils
import com.siju.acexplorer.storage.model.PasteActionInfo
import com.siju.acexplorer.storage.model.operations.*
import com.siju.acexplorer.storage.model.operations.OperationUtils.*
import com.siju.acexplorer.storage.model.operations.Operations.CUT
import java.io.File
import java.util.*

private const val TAG = "MoveService"
private const val NOTIFICATION_ID = 1000
private const val SEPARATOR = "/"
private const val CHANNEL_ID = "operation"
private const val THREAD_NAME = "MoveService"

class MoveService : Service() {

    private lateinit var context: Context
    private lateinit var serviceHandler: ServiceHandler
    private lateinit var files: ArrayList<FileInfo>
    private lateinit var pasteActionInfo: List<PasteActionInfo>

    private var notificationManager: NotificationManager? = null
    private var notifBuilder: NotificationCompat.Builder? = null

    private var filesMovedList = ArrayList<String>()
    private var oldFileList: ArrayList<String>? = null
    private var categories: ArrayList<Int>? = null

    private var stopService = false
    private var isCompleted = false

    override fun onCreate() {
        super.onCreate()
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
            setContentTitle(resources.getString(R.string.moving))
            setSmallIcon(R.drawable.ic_cut_white)
            setOnlyAlertOnce(true)
            setDefaults(0)
            addAction(NotificationCompat.Action(R.drawable.ic_cancel,
                                                getString(R.string.dialog_cancel),
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
        if (isAtleastOreo()) {
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
        if (getArgs(intent, startId)) return START_NOT_STICKY
        return START_STICKY
    }

    private fun getArgs(intent: Intent, startId: Int): Boolean {
        files = intent.getParcelableArrayListExtra(KEY_FILES)
        if (files.isNullOrEmpty()) {
            stopService()
            return true
        }
        pasteActionInfo = intent.getParcelableArrayListExtra(KEY_CONFLICT_DATA)

        val currentDir = intent.getStringExtra(KEY_FILEPATH)

        val msg = serviceHandler.obtainMessage()
        msg.arg1 = startId
        msg.obj = currentDir
        serviceHandler.sendMessage(msg)
        return false
    }

    private fun stopService() {
        stopSelf()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    private inner class ServiceHandler internal constructor(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            Logger.log(TAG, "handleMessage: " + msg.arg1)
            val currentDir = msg.obj as String
            filesMovedList = ArrayList()
            oldFileList = ArrayList()
            categories = ArrayList()
            checkWriteMode(currentDir)
            stopSelf(msg.arg1)
        }
    }

    private fun checkWriteMode(destinationDir: String) {

        when (checkFolder(destinationDir)) {
            WriteMode.INTERNAL -> {
                moveOnInternalStorage(destinationDir)
                publishCompletedResult()
            }

            WriteMode.ROOT     -> {
                moveOnRoot(destinationDir)
                publishCompletedResult()
            }
        }
    }

    private fun moveOnInternalStorage(destDir: String) {
        for (index in files.indices) {
            val sourceFile = files[index]
            if (stopService) {
                publishCompletedResult()
                break
            }
            try {
                if (isNonReadable(sourceFile.filePath)) {
                    moveRoot(sourceFile.filePath, sourceFile.fileName, destDir)
                    continue
                }
                val fileName = sourceFile.fileName
                val action = getAction(sourceFile)
                val path = getNewPathForAction(destDir, index, action)
                moveFiles(sourceFile, fileName, path)
            }
            catch (e: Exception) {
                e.printStackTrace()
                break
            }
        }
    }

    private fun moveOnRoot(destDir: String) {
        for (index in files.indices) {
            val fileInfo = files[index]
            moveRoot(fileInfo.filePath, fileInfo.fileName, destDir)
        }
    }

    private fun isNonReadable(path: String) = !File(path).canRead()

    private fun getAction(sourceFile: FileInfo): Int {
        for (pasteAction in pasteActionInfo) {
            if (pasteAction.filePath == sourceFile.filePath) {
                return pasteAction.action
            }
        }
        return FileUtils.ACTION_NONE
    }

    private fun getNewPathForAction(destDir: String, index: Int, action: Int): String {
        val fileName = files[index].fileName
        var path = destDir + SEPARATOR + fileName

        if (action == FileUtils.ACTION_KEEP) {
            val isDirectory = File(path).isDirectory
            path = if (isDirectory) {
                getNewName(destDir, fileName, true, files[index].extension)
            }
            else {
                val fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf("."))
                destDir + SEPARATOR + getNewName(destDir, fileNameWithoutExt,
                                                 false, files[index].extension)
            }
        }
        return path
    }

    private fun getNewName(destDir: String, fileName: String, isDirectory: Boolean,
                           extension: String): String {
        val file = File(destDir)
        val files = listOf(*file.list())
        val suffix = 1
        val newFileName = "$fileName ($suffix)"
        return if (isDirectory) {
            getNewDirectoryName(files, suffix, newFileName, fileName)
        }
        else {
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
        }
        else {
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
        }
        else {
            fileName
        }
    }

    private fun moveFiles(sourceFileInfo: FileInfo, fileName: String, destinationPath: String) {

        val sourcePath = sourceFileInfo.filePath
        val newFile = File(destinationPath)
        val oldFile = File(sourcePath)
        if (oldFile.renameTo(newFile)) {
            val newPath = newFile.absolutePath
            oldFileList?.add(sourcePath)
            filesMovedList.add(newPath)
            scanFile(context, newPath)
            //            MediaStoreHelper.removeMedia(context, sourcePath, sourceFileInfo.getCategory().getValue());
            categories?.add(sourceFileInfo.category.value)
        }
        publishResults(fileName, files.size.toLong(), filesMovedList.size.toLong())
    }

    private fun moveRoot(path: String, name: String, destinationPath: String) {
        val targetPath = when (destinationPath) {
            File.separator -> destinationPath + name
            else           -> destinationPath + File.separator + name
        }
        try {
            RootUtils.mountRW(destinationPath)
            RootUtils.move(path, targetPath)
            RootUtils.mountRO(destinationPath)
            oldFileList?.add(path)
            filesMovedList.add(targetPath)
            val extension = name.substring(name.lastIndexOf(".") + 1)
            val category = getCategoryFromExtension(extension).value
            categories?.add(category)
            publishResults(name, files.size.toLong(), filesMovedList.size.toLong())
        }
        catch (e: RootDeniedException) {
            e.printStackTrace()
        }

    }

    private fun publishResults(fileName: String, total: Long, completedBytes: Long) {
        val progress = (completedBytes.toFloat() / total * 100).toInt()
        val title = R.string.moving
        notifBuilder?.apply {
            setProgress(100, progress, false)
            setOngoing(true)
            setContentTitle(getString(title))
            setContentText(File(fileName).name + " " + completedBytes + SEPARATOR + total)
        }
        notificationManager?.notify(NOTIFICATION_ID, notifBuilder?.build())

        sendBroadcast(progress, total, completedBytes)
    }

    private fun sendBroadcast(progress: Int, totalBytes: Long, completedBytes: Long) {
        val intent = Intent(MOVE_PROGRESS)
        intent.putExtra(KEY_COMPLETED, completedBytes)
        intent.putExtra(KEY_TOTAL, totalBytes)
        intent.putExtra(KEY_TOTAL_PROGRESS, progress)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun dismissProgressDialog() {
        val intent = Intent(MOVE_PROGRESS)
        intent.putExtra(KEY_END, true)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun publishCompletedResult() {
        if (isCompleted) {
            return
        }
        isCompleted = true
        Logger.log(TAG, "publishCompletedResult" + filesMovedList.size)
        val isMoveSuccess = filesMovedList.size == files.size
        endNotification()
        if (stopService) {
            dismissProgressDialog()
        }
        if (!isMoveSuccess) {
            sendBroadcast(100, files.size.toLong(), 0)
        }

        val intent = Intent(ACTION_OP_REFRESH)
        intent.putExtra(KEY_FILES_COUNT, filesMovedList.size)
        intent.putExtra(KEY_OPERATION, CUT)
        intent.putStringArrayListExtra(KEY_OLD_FILES, oldFileList)
        sendBroadcast(intent)
    }

    private fun endNotification() {
        notificationManager?.cancel(NOTIFICATION_ID)
    }

}
