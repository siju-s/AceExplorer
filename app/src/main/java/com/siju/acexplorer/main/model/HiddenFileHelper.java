package com.siju.acexplorer.main.model;

import android.provider.MediaStore;

import java.io.File;

public class HiddenFileHelper {

    public static boolean shouldSkipHiddenFiles(File file, boolean showHidden) {
        return file.isHidden() && !showHidden;
    }

    public static String constructionNoHiddenFilesArgs() {
        return MediaStore.Files.FileColumns.DATA + " NOT LIKE '%/.%'";
    }
}
