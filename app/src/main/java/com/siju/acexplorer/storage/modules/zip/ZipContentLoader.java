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

import android.content.Context;
import android.net.Uri;
import androidx.preference.PreferenceManager;
import androidx.loader.content.AsyncTaskLoader;

import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.main.model.FileConstants;
import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.main.model.groups.Category;
import com.siju.acexplorer.main.model.helper.RootHelper;
import com.siju.acexplorer.storage.model.ZipModel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static com.siju.acexplorer.main.model.groups.Category.COMPRESSED;
import static com.siju.acexplorer.main.model.helper.SortHelper.comparatorByNameZip;
import static com.siju.acexplorer.main.model.helper.SortHelper.comparatorByNameZipViewer;


@SuppressWarnings("FieldCanBeLocal")
class ZipContentLoader extends AsyncTaskLoader<ArrayList<FileInfo>> {
    private final String TAG = this.getClass().getSimpleName();
    private ArrayList<FileInfo> fileInfoList;

    private final String parentZip;
    private boolean showHidden;
    private final Category category;
    private String mZipPath;
    private boolean mInParentZip;
    private int mSortMode;
    private String outputDir;
    private ZipFile zipFile;
    private String fileName;
    private boolean isZipFormat;
    private ZipEntry entry = null;
    private ArrayList<ZipModel> totalZipList = new ArrayList<>();

    private ZipViewer zipViewer;


    ZipContentLoader(Context context, ZipViewer zipViewer, String parentZipPath, Category category,
                     String zipPath) {
        super(context);
        Logger.log(TAG, "Parent zip:" + parentZipPath + "dir:" + zipPath);
        parentZip = parentZipPath;
        this.category = category;
        showHidden = PreferenceManager.getDefaultSharedPreferences(context).getBoolean
                (FileConstants.PREFS_HIDDEN, false);
        if (zipPath != null && zipPath.endsWith(File.separator))
            zipPath = zipPath.substring(0, zipPath.length() - 1);
        mZipPath = zipPath;
//        mInParentZip = isParentZip;
        mSortMode = PreferenceManager.getDefaultSharedPreferences(context).getInt(
                FileConstants.KEY_SORT_MODE, FileConstants.KEY_SORT_NAME);
        this.zipViewer = zipViewer;
    }


