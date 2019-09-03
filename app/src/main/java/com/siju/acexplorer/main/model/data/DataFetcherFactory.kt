package com.siju.acexplorer.main.model.data

import com.siju.acexplorer.main.model.data.camera.CameraGenericFetcher
import com.siju.acexplorer.main.model.data.doc.CompressedFileFetcher
import com.siju.acexplorer.main.model.data.doc.DocumentFetcher
import com.siju.acexplorer.main.model.data.doc.OtherDocFilesFetcher
import com.siju.acexplorer.main.model.data.doc.PdfFetcher
import com.siju.acexplorer.main.model.data.doc.largefiles.*
import com.siju.acexplorer.main.model.data.folder.FolderAudioFetcher
import com.siju.acexplorer.main.model.data.folder.FolderDocFetcher
import com.siju.acexplorer.main.model.data.folder.FolderImageFetcher
import com.siju.acexplorer.main.model.data.folder.FolderVideoFetcher
import com.siju.acexplorer.main.model.data.image.ImageAllFetcher
import com.siju.acexplorer.main.model.data.image.ImageDataFetcher
import com.siju.acexplorer.main.model.data.image.ImageDetailFetcher
import com.siju.acexplorer.main.model.data.music.*
import com.siju.acexplorer.main.model.data.recent.*
import com.siju.acexplorer.main.model.data.video.VideoAllFetcher
import com.siju.acexplorer.main.model.data.video.VideoDetailFetcher
import com.siju.acexplorer.main.model.data.video.VideoFetcher
import com.siju.acexplorer.main.model.groups.Category

object DataFetcherFactory {

    fun createDataFetcher(category: Category): DataFetcher {
        when (category) {
            Category.FILES, Category.DOWNLOADS, Category.CAMERA, Category.SCREENSHOT, Category.WHATSAPP, Category.TELEGRAM -> return FileDataFetcher()
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

            Category.RECENT, Category.RECENT_ALL -> return RecentFetcher()
            Category.RECENT_IMAGES -> return RecentImageFetcher()
            Category.RECENT_AUDIO -> return RecentAudioFetcher()
            Category.RECENT_VIDEOS -> return RecentVideoFetcher()
            Category.RECENT_DOCS -> return RecentDocFetcher()
            Category.RECENT_APPS -> return RecentAppFetcher()
            Category.RECENT_FOLDER -> return RecentFolderFetcher()

            Category.RECENT_IMAGES_FOLDER -> return RecentFolderImageFetcher()
            Category.RECENT_VIDEOS_FOLDER -> return RecentFolderVideoFetcher()
            Category.RECENT_AUDIO_FOLDER -> return RecentFolderAudioFetcher()
            Category.RECENT_DOC_FOLDER -> return RecentFolderDocFetcher()

            Category.SEARCH_FOLDER_DOCS -> return FolderDocFetcher()
            Category.SEARCH_FOLDER_IMAGES -> return FolderImageFetcher()
            Category.SEARCH_FOLDER_VIDEOS -> return FolderVideoFetcher()
            Category.SEARCH_FOLDER_AUDIO -> return FolderAudioFetcher()

            Category.APPS -> return AppDataFetcher()

            Category.VIDEO, Category.GENERIC_VIDEOS -> return VideoFetcher()
            Category.FOLDER_VIDEOS -> return VideoDetailFetcher()
            Category.VIDEO_ALL -> return VideoAllFetcher()

            Category.IMAGE, Category.GENERIC_IMAGES -> return ImageDataFetcher()
            Category.FOLDER_IMAGES -> return ImageDetailFetcher()
            Category.IMAGES_ALL -> return ImageAllFetcher()

            Category.DOCS -> return DocumentFetcher()
            Category.COMPRESSED -> return CompressedFileFetcher()
            Category.PDF -> return PdfFetcher()
            Category.DOCS_OTHER -> return OtherDocFilesFetcher()

            Category.LARGE_FILES, Category.LARGE_FILES_ALL -> return LargeFilesFetcher()
            Category.LARGE_FILES_AUDIO -> return LargeAudioFilesFetcher()
            Category.LARGE_FILES_VIDEOS -> return LargeVideoFilesFetcher()
            Category.LARGE_FILES_IMAGES -> return LargeImageFilesFetcher()
            Category.LARGE_FILES_DOC -> return LargeDocFilesFetcher()
            Category.LARGE_FILES_COMPRESSED -> return LargeCompressedFilesFetcher()
            Category.LARGE_FILES_APP -> return LargeAppFilesFetcher()
            Category.LARGE_FILES_OTHER -> return LargeOtherFilesFetcher()

            Category.CAMERA_GENERIC -> return CameraGenericFetcher()
            Category.CAMERA_IMAGES -> return FolderImageFetcher()
            Category.CAMERA_VIDEO -> return FolderVideoFetcher()

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