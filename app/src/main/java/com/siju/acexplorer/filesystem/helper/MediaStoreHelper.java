package com.siju.acexplorer.filesystem.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;

import static com.siju.acexplorer.filesystem.helper.UriHelper.createContentUri;


public class MediaStoreHelper {


    public static void scanFile(Context context, String path) {

        Intent intent =
                new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(createContentUri(context, path));
        context.sendBroadcast(intent);


/*        MediaScannerConnection.scanFile(context,
                new String[]{path}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i(TAG, "Scanned " + path + ":");
                        Log.i(TAG, "-> uri=" + uri);
                    }
                });*/
    }

    public static void removeMedia(Context context, File file, int category) {
        ContentResolver resolver = context.getContentResolver();
        String path = file.getAbsolutePath();
        switch (category) {
            case 1:
                Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                resolver.delete(musicUri, MediaStore.Audio.Media.DATA + "=?", new String[]{path});
                break;

            case 2:
                Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                resolver.delete(videoUri, MediaStore.Video.Media.DATA + "=?", new String[]{path});
                break;

            case 3:
                Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                resolver.delete(imageUri, MediaStore.Images.Media.DATA + "=?", new String[]{path});
                break;

            case 4:
                Uri filesUri = MediaStore.Files.getContentUri("external");
                resolver.delete(filesUri, MediaStore.Files.FileColumns.DATA + "=?", new String[]{path});
                break;
            default:
                break;
        }
    }

}
