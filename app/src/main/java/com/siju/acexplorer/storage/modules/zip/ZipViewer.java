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

package com.siju.acexplorer.storage.modules.zip;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import android.view.View;

import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.main.model.FileConstants;
import com.siju.acexplorer.main.view.dialog.DialogHelper;
import com.siju.acexplorer.storage.model.ZipModel;
import com.siju.acexplorer.storage.model.task.ExtractZipEntry;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static android.webkit.MimeTypeMap.getSingleton;
import static com.siju.acexplorer.main.model.groups.Category.FILES;
import static com.siju.acexplorer.main.model.groups.Category.ZIP_VIEWER;
import static com.siju.acexplorer.main.model.helper.UriHelper.createContentUri;
import static com.siju.acexplorer.main.model.helper.UriHelper.grantUriPermission;
import static com.siju.acexplorer.main.view.dialog.DialogHelper.openWith;

public class ZipViewer implements LoaderManager.LoaderCallbacks<ArrayList<FileInfo>> {

    private final String TAG = this.getClass().getSimpleName();
    private static final String EXT_ZIP = ".zip";

    private static final int LOADER_ID = 1000;

    private ArrayList<ZipModel> zipChildren = new ArrayList<>();

    private Fragment fragment;

    private ZipEntry zipEntry;

    private String currentDir;
    private String zipParentPath;
    private String zipPath;
    private boolean inChildZip;

    private String zipEntryFileName;
    private boolean isHomeScreenEnabled;
    private ZipCommunicator zipCommunicator;

    enum ZipFormats {
        ZIP,
        APK;

        static final String zip = "zip";
        static final String apk = "apk";

        public static ZipFormats getFormatFromExt(String extension) {
            switch (extension) {
                case zip:
                    return ZIP;
                case apk:
                    return APK;
            }
            return ZIP;
        }
    }


