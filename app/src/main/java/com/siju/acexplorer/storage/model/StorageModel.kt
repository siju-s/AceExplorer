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

package com.siju.acexplorer.storage.model

import android.net.Uri
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.storage.model.operations.OperationHelper
import com.siju.acexplorer.storage.model.operations.Operations

/**
 * Created by Siju on 04 September,2017
 */
interface StorageModel {

    fun loadData(path: String?, category: Category) : ArrayList<FileInfo>
    fun getViewMode(): ViewMode
    fun saveViewMode(viewMode: ViewMode?)
    fun shouldShowHiddenFiles() : Boolean
    fun onExit()

    fun saveHiddenFileSetting(value: Boolean)
    fun getSortMode(): SortMode
    fun saveSortMode(sortMode: SortMode)
    fun renameFile(operation: Operations, filePath: String, newName: String)
    fun handleSafResult(uri: Uri, flags: Int)
    fun createFolder(operation: Operations, path: String, name: String)
    fun createFile(operation: Operations, path: String, name: String)
    fun deleteFiles(operation: Operations, files: ArrayList<String>)
    fun checkPasteWriteMode(destinationDir: String,
                            files: ArrayList<FileInfo>,
                            pasteActionInfo: ArrayList<PasteActionInfo>,
                            operations: Operations,
                            pasteOperationCallback: OperationHelper.PasteOperationCallback)

    fun extractFile(sourceFilePath : String, destinationDir: String, newName: String, zipOperationCallback: OperationHelper.ZipOperationCallback)

    fun compressFile(destinationDir: String, filesToArchive: ArrayList<FileInfo>,
                     zipOperationCallback: OperationHelper.ZipOperationCallback)

    fun addToFavorite(favList: ArrayList<String>)
    fun deleteFavorite(favPathList: java.util.ArrayList<String>)
    fun isDualModeEnabled(): Boolean
    fun setPermissions(path: String, permissions: String, dir: Boolean) {

    }

    fun onPause()
    fun onResume()
    fun loadRecentData(path: String?, category: Category): ArrayList<RecentTimeData.RecentDataItem>
    fun getImageViewMode(): ViewMode
    fun saveImageViewMode(viewMode: ViewMode?)
    fun getVideoViewMode(): ViewMode
    fun saveVideoViewMode(viewMode: ViewMode?)
}
