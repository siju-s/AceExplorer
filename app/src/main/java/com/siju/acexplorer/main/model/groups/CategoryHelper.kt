package com.siju.acexplorer.main.model.groups


import android.content.Context
import android.util.Log
import android.webkit.MimeTypeMap
import com.siju.acexplorer.R
import com.siju.acexplorer.main.model.FileConstants.EXT_TAR
import com.siju.acexplorer.main.model.FileConstants.EXT_ZIP
import com.siju.acexplorer.main.model.data.doc.DocumentUtils
import com.siju.acexplorer.main.model.groups.Category.*
import java.util.*

private const val MIME_TYPE_APK = "application/vnd.android.package-archive"

object CategoryHelper {

    fun checkIfLibraryCategory(category: Category): Boolean {
        return category != FILES && category != DOWNLOADS
    }

    fun checkIfFileCategory(category: Category): Boolean {
        return category == FILES ||
                category == COMPRESSED ||
                category == DOWNLOADS ||
                category == FAVORITES ||
                category == LARGE_FILES
    }

    fun isDateInMs(category: Category): Boolean {
        return category == FILES ||
                category == DOWNLOADS ||
                category == FAVORITES
    }

    fun isGenericMusic(category: Category): Boolean {
        return category == GENERIC_MUSIC
    }

    fun isMusicCategory(category: Category): Boolean {
        return category == ALBUMS ||
                category == ARTISTS ||
                category == GENRES
    }

    fun checkIfAnyMusicCategory(category: Category): Boolean {
        return isGenericMusic(category) ||
                category == ALBUMS ||
                category == ARTISTS ||
                category == GENRES ||
                category == PODCASTS ||
                category == ALBUM_DETAIL ||
                category == ARTIST_DETAIL ||
                category == GENRE_DETAIL ||
                category == ALL_TRACKS
    }

    fun isRecentCategory(category: Category): Boolean {
        return RECENT_IMAGES == category || RECENT_AUDIO == category ||
                RECENT_APPS == category || RECENT_VIDEOS == category ||
                RECENT_DOCS == category
    }

    fun isAnyLargeFilesCategory(category: Category): Boolean {
        return LARGE_FILES_IMAGES == category || LARGE_FILES_AUDIO == category ||
                LARGE_FILES_VIDEOS == category || LARGE_FILES_DOC == category ||
                LARGE_FILES_APP == category || LARGE_FILES_COMPRESSED == category ||
                LARGE_FILES_OTHER == category
    }

    fun isAnyCameraCategory(category: Category) = category == CAMERA_IMAGES || category == CAMERA_VIDEO

    fun isRecentGenericCategory(category: Category): Boolean {
        return RECENT == category
    }

    fun isLargeFilesOrganisedCategory(category: Category): Boolean {
        return category == LARGE_FILES
    }

    fun isAppManager(category: Category): Boolean {
        return APP_MANAGER == category
    }

    fun isGenericImagesCategory(category: Category): Boolean {
        return GENERIC_IMAGES == category
    }

    fun isGenericVideosCategory(category: Category): Boolean {
        return GENERIC_VIDEOS == category
    }

    fun isPeekPopCategory(category: Category): Boolean {
        return category == IMAGE || category == VIDEO || category == AUDIO ||
                category == FOLDER_IMAGES || category == FOLDER_VIDEOS ||
                RECENT_IMAGES == category || GENERIC_IMAGES == category
    }

    fun isSortOrActionModeUnSupported(category: Category): Boolean {
        Log.d("CategoryHelper", "isSortOrActionModeUnSupported :$category")
        return isMusicCategory(category) || isGenericMusic(category) ||
                isGenericVideosCategory(category) ||
                isGenericImagesCategory(category) || category == LARGE_FILES ||
                category == CAMERA_GENERIC || category == RECENT
    }

    fun getCategoryForRecentFromExtension(extension: String?): Category {
        var ext = extension

        var value = RECENT_DOCS
        if (ext == null) {
            return RECENT_DOCS
        }
        ext = ext.toLowerCase(Locale.ROOT) // necessary
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        //        Log.d("CategoryHelper", "getCategoryForRecentFromExtension: ext;"+extension + " mime:"+mimeType);
        if (mimeType != null) {
            when {
                mimeType.indexOf("image") == 0 -> value = RECENT_IMAGES
                mimeType.indexOf("video") == 0 -> value = RECENT_VIDEOS
                mimeType.indexOf("audio") == 0 -> value = RECENT_AUDIO
                MIME_TYPE_APK == mimeType -> value = RECENT_APPS
            }
        }
        return value
    }

    fun getSubCategoryForRecentFromExtension(extension: String?): Category {
        var ext = extension

        var value = DOCS
        if (ext == null) {
            return DOCS
        }
        ext = ext.toLowerCase(Locale.ROOT) // necessary
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        //        Log.d("CategoryHelper", "getCategoryForRecentFromExtension: ext;"+extension + " mime:"+mimeType);
        if (mimeType != null) {
            when {
                mimeType.indexOf("image") == 0 -> value = IMAGE
                mimeType.indexOf("video") == 0 -> value = VIDEO
                mimeType.indexOf("audio") == 0 -> value = AUDIO
                MIME_TYPE_APK == mimeType -> value = APPS
            }
        }
        return value
    }

