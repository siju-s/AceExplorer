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

import android.content.Intent
import android.net.Uri
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.view.dialog.DialogHelper
import com.siju.acexplorer.storage.model.operations.Operations
import java.util.*

/**
 * Created by Siju on 04 September,2017
 */
interface StorageModel {

//    val userPrefs: Bundle
//
//    val sortMode: Int
//
//
//    fun setListener(listener: Listener)
//
//    fun startPasteOperation(currentDir: String, isMove: Boolean, rooted: Boolean, info: ArrayList<FileInfo>)
//
//
//    fun handleSAFResult(operationIntent: Intent, treeUri: Uri, rooted: Boolean, flags: Int)
//
//    fun saveOldSAFUri(path: String)
//
//    fun createDir(currentDir: String, name: String, rooted: Boolean)
//
//    fun createFile(currentDir: String, name: String, rooted: Boolean)
//
//    fun deleteFiles(filesToDelete: ArrayList<FileInfo>)
//
//    fun onExtractPositiveClick(currentFilePath: String, newFileName: String, isChecked: Boolean,
//                               selectedPath: String)
//
//    fun hideUnHideFiles(infoList: ArrayList<FileInfo>, pos: ArrayList<Int>)
//
//    fun getFilePermissions(filePath: String, directory: Boolean)
//
//    fun persistSortMode(position: Int)
//
//    fun persistTrashState(value: Boolean)
//
//    fun onCompressFile(newFilePath: String, paths: ArrayList<FileInfo>)
//
//    fun setPermissions(path: String, isDir: Boolean, permissions: String)
//
//    fun saveSettingsOnExit(gridCols: Int, viewMode: Int)
//
//    fun updateFavorites(favInfoArrayList: ArrayList<FavInfo>)
//
//    fun renameFile(filePath: String, newFilePath: String, name: String, rooted: Boolean)
//
//    fun moveToTrash(filesToDelete: ArrayList<FileInfo>, trashDir: String)

    fun loadData(path: String?, category: Category) : ArrayList<FileInfo>
    fun getViewMode(): ViewMode
    fun saveViewMode(viewMode: ViewMode?)
    fun shouldShowHiddenFiles() : Boolean


    interface Listener {

        fun showSAFDialog(path: String, data: Intent)

        fun onFileExists(operation: Operations, msg: String)

        fun showConflictDialog(conflictFiles: List<FileInfo>,
                               destFiles: List<FileInfo>, destinationDir: String, isMove: Boolean,
                               pasteConflictListener: DialogHelper.PasteConflictListener)

        fun onLowSpace()

        fun showPasteProgressDialog(destinationDir: String, files: List<FileInfo>, copyData: List<CopyData>, isMove: Boolean)

        fun onOperationFailed(operation: Operations)

        fun onInvalidName(extract: Operations)

        fun dismissDialog(operation: Operations)

        fun onPermissionsFetched(permissionList: ArrayList<Array<Boolean>>)

        fun onPermissionSetError()

        fun onPermissionsSet()

        fun showExtractDialog(intent: Intent)

        fun onFavExists()

        fun onFavAdded(count: Int)

        fun showZipProgressDialog(files: ArrayList<FileInfo>, absolutePath: String)
    }

    fun saveHiddenFileSetting(value: Boolean)
    fun getSortMode(): SortMode
    fun saveSortMode(sortMode: SortMode)
    fun renameFile(operation: Operations, filePath: String, newName: String)
    fun handleSafResult(uri: Uri, flags: Int)
}
