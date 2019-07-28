package com.siju.acexplorer.storage.modules.zipviewer.helper

enum class ZipFormats {

    ZIP,
    APK;

    companion object {

        val zip = "zip"
        val apk = "apk"

        fun getFormatFromExt(extension: String): ZipFormats {
            when (extension) {
                zip -> return ZIP
                apk -> return APK
            }
            return ZIP
        }
    }
}