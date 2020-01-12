package com.siju.acexplorer.storage.model

import com.siju.acexplorer.main.model.groups.Category

object RecentTimeHelper {

    fun isRecentTimeLineCategory(category: Category?) : Boolean {
        return category == Category.RECENT_ALL || category == Category.RECENT_IMAGES ||
                category == Category.RECENT_AUDIO || category == Category.RECENT_VIDEOS ||
                category == Category.RECENT_DOCS
    }
}