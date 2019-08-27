package com.siju.acexplorer.storage.model.operations

import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.helper.MediaScannerHelper
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.main.model.root.RootDeniedException
import com.siju.acexplorer.main.model.root.RootUtils
import com.stericson.RootTools.RootTools
import java.io.File

class DeleteOperation {

    private val filesToMediaIndex = arrayListOf<String>() // Doesn't include directories
    private var filesDeleted = 0

    fun deleteFiles(filesList: ArrayList<String>): Int {
        for (i in 0 until filesList.size) {
            val path = filesList[i]
            val isDeleted = delete(File(path), false)

            if (!isDeleted) {
                val isRootDir = StorageUtils.isRootDirectory(path)
                if (!isRootDir) {
                    return filesDeleted
                }
                else {
                    val rooted = RootUtils.isRooted(AceApplication.appContext) && RootTools.isAccessGiven()
                    if (rooted) {
                        try {
                            RootUtils.mountRW(path)
                            RootUtils.delete(path)
                            RootUtils.mountRO(path)
                            filesDeleted++
                            filesToMediaIndex.add(path)
                        } catch (e: RootDeniedException) {
                            e.printStackTrace()
                        }

                    }
                }
            }
        }
        if (filesToMediaIndex.isNotEmpty()) {
            MediaScannerHelper.scanFiles(AceApplication.appContext,
                                         filesToMediaIndex.toTypedArray())
        }
        return filesDeleted
    }

    fun delete(file: File, mediaIndex : Boolean = true): Boolean {
        val fileDelete = deleteFile(file)

        if (file.delete() || fileDelete) {
            return true
        }

        // Try with Storage Access Framework.
        if (StorageUtils.isOnExtSdCard(file)) {
            return deleteExtSdFile(file)
        }
        if (mediaIndex && filesToMediaIndex.isNotEmpty()) {
            MediaScannerHelper.scanFiles(AceApplication.appContext,
                                         filesToMediaIndex.toTypedArray())
        }
        return !file.exists()
    }

    private fun deleteExtSdFile(file: File): Boolean {
        val document = StorageUtils.getDocumentFile(file, false)
        val isDeleted = document != null && document.delete()
        if (isDeleted) {
            if (MediaScannerHelper.isMediaScanningRequired(file)) {
                filesToMediaIndex.add(file.absolutePath)
            }
            filesDeleted++
        }
        return isDeleted
    }

    private fun deleteFile(file: File): Boolean {
        // First try the normal deletion.
        var isDeleted = false
        if (file.isDirectory) {
            isDeleted = deleteDirectoryFiles(file, isDeleted)
        }
        else {
            val path = file.absolutePath
            isDeleted = file.delete()
            if (isDeleted) {
                filesDeleted++
                if (MediaScannerHelper.isMediaScanningRequired(file)) {
                    filesToMediaIndex.add(path)
                }
            }
        }
        return isDeleted
    }

    private fun deleteDirectoryFiles(file: File, isDeleted: Boolean): Boolean {
        var isDeleted1 = isDeleted
        val fileList = file.listFiles()
        if (fileList != null) {
            for (child in fileList) {
                child.absolutePath
                deleteFile(child)
            }
            file.absolutePath
            isDeleted1 = file.delete()
        }
        return isDeleted1
    }


}