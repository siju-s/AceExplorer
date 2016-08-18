package com.siju.acexplorer.filesystem.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.helper.RootHelper;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.io.IOException;

/**
 * Created by Siju on 18-08-2016.
 */
public class FileOperations {

    private static final String TAG = "FileOperations";

    public interface ErrorCallBack {
        void exists(File file);

        void launchSAF(File file);

        void launchSAF(File oldFile, File newFile);

        void done(File file, boolean b);
    }

    public static void mkdir(final File file, final Context context, final boolean isRoot, @NonNull
    final ErrorCallBack errorCallBack) {
        if (file == null || errorCallBack == null) return;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                if (file.exists()) errorCallBack.exists(file);

//                if (file.isLocal() || isRoot) {
                int mode = FileUtils.checkFolder(new File(file.getParent()), context);
                if (mode == 2) {
                    errorCallBack.launchSAF(file);
                    return null;
                }
                if (mode == 1 || mode == 0)
                    FileUtils.mkdir(file, context);
                // Try the root way
                if (!file.exists() && isRoot) {
//                        file.setMode(HFile.ROOT_MODE);
                    if (file.exists()) errorCallBack.exists(file);
                    boolean remount = false;
                    try {
                        String res;
                        if (!("rw".equals(res = RootTools.getMountedAs(file.getParent()))))
                            remount = true;
                        if (remount)
                            RootTools.remount(file.getParent(), "rw");
                        RootHelper.runAndWait("mkdir \"" + file.getPath() + "\"", true);
                        if (remount) {
                            if (res == null || res.length() == 0) res = "ro";
                            RootTools.remount(file.getParent(), res);
                        }
                    } catch (Exception e) {
                        Logger.log(TAG, file.getPath());
                    }
                    errorCallBack.done(file, file.exists());
                    return null;
                }
                errorCallBack.done(file, file.exists());
                return null;
//                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public static void mkfile(final File file, final Context context, final boolean isRoot, @NonNull
    final ErrorCallBack errorCallBack) {
        if (file == null || errorCallBack == null) return;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                if (file.exists()) errorCallBack.exists(file);

//                if (file.isLocal() || isRoot) {
                int mode = FileUtils.checkFolder(new File(file.getParent()), context);
                if (mode == 2) {
                    errorCallBack.launchSAF(file);
                    return null;
                }
                if (mode == 1 || mode == 0)
                    try {
                        FileUtils.mkfile(file, context);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                // Try the root way
                if (!file.exists() && isRoot) {
//                    file.setMode(HFile.ROOT_MODE);
                    if (file.exists()) errorCallBack.exists(file);
                    boolean remount = false;
                    try {
                        String res;
                        if (!("rw".equals(res = RootTools.getMountedAs(file.getParent()))))
                            remount = true;
                        if (remount)
                            RootTools.remount(file.getParent(), "rw");
                        RootHelper.runAndWait("touch \"" + file.getPath() + "\"", true);
                        if (remount) {
                            if (res == null || res.length() == 0) res = "ro";
                            RootTools.remount(file.getParent(), res);
                        }
                    } catch (Exception e) {
                        Logger.log(TAG, file.getPath());
                    }
                    errorCallBack.done(file, file.exists());
                    return null;
                }
                errorCallBack.done(file, file.exists());
                return null;
//                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public static void rename(final File oldFile, final File newFile, final boolean rootMode,
                              final Context context, final ErrorCallBack errorCallBack) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (newFile.exists()) {
                    errorCallBack.launchSAF(oldFile, newFile);
                    return null;
                }

                int mode = FileUtils.checkFolder(oldFile.getParentFile(), context);
                if (mode == 2) {
                    errorCallBack.launchSAF(oldFile, newFile);
                } else if (mode == 1 || mode == 0) {
                    boolean b = FileUtils.renameFolder(oldFile, newFile, context);
                    boolean a = !oldFile.exists() && newFile.exists();
                    if (!a && rootMode) {
                        try {
                            FileUtils.renameRoot(oldFile, newFile.getName());
                        } catch (Exception e) {
                            Logger.log(TAG, oldFile.getPath() + "\n" + newFile.getPath());
                        }

                        a = !oldFile.exists() && newFile.exists();
                    }
                    errorCallBack.done(newFile, a);
                    return null;
                }

                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }
}
