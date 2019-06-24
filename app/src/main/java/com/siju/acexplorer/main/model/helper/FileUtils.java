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

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.format.Formatter;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.logging.Logger;
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
import java.util.HashMap;
import java.util.Locale;

import static com.siju.acexplorer.main.model.groups.Category.AUDIO;
import static com.siju.acexplorer.main.model.groups.Category.FILES;
import static com.siju.acexplorer.main.model.groups.Category.IMAGE;
import static com.siju.acexplorer.main.model.groups.Category.VIDEO;
import static com.siju.acexplorer.main.model.helper.SdkHelper.isAtleastLollipop;
import static com.siju.acexplorer.main.model.helper.SdkHelper.isKitkat;


public class FileUtils {

    private static final String TAG = "FileUtils";
    public static final int ACTION_NONE = 0;
    public static final int ACTION_KEEP = 3;
    private static final HashMap<String, String> MIME_TYPES = new HashMap<>();
    public static final String EXT_APK = "apk";


    static {

		/*
         * ================= MIME TYPES ====================
		 */
        MIME_TYPES.put("asm", "text/x-asm");
        MIME_TYPES.put("def", "text/plain");
        MIME_TYPES.put("in", "text/plain");
        MIME_TYPES.put("rc", "text/plain");
        MIME_TYPES.put("list", "text/plain");
        MIME_TYPES.put("log", "text/plain");
        MIME_TYPES.put("pl", "text/plain");
        MIME_TYPES.put("prop", "text/plain");
        MIME_TYPES.put("properties", "text/plain");
        MIME_TYPES.put("rc", "text/plain");

        MIME_TYPES.put("epub", "application/epub+zip");
        MIME_TYPES.put("ibooks", "application/x-ibooks+zip");

        MIME_TYPES.put("ifb", "text/calendar");
        MIME_TYPES.put("eml", "message/rfc822");
        MIME_TYPES.put("msg", "application/vnd.ms-outlook");

        MIME_TYPES.put("ace", "application/x-ace-compressed");
        MIME_TYPES.put("bz", "application/x-bzip");
        MIME_TYPES.put("bz2", "application/x-bzip2");
        MIME_TYPES.put("cab", "application/vnd.ms-cab-compressed");
        MIME_TYPES.put("gz", "application/x-gzip");
        MIME_TYPES.put("lrf", "application/octet-stream");
        MIME_TYPES.put("jar", "application/java-archive");
        MIME_TYPES.put("xz", "application/x-xz");
        MIME_TYPES.put("Z", "application/x-compress");

        MIME_TYPES.put("bat", "application/x-msdownload");
        MIME_TYPES.put("ksh", "text/plain");
        MIME_TYPES.put("sh", "application/x-sh");

        MIME_TYPES.put("db", "application/octet-stream");
        MIME_TYPES.put("db3", "application/octet-stream");

        MIME_TYPES.put("otf", "application/x-font-otf");
        MIME_TYPES.put("ttf", "application/x-font-ttf");
        MIME_TYPES.put("psf", "application/x-font-linux-psf");

        MIME_TYPES.put("cgm", "image/cgm");
        MIME_TYPES.put("btif", "image/prs.btif");
        MIME_TYPES.put("dwg", "image/vnd.dwg");
        MIME_TYPES.put("dxf", "image/vnd.dxf");
        MIME_TYPES.put("fbs", "image/vnd.fastbidsheet");
        MIME_TYPES.put("fpx", "image/vnd.fpx");
        MIME_TYPES.put("fst", "image/vnd.fst");
        MIME_TYPES.put("mdi", "image/vnd.ms-mdi");
        MIME_TYPES.put("npx", "image/vnd.net-fpx");
        MIME_TYPES.put("xif", "image/vnd.xiff");
        MIME_TYPES.put("pct", "image/x-pict");
        MIME_TYPES.put("pic", "image/x-pict");

        MIME_TYPES.put("adp", "audio/adpcm");
        MIME_TYPES.put("au", "audio/basic");
        MIME_TYPES.put("snd", "audio/basic");
        MIME_TYPES.put("m2a", "audio/mpeg");
        MIME_TYPES.put("m3a", "audio/mpeg");
        MIME_TYPES.put("oga", "audio/ogg");
        MIME_TYPES.put("spx", "audio/ogg");
        MIME_TYPES.put("aac", "audio/x-aac");
        MIME_TYPES.put("mka", "audio/x-matroska");

        MIME_TYPES.put("jpgv", "video/jpeg");
        MIME_TYPES.put("jpgm", "video/jpm");
        MIME_TYPES.put("jpm", "video/jpm");
        MIME_TYPES.put("mj2", "video/mj2");
        MIME_TYPES.put("mjp2", "video/mj2");
        MIME_TYPES.put("mpa", "video/mpeg");
        MIME_TYPES.put("ogv", "video/ogg");
        MIME_TYPES.put("flv", "video/x-flv");
        MIME_TYPES.put("mkv", "video/x-matroska");

    }

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


