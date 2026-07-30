package com.siju.acexplorer.appmanager.model

import android.content.pm.ApplicationInfo
import androidx.annotation.StringRes
import com.siju.acexplorer.appmanager.R
import java.io.File

/**
 * Native architecture an app is installed for.
 *
 * [ApplicationInfo.primaryCpuAbi] is hidden API, so the ABI is read from the last segment of the
 * native library directory the installer created for the app.
 */
enum class AppArchitecture(
    @StringRes val bitnessRes: Int,
    val abiName: String,
    private val nativeLibraryDirName: String
) {

    ARM64(R.string.architecture_64_bit, "arm64-v8a", "arm64"),
    ARM(R.string.architecture_32_bit, "armeabi-v7a", "arm"),
    X86_64(R.string.architecture_64_bit, "x86_64", "x86_64"),
    X86(R.string.architecture_32_bit, "x86", "x86");

    companion object {

        fun of(applicationInfo: ApplicationInfo): AppArchitecture? {
            val nativeLibraryDir = applicationInfo.nativeLibraryDir ?: return null
            val dirName = File(nativeLibraryDir).name
            return entries.firstOrNull { architecture -> architecture.nativeLibraryDirName == dirName }
        }
    }
}
