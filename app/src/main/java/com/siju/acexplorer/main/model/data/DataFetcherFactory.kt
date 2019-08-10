package com.siju.acexplorer.main.model.data

import com.siju.acexplorer.main.model.data.doc.CompressedFileFetcher
import com.siju.acexplorer.main.model.data.doc.DocumentFetcher
import com.siju.acexplorer.main.model.data.doc.LargeFilesFetcher
import com.siju.acexplorer.main.model.data.doc.PdfFetcher
import com.siju.acexplorer.main.model.data.image.ImageDataFetcher
import com.siju.acexplorer.main.model.data.image.ImageDetailFetcher
import com.siju.acexplorer.main.model.data.music.*
import com.siju.acexplorer.main.model.data.recent.*
import com.siju.acexplorer.main.model.data.video.VideoDetailFetcher
import com.siju.acexplorer.main.model.data.video.VideoFetcher
import com.siju.acexplorer.main.model.groups.Category

object DataFetcherFactory {

    fun createDataFetcher(category: Category): DataFetcher {
        when (category) {
            Category.FILES, Category.DOWNLOADS -> return FileDataFetcher()
            Category.FAVORITES -> return FavoriteDataFetcher()

            Category.ALBUMS -> return AlbumDataFetcher()
            Category.ARTISTS -> return ArtistDataFetcher()
            Category.GENRES -> return GenreDataFetcher()
            Category.PODCASTS -> return PodcastDataFetcher()
            Category.ALL_TRACKS, Category.AUDIO -> return TracksDataFetcher()
            Category.GENERIC_MUSIC -> return GenericMusicFetcher()
            Category.ALBUM_DETAIL -> return AlbumDetailDataFetcher()
            Category.ARTIST_DETAIL -> return ArtistDetailDataFetcher()
            Category.GENRE_DETAIL -> return GenreDetailFetcher()

            Category.RECENT -> return RecentFetcher()
            Category.RECENT_IMAGES -> return RecentImageFetcher()
            Category.RECENT_AUDIO -> return RecentAudioFetcher()
            Category.RECENT_VIDEOS -> return RecentVideoFetcher()
            Category.RECENT_DOCS -> return RecentDocFetcher()
            Category.RECENT_APPS -> return RecentAppFetcher()
            Category.APPS -> return AppDataFetcher()

            Category.VIDEO, Category.GENERIC_VIDEOS -> return VideoFetcher()
            Category.FOLDER_VIDEOS -> return VideoDetailFetcher()

            Category.IMAGE, Category.GENERIC_IMAGES -> return ImageDataFetcher()
            Category.FOLDER_IMAGES -> return ImageDetailFetcher()

            Category.DOCS -> return DocumentFetcher()
            Category.COMPRESSED -> return CompressedFileFetcher()
            Category.PDF -> return PdfFetcher()
            Category.LARGE_FILES -> return LargeFilesFetcher()

            Category.ZIP_VIEWER -> TODO()
            Category.GENERIC_LIST -> TODO()
            Category.PICKER -> TODO()
            Category.APP_MANAGER -> return AppManagerDataFetcher()
//            Category.TRASH -> TODO()
        }
    }

    private fun isFilesCategory(category: Category): Boolean {
        return category == Category.FILES || category == Category.DOWNLOADS
    }

    private fun isMusicCategory(category: Category): Boolean {
        return (category == Category.AUDIO || category == Category.GENERIC_MUSIC || category == Category.ALBUMS
                || category == Category.ARTISTS || category == Category.GENRES || category == Category.PODCASTS ||
                category == Category.ALBUM_DETAIL || category == Category.ARTIST_DETAIL || category == Category.GENRE_DETAIL ||
                category == Category.ALL_TRACKS)
    }

    private fun isVideoCategory(category: Category): Boolean {
        return category == Category.VIDEO || category == Category.GENERIC_VIDEOS || category == Category.FOLDER_VIDEOS
    }

    private fun isImagesCategory(category: Category): Boolean {
        return category == Category.IMAGE || category == Category.GENERIC_IMAGES || category == Category.FOLDER_IMAGES
    }

    private fun isDocCategory(category: Category): Boolean {
        return category == Category.DOCS || category == Category.COMPRESSED || category == Category.PDF ||
                category == Category.LARGE_FILES
    }
}