package com.siju.acexplorer.filesystem.groups;

import java.io.File;

/**
 * Created by SJ on 19-01-2017.
 */

public class StoragesInfo {



    public static long getSpaceLeft(File file) {
        return file.getFreeSpace();
    }

    public static long getTotalSpace(File file) {
        return file.getTotalSpace();
    }


}
