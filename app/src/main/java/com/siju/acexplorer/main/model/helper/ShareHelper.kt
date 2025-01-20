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

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.common.extensions.canHandleIntent
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.main.model.groups.CategoryHelper.checkIfFileCategory
import java.util.*


object ShareHelper {

    fun shareFiles(context: Context, filesList: ArrayList<FileInfo>, category: Category) {
        val intent = Intent()
        val fileCount = filesList.size
        if (filesList.isEmpty()) {
            return
        }
        if (fileCount == 1) {
            intent.action = Intent.ACTION_SEND
        }
        else {
            intent.action = Intent.ACTION_SEND_MULTIPLE
        }
        if (checkIfFileCategory(category)) {
            intent.type = "*/*"
        }
        else {
            val extension = filesList[0].extension
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            intent.type = mimeType ?: "*/*"
        }

        if (fileCount == 1) {
            val uri = getUri(context, filesList[0])
            intent.putExtra(Intent.EXTRA_STREAM, uri)
        }
        else {
            val files = ArrayList<Uri>()
            for (info in filesList) {
                val uri = getUri(context, info)
                uri?.let { files.add(it) }
            }
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
        }

        if (context.canHandleIntent(intent)) {
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.action_share)))
        }
    }

    private fun getUri(context: Context, info: FileInfo): Uri? {
        return UriHelper.createContentUri(context, info.filePath)
    }

    fun shareMedia(context: Context, category: Category?, uri: Uri?, path: String? = null) {
        if (uri == null && path == null) {
            return
        }
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        if (CategoryHelper.isAnyVideoCategory(category)) {
            intent.type = "video/*"
        }
        else if (category == Category.AUDIO || CategoryHelper.isMusicCategory(category)) {
            intent.type = "audio/*"
        }
        else {
            intent.type = "image/*"
        }
        if (uri == null) {
            val newUri = UriHelper.createContentUri(context, path)
            intent.putExtra(Intent.EXTRA_STREAM, newUri)
            intent.clipData = ClipData.newUri(context.contentResolver, null, newUri)
        }
        else {
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.clipData = ClipData.newUri(context.contentResolver, null, uri)
        }
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK

        if (context.canHandleIntent(intent)) {
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.action_share)))
        }
    }
}
