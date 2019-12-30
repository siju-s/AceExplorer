/*
 * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.siju.acexplorer.main.model.helper

import android.content.Context
import android.content.Intent
import android.util.Log
import android.webkit.MimeTypeMap.getSingleton
import com.siju.acexplorer.R
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.imageviewer.ImageViewerActivity
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.view.dialog.DialogHelper
import java.util.*
import kotlin.collections.ArrayList


object ViewHelper {

    const val EXT_APK = "apk"

    fun viewFile(context: Context, path: String, extension: String) {
        val uri = UriHelper.createContentUri(context, path) ?: return
        val ext = extension.toLowerCase(Locale.ROOT)
        val mimeType = getSingleton().getMimeTypeFromExtension(ext)

        if (mimeType == null) {
            Analytics.getLogger().openAsDialogShown()
            DialogHelper.openWith(uri, context)
        }
        else {
            Analytics.getLogger().openFile()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, mimeType)
            val granted = UriHelper.canGrantUriPermission(context, intent)
            if (granted) {
                context.startActivity(intent)
            }
        }
    }

    fun viewApkFile(context: Context, path: String,
                    apkDialogListener: DialogHelper.ApkDialogListener) {
        val texts = arrayOf(context.getString(R.string.package_installer),
                            context.getString(R.string.package_installer_content),
                            context.getString(R.string.install),
                            context.getString(R.string.dialog_cancel),
                            context.getString(R.string.view))
        DialogHelper.showApkDialog(context, texts, path, apkDialogListener)
    }

    fun openImage(context: Context?, data: ArrayList<FileInfo>, pos: Int) {
        if (context == null) {
            return
        }
        val path = data[pos].filePath
        Log.e("ViewHelper", "path:$path, pos: $pos, size : ${data.size}")
        val newList = ArrayList(data.filter { it.category == Category.IMAGE })
        var newPos = 0
        for ((index, item) in newList.withIndex()) {
            if (path == item.filePath) {
                newPos = index
                break
            }
        }
        Log.e("ViewHelper", "newpos: $newPos, size : ${newList.size}, newPath:${newList[newPos].filePath}")
        ImageViewerActivity.createImageViewer(context, newList, newPos)
    }

}
