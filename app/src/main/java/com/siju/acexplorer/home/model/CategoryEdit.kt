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

package com.siju.acexplorer.home.model

import android.content.Context
import com.siju.acexplorer.R
import com.siju.acexplorer.home.edit.model.CategoryEditType
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.search.helper.SearchUtils

class CategoryEdit(var isHeader: Boolean, val categoryId: Int, var headerType: CategoryEditType, var checked: Boolean = false,
                   var path: String? = null) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CategoryEdit

        if (categoryId != other.categoryId) return false

        if (categoryId == Category.FILES.value && other.categoryId == Category.FILES.value) {
            return path == other.path && headerType == other.headerType
        }
        return headerType == other.headerType
    }

    override fun hashCode(): Int {
        return categoryId
    }

    companion object {
        fun getCategoryName(context: Context, categoryId: Int, path: String?): String {
            when (categoryId) {
                Category.FILES.value -> {
                    when (path) {
                        SearchUtils.getCameraDirectory() -> {
                            return context.getString(R.string.category_camera)
                        }
                        SearchUtils.getScreenshotDirectory() -> {
                            return context.getString(R.string.category_screenshot)
                        }
                        SearchUtils.getTelegramDirectory() -> {
                            return context.getString(R.string.category_telegram)
                        }
                        SearchUtils.getWhatsappDirectory() -> {
                            return context.getString(R.string.category_whatsapp)
                        }
                    }
                }
                else -> {
                    return CategoryHelper.getCategoryName(context, Category.valueOf(categoryId))
                }
            }
            return ""
        }

        fun getResourceIdForCategory(categoryId: Int, path: String?): Int {
            when (categoryId) {
                Category.FILES.value -> {
                    when (path) {
                        SearchUtils.getCameraDirectory() -> {
                            return R.drawable.ic_camera
                        }
                        SearchUtils.getScreenshotDirectory() -> {
                            return R.drawable.ic_screenshot
                        }
                        SearchUtils.getTelegramDirectory() -> {
                            return R.drawable.ic_telegram
                        }
                        SearchUtils.getWhatsappDirectory() -> {
                            return R.drawable.ic_whatsapp
                        }
                    }
                }
                else -> {
                    return CategoryHelper.getResourceIdForCategory(Category.valueOf(categoryId))
                }
            }
            return 0
        }

    }
}
