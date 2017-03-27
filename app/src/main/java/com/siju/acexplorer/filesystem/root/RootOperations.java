package com.siju.acexplorer.filesystem.root;


import android.util.Log;

import java.io.File;

public class RootOperations {


    public static void renameRoot(File sourceFile, String newFileName) throws RootDeniedException {
        String destinationPath = sourceFile.getParent() + File.separator + newFileName;
        RootUtils.mountRW(sourceFile.getPath());
        RootUtils.move(sourceFile.getPath(), destinationPath);
        RootUtils.mountRO(sourceFile.getPath());

/*        if (!("rw".equals(res = RootTools.getMountedAs(sourceFile.getParent()))))
            remount = true;
        if (remount)
            RootTools.remount(sourceFile.getParent(), "rw");
        RootHelper.runAndWait("mv \"" + sourceFile.getPath() + "\" \"" + destinationPath + "\"", true);
        if (remount) {
            if (res == null || res.length() == 0) res = "ro";
            RootTools.remount(sourceFile.getParent(), res);
        }*/
    }

    public static boolean fileExists(String path, boolean isDir) throws RootDeniedException {
        Log.d("RootOPerations", "fileExists: "+path);
        RootUtils.mountRW(path);
        boolean exists = RootUtils.fileExists(path, isDir);
        RootUtils.mountRO(path);
        return exists;
    }
}
