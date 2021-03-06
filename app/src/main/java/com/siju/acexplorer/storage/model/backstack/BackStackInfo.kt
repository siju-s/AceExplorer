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

package com.siju.acexplorer.storage.model.backstack

import android.util.Log
import com.siju.acexplorer.logging.Logger
import com.siju.acexplorer.main.model.groups.Category
import java.util.*

private const val TAG = "BackStackInfo"
private const val INVALID_BACKSTACK_INDEX = -1

class BackStackInfo {
    private val backStack = ArrayList<BackStackModel>()

    fun addToBackStack(path: String?, category: Category) {
        Log.d(TAG,
                "addToBackStack--size=" + backStack.size + " Path=" + path + "Category=" + category)
        if (isPathNotInBackStack(category, path)) {
            backStack.add(BackStackModel(path, category))
            Log.d(TAG,
                    "addToBackStack--size=" + backStack.size + " Path=" + path + "Category=" + category)
        }
    }

    private fun isPathNotInBackStack(category: Category, path: String?) = getLastIndexCategory() != category ||
            path != getLastIndexPath()

    private fun getLastIndexPath() : String? {
        val index = getLastIndex()
        if (index >= 0) {
            return backStack[index].filePath
        }
        return null
    }

    private fun getLastIndexCategory() : Category? {
        val index = getLastIndex()
        if (index >= 0) {
            return backStack[index].category
        }
        return null
    }

    fun clearBackStack() {
        Log.d(TAG, "clearBackStack")
        backStack.clear()
    }

    fun removeEntryAtIndex(index: Int) {
        if (index == INVALID_BACKSTACK_INDEX || index >= backStack.size) {
            return
        }
        Logger.log(TAG,
                   "removeEntryAtIndex: " + index + "backstackSize:" + backStack.size + " path:" + backStack[index].filePath)
        backStack.removeAt(index)
    }

    fun removeLastEntry() {
        val index = getLastIndex()
        Log.d(TAG, "removeLastEntry--size= ${backStack.size}, index:$index")
        if (index >= 0) {
            backStack.removeAt(index)
        }
    }

    private fun getLastIndex() = backStack.size - 1

    fun hasBackStack() = backStack.size > 1

    fun getCurrentBackStack(): Pair<String?, Category>? {
        val index = getLastIndex()
        if (index >= 0) {
            val backStack = backStack[index]
            return Pair(backStack.filePath, backStack.category)
        }
        return null
    }

    fun getBackStack() = backStack
}
