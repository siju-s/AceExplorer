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
import com.siju.acexplorer.helper.MediaScannerHelper
import com.siju.acexplorer.logging.Logger
import com.siju.acexplorer.main.model.helper.FileOperations
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.SdkHelper.isAtleastOreo
import com.siju.acexplorer.storage.model.operations.*
import com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_OP_FAILED
import com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_OP_REFRESH
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_END
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILENAME
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH2
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES_COUNT
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_OPERATION
import com.siju.acexplorer.storage.model.operations.Operations.EXTRACT
import com.siju.acexplorer.storage.modules.zipviewer.ZipUtils
import com.siju.acexplorer.storage.modules.zipviewer.ZipUtils.EXT_TAR
import com.siju.acexplorer.storage.modules.zipviewer.ZipUtils.isTar
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.*
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

private const val NOTIFICATION_ID = 1000
private const val CHANNEL_ID = "operation"
private const val THREAD_NAME = "ExtractService"
private const val BUFFER_SIZE_BYTES = 20480
private const val TAG = "ExtractService"

class ExtractService : Service() {

    private val filesToMediaIndex = arrayListOf<String>() // Doesn't include directories

    private lateinit var context: Context
    private lateinit var serviceHandler: ServiceHandler

    private var notificationManager: NotificationManager? = null
    private var notifBuilder: NotificationCompat.Builder? = null
    private var copiedbytes = 0L
    private var totalbytes = 0L
    private var stopService = false
    private var isCompleted = false

