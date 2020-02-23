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

import android.annotation.SuppressLint
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.siju.acexplorer.R
import com.siju.acexplorer.main.model.groups.Category

internal object EnhancedMenuInflater {
    @SuppressLint("AlwaysShowAction")
    fun inflate(inflater: MenuInflater?, menu: Menu, category: Category) {
        inflater ?: return
        inflater.inflate(R.menu.action_mode_bottom, menu)
        if (category != Category.FILES) {
            menu.findItem(R.id.action_cut).isVisible = false
            menu.findItem(R.id.action_copy).isVisible = false
            if (category == Category.FAVORITES) {
                menu.findItem(R.id.action_share).isVisible = false
            }
        }
        val size = menu.size()
        for (i in 0 until size) {
            val item = menu.getItem(i)
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS) // Required to show more icons in bottom menu
        }
    }
}