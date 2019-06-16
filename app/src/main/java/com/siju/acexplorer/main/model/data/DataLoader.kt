package com.siju.acexplorer.main.model.data

import android.content.Context
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import java.util.*

object DataLoader {

    fun fetchDataByCategory(context: Context, dataFetcher: DataFetcher, category: Category, currentDir: String? = null): ArrayList<FileInfo> {

            return dataFetcher.fetchData(context, currentDir, category)

//        when {
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
//
//                return ArrayList()
//            }
//        }
    }

    fun fetchDataCount(context: Context, dataFetcher: DataFetcher, path: String? = null): Int {
        return dataFetcher.fetchCount(context, path)
    }
}
