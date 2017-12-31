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

package com.siju.acexplorer.model;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.util.Log;

import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.R;
import com.siju.acexplorer.model.groups.StoragesGroup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static com.siju.acexplorer.model.groups.StoragesGroup.ANDROID_DATA;
import static com.siju.acexplorer.model.groups.StoragesGroup.STORAGE_SDCARD0;
import static com.siju.acexplorer.model.groups.StoragesGroup.STORAGE_SDCARD1;
import static com.siju.acexplorer.model.helper.SdkHelper.isAtleastKitkat;
import static com.siju.acexplorer.model.helper.SdkHelper.isAtleastMarsh;


public class StorageUtils {

    private final static Pattern DIR_SEPARATOR = Pattern.compile("/");

    public enum StorageType {
        ROOT,
        INTERNAL,
        EXTERNAL;


        public static String getStorageText(Context context, StorageType storageType) {
            switch (storageType) {
                case ROOT:
                    return context.getString(R.string.nav_menu_root);
                case INTERNAL:
                    return context.getString(R.string.nav_menu_internal_storage);
                default:
                    return context.getString(R.string.nav_menu_internal_storage);

            }
        }
    }

    public static String getStorageSpaceText(Context context, String text) {
        String arr[] = text.split("/");
        return arr[0] + " " + context.getResources().getString(R.string.msg_free) +
                " " + arr[1];
    }


    public static String getDownloadsDirectory() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    }

    public static String getInternalStorage() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static List<String> getStorageDirectories() {

        // Final set of paths
        final Set<String> paths = new LinkedHashSet<>();
        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
            // Device has physical external storage; use plain paths.
            if (TextUtils.isEmpty(rawExternalStorage)) {
                // EXTERNAL_STORAGE undefined; falling back to default.
                paths.add(STORAGE_SDCARD0);
            } else {
                paths.add(rawExternalStorage);
            }
        } else {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                rawUserId = "";
            } else {
                final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                final String[] folders = DIR_SEPARATOR.split(path);
                final String lastFolder = folders[folders.length - 1];
                boolean isDigit = false;
                try {
                    //noinspection ResultOfMethodCallIgnored
                    Integer.valueOf(lastFolder);
                    isDigit = true;
                } catch (NumberFormatException ignored) {
                }
                rawUserId = isDigit ? lastFolder : "";
            }
            // /storage/emulated/0[1,2,...]
            if (TextUtils.isEmpty(rawUserId)) {
                paths.add(rawEmulatedStorageTarget);
            } else {
                paths.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(paths, rawSecondaryStorages);
        }
        if (isAtleastMarsh()) {
            paths.clear();
        }
        if (isAtleastKitkat()) {
            String pathList[] = getExtSdCardPaths();
            Collections.addAll(paths, pathList);
        }

        File usb = getUsbDrive();
        if (usb != null && !paths.contains(usb.getPath())) {
            paths.add(usb.getPath());
        }
        return new ArrayList<>(paths);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static String[] getExtSdCardPaths() {
        Context context = AceApplication.getAppContext();
        List<String> paths = new ArrayList<>();
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null) {
                int index = file.getAbsolutePath().lastIndexOf(ANDROID_DATA);
                if (index < 0) {
                    Log.w("FileUtils", "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    if (!paths.contains(path)) {
                        paths.add(path);
                    }
                }
            }
        }

        File file = new File(STORAGE_SDCARD1);
        if (file.exists() && file.canExecute() && !paths.contains(file.getAbsolutePath())) {
            paths.add(STORAGE_SDCARD1);
        }
        return paths.toArray(new String[0]);
    }


    @SuppressLint("SdCardPath")
    private static File getUsbDrive() {
        File parent;
        parent = new File("/storage");

        File[] files = parent.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.exists() && f.getName().toLowerCase().contains("usb") && f.canExecute()) {
                    return f;
                }
            }
        }
        parent = new File("/mnt/sdcard/usbStorage");
        if (parent.exists() && parent.canExecute()) {
            return (parent);
        }
        parent = new File("/mnt/sdcard/usb_storage");
        if (parent.exists() && parent.canExecute()) {
            return parent;
        }

        return null;
    }


    /**
     * Determine the main folder of the external SD card containing the given file.
     *
     * @param file the file.
     * @return The main folder of the external SD card containing this file, if the file is on an SD card. Otherwise,
     * null is returned.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static String getExtSdCardFolder(final File file) {
        String[] extSdPaths = getExtSdCardPaths();
        try {
            for (String extSdPath : extSdPaths) {
                if (file.getCanonicalPath().startsWith(extSdPath)) {
                    return extSdPath;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    /**
     * Determine if a file is on external sd card. (Kitkat or higher.)
     *
     * @param file The file.
     * @return true if on external sd card.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isOnExtSdCard(final File file) {
        return getExtSdCardFolder(file) != null;
    }

    /**
     * Get a DocumentFile corresponding to the given file (for writing on ExtSdCard on Android 5). If the file is not
     * existing, it is created.
     *
     * @param file        The file.
     * @param isDirectory flag indicating if the file should be a directory.
     * @return The DocumentFile
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static DocumentFile getDocumentFile(final File file, final boolean isDirectory) {
        String baseFolder = getExtSdCardFolder(file);
        boolean originalDirectory = false;
        if (baseFolder == null) {
            return null;
        }

        String relativePath = null;
        try {
            String fullPath = file.getCanonicalPath();
            if (!baseFolder.equals(fullPath)) {
                relativePath = fullPath.substring(baseFolder.length() + 1);
            } else {
                originalDirectory = true;
            }
        } catch (IOException e) {
            return null;
        } catch (Exception f) {
            originalDirectory = true;
        }
        Context context = AceApplication.getAppContext();
        String as = PreferenceManager.getDefaultSharedPreferences(context).getString(FileConstants.SAF_URI, null);

        Uri treeUri = null;
        if (as != null) {
            treeUri = Uri.parse(as);
        }
        if (treeUri == null) {
            return null;
        }

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);
        if (originalDirectory) {
            return document;
        }
        String[] parts = relativePath.split("/");
        for (int i = 0; i < parts.length; i++) {
            DocumentFile nextDocument = document.findFile(parts[i]);

            if (nextDocument == null) {
                if ((i < parts.length - 1) || isDirectory) {
                    nextDocument = document.createDirectory(parts[i]);
                } else {
                    nextDocument = document.createFile("image", parts[i]);
                }
            }
            document = nextDocument;
        }

        return document;
    }


    public static long getSpaceLeft(File file) {
        return file.getFreeSpace();
    }

    public static long getTotalSpace(File file) {
        return file.getTotalSpace();
    }

    public static boolean isRootDirectory(String path) {
        if (path.contains(getInternalStorage())) {
            return false;
        } else if (StoragesGroup.getInstance().getExternalSDList().size() > 0) {
            for (String extPath : StoragesGroup.getInstance().getExternalSDList()) {
                if (path.contains(extPath)) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }

}
