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
import android.net.Uri
import androidx.core.content.FileProvider
import com.siju.acexplorer.BuildConfig
import com.siju.acexplorer.R
import com.siju.acexplorer.main.model.helper.IntentResolver.canHandleIntent
import java.io.File

object UriHelper {

    fun createContentUri(context: Context?, path: String?): Uri? {
        if (path == null || context == null) {
            return null
        }
        return if (SdkHelper.isAtleastNougat) {
            val authority = BuildConfig.APPLICATION_ID + ".fileprovider"
            FileProvider.getUriForFile(context, authority, File(path))
        }
        else {
            Uri.fromFile(File(path))
        }
    }

    fun canGrantUriPermission(context: Context, intent: Intent): Boolean {
        return if (canHandleIntent(context, intent)) {
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            true
        }
        else {
            FileUtils.showMessage(context, context.getString(R.string.msg_error_not_supported))
            false
        }
    }
}
