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
import android.text.format.Formatter
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.logging.Logger
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.SdkHelper.isAtleastOreo
import com.siju.acexplorer.storage.model.operations.*
import com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_OP_REFRESH
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_END
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES_COUNT
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_OPERATION
import com.siju.acexplorer.storage.model.operations.Operations.COMPRESS
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private const val NOTIFICATION_ID = 1000
private const val CHANNEL_ID = "operation"
private const val THREAD_NAME = "CreateZipService"
private const val BUFFER_SIZE_BYTES = 2048
private const val TAG = "CreateZipService"

class CreateZipService : Service() {

    private lateinit var context: Context
    private lateinit var serviceHandler: ServiceHandler
    private lateinit var filesToArchive: List<FileInfo>

    private var notificationManager: NotificationManager? = null
    private var notifBuilder: NotificationCompat.Builder? = null

    private var zipOutputStream: ZipOutputStream? = null
    private var filePath: String? = null
    private var destPath: String? = null
    private var stopService = false
    private var isCompleted = false
    private var lastProgress = 0
    private var size = 0L
    private var totalBytes = 0L


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
            setContentTitle(resources.getString(R.string.zip_progress_title))
            setSmallIcon(R.drawable.ic_archive_white)
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
        return PendingIntent.getService(context,
                                        NOTIFICATION_ID,
                                        cancelIntent,
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
        Logger.log(TAG, "onStartCommand: " + intent + "startId:" + startId)
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        val action = intent.action
        if (action == OperationProgress.ACTION_STOP) {
            stopService = true
            stopSelf()
            return START_NOT_STICKY
        }

        destPath = intent.getStringExtra(KEY_FILEPATH)
        filesToArchive = intent.getParcelableArrayListExtra(KEY_FILES)
        createZipFile()
        val msg = serviceHandler.obtainMessage()
        msg.arg1 = startId
        serviceHandler.sendMessage(msg)
        return START_STICKY
    }

    private fun createZipFile() {
        val zipName = File(destPath)
        if (!zipName.exists()) {
            try {
                zipName.createNewFile()
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private inner class ServiceHandler internal constructor(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            Logger.log(this@CreateZipService.javaClass.simpleName, "handleMessage: " + msg.arg1)
            destPath?.let {
                run(convertToFileList(filesToArchive), it)
            }
            stopSelf()
        }
    }

    private fun convertToFileList(fileInfoList: List<FileInfo>): ArrayList<File> {
        val fileList = ArrayList<File>()
        for (i in fileInfoList.indices) {
            fileList.add(File(fileInfoList[i].filePath))
        }
        return fileList
    }

    private fun run(filesToZip: ArrayList<File>, destPath: String) {
        calculateTotalSize(filesToZip)
        val out: OutputStream?
        filePath = destPath
        val zipDirectory = File(destPath)

        try {
            out = FileUtils.getOutputStream(zipDirectory, context)
            zipOutputStream = ZipOutputStream(BufferedOutputStream(out))
        }
        catch (ignored: Exception) {
        }

        for (file in filesToZip) {
            if (stopService) {
                publishCompletedResult()
                break
            }
            try {
                compress(file, "")
            }
            catch (ignored: Exception) {
            }
        }
        try {
            zipOutputStream?.flush()
            zipOutputStream?.close()

        }
        catch (ignored: Exception) {
        }

    }

    private fun calculateTotalSize(zipFiles: ArrayList<File>) {
        for (file in zipFiles) {
            totalBytes += if (file.isDirectory) {
                FileUtils.getFolderSize(file)
            }
            else {
                file.length()
            }
        }
    }


    @Throws(IOException::class)
    private fun compress(sourceFile: File, destPath: String) {
        if (sourceFile.isFile) {
            compressFile(sourceFile, destPath)
        }
        else {
            compressDirectory(sourceFile, destPath)
        }
    }

    @Throws(IOException::class)
    private fun compressFile(file: File, path: String) {
        if (file.length() == 0L) {
            calculateProgress(filePath, size, totalBytes)
        }
        else {
            val inputStream = BufferedInputStream(FileInputStream(file))
            zipOutputStream?.putNextEntry(ZipEntry(path + "/" + file.name))
            zipFile(inputStream)
            inputStream.close()
        }
    }

    @Throws(IOException::class)
    private fun compressDirectory(file: File, destDir: String) {
        val files = file.list()
        if (files == null) {
            return
        }
        else if (files.isEmpty()) {
            compressEmptyFolder(destDir + File.separator + file.name + "/")
        }
        for (fileName in files) {
            val f = File(file.absolutePath + File.separator + fileName)
            compress(f, destDir + File.separator + file.name)
        }
    }

    @Throws(IOException::class)
    private fun zipFile(inputStream: BufferedInputStream) {
        val buf = ByteArray(BUFFER_SIZE_BYTES)
        var len = inputStream.read(buf)
        while (len > 0) {
            if (stopService) {
                publishCompletedResult()
                break
            }
            zipOutputStream?.write(buf, 0, len)
            size += len.toLong()
            val progress = (size / totalBytes.toFloat() * 100).toInt()
            if (progress != lastProgress || lastProgress == 0) {
                calculateProgress(filePath, size, totalBytes)
            }
            lastProgress = progress
            len = inputStream.read(buf)
        }
    }


    @Throws(IOException::class)
    private fun compressEmptyFolder(path: String) {
        zipOutputStream?.putNextEntry(ZipEntry(path))
        calculateProgress(filePath, size, totalBytes)
    }

    private fun publishResults(filePath: String?, progress: Int, done: Long, total: Long) {

        notifBuilder?.apply {
            setProgress(100, progress, false)
            setOngoing(true)
            setContentTitle(getString(R.string.zip_progress_title))
            setContentText(File(filePath).name + " " + Formatter.formatFileSize(context,
                                                                                done) + "/" + Formatter.formatFileSize(
                    context, total))
        }
        notificationManager?.notify(NOTIFICATION_ID, notifBuilder?.build())

        Log.e("CreateZip", "publishResults: progress:$progress total:$total")
        if (progress == 100 || total == 0L) {
            publishCompletedResult()
        }
        sendBroadcast(filePath, progress, done, total)
    }

    private fun sendBroadcast(filePath: String?, progress: Int, done: Long, total: Long) {
        val intent = Intent(ZIP_PROGRESS)
        if (progress == 100 || total == 0L) {
            intent.putExtra(KEY_PROGRESS, 100)
        }
        else {
            intent.putExtra(KEY_PROGRESS, progress)
        }
        intent.putExtra(KEY_COMPLETED, done)
        intent.putExtra(KEY_TOTAL, total)
        intent.putExtra(KEY_FILEPATH, filePath)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun dismissProgressDialog() {
        val intent = Intent(ZIP_PROGRESS)
        intent.putExtra(KEY_END, true)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun publishCompletedResult() {
        if (isCompleted) {
            return
        }
        isCompleted = true
        if (stopService) {
            dismissProgressDialog()
        }
        val intent = Intent(ACTION_OP_REFRESH)
        intent.putExtra(KEY_OPERATION, COMPRESS)
        intent.putExtra(KEY_FILES_COUNT, 1)
        sendBroadcast(intent)
        notificationManager?.cancel(NOTIFICATION_ID)
    }

    private fun calculateProgress(name: String?, copiedBytes: Long, totalBytes: Long) {
        val progress = (copiedBytes / totalBytes.toFloat() * 100).toInt()
        lastProgress = copiedBytes.toInt()
        publishResults(name, progress, copiedBytes, totalBytes)
    }
}

