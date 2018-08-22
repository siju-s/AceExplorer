package com.siju.acexplorer.main.model.data.music;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.main.model.data.MainLoader;
import com.siju.acexplorer.main.model.groups.Category;
import com.siju.acexplorer.main.model.helper.FileUtils;

import java.util.ArrayList;

class GenreDataFetcher {

    static ArrayList<FileInfo> fetchGenres(Context context, Category category) {

        Uri uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;

        Cursor cursor = context.getContentResolver().query(uri, null, null, null,
                                                           MediaStore.Audio.Genres.DEFAULT_SORT_ORDER);
        return getGenreCursorData(cursor, category);
    }

    private static ArrayList<FileInfo> getGenreCursorData(Cursor cursor, Category category) {
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();

        if (cursor == null) {
            return fileInfoList;
        }
        if (cursor.moveToFirst()) {
            if (Category.GENERIC_MUSIC.equals(category)) {
                fileInfoList.add(new FileInfo(category, Category.GENRES, cursor.getCount()));
                cursor.close();
                return fileInfoList;
            }
            int genreNameIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME);
            int genreIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID);
            do {
                long genreId = cursor.getLong(genreIdIdx);
                String genre = cursor.getString(genreNameIdx);
                fileInfoList.add(new FileInfo(category, genreId, genre, MainLoader.INVALID_ID));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return fileInfoList;
    }


    static ArrayList<FileInfo> fetchGenreDetails(Context context, long id) {

        Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", id);

        Cursor cursor = context.getContentResolver().query(uri, null, null, null,
                                                           null);
        return getGenreDetailCursorData(cursor);
    }

    private static ArrayList<FileInfo> getGenreDetailCursorData(Cursor cursor) {
        String[] projection = new String[]{MediaStore.Audio.Genres.Members.TITLE,
                                           MediaStore.Audio.Genres.Members._ID,
                                           MediaStore.Audio.Genres.Members.ALBUM_ID,
                                           MediaStore.Audio.Genres.Members.DATE_MODIFIED,
                                           MediaStore.Audio.Genres.Members.SIZE,
                                           MediaStore.Audio.Genres.Members.DATA};
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();
        if (cursor == null) {
            return fileInfoList;
        }
        int titleIndex = cursor.getColumnIndexOrThrow(projection[0]);
        int audioIdIndex = cursor.getColumnIndexOrThrow(projection[1]);
        int albumIdIndex = cursor.getColumnIndexOrThrow(projection[2]);
        int dateIndex = cursor.getColumnIndexOrThrow(projection[3]);
        int sizeIndex = cursor.getColumnIndexOrThrow(projection[4]);
        int pathIndex = cursor.getColumnIndexOrThrow(projection[5]);
        if (cursor.moveToFirst()) {
            do {
                String fileName = cursor.getString(titleIndex);
                long size1 = cursor.getLong(sizeIndex);
                long date1 = cursor.getLong(dateIndex);
                String path = cursor.getString(pathIndex);
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
