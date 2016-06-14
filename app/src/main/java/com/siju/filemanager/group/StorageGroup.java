package com.siju.filemanager.group;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.text.format.Formatter;

import com.siju.filemanager.R;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Created by Siju on 12-06-2016.
 */

public class StorageGroup {

    private String storageName;
    private String spaceLeft;
    private int storageImage;
    private Context mContext;
    private StorageManager mStorageManager;
    public static String STORAGE_ROOT, STORAGE_INTERNAL, STORAGE_EXTERNAL;


    public StorageGroup(Context context) {
        mContext = context;
//        mStorageManager = context.getSystemService(StorageManager.class);
        STORAGE_ROOT = context.getResources().getString(R.string.nav_menu_root);
        STORAGE_INTERNAL = context.getResources().getString(R.string.nav_menu_internal_storage);
        STORAGE_EXTERNAL = context.getResources().getString(R.string.nav_menu_ext_storage);

    }


    public static File getRootDirectory() {
        return Environment.getRootDirectory();
    }

    public static File getInternalStorage() {
        return Environment.getExternalStorageDirectory();
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

    public String getSpaceLeft(File path) {
        String freeSpace = Formatter.formatFileSize(mContext, path.getFreeSpace());
        return freeSpace;
    }

    public String getTotalSpace(File path) {
        String totalSpace = Formatter.formatFileSize(mContext, path.getTotalSpace());
        return totalSpace;
    }

    public static String convertDate(long dateInMs)
    {
        SimpleDateFormat df2 = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
        String dateText = df2.format(dateInMs);
        return dateText;
    }


}
