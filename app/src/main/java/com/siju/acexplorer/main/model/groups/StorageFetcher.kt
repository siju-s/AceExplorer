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

package com.siju.acexplorer.main.model.groups

import android.content.Context
import android.os.Environment.getRootDirectory
import com.siju.acexplorer.R
import com.siju.acexplorer.main.model.StorageItem
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.main.model.StorageUtils.StorageType.EXTERNAL
import com.siju.acexplorer.main.model.StorageUtils.getSpaceLeft
import com.siju.acexplorer.main.model.StorageUtils.getTotalSpace
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.StorageHelper.getStorageProperties
import com.siju.acexplorer.main.model.root.RootUtils
import java.io.File


const val STORAGE_SDCARD1 = "/storage/sdcard1"

class StorageFetcher(private val context: Context) {

    val externalSDList = ArrayList<String>()

    fun getStorageList(): ArrayList<StorageItem> {
        val storagePaths = StorageUtils.storageDirectories
        return populateStorageList(storagePaths)
    }

    private fun populateStorageList(storagePaths: List<String>): ArrayList<StorageItem> {
        var storageType: StorageUtils.StorageType
        val storageList = ArrayList<StorageItem>()
        for (path in storagePaths) {
            val file = File(path)
            val triple = getStorageProperties(path, file)
            val icon = triple.first
            val name = triple.second
            storageType = triple.third

            if (isExternalStorageType(storageType)) {
                externalSDList.add(path)
            }

            if (isValidStoragePath(file)) {
                val spaceLeft = getSpaceLeft(file)
                val totalSpace = getTotalSpace(file)
                val leftProgress = (spaceLeft.toFloat() / totalSpace * 100).toInt()
                val progress = 100 - leftProgress
                val spaceText = formatStorageSpace(spaceLeft, totalSpace)
                addToStorageList(storageList, StorageItem(name, spaceText, icon, path, progress, Category.FILES, storageType))
            }
        }
        if (RootUtils.isRooted(context) && RootUtils.hasRootAccess()) {
            addRootDir(storageList)
        }
        return storageList
    }

    private fun addRootDir(storageList: ArrayList<StorageItem>) {
        val systemDir = getRootDirectory()
        val rootDir = systemDir.parentFile

        val spaceLeftRoot = getSpaceLeft(systemDir)
        val totalSpaceRoot = getTotalSpace(systemDir)
        val leftProgressRoot = (spaceLeftRoot.toFloat() / totalSpaceRoot * 100).toInt()
        val progressRoot = 100 - leftProgressRoot
        addToStorageList(storageList, StorageItem(context.getString(R.string.nav_menu_root), formatStorageSpace(spaceLeftRoot, totalSpaceRoot), R.drawable
                .ic_root_white, FileUtils.getAbsolutePath(rootDir), progressRoot, Category.FILES, StorageUtils.StorageType.ROOT))
    }

    private fun isExternalStorageType(storageType: StorageUtils.StorageType) = storageType == EXTERNAL

    private fun isValidStoragePath(file: File) = file.isFile || file.canExecute()

    private fun addToStorageList(storageList: ArrayList<StorageItem>, storageItem: StorageItem) {
        storageList.add(storageItem)
    }

    private fun formatStorageSpace(spaceLeft: Long, totalSpace: Long): String {
        val freePlaceholder = "/" //" " + context.getResources().getString(R.string.msg_free) + " ";
        return FileUtils.formatSize(context, spaceLeft) + freePlaceholder +
                FileUtils.formatSize(context, totalSpace)
    }

}
