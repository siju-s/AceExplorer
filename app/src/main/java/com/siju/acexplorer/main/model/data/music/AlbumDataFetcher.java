package com.siju.acexplorer.main.model.data.music;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.main.model.groups.Category;

import java.util.ArrayList;

class AlbumDataFetcher {

    static ArrayList<FileInfo> fetchAlbums(Context context, Category category) {

        Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null,
                                                           MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
        return getAlbumCursorData(category, cursor);
    }

    private static ArrayList<FileInfo> getAlbumCursorData(Category category, Cursor cursor) {
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();

        if (cursor == null) {
            return fileInfoList;
        }

        if (cursor.moveToFirst()) {
            if (Category.GENERIC_MUSIC.equals(category)) {
                fileInfoList.add(new FileInfo(category, Category.ALBUMS, cursor.getCount()));
                cursor.close();
                return fileInfoList;
            }
            int albumNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
            int albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID);
            int numTracksIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS);
            do {
                String album = cursor.getString(albumNameIndex);
                long albumId = cursor.getLong(albumIdIndex);
                long numTracks = cursor.getLong(numTracksIndex);

                fileInfoList.add(new FileInfo(category, albumId, album, numTracks));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return fileInfoList;
    }
}
