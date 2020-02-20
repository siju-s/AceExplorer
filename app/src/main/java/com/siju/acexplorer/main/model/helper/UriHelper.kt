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

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.BaseColumns
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.siju.acexplorer.R
import com.siju.acexplorer.main.model.helper.IntentResolver.canHandleIntent
import java.io.File

object UriHelper {

    fun createContentUri(context: Context?, path: String?): Uri? {
        if (path == null || context == null) {
            return null
        }
        return if (SdkHelper.isAtleastNougat) {
            val authority = context.packageName + ".fileprovider"
            FileProvider.getUriForFile(context, authority, File(path))
        }
        else {
            Uri.fromFile(File(path))
        }
    }

    fun canGrantUriPermission(context: Context, intent: Intent): Boolean {
        return if (canHandleIntent(context, intent)) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            true
        }
        else {
            FileUtils.showMessage(context, context.getString(R.string.msg_error_not_supported))
            false
        }
    }

    @SuppressLint("Recycle")
    fun getUriFromFile(path: String, context: Context): Uri? {
        val resolver = context.contentResolver
        val cursor = resolver.query(MediaStore.Files.getContentUri("external"),
                                        arrayOf(BaseColumns._ID),
                                        MediaStore.MediaColumns.DATA + " = ?",
                                        arrayOf(path), MediaStore.MediaColumns.DATE_ADDED + " desc")
                ?: return null

        cursor.moveToFirst()
        return if (cursor.isAfterLast) {
            cursor.close()
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DATA, path)
            resolver.insert(MediaStore.Files.getContentUri("external"), values)
        }
        else {
            val imageId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID))
            cursor.close()
            val uri = MediaStore.Files.getContentUri("external").buildUpon().appendPath(
                    imageId.toString()).build()
            uri
        }
    }

}
