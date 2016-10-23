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
        if (file == null) return;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                if (file.exists()) errorCallBack.exists(file);

//                if (file.isLocal() || isRoot) {
                int mode = new FileUtils().checkFolder(file.getParent(), context);
                if (mode == 2) {
                    errorCallBack.launchSAF(file);
                    return null;
                }
                else if (mode == 1 || mode == 0) {
                       boolean result = FileUtils.mkdir(file, context);
                    // Try the root way
                    if (!result && isRoot) {
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
                       result = file.exists();
                    }
                    errorCallBack.done(file, result);
                    return null;
                }
             return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public static void mkfile(final File file, final Context context, final boolean isRoot, @NonNull
    final ErrorCallBack errorCallBack) {
        if (file == null) return;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                if (file.exists()) errorCallBack.exists(file);

//                if (file.isLocal() || isRoot) {
                int mode = new FileUtils().checkFolder(file.getParent(), context);
                if (mode == 2) {
                    errorCallBack.launchSAF(file);
                    return null;
                }
                else if (mode == 1 || mode == 0) {
                    boolean result = false;
                    try {
                        result = FileUtils.mkfile(file, context);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // Try the root way
                    if (!result && isRoot) {
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
                        result = file.exists();
                    }
                    errorCallBack.done(file, result);
                    return null;
                }
                return null;
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

                int mode = new FileUtils().checkFolder(oldFile.getParentFile().getAbsolutePath(), context);
                Logger.log(TAG, "Rename--mode=" + mode);

                if (mode == 2) {
                    errorCallBack.launchSAF(oldFile, newFile);
                } else if (mode == 1 || mode == 0) {
                    boolean result = FileUtils.renameFolder(oldFile, newFile, context);
                    boolean a = !oldFile.exists() && newFile.exists();
                    Logger.log(TAG, "Rename--filexists=" + a + "rootmode=" + rootMode + "result==" + result);

                    if (!result) {
                        if (!a && rootMode) {
                            try {
                                FileUtils.renameRoot(oldFile, newFile.getName());
                            } catch (Exception e) {
                                Logger.log(TAG, oldFile.getPath() + "\n" + newFile.getPath());
                            }

                            result = !oldFile.exists() && newFile.exists();
                        }
                    }
                    errorCallBack.done(newFile, result);
                    return null;
                }

                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


}
