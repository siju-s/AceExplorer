package com.siju.acexplorer.main.model;

import java.io.File;

public class HiddenFileHelper {

    public static boolean shouldSkipHiddenFiles(File file, boolean showHidden) {
        return file.isHidden() && !showHidden;
    }
}
