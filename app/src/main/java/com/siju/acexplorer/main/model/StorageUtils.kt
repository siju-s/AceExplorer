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

package com.siju.acexplorer.main.model

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import androidx.documentfile.provider.DocumentFile
import androidx.preference.PreferenceManager
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.main.model.groups.STORAGE_SDCARD1
import com.siju.acexplorer.main.model.groups.StorageFetcher
import com.siju.acexplorer.main.model.helper.SdkHelper
import java.io.File
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

private const val STORAGE_SDCARD0 = "/storage/sdcard0"
private const val ANDROID_DATA = "/Android/data"
private const val PATH_STORAGE_ROOT = "/storage"
private const val PATH_USB = "/mnt/sdcard/usbStorage"
private const val PATH_USB2 = "/mnt/sdcard/usb_storage"

object StorageUtils {

    private val DIR_SEPARATOR = Pattern.compile("/")

    val downloadsDirectory: String
        get() = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).absolutePath

    val internalStorage: String
        get() = Environment.getExternalStorageDirectory().absolutePath

    // Final set of paths
    // Primary physical SD-CARD (not emulated)
    // All Secondary SD-CARDs (all exclude primary) separated by ":"
    // Primary emulated SD-CARD
    // Device has physical external storage; use plain paths.
    // EXTERNAL_STORAGE undefined; falling back to default.
    // Device has emulated storage; external storage paths should have
    // userId burned into them.
    // /storage/emulated/0[1,2,...]
    // Add all secondary storages
    // All Secondary SD-CARDs splited into array
    val storageDirectories: List<String>
        get() {
            val paths = LinkedHashSet<String>()
            if (SdkHelper.isAtleastMarsh) {
                addExternalSdPath(paths)
                addUsbPath(paths)
            }
            else {
                populateStoragePathForLollipop(paths)
            }
            return ArrayList(paths)
        }

    private fun populateStoragePathForLollipop(paths: LinkedHashSet<String>) {
        val rawExternalStorage = getExternalStoragePath()
        val rawSecondaryStorage = getSecondaryStoragePath()
        val rawEmulatedStorage = getEmulatedStoragePath()

        if (TextUtils.isEmpty(rawEmulatedStorage)) {
            if (rawExternalStorage.isNullOrEmpty()) {
                paths.add(STORAGE_SDCARD0)
            }
            else {
                paths.add(rawExternalStorage)
            }
        }
        else {
            val rawUserId = getRawStorageId()
            rawEmulatedStorage?.let {
                if (rawUserId.isEmpty()) {
                    paths.add(it)
                }
                else {
                    paths.add(it + File.separator + rawUserId)
                }
            }
        }
        addRawSecStorage(rawSecondaryStorage, paths)
    }

    private fun addRawSecStorage(rawSecondaryStorage: String?, paths: LinkedHashSet<String>) {
        rawSecondaryStorage?.let { storage ->
            if (storage.isNotEmpty()) {
                val rawSecStorage = storage.split(File.pathSeparator.toRegex())
                        .dropLastWhile { it.isEmpty() }.toTypedArray()
                Collections.addAll(paths, *rawSecStorage)
            }
        }
    }

    private fun getRawStorageId(): String {
        val rawUserId: String
        val path = internalStorage
        val folders = DIR_SEPARATOR.split(path)
        val lastFolder = folders[folders.size - 1]
        var isDigit = false
        try {
            Integer.valueOf(lastFolder)
            isDigit = true
        }
        catch (ignored: NumberFormatException) {
            //no-op
        }

        rawUserId = if (isDigit) lastFolder else ""
        return rawUserId
    }

    private fun addUsbPath(paths: LinkedHashSet<String>) {
        val usb = usbDrive
        if (usb != null && !paths.contains(usb.path)) {
            paths.add(usb.path)
        }
    }

    private fun addExternalSdPath(paths: LinkedHashSet<String>) {
        val pathList = extSdCardPaths
        Collections.addAll(paths, *pathList)
    }

    private fun getEmulatedStoragePath() = System.getenv("EMULATED_STORAGE_TARGET")

    private fun getSecondaryStoragePath() = System.getenv("SECONDARY_STORAGE")

    private fun getExternalStoragePath() = System.getenv("EXTERNAL_STORAGE")


    private
    val extSdCardPaths: Array<String>
        get() {
            val context = AceApplication.appContext
            val paths = ArrayList<String>()

            populateExternalFilesDirPaths(context, paths)

            val file = File(STORAGE_SDCARD1)
            if (file.exists() && file.canExecute() && !paths.contains(file.absolutePath)) {
                paths.add(STORAGE_SDCARD1)
            }
            return paths.toTypedArray()
        }

    private fun populateExternalFilesDirPaths(context: Context, paths: ArrayList<String>) {
        val externalFilesDirs = context.getExternalFilesDirs("external") ?: return
        for (file in externalFilesDirs) {
            if (file == null) {
                continue
            }
            val index = file.absolutePath.lastIndexOf(ANDROID_DATA)
            if (index >= 0) {
                var path = file.absolutePath.substring(0, index)
                try {
                    path = File(path).canonicalPath
                }
                catch (e: IOException) {

                }

                if (!paths.contains(path)) {
                    paths.add(path)
                }
            }
        }
    }


    private val usbDrive: File?
        @SuppressLint("SdCardPath")
        get() {
            var storageDir = File(PATH_STORAGE_ROOT)

            val files = storageDir.listFiles()
            if (files != null) {
                for (file in files) {
                    if (isValidDrive(file) && file.name.toLowerCase(Locale.ROOT).contains("usb")) {
                        return file
                    }
                }
            }
            storageDir = File(PATH_USB)
            if (isValidDrive(storageDir)) {
                return storageDir
            }
            storageDir = File(PATH_USB2)
            return if (isValidDrive(storageDir)) {
                storageDir
            }
            else {
                null
            }
        }

    private fun isValidDrive(file: File) = file.exists() && file.canExecute()

    enum class StorageType {
        ROOT,
        INTERNAL,
        EXTERNAL;


        companion object {
            @JvmStatic
            fun getStorageText(context: Context, storageType: StorageType): String {
                return when (storageType) {
                    ROOT     -> context.getString(R.string.nav_menu_root)
                    INTERNAL -> context.getString(R.string.nav_menu_internal_storage)
                    else     -> context.getString(R.string.nav_menu_internal_storage)
                }
            }
        }
    }

    /**
     * Determine the main folder of the external SD card containing the given file.
     *
     * @param file the file.
     * @return The main folder of the external SD card containing this file, if the file is on an SD card. Otherwise,
     * null is returned.
     */
    private fun getExtSdCardRoot(file: File): String? {
        val extSdPaths = extSdCardPaths
        try {
            for (extSdPath in extSdPaths) {
                if (file.canonicalPath.startsWith(extSdPath)) {
                    return extSdPath
                }
            }
        }
        catch (e: IOException) {
            return null
        }

        return null
    }

    fun isOnExtSdCard(file: File): Boolean {
        return getExtSdCardRoot(file) != null
    }

    /**
     * Get a DocumentFile corresponding to the given file (for writing on ExtSdCard on Android 5). If the file is not
     * existing, it is created.
     *
     * @param file        The file.
     * @param isDirectory flag indicating if the file should be a directory.
     * @return The DocumentFile
     */
    fun getDocumentFile(file: File?, isDirectory: Boolean): DocumentFile? {
        file ?: return null
        val extSdRoot = getExtSdCardRoot(file)
        var originalDirectory = false
        if (extSdRoot == null) {
            return null
        }
        var relativePath: String? = null
        try {
            val filePath = file.canonicalPath
            if (extSdRoot == filePath) {
                originalDirectory = true
            }
            else {
                relativePath = filePath.substring(extSdRoot.length + 1)
            }
        }
        catch (e: IOException) {
            return null
        }
        catch (f: Exception) {
            originalDirectory = true
        }

        val context = AceApplication.appContext
        val safUri = PreferenceManager.getDefaultSharedPreferences(context).getString(
                FileConstants.SAF_URI, null)
                ?: return null
        val treeUri = Uri.parse(safUri) ?: return null

        return getDocumentFile(context, treeUri, relativePath, originalDirectory, isDirectory)
    }

    private fun getDocumentFile(context: Context, treeUri: Uri, relativeFilePath: String?,
                                originalDirectory: Boolean,
                                isDirectory: Boolean): DocumentFile? {
        // start with root of SD card and then parse through document tree.
        var document = DocumentFile.fromTreeUri(context, treeUri)
        if (originalDirectory || relativeFilePath == null) {
            return document
        }
        val parts = relativeFilePath.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        for (part in parts.indices) {
            var nextDocument = document?.findFile(parts[part])

            if (nextDocument == null) {
                nextDocument = if (part < parts.size - 1 || isDirectory) {
                    document?.createDirectory(parts[part])
                }
                else {
                    document?.createFile("image", parts[part])
                }
            }
            document = nextDocument
        }
        return document
    }


    fun getSpaceLeft(file: File): Long {
        return file.freeSpace
    }

    fun getTotalSpace(file: File): Long {
        return file.totalSpace
    }

    fun getSpaceUsed(total : Long, freeSpace : Long): Long {
        return total - freeSpace
    }

    fun isRootDirectory(path: String): Boolean {
        val externalSDList = StorageFetcher(AceApplication.appContext).getExternalSdList()
        when {
            path.contains(internalStorage) -> return false
            externalSDList.size > 0        -> {
                for (extPath in externalSDList) {
                    if (path.contains(extPath)) {
                        return false
                    }
                }
                return true
            }
            else                           -> return true
        }
    }

}
