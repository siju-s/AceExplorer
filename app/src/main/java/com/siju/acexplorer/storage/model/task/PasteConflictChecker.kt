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

import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.main.model.data.FileDataFetcher
import com.siju.acexplorer.main.model.groups.StorageFetcher
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.storage.model.operations.Operations
import java.io.File
import java.util.*


class PasteConflictChecker(private val destinationDir: String, private val rooted: Boolean,
                           private val operation: Operations, private val totalFilesToPaste: ArrayList<FileInfo>) {

    private val conflictFiles = arrayListOf<FileInfo>()
    private val destFiles = arrayListOf<FileInfo>()
    private var pasteResultCallback: PasteResultCallback? = null

    fun run() {
        val totalBytes = calculateFilesSize()
        val isRootDir = checkIfRootDir()

        val destFile = File(destinationDir)

        if (isRootDir || destFile.freeSpace >= totalBytes) {
            onPastePossible()
        }
        else {
            pasteResultCallback?.onLowSpace()
        }
    }

    private fun onPastePossible() {
        findConflictFiles()
        if (conflictFiles.isEmpty()) {
            pasteResultCallback?.checkWriteMode(destinationDir, totalFilesToPaste, operation)
        }
        else {
            pasteResultCallback?.showConflictDialog(totalFilesToPaste, conflictFiles, destFiles,
                                                    destinationDir, operation)
        }
    }

    private fun calculateFilesSize(): Long {
        var totalBytes: Long = 0
        for (i in totalFilesToPaste.indices) {
            val fileInfo = totalFilesToPaste[i]

            val filePath = fileInfo.filePath ?: continue

            totalBytes = if (fileInfo.isDirectory) {
                totalBytes + FileUtils.getFolderSize(File(filePath))
            }
            else {
                totalBytes + File(filePath).length()
            }
        }
        return totalBytes
    }

    private fun checkIfRootDir(): Boolean {
        var isRootDir = !destinationDir.startsWith(StorageUtils.internalStorage)
        val externalSDList = StorageFetcher(AceApplication.appContext).externalSDList

        for (dir in externalSDList) {
            if (destinationDir.startsWith(dir)) {
                isRootDir = false
            }
        }
        return isRootDir
    }

    private fun findConflictFiles() {
        val listFiles = FileDataFetcher.getFilesList(destinationDir,
                                                     rooted, true)

        for (destFile in listFiles) {
            for (fileToPaste in totalFilesToPaste) {
                if (fileToPaste.fileName == destFile.fileName) {
                    conflictFiles.add(fileToPaste)
                    destFiles.add(destFile)
                }
            }
        }
    }

    fun setListener(pasteResultCallback: PasteResultCallback) {
        this.pasteResultCallback = pasteResultCallback
    }

    interface PasteResultCallback {
        fun showConflictDialog(files: ArrayList<FileInfo>, conflictFiles: ArrayList<FileInfo>,
                               destFiles: ArrayList<FileInfo>,
                               destinationDir: String, operation: Operations)

        fun onLowSpace()

        fun checkWriteMode(destinationDir: String, files: ArrayList<FileInfo>,
                           operation: Operations)
    }
}
