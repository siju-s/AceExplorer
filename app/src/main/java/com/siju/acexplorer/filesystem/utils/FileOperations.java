package com.siju.acexplorer.filesystem.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.operations.OperationUtils;

import java.io.File;

public class FileOperations {

    private static final String TAG = "FileOperations";

    public interface FileOperationCallBack {
        void exists();

        void launchSAF(File file);

        void launchSAF(File oldFile, File newFile);

        void opCompleted(File file, boolean b);
    }

    public static void mkdir(final Context context, final File file, final boolean isRoot, @NonNull
    final FileOperationCallBack fileOperationCallBack) {
        if (file == null) return;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                if (file.exists()) fileOperationCallBack.exists();

                OperationUtils.WriteMode mode = OperationUtils.checkFolder(file.getParent(), context);
                if (mode == OperationUtils.WriteMode.EXTERNAL) {
                    fileOperationCallBack.launchSAF(file);
                    return null;
                } else if (mode == OperationUtils.WriteMode.INTERNAL || mode == OperationUtils.WriteMode.ROOT) {
                    boolean result = FileUtils.mkdir(file, context);
                    // Try the root way
                    if (!result && isRoot) {
                        if (file.exists()) fileOperationCallBack.exists();
                        try {
                            String parentPath = file.getParent();
                            RootUtils.mountRW(parentPath);
                            RootUtils.mkDir(file.getAbsolutePath());
                            RootUtils.mountRO(parentPath);
                        } catch (RootNotPermittedException e) {
                            Logger.log(TAG, file.getAbsolutePath());
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

    public static void mkfile(final Context context, final File file, final boolean isRoot, @NonNull
    final FileOperationCallBack fileOperationCallBack) {
        if (file == null) return;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                if (file.exists()) fileOperationCallBack.exists();

                OperationUtils.WriteMode mode = OperationUtils.checkFolder(file.getParent(), context);
                if (mode == OperationUtils.WriteMode.EXTERNAL) {
                    fileOperationCallBack.launchSAF(file);
                    return null;
                } else if (mode == OperationUtils.WriteMode.INTERNAL || mode == OperationUtils.WriteMode.ROOT) {
                    boolean result = FileUtils.mkfile(file, context);

                    // Try the root way
                    if (!result && isRoot) {
                        if (file.exists()) fileOperationCallBack.exists();
                        try {
                            String parentPath = file.getParent();
                            RootUtils.mountRW(parentPath);
                            RootUtils.mkFile(file.getAbsolutePath());
                            RootUtils.mountRO(parentPath);
                        } catch (RootNotPermittedException e) {
                            Logger.log(TAG, file.getAbsolutePath());
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

    public static void rename(final Context context, final File oldFile, final File newFile, final boolean rootMode,
                              final FileOperationCallBack fileOperationCallBack) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (newFile.exists()) {
                    fileOperationCallBack.launchSAF(oldFile, newFile);
                    return null;
                }

                OperationUtils.WriteMode mode = OperationUtils.checkFolder(oldFile.getParentFile().getAbsolutePath(), context);
                Logger.log(TAG, "Rename--mode=" + mode);

                if (mode == OperationUtils.WriteMode.EXTERNAL) {
                    fileOperationCallBack.launchSAF(oldFile, newFile);
                } else if (mode == OperationUtils.WriteMode.INTERNAL || mode == OperationUtils.WriteMode.ROOT) {

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
