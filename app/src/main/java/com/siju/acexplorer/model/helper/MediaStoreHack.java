/*
 * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.siju.acexplorer.model.helper;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;

import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

/**
 * Wrapper for manipulating files via the Android Media Content Provider. As of Android 4.4 KitKat,
 * applications can no longer write to the "secondary storage" of a device. Write operations using
 * the java.io.File API will thus fail. This class restores access to those write operations by way
 * of the Media Content Provider.</p>
 * <p>
 * Note that this class relies on the internal operational characteristics of the media content
 * provider API, and as such is not guaranteed to be future-proof. Then again, we did all think the
 * java.io.File API was going to be future-proof for media card access, so all bets are off.</p>
 * <p>
 * If you're forced to use this class, it's because Google/AOSP made a very poor API decision in
 * Android 4.4 KitKat. Read more at https://plus.google.com/+TodLiebeck/posts/gjnmuaDM8sn</p>
 * <p>
 * Your application must declare the permission "android.permission.WRITE_EXTERNAL_STORAGE".</p>
 * <p>
 * Adapted from: http://forum.xda-developers.com/showpost.php?p=52151865&postcount=20</p>
 *
 * @author Jared Rummler <jared.rummler@gmail.com>
 */
public class MediaStoreHack {

    private static final String ALBUM_ART_URI = "content://media/external/audio/albumart";

    private static final String[] ALBUM_PROJECTION = {
            BaseColumns._ID, MediaStore.Audio.AlbumColumns.ALBUM_ID, "media_type"
    };

    /**
     * Deletes the file. Returns true if the file has been successfully deleted or otherwise does
     * not exist. This operation is not recursive.
     */
    private static void delete(final Context context, final File file) {
        final String where = MediaStore.MediaColumns.DATA + "=?";
        final String[] selectionArgs = new String[]{
                file.getAbsolutePath()
        };
        final ContentResolver contentResolver = context.getContentResolver();
        final Uri filesUri = MediaStore.Files.getContentUri("external");
        // Delete the entry from the media database. This will actually delete media files.
        contentResolver.delete(filesUri, where, selectionArgs);
        // If the file is not a media file, create a new entry.
        if (file.exists()) {
            final ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            // Delete the created entry, such that content provider will delete the file.
            contentResolver.delete(filesUri, where, selectionArgs);
        }
    }

    private static File getExternalFilesDir(final Context context) {
        return context.getExternalFilesDir(null);
    }


    private static OutputStream getOutputStream(String str) {
        OutputStream outputStream = null;
        Context context = AceApplication.getAppContext();
        Uri fileUri = getUriFromFile(str);
        if (fileUri != null) {
            try {
                outputStream = context.getContentResolver().openOutputStream(fileUri);
            } catch (Throwable ignored) {
            }
        }
        return outputStream;
    }

