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
import androidx.loader.content.Loader
import com.siju.acexplorer.appmanager.model.AppLoader
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.main.model.data.DataFetcher
import com.siju.acexplorer.main.model.data.DataLoader
import com.siju.acexplorer.main.model.data.MainLoader
import com.siju.acexplorer.main.model.groups.Category
import java.util.*

/**
 * Created by Siju on 03 September,2017
 */
object DataLoaderFactory {

    fun createLoader(category: Category, showOnlyCount: Boolean): Loader<ArrayList<FileInfo>> {
        var path: String? = null
        when (category) {
            Category.DOWNLOADS -> path = StorageUtils.downloadsDirectory
            else -> {

            }
        }

        return MainLoader(context, path, category, showOnlyCount)
    }

    fun createLoader(path: String, category: Category, isPicker: Boolean, id: Long): Loader<ArrayList<FileInfo>>? {
        if (context == null) {
            return null
        }
        return if (Category.APP_MANAGER == category) {
            AppLoader(context)
        } else MainLoader(context, path, category, isPicker, id, false)
    }

    fun fetchDataCount(context: Context, category: Category, dataFetcher: DataFetcher): ArrayList<FileInfo> {
        when {
//            isFilesCategory(category) -> return FileDataFetcher
//                    .fetchFiles(currentDir, sortMode, showHidden, isRingtonePicker, RootUtils.isRooted(context))
//            isMusicCategory(category) -> return MusicDataFetcher.fetchMusic(context, category, id, sortMode, showOnlyCount, showHidden)
//            isVideoCategory(category) -> return VideoFetcher.queryVideos(context, category, id, sortMode, showOnlyCount, showHidden)
//            isImagesCategory(category) -> return ImageDataFetcher.fetchImages(context, category, id, sortMode, showOnlyCount, showHidden)
//            isDocCategory(category) -> return DocumentDataFetcher.fetchDocumentsByCategory(context, category, showOnlyCount, sortMode, showHidden)
//            else -> {
//                when (category) {
//                    Category.FAVORITES -> return FileDataFetcher.fetchFavorites(context, category, sortMode, showOnlyCount)
//                    Category.GIF -> return ImageDataFetcher.fetchGif(context, category, sortMode, showOnlyCount, showHidden)
//                    Category.RECENT -> return RecentDataFetcher.fetchRecent(context, category, showOnlyCount, showHidden)
//                    Category.RECENT_IMAGES -> return RecentDataFetcher.fetchRecentImages(context, category, showHidden)
//                    Category.RECENT_AUDIO -> return RecentDataFetcher.fetchRecentAudio(context, category, showHidden)
//                    Category.RECENT_VIDEOS -> return RecentDataFetcher.fetchRecentVideos(context, category, showHidden)
//                    Category.RECENT_DOCS -> return RecentDataFetcher.fetchRecentDocs(context, category, showHidden)
//                    Category.RECENT_APPS -> return RecentDataFetcher.fetchRecentApps(context, category, showHidden)
//                    Category.APPS -> return AppDataFetcher.fetchApk(context, category, sortMode, showOnlyCount, showHidden)
//                }

        }
        return DataLoader.fetchDataByCategory(context, dataFetcher, category, showOnlyCount = true)
    }
}