    ZipContentLoader(Context context, String path, String outputDir, String fileName,
                     ZipEntry zipEntry) {
        super(context);
        this.isZipFormat = true;
        this.outputDir = outputDir;
        category = Category.ZIP_VIEWER;
        Logger.log(TAG, "Zip PARENT=" + path + " ENTRY=" + zipEntry);
        try {
            this.zipFile = new ZipFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mZipPath = zipEntry.getName();
        if (mZipPath.endsWith("/")) {
            mZipPath = mZipPath.substring(0, mZipPath.length() - 1);
        }
        parentZip = path;
        this.fileName = fileName;
        this.entry = zipEntry;
    }

    @Override
    protected void onStartLoading() {
        if (fileInfoList != null) {
            deliverResult(fileInfoList);
        }

        if (takeContentChanged() || fileInfoList == null) {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(ArrayList<FileInfo> data) {
        if (isReset()) {
            // The Loader has been reset; ignore the result and invalidate the data.
            return;
        }

        fileInfoList = data;

        if (isStarted()) {
            // If the Loader is in a started state, deliver the results to the
            // client. The superclass method does this for us.
            super.deliverResult(data);
        }
        super.deliverResult(data);
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();

        if (fileInfoList != null) {
            fileInfoList = null;
        }
    }


    @Override
    public void onCanceled(ArrayList<FileInfo> data) {
        super.onCanceled(data);
    }


    @Override
    public ArrayList<FileInfo> loadInBackground() {
        fileInfoList = new ArrayList<>();
        if (entry == null) {
            getZipContents(mZipPath, parentZip);
        } else {
            unzip();
        }
        return fileInfoList;
    }

    private void unzip() {

        ZipViewer.ZipFormats zipFormats = ZipViewer.ZipFormats.getFormatFromExt(entry.getName().
                substring(entry.getName().lastIndexOf(".") + 1));
        if (zipFormats == ZipViewer.ZipFormats.ZIP) {
            try {
                unzipEntry(zipFile, entry, outputDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param dir           Child path names. First time it will be null
     * @param parentZipPath Original zip path with .zip extension
     */
    private void getZipContents(String dir, String parentZipPath) {
        ArrayList<ZipModel> elements = new ArrayList<>();
        try {
            totalZipList = new ArrayList<>();
            populateTotalZipList(parentZipPath);
            traverseZipElements(dir, elements);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(elements, comparatorByNameZipViewer);
        zipViewer.setZipData(elements);
        populateZipModel(parentZipPath, elements);
        Collections.sort(fileInfoList, comparatorByNameZip);
    }

    private void populateZipModel(String parentZipPath, ArrayList<ZipModel> elements) {
        for (ZipModel model : elements) {
            String name = model.getName();
            if (name.startsWith("/")) {
                name = name.substring(1);
            }
            boolean isDirectory = model.isDirectory();
            long size;
            if (isDirectory) {
                int count = 0;
                for (ZipModel zipmodel : totalZipList) {
                    String modelName = zipmodel.getEntry().getName();
                    if (modelName.startsWith(File.separator))
                        modelName = modelName.substring(1);

                    if (!name.equals(modelName) && modelName.startsWith(name)) {
                        count++;
                    }
                }
                size = count;
            } else {
                size = model.getSize();
            }
            long date = model.getTime();
            String extension;

            if (isDirectory) {
                name = name.substring(0, name.length() - 1);
                if (!mInParentZip) {
                    name = name.substring(name.lastIndexOf(File.separator) + 1);
                }
                extension = null;
            } else {
                name = name.substring(name.lastIndexOf(File.separator) + 1);
                extension = name.substring(name.lastIndexOf(".") + 1);
            }

            String path = parentZipPath + File.separator + name;

            FileInfo fileInfo = new FileInfo(COMPRESSED, name, path, date, size,
                    isDirectory, extension, RootHelper.parseFilePermission(new File(path)), false);
            fileInfoList.add(fileInfo);
        }
    }

    private void traverseZipElements(String dir, ArrayList<ZipModel> elements) {
        ArrayList<String> entriesList = new ArrayList<>();
        for (ZipModel entry : totalZipList) {

            String entryName = entry.getName();
            File file = new File(entryName);
            if (dir == null || dir.trim().length() == 0) {
                if (file.getParent() == null || file.getParent().length() == 0 || file.getParent().equals(File
                        .separator)) {
                    if (!entriesList.contains(entryName)) {
                        elements.add(new ZipModel(new ZipEntry(entryName), entry.getTime(), entry.getSize(),
                                entry.isDirectory()));
                        entriesList.add(entryName);
                    }
                } else {
                    addZipEntryDirectory(elements, entriesList, entry, entryName);
                }
            } else {
                if (file.getParent() != null && (file.getParent().equals(dir) || file.getParent().equals(File
                        .separator + dir))) {
                    if (!entriesList.contains(entryName)) {
                        elements.add(new ZipModel(new ZipEntry(entryName), entry.getTime(), entry.getSize(), entry
                                .isDirectory()));
                        entriesList.add(entryName);
                    }
                } else {
                    if (entryName.startsWith(dir + File.separator) && entryName.length() > dir.length() + 1) {
                        String path1 = entryName.substring(dir.length() + 1);

                        int index = dir.length() + 1 + path1.indexOf(File.separator);
                        String path = entryName.substring(0, index + 1);

                        if (!entriesList.contains(path)) {
                            ZipModel zipObj = new ZipModel(new ZipEntry(entryName.substring(0, index + 1)), entry.getTime
                                    (), entry.getSize(), true);
                            entriesList.add(path);
                            elements.add(zipObj);
                        }
                    }
                }

            }
        }
    }

    private void addZipEntryDirectory(ArrayList<ZipModel> elements, ArrayList<String> strings, ZipModel entry, String entryName) {
        boolean slash = false;
        if (entryName.startsWith(File.separator)) {
            slash = true;
            entryName = entryName.substring(1);
        }
        String path = entryName.substring(0, entryName.indexOf(File.separator) + 1);
        if (slash) {
            path = "/" + path;
        }
        ZipModel zipObj;
        if (!strings.contains(path)) {
            zipObj = new ZipModel(new ZipEntry(path), entry.getTime(), entry
                    .getSize(), true);
            strings.add(path);
            elements.add(zipObj);
        }
    }

    private void populateTotalZipList(String parentZipPath) throws IOException {
        if (new File(parentZipPath).canRead()) {
            ZipFile zipfile = new ZipFile(parentZipPath);
            for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                totalZipList.add(new ZipModel(entry, entry.getTime(), entry.getSize(), entry
                        .isDirectory()));
            }
            zipfile.close();
        } else {
            ZipEntry zipEntry;
            Uri uri = Uri.parse(parentZipPath);
            ZipInputStream zipInputStream = new ZipInputStream(getContext().getContentResolver()
                    .openInputStream(uri));
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                totalZipList.add(new ZipModel(zipEntry, zipEntry.getTime(), zipEntry.getSize(), zipEntry
                        .isDirectory()));
            }
            zipInputStream.close();
        }
    }

    private void unzipEntry(ZipFile zipfile, ZipEntry entry, String outputDir)
            throws IOException {

        File output = new File(outputDir, fileName);
        BufferedInputStream inputStream = new BufferedInputStream(
                zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(
                new FileOutputStream(output));
        Logger.log("Extract", "zipfile=" + zipfile.getName() + " zipentry=" + entry + " stream=" + inputStream);
        Logger.log("Extract", "outputDir=" + outputDir + " filename=" + fileName);

        Logger.log("Extract", "Bytes START=" + inputStream.available());

        try {
            int len;
            byte[] buf = new byte[20480];
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                //closing quietly
            }

            try {
                outputStream.close();
            } catch (IOException e) {
                //closing quietly
            }

        }
        getZipContents("", output.getAbsolutePath());
    }


}
