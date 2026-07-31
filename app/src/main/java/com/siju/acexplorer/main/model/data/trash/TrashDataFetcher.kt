package com.siju.acexplorer.main.model.data.trash

import android.content.Context
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.storage.model.trash.TrashDatabase
import kotlinx.coroutines.runBlocking

/**
 * Supplies the item count for the recycle bin's home screen tile.
 *
 * Only [fetchCount] is meaningful. Trashed items are never listed through the normal file list,
 * because the operations it offers - rename, copy, compress - do not apply to something waiting to
 * be restored or destroyed. Tapping the tile opens the dedicated recycle bin screen instead.
 */
class TrashDataFetcher : DataFetcher {

    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {
        return arrayListOf()
    }

    override fun fetchCount(context: Context, path: String?): Int {
        return runBlocking {
            TrashDatabase.getInstance(context).trashDao().getAll().size
        }
    }
}
