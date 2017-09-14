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

package com.siju.acexplorer.storage.model.zip;

import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.AsyncTaskLoader;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.FileConstants;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.model.helper.helper.SortHelper;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.storage.model.ZipModel;
import com.siju.acexplorer.model.helper.RootHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static com.siju.acexplorer.model.groups.Category.COMPRESSED;
import static com.siju.acexplorer.model.helper.helper.SortHelper.comparatorByNameZip;


public class ZipContentLoader extends AsyncTaskLoader<ArrayList<FileInfo>> {
    private final String TAG = this.getClass().getSimpleName();
    private ArrayList<FileInfo> fileInfoList;

    private final String mPath;
    private boolean showHidden;
    private final Category category;
    private String mZipPath;
    private Fragment mFragment;
    private boolean mInParentZip;
    private int mSortMode;
    private String outputDir;
    private ZipFile zipFile;
    private String fileName;
    private boolean isZipFormat;
    private ZipEntry entry = null;
    private Archive rar;
    private FileHeader header;
    private boolean isRooted;
    private String mCurrentZipDir;
    public ArrayList<ZipModel> totalZipList = new ArrayList<>();
    private ZipElements zipElements;

    public Archive mArchive;
    public final ArrayList<FileHeader> totalRarList = new ArrayList<>();
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final ArrayList<FileHeader> rarChildren = new ArrayList<>();
    private ZipViewer zipViewer;


    ZipContentLoader(Fragment fragment, ZipViewer zipViewer, String path, Category category, String zipPath) {
        super(fragment.getContext());
        Logger.log(TAG, "Zip" + "dir=" + zipPath);
        mPath = path;
        Context context = fragment.getContext();
        mFragment = fragment;
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


    ZipContentLoader(Fragment fragment, String path, String outputDir, String fileName,
                     ZipEntry zipEntry) {
        super(fragment.getContext());
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
        mPath = path;
        mFragment = fragment;
        this.fileName = fileName;
        this.entry = zipEntry;
    }

    @Override
    protected void onStartLoading() {
        if (fileInfoList != null) {
            deliverResult(fileInfoList);
        }

        if (takeContentChanged() || fileInfoList == null)
            forceLoad();
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
            mFragment = null;
        }
    }


    @Override
    public void onCanceled(ArrayList<FileInfo> data) {
        super.onCanceled(data);
    }


    @Override
    public ArrayList<FileInfo> loadInBackground() {
        fileInfoList = new ArrayList<>();
        if (entry != null) {
            unzip();
        } else {
            fetchZipContents();
        }
        return fileInfoList;
    }

    private void fetchZipContents() {
        if (mPath.endsWith("rar"))
            getRarContents(mZipPath, mPath);
        else
            getZipContents(mZipPath, mPath);
    }