    static Uri getUriFromFile(final String path) {
        Context context = AceApplication.getAppContext();
        ContentResolver resolver = context.getContentResolver();

        Cursor filecursor = resolver.query(MediaStore.Files.getContentUri("external"),
                                           new String[]{BaseColumns._ID}, MediaStore.MediaColumns.DATA + " = ?",
                                           new String[]{path}, MediaStore.MediaColumns.DATE_ADDED + " desc");
        if (filecursor == null) {
            return null;
        }
        filecursor.moveToFirst();

        if (filecursor.isAfterLast()) {
            filecursor.close();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, path);
            return resolver.insert(MediaStore.Files.getContentUri("external"), values);
        } else {
            int imageId = filecursor.getInt(filecursor.getColumnIndex(BaseColumns._ID));
            Uri uri = MediaStore.Files.getContentUri("external").buildUpon().appendPath(
                    Integer.toString(imageId)).build();
            filecursor.close();
            return uri;
        }
    }

    public static Uri getCustomRingtoneUri(ContentResolver contentResolver, String path, int type) {

        String musicType = type == 1 ? "is_ringtone" : type == 2 ? "is_notification" : "is_alarm";
        String uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString();
        Uri parse;
        Cursor query = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                             new String[]{MediaStore.Audio.Media._ID, musicType}, "_data=?", new String[]{path}, null);
        if (query != null) {
            try {
                if (query.getCount() != 0) {
                    query.moveToFirst();
                    int id = query.getInt(0);
                    /* If ringtone/notification/alarm flag not there in custom uri, add it .
                      This part was necessary to make it work on Note 2 (4.4.2) */
                    if (query.getInt(1) == 0 && type != 0) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(musicType, true);
                        contentResolver.update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues, "_id=" +
                                id, null);
                    }
                    parse = Uri.parse(uri + "/" + id);
                    query.close();
                    return parse;
                } else {
                    return insertAudioIntoMediaStore(contentResolver, path, type);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return insertAudioIntoMediaStore(contentResolver, path, type);
        }
        return null;
    }

    private static Uri insertAudioIntoMediaStore(ContentResolver contentResolver, String path, int type) {
        File file = new File(path);
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Audio.Media.DATA, path);
        contentValues.put(MediaStore.Audio.Media.TITLE, file.getName());
        contentValues.put(MediaStore.Audio.Media.SIZE, file.length());
        contentValues.put(MediaStore.Audio.Media.DATE_MODIFIED, file.lastModified());
        contentValues.put("is_ringtone", type == 1);
        contentValues.put("is_notification", type == 2);
        contentValues.put("is_alarm", type == 4);
        contentValues.put("is_music", true);
        return contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues);
    }


    /**
     * Returns an OutputStream to write to the file. The file will be truncated immediately.
     */

    private static int getTemporaryAlbumId(final Context context) {
        final File temporaryTrack;
        try {
            temporaryTrack = installTemporaryTrack(context);
        } catch (final IOException ex) {
            Log.w("MediaFile", "Error installing tempory track.", ex);
            return 0;
        }
        if (temporaryTrack == null) {
            return 0;
        }
        final Uri filesUri = MediaStore.Files.getContentUri("external");
        final String[] selectionArgs = {
                temporaryTrack.getAbsolutePath()
        };
        final ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(filesUri, ALBUM_PROJECTION,
                                              MediaStore.MediaColumns.DATA + "=?", selectionArgs, null);
        if (cursor == null || !cursor.moveToFirst()) {
            if (cursor != null) {
                cursor.close();
            }
            final ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, temporaryTrack.getAbsolutePath());
            values.put(MediaStore.MediaColumns.TITLE, "{MediaWrite Workaround}");
            values.put(MediaStore.MediaColumns.SIZE, temporaryTrack.length());
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg");
            values.put(MediaStore.Audio.AudioColumns.IS_MUSIC, true);
            contentResolver.insert(filesUri, values);
        }
        cursor = contentResolver.query(filesUri, ALBUM_PROJECTION, MediaStore.MediaColumns.DATA
                + "=?", selectionArgs, null);
        if (cursor == null) {
            return 0;
        }
        if (!cursor.moveToFirst()) {
            cursor.close();
            return 0;
        }
        final int id = cursor.getInt(0);
        final int albumId = cursor.getInt(1);
        final int mediaType = cursor.getInt(2);
        cursor.close();
        final ContentValues values = new ContentValues();
        boolean updateRequired = false;
        if (albumId == 0) {
            values.put(MediaStore.Audio.AlbumColumns.ALBUM_ID, 13371337);
            updateRequired = true;
        }
        if (mediaType != 2) {
            values.put("media_type", 2);
            updateRequired = true;
        }
        if (updateRequired) {
            contentResolver.update(filesUri, values, BaseColumns._ID + "=" + id, null);
        }
        cursor = contentResolver.query(filesUri, ALBUM_PROJECTION, MediaStore.MediaColumns.DATA
                + "=?", selectionArgs, null);
        if (cursor == null) {
            return 0;
        }
        try {
            if (!cursor.moveToFirst()) {
                return 0;
            }
            return cursor.getInt(1);
        } finally {
            cursor.close();
        }
    }

    private static File installTemporaryTrack(final Context context) throws IOException {
        final File externalFilesDir = getExternalFilesDir(context);
        if (externalFilesDir == null) {
            return null;
        }
        final File temporaryTrack = new File(externalFilesDir, "temptrack.mp3");
        if (!temporaryTrack.exists()) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = context.getResources().openRawResource(R.raw.temptrack);
                out = new FileOutputStream(temporaryTrack);
                final byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException ignored) {

                }
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ignored) {

                }

            }
        }
        return temporaryTrack;
    }

    public static boolean mkdir(final File file) throws IOException {
        if (file.exists()) {
            return file.isDirectory();
        }
        final File tmpFile = new File(file, ".MediaWriteTemp");
        Context context = AceApplication.getAppContext();
        final int albumId = getTemporaryAlbumId(context);
        if (albumId == 0) {
            throw new IOException("Failed to create temporary album id.");
        }
        final Uri albumUri = Uri.parse(String.format(Locale.US, ALBUM_ART_URI + "/%d", albumId));
        final ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, tmpFile.getAbsolutePath());
        final ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver.update(albumUri, values, null, null) == 0) {
            values.put(MediaStore.Audio.AlbumColumns.ALBUM_ID, albumId);
            contentResolver.insert(Uri.parse(ALBUM_ART_URI), values);
        }
        try {
            final ParcelFileDescriptor fd = contentResolver.openFileDescriptor(albumUri, "r");
            if (fd != null) {
                fd.close();
            }
        } catch (SecurityException e) { //TODO find solution to remove this
            e.printStackTrace();
        } finally {
            delete(context, tmpFile);
        }
        return file.exists();
    }

    static boolean mkfile(final File file) {
        final OutputStream outputStream = getOutputStream(file.getPath());
        if (outputStream == null) {
            return false;
        }
        try {
            outputStream.close();
            return true;
        } catch (final IOException ignored) {
        }
        return false;
    }

}
