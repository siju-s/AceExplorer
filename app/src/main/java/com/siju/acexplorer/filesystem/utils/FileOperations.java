package com.siju.acexplorer.filesystem.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.helper.RootHelper;
import com.siju.acexplorer.helper.root.RootTools;

import java.io.File;

public class FileOperations {

    private static final String TAG = "FileOperations";

    public interface FileOperationCallBack {
        void exists();

        void launchSAF(File file);

        void launchSAF(File oldFile, File newFile);

        void opCompleted(File file, boolean b);
    }

    public static void mkdir(final String path, final String fileName, final Context context, final boolean isRoot, @NonNull
    final FileOperationCallBack fileOperationCallBack) {
        if (path == null) return;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                File newFile = new File(path + File.separator + fileName);

                if (newFile.exists()) fileOperationCallBack.exists();

//                if (file.isLocal() || isRoot) {
                int mode = new FileUtils().checkFolder(path, context);
                if (mode == 2) {
                    fileOperationCallBack.launchSAF(newFile);
                    return null;
                } else if (mode == 1 || mode == 0) {
                    boolean result = FileUtils.mkdir(newFile, context);
                    // Try the root way
                    if (!result && isRoot) {
                        if (newFile.exists()) fileOperationCallBack.exists();
                        boolean remount = false;
                        try {
                            RootUtils.mountRW(path);
                            RootUtils.mkDir(path, fileName);
                            RootUtils.mountRO(path);

             /*               String res;
                            if (!("rw".equals(res = RootTools.getMountedAs(file.getParent()))))
                                remount = true;
                            if (remount)
                                RootTools.remount(file.getParent(), "rw");
                            RootHelper.runAndWait("mkdir \"" + file.getPath() + "\"", true);
                            if (remount) {
                                if (res == null || res.length() == 0) res = "ro";
                                RootTools.remount(file.getParent(), res);
                            }*/
                        } catch (RootNotPermittedException e) {
                            Logger.log(TAG, newFile.getPath());
                        }
                        result = newFile.exists();
                    }
                    fileOperationCallBack.opCompleted(newFile, result);
                    return null;
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public static void mkfile(final File file, final Context context, final boolean isRoot, @NonNull
    final FileOperationCallBack fileOperationCallBack) {
        if (file == null) return;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                if (file.exists()) fileOperationCallBack.exists();

//                if (file.isLocal() || isRoot) {
                int mode = new FileUtils().checkFolder(file.getParent(), context);
                if (mode == 2) {
                    fileOperationCallBack.launchSAF(file);
                    return null;
                } else if (mode == 1 || mode == 0) {
                    boolean result = FileUtils.mkfile(file, context);

                    // Try the root way
                    if (!result && isRoot) {
                        if (file.exists()) fileOperationCallBack.exists();
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
                    fileOperationCallBack.opCompleted(file, result);
                    return null;
                }
                return null;
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public static void rename(final File oldFile, final File newFile, final boolean rootMode,
                              final Context context, final FileOperationCallBack fileOperationCallBack) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (newFile.exists()) {
                    fileOperationCallBack.launchSAF(oldFile, newFile);
                    return null;
                }

                int mode = new FileUtils().checkFolder(oldFile.getParentFile().getAbsolutePath(), context);
                Logger.log(TAG, "Rename--mode=" + mode);

                if (mode == 2) {
                    fileOperationCallBack.launchSAF(oldFile, newFile);
                } else if (mode == 1 || mode == 0) {
                    boolean result = FileUtils.renameFolder(oldFile, newFile, context);
                    boolean a = !oldFile.exists() && newFile.exists();
                    Logger.log(TAG, "Rename--filexists=" + a + "rootmode=" + rootMode + "result==" + result);

                    if (!result) {
                        if (!a && rootMode) {
                            try {
                                FileUtils.renameRoot(oldFile, newFile.getName());
                            } catch (RootNotPermittedException e) {
                                Logger.log(TAG, oldFile.getPath() + "\n" + newFile.getPath());
                            }

                            result = !oldFile.exists() && newFile.exists();
                        }
                    }
                    fileOperationCallBack.opCompleted(newFile, result);
                    return null;
                }

                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


}
