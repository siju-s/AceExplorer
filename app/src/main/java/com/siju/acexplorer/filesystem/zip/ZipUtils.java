package com.siju.acexplorer.filesystem.zip;


public class ZipUtils {

    /**
     * To be used when RAR as viewable not needed
     *
     * @param filePath
     * @return
     */
    public static boolean isZipViewable(String filePath) {
        return filePath.toLowerCase().endsWith(".zip") ||
                filePath.toLowerCase().endsWith(".jar") ||
                filePath.toLowerCase().endsWith(".apk");
    }
}
