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
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.FileConstants;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.storage.model.ZipModel;
import com.siju.acexplorer.storage.model.task.ExtractZipEntry;
import com.siju.acexplorer.view.dialog.DialogHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static android.webkit.MimeTypeMap.getSingleton;
import static com.siju.acexplorer.model.groups.Category.FILES;
import static com.siju.acexplorer.model.groups.Category.ZIP_VIEWER;
import static com.siju.acexplorer.model.helper.UriHelper.createContentUri;
import static com.siju.acexplorer.model.helper.UriHelper.grantUriPermission;
import static com.siju.acexplorer.view.dialog.DialogHelper.openWith;

public class ZipViewer implements LoaderManager.LoaderCallbacks<ArrayList<FileInfo>> {

    private final String TAG       = this.getClass().getSimpleName();
    private static final int    LOADER_ID = 1000;

    private       ArrayList<ZipModel>   zipChildren = new ArrayList<>();
    private final ArrayList<FileHeader> rarChildren = new ArrayList<>();

    private Fragment fragment;

    private ZipEntry zipEntry;

    private String  currentDir;
    private String  zipParentPath;
    private String  zipPath;
    private boolean inChildZip;

    private String          zipEntryFileName;
    private boolean         isHomeScreenEnabled;
    private ZipCommunicator zipCommunicator;

    enum ZipFormats {
        ZIP,
        RAR,
        APK;

        static final String zip = "zip";
        static final String rar = "rar";
        static final String apk = "apk";

        public static ZipFormats getFormatFromExt(String extension) {
            switch (extension) {
                case zip:
                    return ZIP;
                case rar:
                    return RAR;
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
        Log.d(TAG, "ZipViewer: zipParent:"+zipParentPath);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AceApplication.getAppContext());
        isHomeScreenEnabled = preferences.getBoolean(FileConstants.PREFS_HOMESCREEN, true);
        zipCommunicator.setInitialDir(path);
        setNavDirectory(path);
        zipCommunicator.addToBackStack(path, FILES);
    }

    void setZipData(ArrayList<ZipModel> zipData) {
        zipChildren = zipData;
        Log.d(TAG, "setZipData: " + zipChildren.size());
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
        Log.d(TAG, "checkZipMode: currentDir:"+currentDir + " dir:"+dir + " zipParentDir:"+zipParentPath );
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
            Log.d(TAG, "checkZipMode: zipChildren:" + zipChildren.size());
            if (currentDir == null || currentDir.equals(File.separator)) {
                newPath = zipParentPath;
            } else {
//                if (currentDir.startsWith(File.separator)) {
                newPath = zipParentPath + File.separator + currentDir;
//                } else {
//                    newPath = zipParentPath + currentDir;
//                }
            }
            setNavDirectory(newPath);
        }
    }

    private void endZipMode(String dir) {
        currentDir = null;
        zipChildren.clear();
        zipCommunicator.removeZipScrollPos(zipParentPath);
        zipCommunicator.removeFromBackStack();
        zipCommunicator.endZipMode(dir);
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
        if (zipParentPath.endsWith(".zip")) {
            String name = zipChildren.get(position).getName().substring(zipChildren.get(position)
                                                                                .getName().lastIndexOf("/") + 1);

            ZipEntry zipEntry = zipChildren.get(position).getEntry();
            ZipEntry zipEntry1 = new ZipEntry(zipEntry);
            String cacheDirPath = createCacheDirExtract();
            Logger.log(TAG, "Zip entry NEW:" + zipEntry1 + " zip entry=" + zipEntry);

            if (cacheDirPath != null) {
                if (name.endsWith(".zip")) {
                    this.zipEntry = zipEntry1;
                    zipEntryFileName = name;
                    inChildZip = true;
                    viewZipContents(position);
                    zipPath = cacheDirPath + "/" + name;
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
                    new ExtractZipEntry(zipFile, cacheDirPath,
                                        name, zipEntry1, alertDialogListener)
                            .execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (zipParentPath.endsWith(".rar")) {
            String name = rarChildren.get(position).getFileNameString();
            FileHeader fileHeader = rarChildren.get(position);
            String cacheDirPath = createCacheDirExtract();

            if (cacheDirPath != null) {

                try {
                    Archive rarFile = new Archive(new File(zipParentPath));
                    new ExtractZipEntry(rarFile, cacheDirPath,
                                        name, fileHeader, alertDialogListener)
                            .execute();

                } catch (IOException | RarException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    public void onDirectoryClicked(int position) {

        String name = zipChildren.get(position).getName();
        if (name.startsWith("/")) {
            name = name.substring(1, name.length());
        }
        String name1 = name.substring(0, name.length() - 1); // 2 so that / doesnt come
        zipEntry = zipChildren.get(position).getEntry();
        zipEntryFileName = name1;
        Logger.log(TAG, "handleItemClick--entry=" + zipEntry + " dir=" + zipEntry.isDirectory()
                + "name=" + zipEntryFileName);
        zipCommunicator.calculateZipScroll(zipParentPath + File.separator + zipEntryFileName);
        viewZipContents(position);
    }

    public void onBackPressed() {
        String path = currentDir;
        if (currentDir != null) {
            path = zipParentPath + File.separator + currentDir;
        }
        Log.d(TAG, "onBackPressed: currentDir:"+currentDir + " fullPath:"+path);
        checkZipMode(path);
    }

    public void onBackPressed(String dir) {
        Log.d(TAG, "onBackPressed: dir:"+dir);
        checkZipMode(dir);
    }


    private void viewZipContents(int position) {
        if (zipParentPath.endsWith("rar")) {
            String name = rarChildren.get(position).getFileNameString();
            currentDir = name.substring(0, name.length() - 1);
        } else {
            currentDir = zipChildren.get(position).getName();
        }

        if (currentDir.startsWith(File.separator)) {
            newPath = zipParentPath + currentDir;
        } else {
            newPath = zipParentPath + File.separator + currentDir;
        }

        Log.d(TAG, "viewZipContents: newpath:" + newPath);
        if (newPath.endsWith(File.separator)) {
            newPath = newPath.substring(0, newPath.length() - 1);
        }
        reloadList();
        zipCommunicator.addToBackStack(newPath, FILES);
        setNavDirectory(newPath);
    }

    public void loadData() {
        Log.d(TAG, "loadData: ");
        fragment.getLoaderManager().restartLoader(LOADER_ID, null, this);
    }


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
    public void onLoadFinished(Loader<ArrayList<FileInfo>> loader, ArrayList<FileInfo> data) {
        Log.d(TAG, "onLoadFinished: " + data.size());
        zipCommunicator.onZipContentsLoaded(data);
    }


    @Override
    public void onLoaderReset(Loader<ArrayList<FileInfo>> loader) {

    }

    // Dialog for SAF
    private DialogHelper.AlertDialogListener alertDialogListener = new DialogHelper
            .AlertDialogListener()
    {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onPositiveButtonClick(View view) {
            Uri uri = createContentUri(fragment.getContext(), currentDir);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_INSTALL_PACKAGE);

            String mimeType = getSingleton().getMimeTypeFromExtension("apk");
            intent.setData(uri);

            if (mimeType != null) {
                grantUriPermission(fragment.getContext(), intent, uri);
            } else {
                openWith(uri, fragment.getContext());
            }
        }

        @Override
        public void onNegativeButtonClick(View view) {
            zipCommunicator.openZipViewer(currentDir);
        }

        @Override
        public void onNeutralButtonClick(View view) {

        }
    };
}
