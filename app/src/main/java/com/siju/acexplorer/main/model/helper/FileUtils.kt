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

import android.content.Context
import android.text.format.Formatter
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.siju.acexplorer.main.model.StorageUtils.getDocumentFile
import com.siju.acexplorer.main.model.StorageUtils.isOnExtSdCard
import com.siju.acexplorer.main.model.groups.Category
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {
    const val ACTION_NONE = 0
    const val ACTION_KEEP = 3
    private const val EXT_APK = "apk"

    fun isFileMusic(path: String?): Boolean {
        path ?: return false
        return path.toLowerCase(Locale.ROOT).endsWith(".mp3") ||
                path.toLowerCase(Locale.ROOT).endsWith(".amr")
                || path.toLowerCase(Locale.ROOT).endsWith(".wav")
                || path.toLowerCase(Locale.ROOT).endsWith(".m4a")
    }

    fun getAbsolutePath(file: File?): String? {
        return file?.absolutePath
    }

    @JvmStatic
    fun convertDate(dateInMs: Long): String {
        val df2 = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        val date = Date(dateInMs)
        return df2.format(date)
    }

    /**
     * Validates file name at the time of creation
     * special reserved characters shall not be allowed in the file names
     *
     * @param name the file which needs to be validated
     * @return boolean if the file name is valid or invalid
     */
    fun isFileNameInvalid(name: String): Boolean { /* StringBuilder builder = new StringBuilder(file.getPath());
        String newName = builder.substring(builder.lastIndexOf("/") + 1, builder.length());*/
        val newName = name.trim { it <= ' ' }
        return newName.contains("/") || newName.isEmpty()
    }

    fun showMessage(context: Context?, msg: String?) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun getFolderSize(directory: File): Long {
        var length: Long = 0
        val fileList = directory.listFiles() ?: return length
        try {
            for (file in fileList) {
                length += if (file.isFile) {
                    file.length()
                } else {
                    getFolderSize(file)
                }
            }
        } catch (ignored: Exception) {
        }
        return length
    }

    fun getOutputStream(target: File, context: Context): OutputStream? {
        var outStream: OutputStream? = null
        try { // First try the normal way
            if (isWritable(target)) { // standard way
                outStream = FileOutputStream(target)
            } else { // Storage Access Framework
                val targetDocument = getDocumentFile(target, false)
                if (targetDocument != null) {
                    outStream = context.contentResolver.openOutputStream(targetDocument.uri)
                }
            }
        } catch (ignored: Exception) {
        }
        return outStream
    }

    /**
     * Check is a file is writable. Detects write issues on external SD card.
     *
     * @param file The file
     * @return true if the file is writable.
     */
    fun isWritable(file: File?): Boolean {
        if (file == null) {
            return false
        }
        val isExisting = file.exists()
        try {
            val output = FileOutputStream(file, true)
            try {
                output.close()
            } catch (e: IOException) { // do nothing.
            }
        } catch (e: FileNotFoundException) {
            return false
        }
        val result = file.canWrite()
        // Ensure that file is not created during this process.
        if (!isExisting) {
            file.delete()
        }
        return result
    }

    fun getCategoryFromExtension(extension: String?): Category {
        var value = Category.FILES
        if (extension == null) {
            return Category.FILES
        }
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase(Locale.ROOT))
        if (mimeType != null) {
            when {
                mimeType.indexOf("image") == 0 -> {
                    value = Category.IMAGE
                }
                mimeType.indexOf("video") == 0 -> {
                    value = Category.VIDEO
                }
                mimeType.indexOf("audio") == 0 -> {
                    value = Category.AUDIO
                }
            }
        }
        return value
    }

    /**
     * Check for a directory if it is possible to create files within this directory, either via normal writing or via
     * Storage Access Framework.
     *
     * @param folder The directory
     * @return true if it is possible to write in this directory.
     */
    fun isFileNonWritable(folder: File?): Boolean { // Verify that this is a directory.
        if (folder == null) {
            return false
        }
        if (!folder.exists() || !folder.isDirectory) {
            return false
        }
        // Find a non-existing file in this directory.
        var i = 0
        var file: File
        do {
            val fileName = "SJDiagnoseDummyFile" + ++i
            file = File(folder, fileName)
        } while (file.exists())
        if (isWritable(file)) {
            return false
        }
        val document = getDocumentFile(file, false) ?: return true
        // This should have created the file - otherwise something is wrong with access URL.
        val result = document.canWrite() && file.exists()
        // Ensure that the dummy file is not remaining.
        deleteFile(file)
        return !result
    }

    /**
     * Delete a file. May be even on external SD card.
     *
     * @param file the file to be deleted.
     */
    private fun deleteFile(file: File) { // First try the normal deletion.
        if (file.delete()) {
            return
        }
        // Try with Storage Access Framework.
        if (isOnExtSdCard(file)) {
            val document = getDocumentFile(file, false)
            document?.delete()
            return
        }
        file.exists()
    }

    fun isFileCompressed(filePath: String?): Boolean {
        filePath ?: return false
        return filePath.toLowerCase(Locale.ROOT).endsWith(".zip") ||
                filePath.toLowerCase(Locale.ROOT).endsWith(".tar") ||
                filePath.toLowerCase(Locale.ROOT).endsWith(".tar.gz")
    }

    fun isApk(extension: String?): Boolean {
        return EXT_APK == extension
    }

    fun isFileExisting(currentDir: String?, fileName: String): Boolean {
        currentDir ?: return false
        val file = File(currentDir)
        val list = file.list() ?: return false
        for (aList in list) {
            if (fileName == aList) {
                return true
            }
        }
        return false
    }

    /**
     * Gets the extension of a file name without ".".
     */
    fun getExtension(path: String?): String? {
        if (path == null) {
            return null
        }
        val dot = getLastDotIndex(path)
        return substring(path, dot + 1)
    }

    fun getExtensionWithDot(path: String?): String? {
        if (path == null) {
            return null
        }
        val dot = getLastDotIndex(path)
        return substring(path, dot)
    }

    private fun getLastDotIndex(path: String): Int {
        return path.lastIndexOf(".")
    }

    private fun substring(path: String, index: Int): String {
        return if (index >= 0) {
            path.substring(index)
        } else {
            ""
        }
    }

    fun constructFileNameWithExtension(fileName: String, extension: String?): String {
        return "$fileName.$extension"
    }

    @JvmStatic
    fun getFileNameWithoutExt(filePath: String?): String? {
        if (filePath == null) {
            return null
        }
        val file = File(filePath)
        val fileName: String
        fileName = if (file.isFile) {
            val dotIndex = getLastDotIndex(filePath)
            val fileNameIdx = filePath.lastIndexOf("/") + 1
            if (dotIndex <= fileNameIdx) {
                return null
            }
            filePath.substring(fileNameIdx, dotIndex)
        } else {
            file.name
        }
        return fileName
    }

    fun formatSize(context: Context?, sizeInBytes: Long): String {
        return Formatter.formatFileSize(context, sizeInBytes)
    }
}