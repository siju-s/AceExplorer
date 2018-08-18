package com.siju.acexplorer.model.data.music;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.HiddenFileHelper;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.model.helper.FileUtils;

import java.io.File;
import java.util.ArrayList;

import static com.siju.acexplorer.model.data.MainLoader.INVALID_ID;
import static com.siju.acexplorer.model.data.music.AlbumDataFetcher.fetchAlbums;
import static com.siju.acexplorer.model.data.music.ArtistDataFetcher.fetchArtists;
import static com.siju.acexplorer.model.data.music.GenreDataFetcher.fetchGenreDetails;
import static com.siju.acexplorer.model.data.music.GenreDataFetcher.fetchGenres;
import static com.siju.acexplorer.model.helper.SortHelper.sortFiles;

public class MusicDataFetcher {


    @SuppressWarnings("unused")
    private static final String TAG = "MusicDataFetcher";

    public static ArrayList<FileInfo> fetchMusic(Context context, Category category, long id, int sortMode,
                                                 boolean showOnlyCount, boolean showHidden)
    {
        ArrayList<FileInfo> fileInfoList;
        switch (category) {
            case GENERIC_MUSIC:
                return fetchGenericMusic(context, category, showOnlyCount, showHidden);
            case ALBUMS:
                return fetchAlbums(context, category);
            case ARTISTS:
                return fetchArtists(context, category);
            case GENRES:
                return fetchGenres(context, category);
            case AUDIO:
            case ALL_TRACKS:
            case ALARMS:
            case NOTIFICATIONS:
            case RINGTONES:
            case PODCASTS:
            case ALBUM_DETAIL:
            case ARTIST_DETAIL:
                fileInfoList = fetchMusicDetail(context, category, id, false, showOnlyCount, showHidden);
                break;
            case GENRE_DETAIL:
                fileInfoList = fetchGenreDetails(context, id);
                break;
            default:
                fileInfoList = new ArrayList<>();
                break;
        }
        sortFiles(fileInfoList, sortMode);
        return fileInfoList;
    }

    private static ArrayList<FileInfo> fetchGenericMusic(Context context, Category category, boolean showOnlyCount,
                                                         boolean showHidden)
    {
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();
        fileInfoList.addAll(fetchMusicDetail(context, Category.ALARMS, INVALID_ID, true, showOnlyCount,
                                             showHidden));
        fileInfoList.addAll(fetchAlbums(context, category));
        fileInfoList.addAll(fetchMusicDetail(context, Category.ALL_TRACKS, INVALID_ID, true, showOnlyCount,
                                             showHidden));
        fileInfoList.addAll(fetchArtists(context, category));
        fileInfoList.addAll(fetchGenres(context, category));
        fileInfoList.addAll(fetchMusicDetail(context, Category.NOTIFICATIONS, INVALID_ID, true, showOnlyCount,
                                             showHidden));
        fileInfoList.addAll(fetchMusicDetail(context, Category.PODCASTS, INVALID_ID, true, showOnlyCount,
                                             showHidden));
        fileInfoList.addAll(fetchMusicDetail(context, Category.RINGTONES, INVALID_ID, true, showOnlyCount,
                                             showHidden));
        return fileInfoList;
    }

    private static ArrayList<FileInfo> fetchMusicDetail(Context context, Category category, long id,
                                                        boolean isGeneric, boolean isHome, boolean showHidden)
    {

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = null;
        String selectionArgs[] = null;
        switch (category) {
            case ALBUM_DETAIL:
                selection = MediaStore.Audio.Media.ALBUM_ID + "=?";
                selectionArgs = new String[]{String.valueOf(id)};
                break;
            case ARTIST_DETAIL:
                selection = MediaStore.Audio.Media.ARTIST_ID + "=?";
                selectionArgs = new String[]{String.valueOf(id)};
                break;
            case ALARMS:
                selection = MediaStore.Audio.Media.IS_ALARM + "=1";
                uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
                break;
            case NOTIFICATIONS:
                selection = MediaStore.Audio.Media.IS_NOTIFICATION + "=1";
                uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
                break;
            case RINGTONES:
                selection = MediaStore.Audio.Media.IS_RINGTONE + "=1";
                uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
                break;
            case PODCASTS:
                selection = MediaStore.Audio.Media.IS_PODCAST + "=1";
                uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
                break;
        }

        String[] projection = new String[]{MediaStore.Audio.Media.TITLE,
                                           MediaStore.Audio.Media._ID,
                                           MediaStore.Audio.Media.ALBUM_ID,
                                           MediaStore.Audio.Media.DATE_MODIFIED,
                                           MediaStore.Audio.Media.SIZE,
                                           MediaStore.Audio.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                                                           null);
        return getDetailCursorData(cursor, category, isGeneric, isHome, showHidden);
    }

    private static ArrayList<FileInfo> getDetailCursorData(Cursor cursor, Category category, boolean isGeneric,
                                                           boolean showOnlyCount, boolean showHidden)
    {
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();

        if (cursor == null) {
            return fileInfoList;
        }

        if (cursor.moveToFirst()) {
            if (showOnlyCount) {
                fileInfoList.add(new FileInfo(category, cursor.getCount()));
                cursor.close();
                return fileInfoList;
            } else if (isGeneric) {
                fileInfoList.add(new FileInfo(Category.GENERIC_MUSIC, category, cursor.getCount()));
                cursor.close();
                return fileInfoList;
            }
            int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED);
            int audioIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
            int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            do {
                String fileName = cursor.getString(titleIndex);
                long size1 = cursor.getLong(sizeIndex);
                long date1 = cursor.getLong(dateIndex);
                String path = cursor.getString(pathIndex);
                File file = new File(path);
                if (HiddenFileHelper.shouldSkipHiddenFiles(file, showHidden)) {
                    continue;
                }
                long audioId = cursor.getLong(audioIdIndex);
                long albumId = cursor.getLong(albumIdIndex);
                String extension = FileUtils.getExtension(path);
                String nameWithExt = FileUtils.constructFileNameWithExtension(fileName, extension);
                fileInfoList.add(new FileInfo(Category.AUDIO, audioId, albumId, nameWithExt, path, date1, size1,
                                              extension));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return fileInfoList;
    }

}