    fun getCategoryForLargeFilesFromExtension(extension: String?): Category {
        var ext = extension

        var value = LARGE_FILES_OTHER
        if (ext == null) {
            return LARGE_FILES_OTHER
        }
        ext = ext.toLowerCase(Locale.ROOT)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        //        Log.d("CategoryHelper", "getCategoryForRecentFromExtension: ext;"+extension + " mime:"+mimeType);
        if (mimeType != null) {
            if (mimeType.indexOf("image") == 0) {
                value = LARGE_FILES_IMAGES
            } else if (mimeType.indexOf("video") == 0) {
                value = LARGE_FILES_VIDEOS
            } else if (mimeType.indexOf("audio") == 0) {
                value = LARGE_FILES_AUDIO
            } else if (MIME_TYPE_APK == mimeType) {
                value = LARGE_FILES_APP
            } else if (ext.endsWith(EXT_ZIP) || ext.endsWith(EXT_TAR)) {
                value = LARGE_FILES_COMPRESSED
            } else if (DocumentUtils.isDocumentFileType(ext)) {
                value = LARGE_FILES_DOC
            }
        }
        return value
    }

    fun getSubcategoryForLargeFilesFromExtension(extension: String?): Category {
        var ext = extension

        var value = LARGE_FILES_OTHER
        if (ext == null) {
            return LARGE_FILES_OTHER
        }
        ext = ext.toLowerCase(Locale.ROOT)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        //        Log.d("CategoryHelper", "getCategoryForRecentFromExtension: ext;"+extension + " mime:"+mimeType);
        if (mimeType != null) {
            if (mimeType.indexOf("image") == 0) {
                value = IMAGE
            } else if (mimeType.indexOf("video") == 0) {
                value = VIDEO
            } else if (mimeType.indexOf("audio") == 0) {
                value = AUDIO
            } else if (MIME_TYPE_APK == mimeType) {
                value = APPS
            } else if (ext.endsWith(EXT_ZIP) || ext.endsWith(EXT_TAR)) {
                value = COMPRESSED
            } else if (DocumentUtils.isDocumentFileType(ext)) {
                value = DOCS
            }
        }
        return value
    }

    fun getCategoryName(context: Context, category: Category?): String {
        when (category) {
            RECENT_AUDIO, AUDIO -> return context.getString(R.string.audio)
            RECENT_VIDEOS, VIDEO, GENERIC_VIDEOS -> return context.getString(R.string.nav_menu_video)
            RECENT_IMAGES, IMAGE, GENERIC_IMAGES -> return context.getString(R.string.nav_menu_image)
            RECENT_DOCS, DOCS -> return context.getString(R.string.nav_menu_docs)
            DOWNLOADS -> return context.getString(R.string.downloads)
            COMPRESSED -> return context.getString(R.string.compressed)
            FAVORITES -> return context.getString(R.string.nav_header_favourites)
            PDF -> return context.getString(R.string.pdf)
            RECENT_APPS, APPS -> return context.getString(R.string.apk)
            LARGE_FILES -> return context.getString(R.string.library_large)
            RECENT -> return context.getString(R.string.library_recent)
            ALBUMS -> return context.getString(R.string.albums)
            ARTISTS -> return context.getString(R.string.artists)
            GENRES -> return context.getString(R.string.genres)
            PODCASTS -> return context.getString(R.string.podcasts)
            ALL_TRACKS -> return context.getString(R.string.all_tracks)
            APP_MANAGER -> return context.getString(R.string.app_manager)
            CAMERA -> return context.getString(R.string.category_camera)
            SCREENSHOT -> return context.getString(R.string.category_screenshot)
            WHATSAPP -> return context.getString(R.string.category_whatsapp)
            TELEGRAM -> return context.getString(R.string.category_telegram)
            LARGE_FILES_OTHER -> return context.getString(R.string.search_type_other)
            else -> return ""
        }

    }

    fun getResourceIdForCategory(category: Category?): Int {
        when (category) {
            AUDIO -> return R.drawable.ic_library_music
            VIDEO -> return R.drawable.ic_library_videos
            IMAGE -> return R.drawable.ic_library_images
            DOCS -> return R.drawable.ic_library_docs
            DOWNLOADS -> return R.drawable.ic_library_downloads
            COMPRESSED -> return R.drawable.ic_library_compressed
            FAVORITES -> return R.drawable.ic_library_favorite
            PDF -> return R.drawable.ic_library_pdf
            APPS -> return R.drawable.ic_library_apk
            LARGE_FILES -> return R.drawable.ic_library_large
            RECENT -> return R.drawable.ic_library_recents
            CAMERA -> return R.drawable.ic_camera
            SCREENSHOT -> return R.drawable.ic_screenshot
            WHATSAPP -> return R.drawable.ic_whatsapp
            TELEGRAM -> return R.drawable.ic_telegram
            else -> return 0
        }
    }
}