    private var time = System.nanoTime() / 500000000


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
            setContentTitle(resources.getString(R.string.extracting))
            setSmallIcon(R.drawable.ic_doc_compressed)
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
            Logger.log(this.javaClass.simpleName, "Null intent")
            stopService()
            return START_NOT_STICKY
        }
        val action = intent.action
        if (action != null && action == OperationProgress.ACTION_STOP) {
            stopService = true
            stopSelf()
            return START_NOT_STICKY
        }
        getArgs(intent, startId)
        return START_STICKY
    }

    private fun getArgs(intent: Intent, startId: Int) {
        val file = intent.getStringExtra(KEY_FILEPATH)
        val newFile = intent.getStringExtra(KEY_FILEPATH2)

        val msg = serviceHandler.obtainMessage()
        msg.arg1 = startId
        val bundle = Bundle()
        bundle.putString(KEY_FILEPATH, file)
        bundle.putString(KEY_FILEPATH2, newFile)
        msg.data = bundle
        serviceHandler.sendMessage(msg)
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun stopService() {
        stopSelf()
    }

    private inner class ServiceHandler internal constructor(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            Logger.log(TAG, "handleMessage: " + msg.arg1)
            val bundle = msg.data
            val zipFilePath = bundle.getString(KEY_FILEPATH)
            val destPath = bundle.getString(KEY_FILEPATH2)
            destPath?.let {
                start(zipFilePath, destPath)
            }
            stopSelf()
        }

    }

    private fun start(zipFilePath: String?, newFile: String) {
        zipFilePath?.let {
            val zipFile = File(zipFilePath)
            when {
                ZipUtils.isZipViewable(zipFilePath) -> extract(zipFile, newFile)
                isTar(zipFilePath)                  -> extractTar(zipFile, newFile)
            }
        }
    }

    private fun extract(sourceFile: File, destinationPath: String) {
        try {
            val arrayList = ArrayList<ZipEntry>()
            val zipFile = ZipFile(sourceFile)
            val e = zipFile.entries()
            while (e.hasMoreElements()) {
                val entry = e.nextElement() as ZipEntry
                arrayList.add(entry)
            }
            calculateZipFilesSize(arrayList)
            for (entry in arrayList) {
                if (stopService) {
                    publishCompletedResult()
                    break
                }
                unzipEntry(zipFile, entry, destinationPath)
            }
            scanFiles()
            sendRefreshBroadcast(1)
            calculateProgress(sourceFile.name, copiedbytes, totalbytes)
            zipFile.close()
        }
        catch (e: Exception) {
            Log.d(this.javaClass.simpleName, "Error while extracting file $sourceFile", e)
            val intent = Intent(ACTION_OP_FAILED)
            intent.putExtra(KEY_OPERATION, EXTRACT)
            sendBroadcast(intent)
            publishResults(sourceFile.name, 100, totalbytes, copiedbytes)
        }
    }

    private fun scanFiles() {
        MediaScannerHelper.scanFiles(context, filesToMediaIndex.toTypedArray())
    }

    private fun calculateZipFilesSize(arrayList: ArrayList<ZipEntry>) {
        for (entry in arrayList) {
            totalbytes += entry.size
        }
    }

    private fun extractTar(archive: File, destinationPath: String?) {
        try {
            val archiveEntries = ArrayList<TarArchiveEntry>()
            val inputStream: TarArchiveInputStream
            if (archive.name.endsWith(EXT_TAR)) {
                inputStream = TarArchiveInputStream(BufferedInputStream(FileInputStream(archive)))
            }
            else {
                inputStream = TarArchiveInputStream(GZIPInputStream(FileInputStream(archive)))
            }
            publishResults(archive.name, 0, totalbytes, copiedbytes)
            var tarArchiveEntry: TarArchiveEntry? = inputStream.nextTarEntry
            while (tarArchiveEntry != null) {
                if (stopService) {
                    publishCompletedResult()
                    break
                }
                archiveEntries.add(tarArchiveEntry)
                tarArchiveEntry = inputStream.nextTarEntry
            }
            calculateTarFilesSize(archiveEntries)
            for (entry in archiveEntries) {
                unzipTAREntry(inputStream, entry, destinationPath, archive.name)
            }

            inputStream.close()
            sendRefreshBroadcast(1)
            publishResults(archive.name, 100, totalbytes, copiedbytes)

        }
        catch (e: Exception) {

            publishResults(archive.name, 100, totalbytes, copiedbytes)

        }
    }

    private fun sendRefreshBroadcast(count: Int) {
        val intent = Intent(ACTION_OP_REFRESH)
        intent.putExtra(KEY_OPERATION, EXTRACT)
        intent.putExtra(KEY_FILES_COUNT, count)
        sendBroadcast(intent)
    }

    private fun calculateTarFilesSize(archiveEntries: ArrayList<TarArchiveEntry>) {
        for (entry in archiveEntries) {
            totalbytes += entry.size
        }
    }


    private fun publishResults(fileName: String, progress: Int, total: Long, done: Long) {

        notifBuilder?.apply {
            setProgress(100, progress, false)
            setOngoing(true)
            setContentTitle(getString(R.string.extracting))
            setContentText(File(fileName).name + " " + Formatter.formatFileSize(context,
                                                                                done) + "/" + Formatter.formatFileSize(
                    context, total))
        }
        if (progress == 100) {
            publishCompletedResult()
        }

        if (stopService) {
            endNotification()
        }
        Logger.log(this@ExtractService.javaClass.simpleName,
                   "Progress=" + progress + " done=" + done + " total="
                           + total)
        sendBroadcast(progress, done, total, fileName)
    }

    private fun sendBroadcast(progress: Int = 0, completed: Long, total: Long,
                              fileName: String) {
        val intent = Intent(EXTRACT_PROGRESS)
        intent.putExtra(KEY_PROGRESS, progress)
        intent.putExtra(KEY_COMPLETED, completed)
        intent.putExtra(KEY_TOTAL, total)
        intent.putExtra(KEY_FILENAME, fileName)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }


    private fun createDir(dir: File) {
        FileOperations.mkdir(dir)
    }


    private fun calculateProgress(name: String, copiedbytes: Long, totalbytes: Long) {
        val progress = (copiedbytes / totalbytes.toFloat() * 100).toInt()
        publishResults(name, progress, totalbytes, copiedbytes)
    }

    @Throws(Exception::class)
    private fun unzipEntry(zipFile: ZipFile, entry: ZipEntry, outputDir: String?) {
        if (entry.isDirectory) {
            createDir(File(outputDir, entry.name))
            return
        }
        val outputFile = File(outputDir, entry.name)
        Logger.log(TAG, "unzipEntry: $outputFile")
        if (!outputFile.parentFile.exists()) {
            createDir(outputFile.parentFile)
        }

        val inputStream = BufferedInputStream(
                zipFile.getInputStream(entry))
        val outputStream = BufferedOutputStream(
                FileUtils.getOutputStream(outputFile, context))

        try {
            val buf = ByteArray(BUFFER_SIZE_BYTES)
            var len = inputStream.read(buf)
            while (len > 0) {
                if (stopService) {
                    File(outputDir).delete()
                    publishCompletedResult()
                    break
                }

                outputStream.write(buf, 0, len)
                copiedbytes += len

                val time1 = System.nanoTime() / 500000000
                if (time1.toInt() > time.toInt()) {
                    calculateProgress(zipFile.name, copiedbytes, totalbytes)
                    time = System.nanoTime() / 500000000
                }
                len = inputStream.read(buf)
            }
            if (MediaScannerHelper.isMediaScanningRequired(outputFile)) {
                filesToMediaIndex.add(outputFile.absolutePath)
            }
        }
        finally {
            try {
                inputStream.close()
            }
            catch (e: IOException) {
                //no-op
            }

            try {
                outputStream.close()
            }
            catch (e: IOException) {
                //no-op
            }

        }
    }

    @Throws(Exception::class)
    private fun unzipTAREntry(zipFile: TarArchiveInputStream, entry: TarArchiveEntry,
                              outputDir: String?,
                              fileName: String) {
        val name = entry.name
        if (entry.isDirectory) {
            createDir(File(outputDir, name))
            return
        }
        val outputFile = File(outputDir, name)
        if (!outputFile.parentFile.exists()) {
            createDir(outputFile.parentFile)
        }

        val outputStream = BufferedOutputStream(
                FileUtils.getOutputStream(outputFile, baseContext))
        try {
            val buf = ByteArray(BUFFER_SIZE_BYTES)
            var len = zipFile.read(buf)
            while (len > 0) {
                if (stopService) {
                    outputFile.delete()
                    publishCompletedResult()
                }
                outputStream.write(buf, 0, len)
                copiedbytes += len
                val time1 = System.nanoTime() / 500000000
                if (time1.toInt() > time.toInt()) {
                    calculateProgress(fileName, copiedbytes, totalbytes)
                    time = System.nanoTime() / 500000000
                }
                len = zipFile.read(buf)
            }
        }
        finally {
            try {
                outputStream.close()
            }
            catch (e: IOException) {
                //close
            }

        }
    }

    private fun dismissProgressDialog() {
        val intent = Intent(EXTRACT_PROGRESS)
        intent.putExtra(KEY_END, true)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun endNotification() {
        notificationManager?.cancel(NOTIFICATION_ID)
    }


    private fun publishCompletedResult() {
        if (isCompleted) {
            return
        }
        isCompleted = true
        endNotification()
        if (stopService) {
            dismissProgressDialog()
        }
    }
}


