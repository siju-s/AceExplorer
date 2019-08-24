package com.siju.acexplorer.search.types

import com.siju.acexplorer.main.model.groups.Category

class ChipInfo(val path : String?, val category: Category) {

    companion object {
        fun createRecentCategoryFolder(path: String? = null, category: Category) : ChipInfo {
            return ChipInfo(path, category)
        }

        fun createRecentCategory(path: String? = null, category: Category): ChipInfo {
            return ChipInfo(path, category)
        }

        fun createFolderCategory(path: String? = null, category: Category): ChipInfo {
            return ChipInfo(path, category)
        }

        fun createRecentFolder(path: String? = null, category: Category): ChipInfo {
            return ChipInfo(path, category)
        }

        fun createRecent(path: String? = null): ChipInfo {
            return ChipInfo(path, Category.RECENT)
        }

        fun createCategory(path: String? = null, category: Category): ChipInfo {
            return ChipInfo(path, category)
        }

        fun createFolder(path: String? = null): ChipInfo {
            return ChipInfo(path, Category.FILES)
        }

    }
}