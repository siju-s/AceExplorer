package com.siju.acexplorer.extensions

import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.util.*

fun File.getMimeType(fallback: String = "*/*"): String {
    val extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(this).toString())
            ?: return fallback
    val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(extension.toLowerCase(Locale.ROOT))
    return mimeType ?: return fallback

}