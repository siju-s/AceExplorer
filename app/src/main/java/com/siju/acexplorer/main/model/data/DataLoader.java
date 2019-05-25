package com.siju.acexplorer.main.model.data;

import android.content.Context;
import androidx.preference.PreferenceManager;

import com.siju.acexplorer.main.model.FileConstants;
import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.main.model.data.music.MusicDataFetcher;
import com.siju.acexplorer.main.model.groups.Category;
import com.siju.acexplorer.main.model.root.RootUtils;

import java.util.ArrayList;

class DataLoader {

    static ArrayList<FileInfo> fetchDataByCategory(Context context, Category category, String currentDir, long id,
                                                   boolean isRingtonePicker, boolean showOnlyCount)
    {
        int sortMode = getSortMode(context);
        boolean showHidden = canShowHiddenFiles(context);

        if (isFilesCategory(category)) {
            return FileDataFetcher
                    .fetchFiles(currentDir, sortMode, showHidden, isRingtonePicker, RootUtils.isRooted(context));
        } else if (isMusicCategory(category)) {
            return MusicDataFetcher.fetchMusic(context, category, id, sortMode, showOnlyCount, showHidden);
        } else if (isVideoCategory(category)) {
            return VideoDataFetcher.fetchVideos(context, category, id, sortMode, showOnlyCount, showHidden);
        } else if (isImagesCategory(category)) {
            return ImageDataFetcher.fetchImages(context, category, id, sortMode, showOnlyCount, showHidden);
        } else if (isDocCategory(category)) {
            return DocumentDataFetcher.fetchDocumentsByCategory(context, category, showOnlyCount, sortMode, showHidden);
        }
        switch (category) {
            case FAVORITES:
                return FileDataFetcher.fetchFavorites(context, category, sortMode, showOnlyCount);
            case GIF:
                return ImageDataFetcher.fetchGif(context, category, sortMode, showOnlyCount, showHidden);
            case RECENT:
                return RecentDataFetcher.fetchRecent(context, category, showOnlyCount, showHidden);
            case RECENT_IMAGES:
                return RecentDataFetcher.fetchRecentImages(context,category , showHidden);
            case RECENT_AUDIO:
                return RecentDataFetcher.fetchRecentAudio(context,category , showHidden);
            case RECENT_VIDEOS:
                return RecentDataFetcher.fetchRecentVideos(context,category , showHidden);
            case RECENT_DOCS:
                return RecentDataFetcher.fetchRecentDocs(context,category , showHidden);
            case RECENT_APPS:
                return RecentDataFetcher.fetchRecentApps(context,category , showHidden);
            case APPS:
                return AppDataFetcher.fetchApk(context, category, sortMode, showOnlyCount, showHidden);
        }

        return new ArrayList<>();
    }

    private static int getSortMode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(
                FileConstants.KEY_SORT_MODE, FileConstants.KEY_SORT_NAME);
    }

    private static boolean canShowHiddenFiles(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean
                (FileConstants.PREFS_HIDDEN, false);
    }

    private static boolean isFilesCategory(Category category) {
        return category == Category.FILES || category == Category.DOWNLOADS;
    }

    private static boolean isMusicCategory(Category category) {
        return category == Category.AUDIO || category == Category.GENERIC_MUSIC || category == Category.ALBUMS
               || category == Category.ARTISTS || category == Category.GENRES || category == Category.ALARMS ||
               category == Category.NOTIFICATIONS || category == Category.RINGTONES || category == Category.PODCASTS ||
               category == Category.ALBUM_DETAIL || category == Category.ARTIST_DETAIL || category == Category.GENRE_DETAIL ||
               category == Category.ALL_TRACKS;
    }

    private static boolean isVideoCategory(Category category) {
        return category == Category.VIDEO || category == Category.GENERIC_VIDEOS || category == Category.FOLDER_VIDEOS;
    }

    private static boolean isImagesCategory(Category category) {
        return category == Category.IMAGE || category == Category.GENERIC_IMAGES || category == Category.FOLDER_IMAGES;
    }

    private static boolean isDocCategory(Category category) {
        return category == Category.DOCS || category == Category.COMPRESSED || category == Category.PDF ||
               category == Category.LARGE_FILES;
    }

}
