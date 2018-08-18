package com.siju.acexplorer.model;

import java.io.File;

public class HiddenFileHelper {

    public static boolean shouldSkipHiddenFiles(File file, boolean showHidden) {
        return file.isHidden() && !showHidden;
    }
}