    public static boolean isMediaScanningRequired(String mimeType) {
        return mimeType != null && (mimeType.startsWith("audio") ||
                mimeType.startsWith("video") || mimeType.startsWith("image"));
    }


    public static OutputStream getOutputStream(@NonNull final File target, Context context) {

        OutputStream outStream = null;
        try {
            // First try the normal way
            if (isWritable(target)) {
                // standard way
                outStream = new FileOutputStream(target);
            } else {
                if (isAtleastLollipop()) {
                    // Storage Access Framework
                    DocumentFile targetDocument = StorageUtils.INSTANCE.getDocumentFile(target, false);
                    if (targetDocument != null) {
                        outStream =
                                context.getContentResolver().openOutputStream(targetDocument.getUri());
                    }
                } else if (isKitkat()) {
                    // Workaround for Kitkat ext SD card
                    return getOutputStream(context, target.getPath());
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


    public static String getMimeType(File file) {
        if (file.isDirectory()) {
            return null;
        }

        String type = "*/*";
        final String extension = getExtension(file.getName());

        if (extension != null && !extension.isEmpty()) {
            final String extensionLowerCase = extension.toLowerCase(Locale
                    .getDefault());
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extensionLowerCase);
            if (type == null) {
                type = MIME_TYPES.get(extensionLowerCase);
            }
        }
        if (type == null) {
            type = "*/*";
        }
        return type;
    }


    /**
     * Check for a directory if it is possible to create files within this directory, either via normal writing or via
     * Storage Access Framework.
     *
     * @param folder The directory
     * @return true if it is possible to write in this directory.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
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
     * @return True if successfully deleted.
     */
    static boolean deleteFile(@NonNull final File file) {
        // First try the normal deletion.
        boolean fileDelete = deleteFilesInFolder(file);
        if (file.delete() || fileDelete) {
            return true;
        }

        // Try with Storage Access Framework.
        if (isAtleastLollipop() && StorageUtils.INSTANCE.isOnExtSdCard(file)) {

            DocumentFile document = StorageUtils.INSTANCE.getDocumentFile(file, false);
            return document != null && document.delete();
        }

        // Try the Kitkat workaround.
        if (isKitkat()) {
            Context context = AceApplication.Companion.getAppContext();
            ContentResolver resolver = context.getContentResolver();

            try {
                Uri uri = UriHelper.INSTANCE.getUriFromFile(file.getAbsolutePath(), context);
                if (uri != null) {
                    resolver.delete(uri, null, null);
                }
                return !file.exists();
            } catch (Exception e) {
                Logger.log(TAG, "Error when deleting file " + file.getAbsolutePath());
                return false;
            }
        }

        return !file.exists();
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

    /**
     * Delete all files in a folder.
     *
     * @param file the folder
     * @return true if successful.
     */
    private static boolean deleteFilesInFolder(final File file) {
        boolean totalSuccess;
        if (file == null) {
            return false;
        }
        String path = file.getAbsolutePath();
        if (file.isDirectory()) {
            File [] fileList = file.listFiles();
            if (fileList != null) {
                for (File child : fileList) {
                    deleteFilesInFolder(child);
                }
            }
        }
        totalSuccess = file.delete();
        if (totalSuccess) {
            MediaStoreHelper.removeMedia(AceApplication.Companion.getAppContext(), path, 0);
        }
        return totalSuccess;
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
        return substring(path,dot + 1);
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

    static String getFileNameWithoutExt(String filePath) {
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
