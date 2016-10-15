package com.siju.acexplorer.filesystem.utils;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;

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
    public static boolean delete(final Context context, final File file) {
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
        return !file.exists();
    }

    private static File getExternalFilesDir(final Context context) {
        return context.getExternalFilesDir(null);
    }

    public static InputStream
    getInputStream(final Context context, final File file, final long size) {
        try {
            final String where = MediaStore.MediaColumns.DATA + "=?";
            final String[] selectionArgs = new String[]{
                    file.getAbsolutePath()
            };
            final ContentResolver contentResolver = context.getContentResolver();
            final Uri filesUri = MediaStore.Files.getContentUri("external");
            contentResolver.delete(filesUri, where, selectionArgs);
            final ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
            values.put(MediaStore.MediaColumns.SIZE, size);
            final Uri uri = contentResolver.insert(filesUri, values);
            return contentResolver.openInputStream(uri);
        } catch (final Throwable t) {
            return null;
        }
    }

    public static OutputStream getOutputStream(Context context, String str) {
        OutputStream outputStream = null;
        Uri fileUri = getUriFromFile(str, context);
        if (fileUri != null) {
            try {
                outputStream = context.getContentResolver().openOutputStream(fileUri);
            } catch (Throwable th) {
            }
        }
        return outputStream;
    }

    public static Uri getUriFromFile(final String path, Context context) {
        ContentResolver resolver = context.getContentResolver();

        Cursor filecursor = resolver.query(MediaStore.Files.getContentUri("external"),
                new String[]{BaseColumns._ID}, MediaStore.MediaColumns.DATA + " = ?",
                new String[]{path}, MediaStore.MediaColumns.DATE_ADDED + " desc");
        if (filecursor == null) return null;
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
                    /** If ringtone/notification/alarm flag not there in custom uri, add it .
                     * This part was necessary to make it work on Note 2 (4.4.2) */
                    if (query.getInt(1) == 0 && type != 0) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(musicType, true);
                        contentResolver.update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues, "_id=" + id, null);
                    }
                    parse = Uri.parse(uri + "/" + id);
                    query.close();
                    return parse;
                }
                else {
                    return insertAudioIntoMediaStore(contentResolver,path,type);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
           return insertAudioIntoMediaStore(contentResolver,path,type);
        }
        return null;
    }

    public static Uri insertAudioIntoMediaStore(ContentResolver contentResolver, String path, int type) {
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
        Uri uri = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues);
        return uri;
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
                cursor = null;
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
                out.close();
                in.close();
            }
        }
        return temporaryTrack;
    }

    public static boolean mkdir(final Context context, final File file) throws IOException {
        if (file.exists()) {
            return file.isDirectory();
        }
/*
        ContentValues values;
        Uri uri;
        Uri filesUri =  MediaStore.Files.getContentUri("external");
        Uri imagesUri  = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        // Create a media database entry for the directory. This step will not actually cause the directory to be created.
        values = new ContentValues();
        values.put(MediaStore.Files.FileColumns.DATA, file.getAbsolutePath());
        final ContentResolver contentResolver = context.getContentResolver();
        contentResolver.insert(filesUri, values);

        // Create an entry for a temporary image file within the created directory.
        // This step actually causes the creation of the directory.
        values = new ContentValues();
        values.put(MediaStore.Files.FileColumns.DATA, file.getAbsolutePath() + "/temp.jpg");
        uri = contentResolver.insert(imagesUri, values);

        // Delete the temporary entry.
        contentResolver.delete(uri, null, null);*/

        final File tmpFile = new File(file, ".MediaWriteTemp");
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
            if (fd != null)
                fd.close();
        } catch (SecurityException e) { //TODO find solution to remove this
            e.printStackTrace();
        } finally {
            delete(context, tmpFile);
        }
        return file.exists();
    }

    public static boolean mkfile(final Context context, final File file) {
        final OutputStream outputStream = getOutputStream(context, file.getPath());
        if (outputStream == null) {
            return false;
        }
        try {
            outputStream.close();
            return true;
        } catch (final IOException e) {
        }
        return false;
    }

}
