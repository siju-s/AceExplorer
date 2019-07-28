package com.siju.acexplorer.storage.modules.zipviewer.model

import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.storage.model.task.ExtractZipEntry
import java.util.zip.ZipEntry

interface ZipViewerModel {
    fun loadData(path: String?, parentZipPath: String,
                 zipElementsResultCallback: ZipLoader.ZipElementsResultCallback): ArrayList<FileInfo>

    fun onFileClicked(name: String, zipEntry: ZipEntry, parentZipPath: String,
                      zipFileViewCallback: ExtractZipEntry.ZipFileViewCallback)

    fun clearCache()
}