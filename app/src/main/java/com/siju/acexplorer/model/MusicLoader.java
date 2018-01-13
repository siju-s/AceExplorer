package com.siju.acexplorer.model;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.siju.acexplorer.model.groups.Category;

import java.io.File;
import java.util.ArrayList;

import static com.siju.acexplorer.model.FileListLoader.INVALID_ID;
import static com.siju.acexplorer.model.helper.SortHelper.sortFiles;

public class MusicLoader {


    private static final String TAG = "MusicLoader";

    public static ArrayList<FileInfo> fetchMusic(Context context, Category category, long id, int sortMode,
                                                 boolean isHome, boolean showHidden) {
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();
        switch (category) {
            case GENERIC_MUSIC:
                fileInfoList.addAll(fetchMusicDetail(context, Category.ALARMS, INVALID_ID, true, isHome, showHidden));
                fileInfoList.addAll(fetchAlbums(context, category));
                fileInfoList.addAll(fetchMusicDetail(context, Category.ALL_TRACKS, INVALID_ID, true, isHome, showHidden));
                fileInfoList.addAll(fetchArtists(context, category));
                fileInfoList.addAll(fetchGenres(context, category));
                fileInfoList.addAll(fetchMusicDetail(context, Category.NOTIFICATIONS, INVALID_ID, true, isHome, showHidden));
                fileInfoList.addAll(fetchMusicDetail(context, Category.PODCASTS, INVALID_ID, true, isHome, showHidden));
                fileInfoList.addAll(fetchMusicDetail(context, Category.RINGTONES, INVALID_ID, true, isHome, showHidden));
                return fileInfoList;
            case ALBUMS:
                fileInfoList.addAll(fetchAlbums(context, category));
                return fileInfoList;
            case ARTISTS:
                fileInfoList.addAll(fetchArtists(context, category));
                return fileInfoList;
            case GENRES:
                fileInfoList.addAll(fetchGenres(context, category));
                return fileInfoList;
            case AUDIO:
            case ALL_TRACKS:
            case ALARMS:
            case NOTIFICATIONS:
            case RINGTONES:
            case PODCASTS:
            case ALBUM_DETAIL:
            case ARTIST_DETAIL:
                fileInfoList.addAll(fetchMusicDetail(context, category, id, false, isHome, showHidden));
                break;
            case GENRE_DETAIL:
                fileInfoList.addAll(fetchGenreDetails(context, category, id));
                break;

        }
        if (fileInfoList.size() != 0) {
            fileInfoList = sortFiles(fileInfoList, sortMode);
        }
        return fileInfoList;
    }

    private static ArrayList<FileInfo> fetchAlbums(Context context, Category category) {
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();

        Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

        Cursor cursor = context.getContentResolver().query(uri, null, null, null,
                                                           MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                if (category.equals(Category.GENERIC_MUSIC)) {
                    fileInfoList.add(new FileInfo(category, Category.ALBUMS, cursor.getCount()));
                    cursor.close();
                    return fileInfoList;
                }
                do {
                    int albumNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
                    int albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID);
                    int numTracksIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS);

                    String album = cursor.getString(albumNameIndex);
                    long albumId = cursor.getLong(albumIdIndex);
                    long numTracks = cursor.getLong(numTracksIndex);

                    fileInfoList.add(new FileInfo(category, albumId, album, numTracks));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return fileInfoList;
    }


    private static ArrayList<FileInfo> fetchArtists(Context context, Category category) {
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();

        Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;

        Cursor cursor = context.getContentResolver().query(uri, null, null, null,
                                                           MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                if (category.equals(Category.GENERIC_MUSIC)) {
                    fileInfoList.add(new FileInfo(category, Category.ARTISTS, cursor.getCount()));
                    cursor.close();
                    return fileInfoList;
                }
                do {
                    int artistNameIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST);
                    int artistIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID);
                    int numTracksIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS);

                    String artist = cursor.getString(artistNameIdx);
                    long artistId = cursor.getLong(artistIdIdx);
                    long numTracks = cursor.getLong(numTracksIndex);

