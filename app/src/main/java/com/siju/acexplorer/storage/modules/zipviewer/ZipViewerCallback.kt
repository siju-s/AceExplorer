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

package com.siju.acexplorer.storage.modules.zipviewer

import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import java.util.*


interface ZipViewerCallback {

    fun removeZipScrollPos(newPath: String)

    fun onZipModeEnd(dir: String?)

    fun calculateZipScroll(dir: String)

    fun onZipContentsLoaded(data: ArrayList<FileInfo>)

    fun openZipViewer(currentDir: String)

    fun setNavDirectory(path: String, isHomeScreenEnabled: Boolean, category: Category)

    fun addToBackStack(path: String, category: Category)

    fun removeFromBackStack()

    fun setInitialDir(path: String)
}
