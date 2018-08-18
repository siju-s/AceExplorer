package com.siju.acexplorer.model.data.music;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.groups.Category;

import java.util.ArrayList;

class ArtistDataFetcher {

    static ArrayList<FileInfo> fetchArtists(Context context, Category category) {

        Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null,
                                                           MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);
        return getArtistCursorData(category, cursor);
    }

    private static ArrayList<FileInfo> getArtistCursorData(Category category, Cursor cursor) {
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();
        if (cursor == null) {
            return fileInfoList;
        }

        if (cursor.moveToFirst()) {
            if (Category.GENERIC_MUSIC.equals(category)) {
                fileInfoList.add(new FileInfo(category, Category.ARTISTS, cursor.getCount()));
                cursor.close();
                return fileInfoList;
            }
            int artistNameIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST);
            int artistIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID);
            int numTracksIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS);
            do {
                String artist = cursor.getString(artistNameIdx);
                long artistId = cursor.getLong(artistIdIdx);
                long numTracks = cursor.getLong(numTracksIndex);

                fileInfoList.add(new FileInfo(category, artistId, artist, numTracks));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return fileInfoList;
    }

}
