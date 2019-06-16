package com.siju.acexplorer.main.model.data.music

import android.content.Context
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.groups.Category
import java.util.ArrayList

class GenreDetailFetcher : DataFetcher {
    override fun fetchData(context: Context, path: String?, category: Category): ArrayList<FileInfo> {

    }

    override fun fetchCount(context: Context, path: String?): Int {

    }
}