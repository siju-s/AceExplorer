package com.siju.acexplorer.search.helper

import android.os.Environment
import com.siju.acexplorer.AceApplication
import java.io.File

private const val WHATSAPP_PACKAGE_NAME = "com.whatsapp"

object SearchUtils {

    fun getCameraDirectory(): String {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/Camera"
    }

    fun getScreenshotDirectory(): String {
        val screenShotPath1 =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/Screenshots"
        val screenShotPath2 =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/Screenshots"

        return when {
            File(screenShotPath1).exists() -> {
                screenShotPath1
            }
            File(screenShotPath2).exists() -> {
                screenShotPath2
            }
            else                           -> screenShotPath1
        }
    }

    fun getWhatsappDirectory(): String {
        val whatsappLegacyPath = Environment.getExternalStorageDirectory().absolutePath + "/WhatsApp/Media"
        if (File(whatsappLegacyPath).exists()) {
            return whatsappLegacyPath
        }
        val mediaDirs = AceApplication.appContext.externalMediaDirs
        if (mediaDirs != null) {
            val path = mediaDirs[0].absolutePath
            val genericMediaDir = path.substring(0, path.lastIndexOf("/"))
            val whatsappPath = File(genericMediaDir, WHATSAPP_PACKAGE_NAME)
            if (whatsappPath.exists()) {
                return whatsappPath.absolutePath + "/WhatsApp/Media"
            }
        }
        return Environment.getExternalStorageDirectory().absolutePath
    }

    fun getWhatsappImagesDirectory(): String {
        return getWhatsappDirectory() + "/WhatsApp Images"
    }

    fun getWhatsappVideosDirectory(): String {
        return getWhatsappDirectory() + "/WhatsApp Video"
    }

    fun getWhatsappAudioDirectory(): String {
        return getWhatsappDirectory() + "/WhatsApp Audio"
    }

    fun getWhatsappDocDirectory(): String {
        return getWhatsappDirectory() + "/WhatsApp Documents"
    }

    fun getTelegramDirectory(): String {
        return Environment.getExternalStorageDirectory().absolutePath + "/Telegram"
    }

    fun getTelegramImagesDirectory(): String {
        return getTelegramDirectory() + "/Telegram Images"
    }

    fun getTelegramVideosDirectory(): String {
        return getTelegramDirectory() + "/Telegram Video"
    }

    fun getTelegramAudioDirectory(): String {
        return getTelegramDirectory() + "/Telegram Audio"
    }

    fun getTelegramDocsDirectory(): String {
        return getTelegramDirectory() + "/Telegram Documents"
    }

}