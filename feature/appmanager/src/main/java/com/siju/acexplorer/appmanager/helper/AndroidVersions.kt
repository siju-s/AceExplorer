package com.siju.acexplorer.appmanager.helper

import android.content.Context
import com.siju.acexplorer.appmanager.R

/**
 * Maps an SDK level to the marketing Android version users recognise.
 */
object AndroidVersions {

    private val versionNameBySdk = mapOf(
        21 to "5.0",
        22 to "5.1",
        23 to "6",
        24 to "7",
        25 to "7.1",
        26 to "8",
        27 to "8.1",
        28 to "9",
        29 to "10",
        30 to "11",
        31 to "12",
        32 to "12L",
        33 to "13",
        34 to "14",
        35 to "15",
        36 to "16"
    )

    /**
     * Returns "30 (Android 11)" when the version is known, otherwise the bare SDK level.
     */
    fun describeSdk(context: Context, sdkVersion: Int): String {
        val versionName = versionNameBySdk[sdkVersion] ?: return sdkVersion.toString()
        return context.getString(R.string.sdk_with_android_version, sdkVersion, versionName)
    }
}
