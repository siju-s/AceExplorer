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

package com.siju.acexplorer.storage.model.operations

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.logging.Logger
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_END
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_RESULT
import com.siju.acexplorer.storage.model.task.CopyService
import com.siju.acexplorer.storage.model.task.CreateZipService
import com.siju.acexplorer.storage.model.task.ExtractService
import java.util.*
import kotlin.collections.ArrayList
import com.siju.acexplorer.common.R as RC
private const val TAG = "OperationProgress"


class OperationProgress {
    private var progressBarPaste: ProgressBar? = null
    private var progressDialog: AlertDialog? = null
    private var textFileName: TextView? = null
    private var textFileFromPath: TextView? = null
    private var textFileCount: TextView? = null
    private var textProgress: TextView? = null
    private var context: Context? = null

    private var copiedFileInfo = ArrayList<FileInfo>()
    private var copiedFilesSize = 0

    private val operationProgressReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent == null || intent.action == null) {
                return
            }
            handleMessage(intent)
        }
    }

    @SuppressLint("InflateParams")
    fun showPasteDialog(context: Context, destinationDir: String, files: ArrayList<FileInfo>,
                        operations: Operations) {

        this.context = context
        registerReceiver(context)
        val title = if (operations == Operations.CUT) {
            context.getString(R.string.moving)
        }
        else {
            context.getString(R.string.copying)
        }
        val texts = arrayOf(title, context.getString(R.string.background),
                            context.getString(RC.string.dialog_cancel))

        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_progress_paste, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        progressDialog = builder.create()
        progressDialog?.setCancelable(false)

        var titleText: TextView
        var positiveButton: Button
        var negativeButton: Button
        var textFileToPath: TextView

        dialogView.apply {
            textFileName = findViewById(R.id.textFileName)
            textFileToPath = findViewById(R.id.textFileToPath)
            textFileCount = findViewById(R.id.textFilesLeft)
            textProgress = findViewById(R.id.textProgressPercent)
            progressBarPaste = findViewById(R.id.progressBarPaste)
            titleText = findViewById(R.id.textTitle)
            positiveButton = findViewById(R.id.buttonPositive)
            negativeButton = findViewById(R.id.buttonNegative)
        }

        titleText.text = texts[0]
        positiveButton.text = texts[1]
        negativeButton.text = texts[2]

        copiedFileInfo.clear()
        copiedFileInfo.addAll(files)
        copiedFilesSize = copiedFileInfo.size
        Logger.log(TAG, "CopiedFilesSize=$copiedFilesSize")

        textFileFromPath?.text = copiedFileInfo[0].filePath
        textFileName?.text = copiedFileInfo[0].fileName
        textFileToPath.text = destinationDir
        textFileCount?.text = String.format(Locale.getDefault(), "%s%d",
                                            context.getString(R.string.count_placeholder),
                                            copiedFilesSize)
        textProgress?.text = context.getString(R.string.zero_percent)

        positiveButton.setOnClickListener { progressDialog?.dismiss() }

        negativeButton.setOnClickListener {
            stopCopyService()
            progressDialog?.dismiss()
        }
        progressDialog?.show()
    }

    @SuppressLint("InflateParams")
    fun showZipProgressDialog(context: Context, destinationPath: String,
                              files: ArrayList<FileInfo>) {
        this.context = context
        registerReceiver(context)
        val title = context.getString(R.string.zip_progress_title)
        val texts = arrayOf(title, context.getString(R.string.background),
                            context.getString(RC.string.dialog_cancel))

        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_progress_paste, null)
        builder.setView(dialogView)
        progressDialog = builder.create()
        progressDialog?.setCancelable(false)
        textFileName = dialogView.findViewById(R.id.textFileName)
        textFileFromPath = dialogView.findViewById(R.id.textFileFromPath)
        val textFromPlaceHolder = dialogView.findViewById<TextView>(R.id.textFileFromPlaceHolder)
        dialogView.findViewById<View>(R.id.textFileToPlaceHolder).visibility = View.GONE

        textFromPlaceHolder.visibility = View.GONE
        textFileCount = dialogView.findViewById(R.id.textFilesLeft)
        textProgress = dialogView.findViewById(R.id.textProgressPercent)
        progressBarPaste = dialogView.findViewById(R.id.progressBarPaste)
        val titleText = dialogView.findViewById<TextView>(R.id.textTitle)

        val positiveButton = dialogView.findViewById<Button>(R.id.buttonPositive)
        val negativeButton = dialogView.findViewById<Button>(R.id.buttonNegative)

        textFileName?.visibility = View.GONE
        titleText.text = title
        positiveButton.text = texts[1]
        negativeButton.text = texts[2]

        copiedFileInfo = files
        copiedFilesSize = copiedFileInfo.size
        Logger.log(TAG, "Totalfiles=$copiedFilesSize")
        textFileFromPath?.text = destinationPath
        textProgress?.setText(R.string.zero_percent)

        positiveButton.setOnClickListener {
            progressDialog?.dismiss()
        }

        negativeButton.setOnClickListener {
            stopZipService()
            progressDialog?.dismiss()
        }

        progressDialog?.show()
    }

    @SuppressLint("InflateParams")
    fun showExtractProgressDialog(context: Context, zipFilePath: String, destinationPath: String) {
        this.context = context
        registerReceiver(context)

        val title = context.getString(R.string.extracting)
        val texts = arrayOf(title, context.getString(R.string.background),
                            context.getString(RC.string.dialog_cancel))
        val builder = AlertDialog.Builder(context)

        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_progress_paste, null)
        builder.setView(dialogView)
        progressDialog = builder.create()

        progressDialog?.setCancelable(false)
        textFileName = dialogView.findViewById(R.id.textFileName)
        textFileFromPath = dialogView.findViewById(R.id.textFileFromPath)
        val textFileToPath = dialogView.findViewById<TextView>(R.id.textFileToPath)
        val textFromPlaceHolder = dialogView.findViewById<TextView>(R.id.textFileFromPlaceHolder)
        textFromPlaceHolder.visibility = View.GONE
        textFileCount = dialogView.findViewById(R.id.textFilesLeft)
        textProgress = dialogView.findViewById(R.id.textProgressPercent)
        progressBarPaste = dialogView.findViewById(R.id.progressBarPaste)
        val titleText = dialogView.findViewById<TextView>(R.id.textTitle)

        val positiveButton = dialogView.findViewById<Button>(R.id.buttonPositive)
        val negativeButton = dialogView.findViewById<Button>(R.id.buttonNegative)

        titleText.text = title
        positiveButton.text = texts[1]
        negativeButton.text = texts[2]

        textFileFromPath?.text = zipFilePath
        textFileToPath.text = destinationPath

        textFileName?.text = title
        textProgress?.text = "0%"

        positiveButton.setOnClickListener { progressDialog?.dismiss() }

        negativeButton.setOnClickListener {
            stopExtractService()
            progressDialog?.dismiss()
        }

        progressDialog?.show()
    }


    private fun stopZipService() {
        val context = AceApplication.appContext
        val intent = Intent(context, CreateZipService::class.java)
        intent.action = ACTION_STOP
        context.startService(intent)
        unregisterReceiver(context)
    }

    private fun stopExtractService() {
        val context = AceApplication.appContext
        val intent = Intent(context, ExtractService::class.java)
        intent.action = ACTION_STOP
        context.startService(intent)
        unregisterReceiver(context)

    }

    private fun stopCopyService() {
        val context = AceApplication.appContext
        val intent = Intent(context, CopyService::class.java)
        intent.action = ACTION_STOP
        context.startService(intent)
        unregisterReceiver(context)
    }

    private fun unregisterReceiver(context: Context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(operationProgressReceiver)
    }

    private fun registerReceiver(context: Context) {
        val filter1 = IntentFilter(COPY_PROGRESS)
        filter1.addAction(MOVE_PROGRESS)
        filter1.addAction(EXTRACT_PROGRESS)
        filter1.addAction(ZIP_PROGRESS)
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(operationProgressReceiver, filter1)
    }

    private fun handleMessage(intent: Intent) {
        when (intent.action) {
            ZIP_PROGRESS     -> {
                handleZipProgress(intent)
            }

            EXTRACT_PROGRESS -> {
                handleExtractProgress(intent)
            }
            COPY_PROGRESS    -> {
                handleCopyProgress(intent)
            }
            MOVE_PROGRESS    -> {
                handleCopyProgress(intent)
            }
        }
    }

    private fun handleZipProgress(intent: Intent) {
        val copiedBytes = intent.getLongExtra(KEY_COMPLETED, 0)
        val totalBytes = intent.getLongExtra(KEY_TOTAL, 0)
        val progress = intent.getIntExtra(KEY_PROGRESS, 0)
        val isCompleted = intent.getBooleanExtra(KEY_END, false)

        progressBarPaste?.progress = progress
        textFileCount?.text = String.format("%s/%s",
                                            Formatter.formatFileSize(context, copiedBytes),
                                            Formatter.formatFileSize(context, totalBytes))
        textProgress?.text = String.format(Locale.getDefault(), "%d%s", progress,
                                           context?.getString(
                                                   R.string.percent_placeholder))
        //                Logger.log("FileUtils", "KEY_PROGRESS=" + progress + "Copied bytes=" + copiedBytes + " KEY_TOTAL bytes=" + totalBytes);

        if (progress == 100 || isCopied(totalBytes, copiedBytes)) {
            stopZipService()
            progressDialog?.dismiss()
        }
        if (isCompleted) {
            progressDialog?.dismiss()
        }
    }

    private fun handleExtractProgress(intent: Intent) {
        val copiedBytes = intent.getLongExtra(KEY_COMPLETED, 0)
        val totalBytes = intent.getLongExtra(KEY_TOTAL, 0)
        val progress = intent.getIntExtra(KEY_PROGRESS, 0)
        val isCompleted = intent.getBooleanExtra(KEY_END, false)

        Logger.log(TAG, "Progress=" + progress + "Operation=" + EXTRACT_PROGRESS)
        progressBarPaste?.progress = progress
        textFileCount?.text = String.format("%s/%s",
                                            Formatter.formatFileSize(context, copiedBytes),
                                            Formatter.formatFileSize(context, totalBytes))
        textProgress?.text = String.format(Locale.getDefault(), "%d%s", progress,
                                           context?.getString(
                                                   R.string.percent_placeholder))

        if (progress == 100 || isCopied(copiedBytes, totalBytes)) {
            stopExtractService()
            progressDialog?.dismiss()
        }
        if (isCompleted) {
            progressDialog?.dismiss()
        }
    }

    private fun handleCopyProgress(intent: Intent) {
        val isSuccess = intent.getBooleanExtra(KEY_RESULT, true)
        val progress = intent.getIntExtra(KEY_PROGRESS, 0)
        val copiedBytes = intent.getLongExtra(KEY_COMPLETED, 0)
        val totalBytes = intent.getLongExtra(KEY_TOTAL, 0)
        val end = intent.getBooleanExtra(KEY_END, false)

        if (!isSuccess) {
            stopCopyService()
            progressDialog?.dismiss()
            return
        }
        val totalProgress = intent.getIntExtra(KEY_TOTAL_PROGRESS, 0)
        progressBarPaste?.progress = totalProgress
        textProgress?.text = String.format(Locale.getDefault(), "%d%s", totalProgress,
                                           context?.getString(R.string.percent_placeholder))

        if (progress == 100 || isCopied(totalBytes, copiedBytes)) {
            val count = intent.getIntExtra(KEY_COUNT, 1)
            Logger.log(TAG, "KEY_COUNT=$count")
            if (isCopied(copiedBytes, totalBytes)) {
                stopCopyService()
                progressDialog?.dismiss()
            }
            else {
                val newCount = count + 1
                if (newCount >= copiedFileInfo.size) {
                    return
                }
                textFileFromPath?.text = copiedFileInfo[count].filePath
                textFileName?.text = copiedFileInfo[count].fileName
                textFileCount?.text = String.format(Locale.getDefault(), "%d/%d", newCount,
                                                    copiedFilesSize)
            }
        }
        if (end) {
            progressDialog?.dismiss()
        }
    }

    private fun isCopied(copiedBytes: Long, totalBytes: Long) = copiedBytes == totalBytes

    companion object {
        const val ACTION_STOP = "com.siju.acexplorer.ACTION_STOP"
    }
}
