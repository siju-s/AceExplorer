package com.siju.filemanager.filesystem;

import android.content.Context;
import android.os.Environment;
import android.text.format.Formatter;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Created by Siju on 16-06-2016.
 */

public class FileUtils {

    private static String mCurrentDirectory;
    public static File getRootDirectory() {
        return Environment.getRootDirectory();
    }

    public static File getInternalStorage() {
        return Environment.getExternalStorageDirectory();
    }

    public static String getAbsolutePath(File file) {
        if (file == null)
            return null;
        return file.getAbsolutePath();
    }

    public static File getDownloadsDirectory() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    public static String getSpaceLeft(Context context, File path) {
        String freeSpace = Formatter.formatFileSize(context, path.getFreeSpace());
        return freeSpace;
    }

    public static String getTotalSpace(Context context, File path) {
        String totalSpace = Formatter.formatFileSize(context, path.getTotalSpace());
        return totalSpace;
    }

    public static String getCurrentDirectory()
    {
        return mCurrentDirectory;
    }

    public static void setCurrentDirectory(String path)
    {
        mCurrentDirectory = path;
    }


    public static File getExternalStorage() {

        File internalStorage = getInternalStorage();
        File parent = internalStorage.getParentFile().getParentFile();

        if (parent.exists()) {
            File extSD = new File(parent, "sdcard1");

            if (extSD.exists()) {
                return extSD;
            } else {
                File extSD1 = new File(parent, "MicroSD");
                if (extSD1.exists()) {
                    return extSD1;
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }

    }


    public static String convertDate(long dateInMs) {
        SimpleDateFormat df2 = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
        String dateText = df2.format(dateInMs);
        return dateText;
    }
}
