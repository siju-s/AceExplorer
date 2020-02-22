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
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

object AppUtils {
    fun getAppInfo(context: Context, path: String?): ApplicationInfo? {
        if (path == null) {
            return null
        }
        val appInfo: ApplicationInfo
        try {
            val packageInfo = context.packageManager.getPackageArchiveInfo(path,
                    PackageManager.GET_ACTIVITIES)
                    ?: return null
            appInfo = packageInfo.applicationInfo
            appInfo.sourceDir = path
            appInfo.publicSourceDir = path
        } catch (e: Exception) {
            return null
        }
        return appInfo
    }

    fun getAppIconForFolder(context: Context, packageName: String?): Drawable? {
        if (packageName == null) {
            return null
        }
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
}