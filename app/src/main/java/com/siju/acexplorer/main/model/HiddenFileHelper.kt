package com.siju.acexplorer.main.model

import android.provider.MediaStore
import java.io.File

object HiddenFileHelper {
    fun shouldSkipHiddenFiles(file: File, showHidden: Boolean): Boolean {
        return file.isHidden && !showHidden
    }

    // DATA field is required to check path. Works fine till Android 12 even though deprecated
    @Suppress("Deprecation")
    fun constructionNoHiddenFilesArgs(): String {
        return MediaStore.Files.FileColumns.DATA + " NOT LIKE '%/.%'"
    }
}