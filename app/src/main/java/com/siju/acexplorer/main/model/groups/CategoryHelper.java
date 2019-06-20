package com.siju.acexplorer.main.model.groups;


import android.content.Context;
import android.webkit.MimeTypeMap;

import com.siju.acexplorer.R;

import static com.siju.acexplorer.main.model.groups.Category.ALBUMS;
import static com.siju.acexplorer.main.model.groups.Category.ALBUM_DETAIL;
import static com.siju.acexplorer.main.model.groups.Category.ALL_TRACKS;
import static com.siju.acexplorer.main.model.groups.Category.APPS;
import static com.siju.acexplorer.main.model.groups.Category.APP_MANAGER;
import static com.siju.acexplorer.main.model.groups.Category.ARTISTS;
import static com.siju.acexplorer.main.model.groups.Category.ARTIST_DETAIL;
import static com.siju.acexplorer.main.model.groups.Category.AUDIO;
import static com.siju.acexplorer.main.model.groups.Category.COMPRESSED;
import static com.siju.acexplorer.main.model.groups.Category.DOCS;
import static com.siju.acexplorer.main.model.groups.Category.DOWNLOADS;
import static com.siju.acexplorer.main.model.groups.Category.FAVORITES;
import static com.siju.acexplorer.main.model.groups.Category.FILES;
import static com.siju.acexplorer.main.model.groups.Category.FOLDER_IMAGES;
import static com.siju.acexplorer.main.model.groups.Category.FOLDER_VIDEOS;
import static com.siju.acexplorer.main.model.groups.Category.GENERIC_IMAGES;
import static com.siju.acexplorer.main.model.groups.Category.GENERIC_LIST;
import static com.siju.acexplorer.main.model.groups.Category.GENERIC_MUSIC;
import static com.siju.acexplorer.main.model.groups.Category.GENERIC_VIDEOS;
import static com.siju.acexplorer.main.model.groups.Category.GENRES;
import static com.siju.acexplorer.main.model.groups.Category.GENRE_DETAIL;
import static com.siju.acexplorer.main.model.groups.Category.IMAGE;
import static com.siju.acexplorer.main.model.groups.Category.LARGE_FILES;
import static com.siju.acexplorer.main.model.groups.Category.PDF;
import static com.siju.acexplorer.main.model.groups.Category.PICKER;
import static com.siju.acexplorer.main.model.groups.Category.PODCASTS;
import static com.siju.acexplorer.main.model.groups.Category.RECENT;
import static com.siju.acexplorer.main.model.groups.Category.RECENT_APPS;
import static com.siju.acexplorer.main.model.groups.Category.RECENT_AUDIO;
import static com.siju.acexplorer.main.model.groups.Category.RECENT_DOCS;
import static com.siju.acexplorer.main.model.groups.Category.RECENT_IMAGES;
import static com.siju.acexplorer.main.model.groups.Category.RECENT_VIDEOS;
import static com.siju.acexplorer.main.model.groups.Category.TRASH;
import static com.siju.acexplorer.main.model.groups.Category.VIDEO;
import static com.siju.acexplorer.main.model.groups.Category.ZIP_VIEWER;


public class CategoryHelper {

    public static final String MIME_TYPE_APK = "application/vnd.android.package-archive";

    public static Category getCategory(int position) {
        switch (position) {
            case 0:
                return FILES;
            case 1:
                return AUDIO;
            case 2:
                return VIDEO;
            case 3:
                return IMAGE;
            case 4:
                return DOCS;
            case 5:
                return DOWNLOADS;
            case 7:
                return COMPRESSED;
            case 8:
                return FAVORITES;
            case 9:
                return PDF;
            case 10:
                return APPS;
            case 11:
                return LARGE_FILES;
            case 12:
                return ZIP_VIEWER;
            case 13:
                return GENERIC_LIST;
            case 14:
                return PICKER;
//            case 15:
//                return GIF;
            case 16:
                return RECENT;
            case 33:
                return APP_MANAGER;
            case 34:
                return TRASH;
        }
        return FILES;
    }

    public static boolean checkIfFileCategory(Category category) {
        return category.equals(FILES) ||
                category.equals(COMPRESSED) ||
                category.equals(DOWNLOADS) ||
                category.equals(FAVORITES) ||
                category.equals(LARGE_FILES);
    }

    public static boolean isDateInMs(Category category) {
        return category.equals(FILES) ||
               category.equals(DOWNLOADS) ||
               category.equals(FAVORITES);
    }

    public static boolean isGenericMusic(Category category) {
        return category.equals(GENERIC_MUSIC);
    }

    public static boolean isMusicCategory(Category category) {
        return category.equals(ALBUMS) ||
                category.equals(ARTISTS) ||
                category.equals(GENRES);
    }

    public static boolean checkIfAnyMusicCategory(Category category) {
        return isGenericMusic(category) ||
                category.equals(ALBUMS) ||
                category.equals(ARTISTS) ||
                category.equals(GENRES) ||
                category.equals(PODCASTS) ||
                category.equals(ALBUM_DETAIL) ||
                category.equals(ARTIST_DETAIL) ||
                category.equals(GENRE_DETAIL) ||
                category.equals(ALL_TRACKS) ;
    }

