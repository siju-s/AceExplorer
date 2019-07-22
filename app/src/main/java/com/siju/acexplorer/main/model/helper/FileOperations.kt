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

package com.siju.acexplorer.main.model.helper

import android.util.Log
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.logging.Logger
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.main.model.StorageUtils.getDocumentFile
import com.siju.acexplorer.main.model.StorageUtils.isOnExtSdCard
import com.siju.acexplorer.main.model.helper.SdkHelper.isAtleastLollipop
import java.io.*
import java.nio.channels.FileChannel


private const val TAG = "FileOperations"
private const val BUFFER_SIZE_KB = 16384

object FileOperations {

    fun renameFolder(source: File, target: File): Boolean {
        // First try the normal rename.
        if (rename(source, target)) {
            return true
        }
        if (target.exists()) {
            return false
        }

        // Try the Storage Access Framework if it is just a rename within the same parent folder.
        if (source.parent == target.parent && StorageUtils.isOnExtSdCard(source)) {

            val document = StorageUtils.getDocumentFile(source, true)

            Logger.log(TAG, " Document uri in rename=$document")
            if (document?.renameTo(target.name) == true) {
                return true
            }
        }

        if (source.isFile) {
            Logger.log(TAG, "Rename--root=")

            if (!mkfile(target)) {
                return false
            }

        }
        else {
            // Try the manual way, moving files individually.
            if (!mkdir(target)) {
                return false
            }
        }

        val sourceFileList = source.listFiles()

        if (sourceFileList.isNullOrEmpty()) {
            return true
        }

        for (sourceFile in sourceFileList) {
            val fileName = sourceFile.name
            val targetFile = File(target, fileName)
            if (!copyFile(sourceFile, targetFile)) {
                // stop on first error
                return false
            }
        }
        // Only after successfully copying all files, delete files on source folder.
        for (sourceFile in sourceFileList) {
            if (!FileUtils.deleteFile(sourceFile)) {
                // stop on first error
                return false
            }
        }
        return true
    }

    fun mkfile(file: File?): Boolean {
        if (file == null)
            return false
        if (file.exists()) {
            // nothing to create.
            return !file.isDirectory
        }

        // Try the normal way
        try {
            return file.createNewFile()
        }
        catch (e: IOException) {
            e.printStackTrace()
        }

        // Even after exception, In Kitkat file might have got created so return true
        if (file.exists()) return true
        val result: Boolean
        // Try with Storage Access Framework.
        if (isAtleastLollipop() && StorageUtils.isOnExtSdCard(file)) {
            val document = StorageUtils.getDocumentFile(file.parentFile, true)
            // getDocumentFile implicitly creates the directory.
            return try {
                Logger.log(TAG, "mkfile--doc=$document")
                result = document?.createFile(FileUtils.getMimeType(file)!!,
                                              file.name) != null
                result
            }
            catch (e: Exception) {
                e.printStackTrace()
                false
            }

        }
        return false
    }

    fun mkdir(file: File?): Boolean {
        if (file == null)
            return false
        if (file.exists()) {
            // nothing to create.
            return file.isDirectory
        }

        // Try the normal way
        if (file.mkdirs()) {
            return true
        }

        // Try with Storage Access Framework.
        if (isOnExtSdCard(file)) {
            val document = getDocumentFile(file, true)
            // getDocumentFile implicitly creates the directory.
            return document != null && document.exists()
        }
        return false
    }


    private fun copyFile(source: File, target: File): Boolean {
        var inputStream: FileInputStream? = null
        var outputStream: OutputStream? = null
        var inChannel: FileChannel? = null
        var outChannel: FileChannel? = null
        try {
            inputStream = FileInputStream(source)

            // First try the normal way
            if (FileUtils.isWritable(target)) {
                // standard way
                outputStream = FileOutputStream(target)
                inChannel = inputStream.channel
                outChannel = outputStream.channel
                inChannel!!.transferTo(0, inChannel.size(), outChannel)
            }
            else {
                // Storage Access Framework
                val targetDocument = StorageUtils.getDocumentFile(target, false)
                if (targetDocument != null)
                    outputStream = AceApplication.appContext.contentResolver.openOutputStream(
                            targetDocument.uri)

                outputStream?.let {
                    // For SAF, write to output stream.
                    val buffer = ByteArray(BUFFER_SIZE_KB)
                    var bytesRead = inputStream.read(buffer)

                    while (bytesRead != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        bytesRead = inputStream.read(buffer)
                    }
                }

            }
        }
        catch (e: Exception) {
            Log.e(TAG,
                  "Error when copying file from " + source.absolutePath + " to " + target.absolutePath,
                  e)
            return false
        }
        finally {
            try {
                inputStream?.close()
                outputStream?.close()
                inChannel?.close()
                outChannel?.close()

            }
            catch (e: IOException) {
                e.printStackTrace()
            }

        }
        return true
    }

    private fun rename(file: File, newFile: File): Boolean {
        if (file.parentFile.canWrite()) {
            Logger.log(TAG, "Rename--canWrite=" + true)
            return file.renameTo(newFile)
        }
        return false
    }

    fun deleteFiles(filesList: ArrayList<String>) : Int {
        var filesDeleted = 0
        for (i in 0 until filesList.size) {
            val path = filesList[i]
            val isDeleted = delete(File(path))

            if (!isDeleted) {
                val isRootDir = StorageUtils.isRootDirectory(path)
                if (!isRootDir) {
                    return filesDeleted
                }
            }
            else {
                filesDeleted++
            }
        }
        return filesDeleted
    }

    private fun delete(file: File): Boolean {
        val fileDelete = deleteFile(file)

        if (file.delete() || fileDelete) {
            return true
        }

        // Try with Storage Access Framework.
        if (isOnExtSdCard(file)) {
            val document = getDocumentFile(file, false)
            return document != null && document.delete()
        }
        return !file.exists()
    }

    private fun deleteFile(file: File): Boolean {
        // First try the normal deletion.
        var isDeleted = false
        if (file.isDirectory) {
            val fileList = file.listFiles()
            if (fileList != null) {
                for (child in fileList) {
                    val path = child.absolutePath
                    isDeleted = deleteFile(child)
                }
                val path = file.absolutePath
                isDeleted = file.delete()
            }
        }
        else {
            val path = file.absolutePath
            isDeleted = file.delete()
        }
        return isDeleted
    }

}
