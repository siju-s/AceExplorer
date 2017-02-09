package com.siju.acexplorer.filesystem.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.operations.OperationUtils;
import com.siju.acexplorer.filesystem.root.RootDeniedException;
import com.siju.acexplorer.filesystem.root.RootUtils;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import static com.siju.acexplorer.filesystem.operations.OperationUtils.WriteMode.EXTERNAL;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.WriteMode.INTERNAL;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.WriteMode.ROOT;
import static com.siju.acexplorer.filesystem.root.RootOperations.renameRoot;
import static com.siju.acexplorer.filesystem.storage.StorageUtils.getDocumentFile;
import static com.siju.acexplorer.filesystem.storage.StorageUtils.isOnExtSdCard;
import static com.siju.acexplorer.filesystem.utils.FileUtils.deleteFile;
import static com.siju.acexplorer.filesystem.utils.FileUtils.getMimeType;
import static com.siju.acexplorer.filesystem.utils.FileUtils.isWritable;
import static com.siju.acexplorer.utils.Utils.isAtleastLollipop;
import static com.siju.acexplorer.utils.Utils.isKitkat;

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
                if (mode == EXTERNAL) {
                    fileOperationCallBack.launchSAF(file);
                    return null;
                } else if (mode == INTERNAL || mode == ROOT) {
                    boolean result = mkdir(file, context);

                    if (!result && isRoot) {
                        if (file.exists()) fileOperationCallBack.exists();
                        try {
                            String parentPath = file.getParent();
                            RootUtils.mountRW(parentPath);
                            RootUtils.mkDir(file.getAbsolutePath());
                            RootUtils.mountRO(parentPath);
                        } catch (RootDeniedException e) {
                            Logger.log(TAG, file.getAbsolutePath());
                        }
                        result = true;

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
                if (mode == EXTERNAL) {
                    fileOperationCallBack.launchSAF(file);
                    return null;
                } else if (mode == INTERNAL || mode == ROOT) {
                    boolean result = mkfile(file, context);

                    // Try the root way
                    if (!result && isRoot) {
                        RootTools.debugMode = true;
                        if (file.exists()) fileOperationCallBack.exists();
                        try {
                            String parentPath = file.getParent();
                            RootUtils.mountRW(parentPath);
                            RootUtils.mkFile(file.getAbsolutePath());
                            RootUtils.mountRO(parentPath);
                        } catch (RootDeniedException e) {
                            Logger.log(TAG, file.getAbsolutePath());
                        }
                        Log.d(TAG, "AFTER CMD FINISH mkfile: "+file.getAbsolutePath());

                        result = true;
//                        Log.d(TAG, "Result: "+result);

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

                if (mode == EXTERNAL) {
                    fileOperationCallBack.launchSAF(oldFile, newFile);
                } else if (mode == INTERNAL || mode == ROOT) {

                    boolean result = renameFolder(oldFile, newFile, context);
                    boolean exists = !oldFile.exists() && newFile.exists();
                    Logger.log(TAG, "Rename--filexists=" + exists + "rootmode=" + rootMode + "result==" + result);

                    if (!result) {
                        if (!exists && rootMode) {
                            try {
                                renameRoot(oldFile, newFile.getName());
                            } catch (RootDeniedException e) {
                                Logger.log(TAG, oldFile.getPath() + "\n" + newFile.getPath());
                            }

                            result = true;
                        }
                    }
                    fileOperationCallBack.opCompleted(newFile, result);
                    return null;
                }

                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    /**
     * Rename a folder. In case of extSdCard in Kitkat, the old folder stays in place, but files are moved.
     *
     * @param source The source folder.
     * @param target The target folder.
     * @return true if the renaming was successful.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean renameFolder(@NonNull final File source, @NonNull final File target, Context context) {
        // First try the normal rename.
        if (rename(source, target.getName())) {
            return true;
        }
        if (target.exists()) {
            return false;
        }

        // Try the Storage Access Framework if it is just a rename within the same parent folder.
        if (isAtleastLollipop()
                && source.getParent().equals(target.getParent()) && isOnExtSdCard(source, context)) {

            DocumentFile document = getDocumentFile(source, true, context);

            Log.d(TAG, " Document uri in rename=" + document);
            if (document != null && document.renameTo(target.getName())) {
                return true;
            }
        }

        if (source.isFile()) {
            Logger.log(TAG, "Rename--root=");


            if (!mkfile(target, context)) {
                return false;
            }

        } else {
            // Try the manual way, moving files individually.
            if (!mkdir(target, context)) {
                return false;
            }
        }

        File[] sourceFiles = source.listFiles();

        if (sourceFiles == null || sourceFiles.length == 0) {
            return true;
        }

        for (File sourceFile : sourceFiles) {
            String fileName = sourceFile.getName();
            File targetFile = new File(target, fileName);
            if (!copyFile(sourceFile, targetFile, context)) {
                // stop on first error
                return false;
            }
        }
        // Only after successfully copying all files, delete files on source folder.
        for (File sourceFile : sourceFiles) {
            if (!deleteFile(sourceFile, context)) {
                // stop on first error
                return false;
            }
        }
        return true;
    }

    static boolean mkfile(final File file, Context context) {
        if (file == null)
            return false;
        if (file.exists()) {
            // nothing to create.
            return !file.isDirectory();
        }

        // Try the normal way
        try {
            if (file.createNewFile()) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Even after exception, In Kitkat file might have got created so return true
        if (file.exists()) return true;
        boolean result;
        // Try with Storage Access Framework.
        if (isAtleastLollipop() && isOnExtSdCard(file, context)) {
            DocumentFile document = getDocumentFile(file.getParentFile(), true, context);
            // getDocumentFile implicitly creates the directory.
            try {
                Logger.log(TAG, "mkfile--doc=" + document);
                result = document != null && document.createFile(getMimeType(file), file.getName()) != null;
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        if (isKitkat()) {
            try {
                return MediaStoreHack.mkfile(context, file);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Create a folder. The folder may even be on external SD card for Kitkat.
     *
     * @param file The folder to be created.
     * @return True if creation was successful.
     */
    public static boolean mkdir(final File file, Context context) {
        if (file == null)
            return false;
        if (file.exists()) {
            // nothing to create.
            return file.isDirectory();
        }

        // Try the normal way
        if (file.mkdirs()) {
            return true;
        }

        // Try with Storage Access Framework.
        if (isAtleastLollipop() && isOnExtSdCard(file, context)) {
            DocumentFile document = getDocumentFile(file, true, context);
            // getDocumentFile implicitly creates the directory.
            return document != null && document.exists();
        }

        // Try the Kitkat workaround.
        if (isKitkat()) {
            try {
                return MediaStoreHack.mkdir(context, file);
            } catch (IOException e) {
                return false;
            }
        }

        return false;
    }


    /**
     * Copy a file. The target file may even be on external SD card for Kitkat.
     *
     * @param source The source file
     * @param target The target file
     * @return true if the copying was successful.
     */
    @SuppressWarnings("null")
    private static boolean copyFile(final File source, final File target, Context context) {
        FileInputStream inStream = null;
        OutputStream outStream = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inStream = new FileInputStream(source);

            // First try the normal way
            if (isWritable(target)) {
                // standard way
                outStream = new FileOutputStream(target);
                inChannel = inStream.getChannel();
                outChannel = ((FileOutputStream) outStream).getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } else {
                if (isAtleastLollipop()) {
                    // Storage Access Framework
                    DocumentFile targetDocument = getDocumentFile(target, false, context);
                    if (targetDocument != null)
                        outStream =
                                context.getContentResolver().openOutputStream(targetDocument.getUri());
                } else if (isKitkat()) {
                    // Workaround for Kitkat ext SD card
                    Uri uri = MediaStoreHack.getUriFromFile(target.getAbsolutePath(), context);
                    if (uri != null)
                        outStream = context.getContentResolver().openOutputStream(uri);
                } else {
                    return false;
                }

                if (outStream != null) {
                    // Both for SAF and for Kitkat, write to output stream.
                    byte[] buffer = new byte[16384]; // MAGIC_NUMBER
                    int bytesRead;
                    while ((bytesRead = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);
                    }
                }

            }
        } catch (Exception e) {
            Log.e(TAG,
                    "Error when copying file from " + source.getAbsolutePath() + " to " + target.getAbsolutePath(), e);
            return false;
        } finally {
            try {
                if (inStream != null) inStream.close();
                if (outStream != null) outStream.close();
                if (inChannel != null) inChannel.close();
                if (outChannel != null) outChannel.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return true;
    }

    private static boolean rename(File f, String name) {
        String newname = f.getParent() + "/" + name;
        if (f.getParentFile().canWrite()) {
            Logger.log(TAG, "Rename--canWrite=" + true);
            return f.renameTo(new File(newname));
        }
        return false;
    }


}
