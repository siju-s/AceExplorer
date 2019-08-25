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

package com.siju.acexplorer.home.types

import com.siju.acexplorer.main.model.groups.Category

data class HomeLibraryInfo(var category: Category, var categoryName: String?, var resourceId: Int, var count: Int = 0,
                           var path : String? = null) {

    companion object {
        fun createCategoryWithPath(category: Category, categoryName: String?, resourceId: Int, count: Int = 0,
                                   path : String?) : HomeLibraryInfo {
            return HomeLibraryInfo(category, categoryName, resourceId, count, path)
        }
    }
}