package com.siju.acexplorer.storage.modules.zipviewer.model

import android.content.Context
import android.util.Log
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.FileConstants
import com.siju.acexplorer.storage.model.task.ExtractZipEntry
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

private const val NO_MEDIA_FILE_NAME = ".nomedia"
private const val TMP_DIR_EXT = ".tmp"
private const val TAG = "ZipViewerModelImpl"

class ZipViewerModelImpl(val context: Context) : ZipViewerModel {

    private val zipLoader = ZipLoader(context)

    override fun populateZipList(parentZipPath: String) {
        zipLoader.populateTotalZipList(parentZipPath)
    }

    override fun loadData(path: String?, parentZipPath: String,
                          zipElementsResultCallback: ZipLoader.ZipElementsResultCallback): ArrayList<FileInfo> {
        return zipLoader.getZipContents(path, parentZipPath, zipElementsResultCallback)
    }

    private fun getCacheTempDirPath(): File? {
        val cacheDir = getCacheDir() ?: return null
        return File(cacheDir.parent, TMP_DIR_EXT)
    }

    private fun createCacheDir(file: File): Boolean {
        val result = file.mkdir()
        if (result) {
            createNoMediaFile(file)
            return true
        }
        return false
    }

    private fun getCacheDir() = context.externalCacheDir

    private fun createNoMediaFile(file: File) {
        val noMedia = File(file.toString() + File.separator + NO_MEDIA_FILE_NAME)
        try {
            noMedia.createNewFile()
        }
        catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onFileClicked(name: String, zipEntry: ZipEntry, parentZipPath: String,
                               zipFileViewCallback: ExtractZipEntry.ZipFileViewCallback) {
        val outputDir = getCacheTempDirPath() ?: return
        if (!outputDir.exists()) {
            createCacheDir(outputDir)
        }
        Log.e(TAG, "Zip entry NEW:$zipEntry")

        if (isZipExtension(name)) {
            return
        }
        try {
            val zipFile = ZipFile(parentZipPath)
            ExtractZipEntry()
                    .unzipEntry(zipFile, zipEntry, outputDir.absolutePath, name, zipFileViewCallback)
        }
        catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun isZipExtension(name: String?) = name?.endsWith(FileConstants.EXT_ZIP) == true

    override fun clearCache() {
        val cacheTmpDir = getCacheTempDirPath()
        cacheTmpDir?.let {
            val files = cacheTmpDir.listFiles()
            files?.let {
                for (file in files) {
                    file.delete()
                }
            }
        }
    }


}