                    fileInfoList.add(new FileInfo(category, artistId, artist, numTracks));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return fileInfoList;
    }


    private static ArrayList<FileInfo> fetchGenres(Context context, Category category) {
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();

        Uri uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;

        Cursor cursor = context.getContentResolver().query(uri, null, null, null,
                                                           MediaStore.Audio.Genres.DEFAULT_SORT_ORDER);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                if (category.equals(Category.GENERIC_MUSIC)) {
                    fileInfoList.add(new FileInfo(category, Category.GENRES, cursor.getCount()));
                    cursor.close();
                    return fileInfoList;
                }
                do {
                    int genreNameIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME);
                    int genreIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID);

                    String genre = cursor.getString(genreNameIdx);
                    long genreId = cursor.getLong(genreIdIdx);

                    fileInfoList.add(new FileInfo(category, genreId, genre, INVALID_ID));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return fileInfoList;
    }


    private static ArrayList<FileInfo> fetchGenreDetails(Context context, Category category, long id) {
        Log.d(TAG, "fetchGenreDetails() called with: category = [" + category + "], id = [" + id + "]");
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();

        Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", id);

        Cursor cursor = context.getContentResolver().query(uri, null, null, null,
                                                           null);

        String[] projection = new String[]{MediaStore.Audio.Genres.Members.TITLE,
                MediaStore.Audio.Genres.Members._ID,
                MediaStore.Audio.Genres.Members.ALBUM_ID,
                MediaStore.Audio.Genres.Members.DATE_MODIFIED,
                MediaStore.Audio.Genres.Members.SIZE,
                MediaStore.Audio.Genres.Members.DATA};

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int titleIndex = cursor.getColumnIndexOrThrow(projection[0]);
                    int audioIdIndex = cursor.getColumnIndexOrThrow(projection[1]);
                    int albumIdIndex = cursor.getColumnIndexOrThrow(projection[2]);
                    int dateIndex = cursor.getColumnIndexOrThrow(projection[3]);
                    int sizeIndex = cursor.getColumnIndexOrThrow(projection[4]);
                    int pathIndex = cursor.getColumnIndexOrThrow(projection[5]);

                    String fileName = cursor.getString(titleIndex);
                    long size1 = cursor.getLong(sizeIndex);
                    long date1 = cursor.getLong(dateIndex);
                    String path = cursor.getString(pathIndex);
                    long audioId = cursor.getLong(audioIdIndex);
                    long albumId = cursor.getLong(albumIdIndex);
                    String extension = path.substring(path.lastIndexOf(".") + 1);
                    String nameWithExt = fileName + "." + extension;

                    fileInfoList.add(new FileInfo(Category.AUDIO, audioId, albumId, nameWithExt, path, date1, size1,
                                                  extension));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return fileInfoList;
    }


    private static ArrayList<FileInfo> fetchMusicDetail(Context context, Category category, long id,
                                                        boolean isGeneric, boolean isHome, boolean showHidden) {
        Log.d(TAG, "fetchMusicDetail() called with: category = [" + category + "], id = [" + id + "], isGeneric = [" + isGeneric + "], isHome = [" + isHome + "]");
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();

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

        String[] projection = new String[]{MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media._ID, MediaStore
                .Audio
                .Media.ALBUM_ID,
                MediaStore.Audio.Media.DATE_MODIFIED, MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                                                           null);

        if (cursor != null) {
            Log.d(TAG, "fetchMusicDetail: category:" + category + " count:" + cursor.getCount());
            if (cursor.moveToFirst()) {
                if (isHome) {
                    fileInfoList.add(new FileInfo(category, cursor.getCount()));
                    cursor.close();
                    return fileInfoList;
                } else if (isGeneric) {
                    fileInfoList.add(new FileInfo(Category.GENERIC_MUSIC, category, cursor.getCount()));
                    cursor.close();
                    return fileInfoList;
                }
                do {
                    int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                    int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
                    int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED);
                    int audioIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                    int albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                    int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);

                    String fileName = cursor.getString(titleIndex);
                    long size1 = cursor.getLong(sizeIndex);
                    long date1 = cursor.getLong(dateIndex);
                    String path = cursor.getString(pathIndex);
                    File file = new File(path);
                    if (file.isHidden() && !showHidden) {
                        continue;
                    }
                    long audioId = cursor.getLong(audioIdIndex);
                    long albumId = cursor.getLong(albumIdIndex);
                    String extension = path.substring(path.lastIndexOf(".") + 1);
                    String nameWithExt = fileName + "." + extension;

                    fileInfoList.add(new FileInfo(Category.AUDIO, audioId, albumId, nameWithExt, path, date1, size1,
                                                  extension));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return fileInfoList;
    }


}
