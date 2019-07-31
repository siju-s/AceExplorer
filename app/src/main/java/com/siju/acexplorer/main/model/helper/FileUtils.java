/*
 * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.siju.acexplorer.main.model.helper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.Formatter;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.siju.acexplorer.main.model.StorageUtils;
import com.siju.acexplorer.main.model.groups.Category;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.siju.acexplorer.main.model.groups.Category.AUDIO;
import static com.siju.acexplorer.main.model.groups.Category.FILES;
import static com.siju.acexplorer.main.model.groups.Category.IMAGE;
import static com.siju.acexplorer.main.model.groups.Category.VIDEO;


public class FileUtils {

    private static final String TAG = "FileUtils";
    public static final int ACTION_NONE = 0;
    public static final int ACTION_KEEP = 3;
    public static final String EXT_APK = "apk";


    public static boolean isFileMusic(String path) {
        return path.toLowerCase().endsWith(".mp3") || path.toLowerCase().endsWith(".amr") || path.
                toLowerCase().endsWith(".wav") || path.
                toLowerCase().endsWith(".m4a");
    }


    public static String getAbsolutePath(File file) {
        if (file == null) {
            return null;
        }
        return file.getAbsolutePath();
    }


    public static String convertDate(long dateInMs) {
        SimpleDateFormat df2 = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        Date date = new Date(dateInMs);
        return df2.format(date);
    }


    /**
     * Validates file name at the time of creation
     * special reserved characters shall not be allowed in the file names
     *
     * @param name the file which needs to be validated
     * @return boolean if the file name is valid or invalid
     */
    public static boolean isFileNameInvalid(String name) {

       /* StringBuilder builder = new StringBuilder(file.getPath());
        String newName = builder.substring(builder.lastIndexOf("/") + 1, builder.length());*/
        String newName = name.trim();
        return newName.contains("/") || newName.length() == 0;
    }


    public static void showMessage(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }


    public static long getFolderSize(File directory) {
        long length = 0;
        File[] fileList = directory.listFiles();
        if (fileList == null) {
            return length;
        }
        try {
            for (File file : fileList) {

                if (file.isFile()) {
                    length += file.length();
                } else {
                    length += getFolderSize(file);
                }
            }
        } catch (Exception ignored) {
        }
        return length;
    }


    public static OutputStream getOutputStream(@NonNull final File target, Context context) {

        OutputStream outStream = null;
        try {
            // First try the normal way
            if (isWritable(target)) {
                // standard way
                outStream = new FileOutputStream(target);
            } else {
                // Storage Access Framework
                DocumentFile targetDocument = StorageUtils.INSTANCE.getDocumentFile(target, false);
                if (targetDocument != null) {
                    outStream =
                            context.getContentResolver().openOutputStream(targetDocument.getUri());
                }

            }
        } catch (Exception ignored) {
        }
        return outStream;
    }


    /**
     * Check is a file is writable. Detects write issues on external SD card.
     *
     * @param file The file
     * @return true if the file is writable.
     */
    public static boolean isWritable(final File file) {
        if (file == null) {
            return false;
        }
        boolean isExisting = file.exists();

        try {
            FileOutputStream output = new FileOutputStream(file, true);
            try {
                output.close();
            } catch (IOException e) {
                // do nothing.
            }
        } catch (FileNotFoundException e) {
            return false;
        }
        boolean result = file.canWrite();

        // Ensure that file is not created during this process.
        if (!isExisting) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }

        return result;
    }


    private static OutputStream getOutputStream(Context context, String str) {
        OutputStream outputStream = null;
        Uri fileUri = UriHelper.INSTANCE.getUriFromFile(str, context);
        if (fileUri != null) {
            try {
                outputStream = context.getContentResolver().openOutputStream(fileUri);
            } catch (Throwable ignored) {
            }
        }
        return outputStream;
    }

    public static Category getCategoryFromExtension(String extension) {

        Category value = FILES;
        if (extension == null) {
            return FILES;
        }
        extension = extension.toLowerCase(); // necessary
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if (mimeType != null) {
            if (mimeType.indexOf("image") == 0) {
                value = IMAGE;
            } else if (mimeType.indexOf("video") == 0) {
                value = VIDEO;
            } else if (mimeType.indexOf("audio") == 0) {
                value = AUDIO;
            }
        }
        return value;
    }


    /**
     * Check for a directory if it is possible to create files within this directory, either via normal writing or via
     * Storage Access Framework.
     *
     * @param folder The directory
     * @return true if it is possible to write in this directory.
     */
    public static boolean isFileNonWritable(final File folder) {
        // Verify that this is a directory.
        if (folder == null) {
            return false;
        }
        if (!folder.exists() || !folder.isDirectory()) {
            return false;
        }

        // Find a non-existing file in this directory.
        int i = 0;
        File file;
        do {
            String fileName = "AugendiagnoseDummyFile" + (++i);
            file = new File(folder, fileName);
        }
        while (file.exists());

        // First check regular writability
        if (isWritable(file)) {
            return false;
        }

        // Next check SAF writability.
        DocumentFile document = StorageUtils.INSTANCE.getDocumentFile(file, false);

        if (document == null) {
            return true;
        }

        // This should have created the file - otherwise something is wrong with access URL.
        boolean result = document.canWrite() && file.exists();

        // Ensure that the dummy file is not remaining.
        deleteFile(file);
        return !result;
    }

    /**
     * Delete a file. May be even on external SD card.
     *
     * @param file the file to be deleted.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void deleteFile(@NonNull final File file) {
        // First try the normal deletion.
        if (file.delete()) {
            return;
        }

        // Try with Storage Access Framework.
        if (StorageUtils.INSTANCE.isOnExtSdCard(file)) {
            DocumentFile document = StorageUtils.INSTANCE.getDocumentFile(file, false);
            if (document != null) {
                document.delete();
            }
            return;
        }
        file.exists();
    }


    public static boolean isPackageIntentUnavailable(Context context, Intent intent) {
        return context.getPackageManager().resolveActivity(intent, 0) == null;
    }

    public static boolean isFileCompressed(String filePath) {
        return filePath.toLowerCase().endsWith(".zip") ||
                filePath.toLowerCase().endsWith(".tar") ||
                filePath.toLowerCase().endsWith(".tar.gz");
    }

    public static boolean isApk(String extension) {
        return EXT_APK.equals(extension);
    }


    public static boolean isFileExisting(String currentDir, String fileName) {
        File file = new File(currentDir);
        String[] list = file.list();
        if (list == null) {
            return false;
        }
        for (String aList : list) {
            if (fileName.equals(aList)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Gets the extension of a file name without ".".
     */
    public static String getExtension(String path) {
        if (path == null) {
            return null;
        }
        int dot = getLastDotIndex(path);
        return substring(path, dot + 1);
    }

    public static String getExtensionWithDot(String path) {
        if (path == null) {
            return null;
        }
        int dot = getLastDotIndex(path);
        return substring(path, dot);
    }

    private static int getLastDotIndex(String path) {
        return path.lastIndexOf(".");
    }

    private static String substring(String path, int index) {
        if (index >= 0) {
            return path.substring(index);
        } else {
            return "";
        }
    }

    public static String constructFileNameWithExtension(String fileName, String extension) {
        return fileName + "." + extension;
    }

    public static String getFileNameWithoutExt(String filePath) {
        if (filePath == null) {
            return null;
        }
        File file = new File(filePath);
        String fileName;
        if (file.isFile()) {
            int dotIndex = getLastDotIndex(filePath);
            int fileNameIdx = filePath.lastIndexOf("/") + 1;
            if (dotIndex <= fileNameIdx) {
                return null;
            }
            fileName = filePath.substring(fileNameIdx, dotIndex);
        } else {
            fileName = file.getName();
        }
        return fileName;
    }

    public static String formatSize(Context context, long sizeInBytes) {
        return Formatter.formatFileSize(context, sizeInBytes);
    }

    public static String getFastHash(String filepath) {
        MessageDigest md;
        String hash;
        File file = new File(filepath);
        try {
            try {
                md = MessageDigest.getInstance("MD5");

            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("cannot initialize MD5 hash function", e);
            }
            FileInputStream fin = new FileInputStream(file);
            if (file.length() > 1048576L) {
                byte data[] = new byte[(int) file.length() / 100];
                //noinspection ResultOfMethodCallIgnored
                fin.read(data);
                fin.close();
                hash = new BigInteger(1, md.digest(data)).toString(16);
            } else if (file.length() > 1024L) {
                byte data[] = new byte[(int) file.length() / 10];
                //noinspection ResultOfMethodCallIgnored
                fin.read(data);
                fin.close();
                hash = new BigInteger(1, md.digest(data)).toString(16);
            } else {
                byte data[] = new byte[(int) file.length()];
                //noinspection ResultOfMethodCallIgnored
                fin.read(data);
                fin.close();
                hash = new BigInteger(1, md.digest(data)).toString(16);
            }
        } catch (IOException e) {
            throw new RuntimeException("cannot read file " + file.getAbsolutePath(), e);
        }
        return hash;
    }
}