    public ZipViewer(ZipCommunicator zipCommunicator, Fragment fragment, String path) {
        this.fragment = fragment;
        this.zipCommunicator = zipCommunicator;
        zipParentPath = zipPath = path;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AceApplication.getAppContext());
        isHomeScreenEnabled = preferences.getBoolean(FileConstants.PREFS_HOMESCREEN, true);
        zipCommunicator.setInitialDir(path);
        setNavDirectory(path);
        zipCommunicator.addToBackStack(path, FILES);
    }

    void setZipData(ArrayList<ZipModel> zipData) {
        zipChildren = zipData;
    }

    private void reloadList() {
        fragment.getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    private String createCacheDirExtract() {
        String cacheTempDir = ".tmp";
        File cacheDir = fragment.getActivity().getExternalCacheDir();
        if (cacheDir == null) {
            return null;
        }
        File file = new File(cacheDir.getParent(), cacheTempDir);

        if (!file.exists()) {
            boolean result = file.mkdir();
            if (result) {
                String nomedia = ".nomedia";
                File noMedia = new File(file + File.separator + nomedia);
                try {
                    //noinspection ResultOfMethodCallIgnored
                    noMedia.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return file.getAbsolutePath();
            }
        } else {
            return file.getAbsolutePath();
        }
        return null;
    }

    private String newPath;

    private void checkZipMode(String dir) {
        if (currentDir == null || currentDir.length() == 0 || dir == null || !dir.contains(zipParentPath)) {
            endZipMode(dir);
        } else {
            zipCommunicator.removeFromBackStack();
            zipCommunicator.removeZipScrollPos(newPath);
            inChildZip = false;
            Logger.log(TAG, "checkZipMode--currentzipdir B4=" + currentDir);
            currentDir = new File(currentDir).getParent();
            if (currentDir != null && currentDir.equals(File.separator)) {
                currentDir = null;
            }
            Logger.log(TAG, "checkZipMode--currentzipdir AFT=" + currentDir);
            reloadList();
            if (currentDir == null || currentDir.equals(File.separator)) {
                newPath = zipParentPath;
            } else {
                if (currentDir.startsWith(File.separator)) {
                    newPath = zipParentPath + currentDir;
                } else {
                    newPath = zipParentPath + File.separator + currentDir;
                }
            }
            setNavDirectory(newPath);
        }
    }

    public void endZipMode(String dir) {
        fragment.getLoaderManager().destroyLoader(LOADER_ID);
        currentDir = null;
        zipChildren.clear();
        zipCommunicator.removeZipScrollPos(zipParentPath);
        zipCommunicator.removeFromBackStack();
        zipCommunicator.onZipModeEnd(dir);
        clearCache();
    }

    private void clearCache() {
        String path = createCacheDirExtract();
        if (path != null) {
            File[] files = new File(path).listFiles();

            if (files != null) {
                for (File file : files) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
        }
    }


    private void setNavDirectory(String path) {
        zipCommunicator.setNavDirectory(path, isHomeScreenEnabled, FILES);
    }

    public void onFileClicked(int position) {
        if (zipParentPath.endsWith(EXT_ZIP)) {
            String name = zipChildren.get(position).getName().substring(zipChildren.get(position)
                    .getName().lastIndexOf("/") + 1);

            ZipEntry zipEntry = zipChildren.get(position).getEntry();
            ZipEntry zipEntry1 = new ZipEntry(zipEntry);
            String cacheDirPath = createCacheDirExtract();
            Logger.log(TAG, "Zip entry NEW:" + zipEntry1 + " zip entry=" + zipEntry);

            if (cacheDirPath != null) {
                if (name.endsWith(EXT_ZIP)) {
                    return;
                }


                try {
                    ZipFile zipFile;
                    if (inChildZip) {
                        zipFile = new ZipFile(zipPath);
                    } else {
                        zipFile = new ZipFile(zipParentPath);
                    }
                    zipEntry1 = zipEntry;
                    new ExtractZipEntry(fragment.getContext(), zipFile, cacheDirPath,
                            name, zipEntry1, alertDialogListener)
                            .execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onDirectoryClicked(int position) {

        String name = zipChildren.get(position).getName();
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        String name1 = name.substring(0, name.length() - 1); // 2 so that / doesnt come
        zipEntry = zipChildren.get(position).getEntry();
        zipEntryFileName = name1;
        if (zipEntryFileName.contains("/")) {
            String dirPath = zipEntryFileName.substring(0, zipEntryFileName.lastIndexOf("/"));
            scrollDir = zipParentPath + File.separator + dirPath;
            zipCommunicator.calculateZipScroll(scrollDir);
        }
        else {
            scrollDir = null;
        }
        Logger.log(TAG, "handleItemClick--entry=" + zipEntry + " dir=" + zipEntry.isDirectory()
                + " name=" + zipEntryFileName);
        viewZipContents(position);
    }

    private String scrollDir;

    public String getScrollDir() {
        return scrollDir;

    }

    public void onBackPressed() {
        String path = currentDir;
        if (currentDir != null) {
            path = zipParentPath + File.separator + currentDir;
        }
        checkZipMode(path);
    }

    public void onBackPressed(String dir) {
        checkZipMode(dir);
    }


    private void viewZipContents(int position) {
        currentDir = zipChildren.get(position).getName();

        if (currentDir.startsWith(File.separator)) {
            newPath = zipParentPath + currentDir;
        } else {
            newPath = zipParentPath + File.separator + currentDir;
        }

        if (newPath.endsWith(File.separator)) {
            newPath = newPath.substring(0, newPath.length() - 1);
        }
        reloadList();
        zipCommunicator.addToBackStack(newPath, FILES);
        setNavDirectory(newPath);
    }

    public void loadData() {
        fragment.getLoaderManager().restartLoader(LOADER_ID, null, this);
    }


    @NonNull
    @Override
    public Loader<ArrayList<FileInfo>> onCreateLoader(int id, Bundle args) {
        if (inChildZip) {
            String path;
            if (zipEntry.isDirectory()) {
                path = zipPath;
            } else {
                path = zipParentPath;
            }
            return new ZipContentLoader(fragment.getContext(), path, createCacheDirExtract(),
                    zipEntryFileName, zipEntry);
        }
        return new ZipContentLoader(fragment.getContext(), this, zipPath, ZIP_VIEWER, currentDir);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<FileInfo>> loader, ArrayList<FileInfo> data) {
        zipCommunicator.onZipContentsLoaded(data);
    }


    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<FileInfo>> loader) {

    }

    // Dialog for SAF
    private DialogHelper.AlertDialogListener alertDialogListener = new DialogHelper
            .AlertDialogListener() {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onPositiveButtonClick(View view) {
            Context context = fragment.getContext();
            if (context == null) {
                return;
            }
            Uri uri = createContentUri(context, currentDir);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_INSTALL_PACKAGE);

            String mimeType = getSingleton().getMimeTypeFromExtension("apk");
            intent.setData(uri);

            if (mimeType != null) {
                grantUriPermission(context, intent, uri);
            } else {
                openWith(uri, context);
            }
        }

        @Override
        public void onNegativeButtonClick(View view) {
        }

        @Override
        public void onNeutralButtonClick(View view) {

        }
    };
}