    private void unzip() {

        ZipViewer.ZipFormats zipFormats = ZipViewer.ZipFormats.getFormatFromExt(entry.getName().
                substring(entry.getName().lastIndexOf(".") + 1, entry.getName().length()));
        switch (zipFormats) {
            case ZIP:
                try {
                    unzipEntry(zipFile, entry, outputDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

        }

    }

    /**
     * @param dir           Child path names. First time it will be null
     * @param parentZipPath Original zip path with .zip extension
     * @return Zip contents
     */
    private void getZipContents(String dir, String parentZipPath) {
        ZipFile zipfile;
        ArrayList<ZipModel> elements = new ArrayList<>();

        try {
//            if (totalZipList.size() == 0 || entry != null) {
            if (new File(parentZipPath).canRead()) {
                totalZipList = new ArrayList<>();
                zipfile = new ZipFile(parentZipPath);
                for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
                    totalZipList.add(new ZipModel(entry, entry.getTime(), entry.getSize(), entry
                            .isDirectory()));
                }
            } else {
                totalZipList = new ArrayList<>();
                ZipEntry zipEntry;
                Uri uri = Uri.parse(parentZipPath);
                ZipInputStream zipfile1 = new ZipInputStream(getContext().getContentResolver()
                        .openInputStream(uri));
                while ((zipEntry = zipfile1.getNextEntry()) != null) {
                    totalZipList.add(new ZipModel(zipEntry, zipEntry.getTime(), zipEntry.getSize(), zipEntry
                            .isDirectory()));
                }
            }

            ArrayList<String> strings = new ArrayList<>();
            for (ZipModel entry : totalZipList) {

                File file = new File(entry.getName());
                if (dir == null || dir.trim().length() == 0) {
                    String y = entry.getName();
                    System.out.println("entry name==" + y);

              /*      if (y.startsWith(File.separator))
                        y = y.substring(1, y.length());*/
                    if (file.getParent() == null || file.getParent().length() == 0 || file.getParent().equals(File
                            .separator)) {
                        System.out.println("entry if isdir==" + entry.isDirectory() + "y=" + y);
                        if (!strings.contains(y)) {
                            elements.add(new ZipModel(new ZipEntry(y), entry.getTime(), entry.getSize(),
                                    entry.isDirectory()));
                            strings.add(y);
                        }
                    } else {
                        boolean slash = false;
                        if (y.startsWith(File.separator)) {
                            slash = true;
                            y = y.substring(1, y.length());
                        }
                        String path = y.substring(0, y.indexOf(File.separator) + 1);
                        if (slash) {
                            path = "/" + path;
                        }
                        System.out.println("entry else path==" + path);
                        ZipModel zipObj;
                        if (!strings.contains(path)) {
                            zipObj = new ZipModel(new ZipEntry(path), entry.getTime(), entry
                                    .getSize(), true);
                            strings.add(path);
                            elements.add(zipObj);
                        }
                    }
                } else {
                    String y = entry.getName();
                    System.out.println("ZIP ITEM==" + y + "dir=" + dir);
                /*    if (y.startsWith(File.separator))
                        y = y.substring(1, y.length());*/

                    if (file.getParent() != null && (file.getParent().equals(dir) || file.getParent().equals(File
                            .separator + dir))) {
                        if (!strings.contains(y)) {
                            elements.add(new ZipModel(new ZipEntry(y), entry.getTime(), entry.getSize(), entry
                                    .isDirectory()));
                            strings.add(y);
                        }
                    } else {
                        if (y.startsWith(dir + File.separator) && y.length() > dir.length() + 1) {
                            String path1 = y.substring(dir.length() + 1, y.length());
                            System.out.println("path1==" + path1);

                            int index = dir.length() + 1 + path1.indexOf(File.separator);
                            String path = y.substring(0, index + 1);
                            System.out.println("path==" + path);

                            if (!strings.contains(path)) {
                                ZipModel zipObj = new ZipModel(new ZipEntry(y.substring(0, index + 1)), entry.getTime
                                        (), entry.getSize(), true);
                                strings.add(path);
                                elements.add(zipObj);
                            }
                        }
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(elements, SortHelper.comparatorByNameZip1);

        zipViewer.setZipData(elements);
        for (ZipModel model : elements) {
            String name = model.getName();
            if (name.startsWith("/")) {
                name = name.substring(1, name.length());
            }
            boolean isDirectory = model.isDirectory();
            long size;
            if (isDirectory) {
                int count = 0;
                for (ZipModel zipmodel : totalZipList) {
                    String modelName = zipmodel.getEntry().getName();
                    if (modelName.startsWith(File.separator))
                        modelName = modelName.substring(1, modelName.length());
                    System.out.println("SIJU --Dir true--modelname" + modelName + " name=" + name);

                    if (modelName.startsWith(name)) {
                        count++;
                    }
                }
                size = count;
            } else {
                size = model.getSize();
            }
            int type = COMPRESSED.getValue();
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
                extension = name.substring(name.lastIndexOf(".") + 1, name.length());
            }
            String path = parentZipPath + File.separator + name;

            FileInfo fileInfo = new FileInfo(COMPRESSED, name, path, date, size,
                    isDirectory, extension, RootHelper.parseFilePermission(new File(path)), false);
            fileInfoList.add(fileInfo);
        }
        Collections.sort(fileInfoList, comparatorByNameZip);
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
            byte buf[] = new byte[20480];
            while ((len = inputStream.read(buf)) > 0) {
                //System.out.println(id + " " + hash.get(id));
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

    private void getRarContents(String dir, String parentZipPath) {
        ArrayList<FileHeader> elements = new ArrayList<>();
        try {
            mArchive = new Archive(new File(parentZipPath));

            if (totalRarList.size() == 0) {

                FileHeader fh = mArchive.nextFileHeader();
                while (fh != null) {
                    totalRarList.add(fh);
                    fh = mArchive.nextFileHeader();
                }
            }
            if (dir == null || dir.trim().length() == 0 || dir.equals("")) {

                for (FileHeader header : totalRarList) {
                    String name = header.getFileNameString();

                    if (!name.contains("\\")) {
                        elements.add(header);

                    }
                }
            } else {
                for (FileHeader header : totalRarList) {
                    String name = header.getFileNameString();
                    if (name.substring(0, name.lastIndexOf("\\")).equals(dir)) {
                        elements.add(header);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        for (FileHeader fileHeader : elements) {
            String name = fileHeader.getFileNameString();

            boolean isDirectory = fileHeader.isDirectory();
        /*    if (isDirectory) {
                name = name.substring(name.lastIndexOf(File.separator) + 1);
            }*/
            long size = fileHeader.getPackSize();
            int type = COMPRESSED.getValue();
            Date date = fileHeader.getMTime();
            long date1 = date.getTime();
//            String noOfFilesOrSize = Formatter.formatFileSize(mContext, size);
//            String fileModifiedDate = FileUtils.convertDate(date);
            String extension;
            if (isDirectory) {
                name = name.substring(0, name.length() - 1);
                if (!mInParentZip) {
                    name = name.substring(name.lastIndexOf(File.separator) + 1);
                }
                extension = null;
            } else {
                name = name.substring(name.lastIndexOf(File.separator) + 1);
                extension = name.substring(name.lastIndexOf(".") + 1, name.length());
            }
            String path = parentZipPath + File.separator + name;

            FileInfo fileInfo = new FileInfo(COMPRESSED, name, path, date1, size,
                    isDirectory, extension, RootHelper.parseFilePermission(new File(path)), false);
            fileInfoList.add(fileInfo);
        }
        Collections.sort(fileInfoList, comparatorByNameZip);
    }


    private void unzipRAREntry(Archive zipfile, FileHeader header, String outputDir)
            throws IOException, RarException {

        File output = new File(outputDir + "/" + header.getFileNameString().trim());
        FileOutputStream fileOutputStream = new FileOutputStream(output);
        zipfile.extractFile(header, fileOutputStream);
    }

}
