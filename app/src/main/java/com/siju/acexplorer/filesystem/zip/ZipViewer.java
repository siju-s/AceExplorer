package com.siju.acexplorer.filesystem.zip;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.BaseFileList;
import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.filesystem.backstack.BackStackInfo;
import com.siju.acexplorer.filesystem.backstack.NavigationInfo;
import com.siju.acexplorer.filesystem.groups.Category;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.model.ZipModel;
import com.siju.acexplorer.filesystem.task.ExtractZipEntry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.siju.acexplorer.filesystem.groups.Category.FILES;
import static com.siju.acexplorer.filesystem.groups.Category.ZIP_VIEWER;
import static com.siju.acexplorer.filesystem.storage.StorageUtils.getInternalStorage;

public class ZipViewer implements ZipElements {

    private final String TAG = this.getClass().getSimpleName();
    private final int LOADER_ID = 1000;
    private Fragment context;
    private NavigationInfo navigationInfo;
    private BackStackInfo backStackInfo;
    private String currentDir = null;
    private String zipParentPath;
    private String zipPath;
    private boolean inChildZip;
    private ZipEntry zipEntry = null;
    private String zipEntryFileName;
    private ArrayList<ZipModel> zipChildren = new ArrayList<>();
    private final ArrayList<FileHeader> rarChildren = new ArrayList<>();
    private boolean isHomeScreenEnabled;
    private Category category = FILES;


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


    public ZipViewer(Fragment fragment, String path) {
        context = fragment;
        zipParentPath = zipPath = path;
        navigationInfo = new NavigationInfo(fragment);
        backStackInfo = new BackStackInfo();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(fragment.getContext());
        isHomeScreenEnabled = preferences.getBoolean(FileConstants.PREFS_HOMESCREEN, true);
        setNavDirectory(path);
        backStackInfo.addToBackStack(path, category);
    }

    public void setZipData(ArrayList<ZipModel> zipData) {
        zipChildren = zipData;
    }

    private void reloadList() {
        context.getLoaderManager().restartLoader(LOADER_ID, null, (LoaderManager
                .LoaderCallbacks<ArrayList<FileInfo>>) context);
    }

    private String createCacheDirExtract() {
        String cacheTempDir = ".tmp";
        File cacheDir = context.getActivity().getExternalCacheDir();
        if (cacheDir == null) return null;
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

    private boolean checkZipMode() {
        if (currentDir == null || currentDir.length() == 0) {
            endZipMode();
            return true;
        } else {
            inChildZip = false;
            Logger.log(TAG, "checkZipMode--currentzipdir B4=" + currentDir);
            currentDir = new File(currentDir).getParent();
            if (currentDir != null && currentDir.equals(File.separator)) {
                currentDir = null;
            }
            Logger.log(TAG, "checkZipMode--currentzipdir AFT=" + currentDir);
            reloadList();
            String newPath;
            if (currentDir == null || currentDir.equals(File.separator)) {
                newPath = zipParentPath;
            } else {
                if (currentDir.startsWith(File.separator)) {
                    newPath = zipParentPath + File.separator + currentDir;
                } else {
                    newPath = zipParentPath + currentDir;
                }
            }
            setNavDirectory(newPath);
            return false;
        }
    }

    public void endZipMode() {
        currentDir = null;
        zipChildren.clear();
        ((BaseFileList) context).endZipMode();
        clearCache();
        reloadList();
        setNavDirectory(getInternalStorage());
    }

    private void clearCache() {
        String path = createCacheDirExtract();
        if (path != null) {
            File[] files = new File(path).listFiles();

            if (files != null) {
                for (File file : files)
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
            }
        }
    }


    public boolean isInZipMode(String path) {
        if (currentDir == null || currentDir.length() == 0 || !path.contains(zipParentPath)) {
            endZipMode();
            return true;
        } else if (path.equals(zipParentPath)) {
            currentDir = null;
            reloadList();
            setNavDirectory(path);
            return false;
        } else {
            String newPath = path.substring(zipParentPath.length() + 1, path.length());
            Logger.log(TAG, "New zip path=" + newPath);
            currentDir = newPath;
            reloadList();
            setNavDirectory(path);
            return false;
        }
    }

    private void setNavDirectory(String path) {
        navigationInfo.setNavDirectory(path, isHomeScreenEnabled, FILES);

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
                            context, name, zipEntry1)
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
                            context, name, fileHeader)
                            .execute();

                } catch (IOException | RarException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    public void onDirectoryClicked(int position) {
        String name = zipChildren.get(position).getName();
        if (name.startsWith("/")) name = name.substring(1, name.length());
        String name1 = name.substring(0, name.length() - 1); // 2 so that / doesnt come
        zipEntry = zipChildren.get(position).getEntry();
        zipEntryFileName = name1;
        Logger.log(TAG, "handleItemClick--entry=" + zipEntry + " dir=" + zipEntry.isDirectory()
                + "name=" + zipEntryFileName);
        viewZipContents(position);
    }

    public void onBackPressed() {
        checkZipMode();
/*        if (!checkZipMode()) {
            backStackInfo.removeEntryAtIndex(backStackInfo.getBackStack().size() - 1);
            int backStackSize = backStackInfo.getBackStack().size();
            String currentDir = backStackInfo.getDirAtPosition(backStackInfo.getBackStack().size() - 1);
            Category currentCategory = backStackInfo.getCategoryAtPosition(backStackInfo.getBackStack().size() - 1);
            reloadList();
            navigationInfo.setNavDirectory(currentDir, isHomeScreenEnabled, currentCategory);
        }*/
    }


    private void viewZipContents(int position) {
        if (zipParentPath.endsWith("rar")) {
            String name = rarChildren.get(position).getFileNameString();
            currentDir = name.substring(0, name.length() - 1);
        } else {
            currentDir = zipChildren.get(position).getName();
        }

        reloadList();
        String newPath;
        if (currentDir.startsWith(File.separator)) {
            newPath = zipParentPath + currentDir;
        } else {
            newPath = zipParentPath + File.separator + currentDir;
        }
        setNavDirectory(newPath);
    }

    public Loader<ArrayList<FileInfo>> onCreateLoader(int id, Bundle args) {
        if (inChildZip) {
            String path;
            if (zipEntry.isDirectory()) {
                path = zipPath;
            } else {
                path = zipParentPath;
            }
            return new ZipContentLoader(context, path, createCacheDirExtract(),
                    zipEntryFileName, zipEntry);
        }
        return new ZipContentLoader(context, this, zipPath, ZIP_VIEWER, currentDir);
    }


    public void onLoadFinished(Loader<ArrayList<FileInfo>> loader, ArrayList<FileInfo> data) {
        ((BaseFileList) context).onZipContentsLoaded(data);
    }


    public void onLoaderReset(Loader<ArrayList<FileInfo>> loader) {

    }
}
