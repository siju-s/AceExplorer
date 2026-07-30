package com.siju.acexplorer.appmanager.model

import android.content.Context
import android.content.pm.ApplicationInfo
import android.text.format.Formatter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

/**
 * Measures the installed APK files of an app.
 *
 * This only covers the APKs, not user data or cache, because reading those needs the Usage Access
 * special permission. Touches the file system, so callers must stay off the main thread.
 */
class AppSizeCalculator @Inject constructor(@ApplicationContext private val context: Context) {

    fun formattedApkSize(applicationInfo: ApplicationInfo): String {
        return Formatter.formatFileSize(context, apkBytes(applicationInfo))
    }

    private fun apkBytes(applicationInfo: ApplicationInfo): Long {
        val apkPaths = sequenceOf(applicationInfo.sourceDir, *applicationInfo.splitSourceDirs.orEmpty())
        return apkPaths.sumOf { path -> File(path).length() }
    }
}
