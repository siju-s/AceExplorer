package com.siju.acexplorer.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.siju.acexplorer.R

object Clipboard {
    fun copyTextToClipBoard(context: Context?, text: String?) {
        context ?: return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText(context.getString(R.string.app_name), text)
        clipboard?.setPrimaryClip(clip)
    }
}