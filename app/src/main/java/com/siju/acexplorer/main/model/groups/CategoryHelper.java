package com.siju.acexplorer.main.model.groups;


import android.content.Context;

import com.siju.acexplorer.R;

import static com.siju.acexplorer.main.model.groups.Category.*;


public class CategoryHelper {

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
            case 15:
                return GIF;
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
                category.equals(ALARMS) ||
                category.equals(NOTIFICATIONS) ||
                category.equals(RINGTONES) ||
                category.equals(PODCASTS) ||
                category.equals(ALBUM_DETAIL) ||
                category.equals(ARTIST_DETAIL) ||
                category.equals(GENRE_DETAIL) ||
                category.equals(ALL_TRACKS) ;
    }



    public static boolean isGenericImagesCategory(Category category) {
        return GENERIC_IMAGES.equals(category);
    }

    public static boolean isGenericVideosCategory(Category category) {
        return GENERIC_VIDEOS.equals(category);
    }

    public static boolean isPeekPopCategory(Category category) {
        return category.equals(IMAGE) || category.equals(VIDEO) || category.equals(AUDIO) ||
                category.equals(FOLDER_IMAGES) || category.equals(FOLDER_VIDEOS);
    }

    public static boolean showLibSpecificNavigation(Category category) {
        return  checkIfAnyMusicCategory(category) ||
                category.equals(FOLDER_IMAGES) || category.equals(FOLDER_VIDEOS);
    }

    public static boolean isSortOrActionModeUnSupported(Category category) {
        return (isMusicCategory(category) || isGenericMusic(category) ||
                isGenericVideosCategory(category) ||
                isGenericImagesCategory(category));
    }

    public static String getCategoryName(Context context, Category categoryId) {
        switch (categoryId) {
            case AUDIO:
                return context.getString(R.string.audio);
            case VIDEO:
            case GENERIC_VIDEOS:
                return context.getString(R.string.nav_menu_video);
            case IMAGE:
            case GENERIC_IMAGES:
                return context.getString(R.string.nav_menu_image);
            case DOCS:
                return context.getString(R.string.nav_menu_docs);
            case DOWNLOADS:
                return context.getString(R.string.downloads);
            case ADD:
                return context.getString(R.string.home_add);
            case COMPRESSED:
                return context.getString(R.string.compressed);
            case FAVORITES:
                return context.getString(R.string.nav_header_favourites);
            case PDF:
                return context.getString(R.string.pdf);
            case APPS:
                return context.getString(R.string.apk);
            case LARGE_FILES:
                return context.getString(R.string.library_large);
            case GIF:
                return context.getString(R.string.library_gif);
            case RECENT:
                return context.getString(R.string.library_recent);
            case ALBUMS:
                return context.getString(R.string.albums);
            case ARTISTS:
                return context.getString(R.string.artists);
            case ALARMS:
                return context.getString(R.string.alarms);
            case NOTIFICATIONS:
                return context.getString(R.string.notifications);
            case GENRES:
                return context.getString(R.string.genres);
            case RINGTONES:
                return context.getString(R.string.ringtones);
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
            case ADD:
                return R.drawable.ic_library_add;
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
            case GIF:
                return R.drawable.ic_library_gif;
            case RECENT:
                return R.drawable.ic_library_recents;
        }
        return 0;
    }
}
