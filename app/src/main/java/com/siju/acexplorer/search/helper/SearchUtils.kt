package com.siju.acexplorer.search.helper

import android.os.Environment
import java.io.File

object SearchUtils {

    fun getCameraDirectory(): String? {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/Camera"
    }

    fun getScreenshotDirectory() : String ? {
        val screenShotPath1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/Screenshots"
        val screenShotPath2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/Screenshots"

        return when {
            File(screenShotPath1).exists() -> {
                screenShotPath1
            }
            File(screenShotPath2).exists() -> {
                screenShotPath2
            }
            else -> screenShotPath1
        }
    }

    fun getWhatsappDirectory() : String? {
        return Environment.getExternalStorageDirectory().absolutePath + "/WhatsApp/Media"
    }

    fun getWhatsappImagesDirectory() : String? {
        return getWhatsappDirectory() + "/WhatsApp Images"
    }

    fun getWhatsappVideosDirectory() : String? {
        return getWhatsappDirectory() + "/WhatsApp Video"
    }

    fun getWhatsappAudioDirectory() : String? {
        return getWhatsappDirectory() + "/WhatsApp Audio"
    }

    fun getWhatsappDocDirectory() : String? {
        return getWhatsappDirectory() + "/WhatsApp Documents"
    }

    fun getTelegramDirectory() : String? {
        return Environment.getExternalStorageDirectory().absolutePath + "/Telegram"
    }

    fun getTelegramImagesDirectory() : String? {
        return getTelegramDirectory() + "/Telegram Images"
    }

    fun getTelegramVideosDirectory() : String? {
        return getTelegramDirectory() + "/Telegram Video"
    }

    fun getTelegramAudioDirectory() : String? {
        return getTelegramDirectory() + "/Telegram Audio"
    }

    fun getTelegramDocsDirectory() : String? {
        return getTelegramDirectory() + "/Telegram Documents"
    }

}