    public static boolean isRecentCategory(Category category) {
        return RECENT_IMAGES.equals(category) || RECENT_AUDIO.equals(category) ||
               RECENT_APPS.equals(category) || RECENT_VIDEOS.equals(category) ||
               RECENT_DOCS.equals(category);
    }

    public static boolean isRecentGenericCategory(Category category) {
        return RECENT.equals(category);
    }


    public static boolean isGenericImagesCategory(Category category) {
        return GENERIC_IMAGES.equals(category) || IMAGE.equals(category);
    }

    public static boolean isGenericVideosCategory(Category category) {
        return GENERIC_VIDEOS.equals(category);
    }

    public static boolean isPeekPopCategory(Category category) {
        return category.equals(IMAGE) || category.equals(VIDEO) || category.equals(AUDIO) ||
                category.equals(FOLDER_IMAGES) || category.equals(FOLDER_VIDEOS) ||
                RECENT_IMAGES.equals(category) || GENERIC_IMAGES.equals(category);
    }

    public static boolean showLibSpecificNavigation(Category category) {
        return  checkIfAnyMusicCategory(category) ||
                category.equals(FOLDER_IMAGES) || category.equals(FOLDER_VIDEOS) ||
                isRecentCategory(category);
    }

    public static boolean isSortOrActionModeUnSupported(Category category) {
        return (isMusicCategory(category) || isGenericMusic(category) ||
                isGenericVideosCategory(category) ||
                isGenericImagesCategory(category));
    }

    public static Category getCategoryForRecentFromExtension(String extension) {

        Category value = RECENT_DOCS;
        if (extension == null) {
            return RECENT_DOCS;
        }
        extension = extension.toLowerCase(); // necessary
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
//        Log.d("CategoryHelper", "getCategoryForRecentFromExtension: ext;"+extension + " mime:"+mimeType);
        if (mimeType != null) {
            if (mimeType.indexOf("image") == 0) {
                value = RECENT_IMAGES;
            } else if (mimeType.indexOf("video") == 0) {
                value = RECENT_VIDEOS;
            } else if (mimeType.indexOf("audio") == 0) {
                value = RECENT_AUDIO;
            } else if (MIME_TYPE_APK.equals(mimeType)) {
                value = RECENT_APPS;
            }
        }
        return value;
    }

    public static Category getSubCategoryForRecentFromExtension(String extension) {

        Category value = DOCS;
        if (extension == null) {
            return DOCS;
        }
        extension = extension.toLowerCase(); // necessary
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
//        Log.d("CategoryHelper", "getCategoryForRecentFromExtension: ext;"+extension + " mime:"+mimeType);
        if (mimeType != null) {
            if (mimeType.indexOf("image") == 0) {
                value = IMAGE;
            } else if (mimeType.indexOf("video") == 0) {
                value = VIDEO;
            } else if (mimeType.indexOf("audio") == 0) {
                value = AUDIO;
            } else if (MIME_TYPE_APK.equals(mimeType)) {
                value = APPS;
            }
        }
        return value;
    }

    public static String getCategoryName(Context context, Category categoryId) {
        switch (categoryId) {
            case RECENT_AUDIO:
            case AUDIO:
                return context.getString(R.string.audio);
            case RECENT_VIDEOS:
            case VIDEO:
            case GENERIC_VIDEOS:
                return context.getString(R.string.nav_menu_video);
            case RECENT_IMAGES:
            case IMAGE:
            case GENERIC_IMAGES:
                return context.getString(R.string.nav_menu_image);
            case RECENT_DOCS:
            case DOCS:
                return context.getString(R.string.nav_menu_docs);
            case DOWNLOADS:
                return context.getString(R.string.downloads);
            case COMPRESSED:
                return context.getString(R.string.compressed);
            case FAVORITES:
                return context.getString(R.string.nav_header_favourites);
            case PDF:
                return context.getString(R.string.pdf);
            case RECENT_APPS:
            case APPS:
                return context.getString(R.string.apk);
            case LARGE_FILES:
                return context.getString(R.string.library_large);
            case RECENT:
                return context.getString(R.string.library_recent);
            case ALBUMS:
                return context.getString(R.string.albums);
            case ARTISTS:
                return context.getString(R.string.artists);
            case GENRES:
                return context.getString(R.string.genres);
            case PODCASTS:
                return context.getString(R.string.podcasts);
            case ALL_TRACKS:
                return context.getString(R.string.all_tracks);
            case APP_MANAGER:
                return context.getString(R.string.app_manager);
            case TRASH:
                return context.getString(R.string.trash);
        }
        return context.getString(R.string.audio);
    }

    public static int getResourceIdForCategory(Category categoryId) {
        switch (categoryId) {
            case AUDIO:
                return R.drawable.ic_library_music;
            case VIDEO:
                return R.drawable.ic_library_videos;
            case IMAGE:
                return R.drawable.ic_library_images;
            case DOCS:
                return R.drawable.ic_library_docs;
            case DOWNLOADS:
                return R.drawable.ic_library_downloads;
            case COMPRESSED:
                return R.drawable.ic_library_compressed;
            case FAVORITES:
                return R.drawable.ic_library_favorite;
            case PDF:
                return R.drawable.ic_library_pdf;
            case APPS:
                return R.drawable.ic_library_apk;
            case LARGE_FILES:
                return R.drawable.ic_library_large;
            case RECENT:
                return R.drawable.ic_library_recents;
        }
        return 0;
    }
}
