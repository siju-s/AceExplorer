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

package com.siju.acexplorer.storage.view


import android.os.Bundle
import com.siju.acexplorer.main.model.groups.Category

class FileListFragment : BaseFileListFragment() {
    companion object {
        fun newInstance(path: String?, category: Category, showNavigation : Boolean = true): FileListFragment {
            val bundle = Bundle()
            bundle.apply {
                putString(KEY_PATH, path)
                putSerializable(KEY_CATEGORY, category)
                putBoolean(KEY_SHOW_NAVIGATION, showNavigation)
            }
            //        args.putBoolean(FileConstants.KEY_DUAL_ENABLED, isDualMode);
            val fileList = FileListFragment()
            fileList.arguments = bundle
            return fileList
        }
    }
}
