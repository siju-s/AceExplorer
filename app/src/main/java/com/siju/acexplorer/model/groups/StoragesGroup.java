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

package com.siju.acexplorer.model.groups;

import android.annotation.SuppressLint;
import android.content.Context;

import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.R;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.model.SectionItems;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.os.Environment.getRootDirectory;
import static com.siju.acexplorer.model.StorageUtils.getSpaceLeft;
import static com.siju.acexplorer.model.StorageUtils.getStorageDirectories;
import static com.siju.acexplorer.model.StorageUtils.getTotalSpace;

@SuppressLint("StaticFieldLeak")
public class StoragesGroup {

    private static Context context;
    private static StoragesGroup storagesGroup;
    private ArrayList<SectionItems> totalStorages;
    private ArrayList<SectionItems> storagesList = new ArrayList<>();
    private ArrayList<String> externalSDPaths;
    private String STORAGE_ROOT;
    private String STORAGE_INTERNAL;
    private String STORAGE_EXTERNAL;
    private final String legacyPath = "/storage/emulated/legacy";
    private final String storagePath = "/storage/emulated/0";
    private final String sdCardPath = "/storage/sdcard1";


    public static StoragesGroup getInstance() {
        if (storagesGroup == null) {
            storagesGroup = new StoragesGroup();
        }
        context = AceApplication.getAppContext();
        return storagesGroup;
    }


    private void initConstants() {
        STORAGE_ROOT = context.getResources().getString(R.string.nav_menu_root);
        STORAGE_INTERNAL = context.getResources().getString(R.string.nav_menu_internal_storage);
        STORAGE_EXTERNAL = context.getResources().getString(R.string.nav_menu_ext_storage);
    }


    private void clearStoragesList() {
        totalStorages = new ArrayList<>();
        storagesList = new ArrayList<>();
    }

    ArrayList<SectionItems> getStorageGroupData() {
        initConstants();
        clearStoragesList();
        addRootDir();
        addStorages();
        totalStorages.addAll(storagesList);
        return totalStorages;
    }


    private void addRootDir() {
        File systemDir = getRootDirectory();
        File rootDir = systemDir.getParentFile();

        long spaceLeftRoot = getSpaceLeft(systemDir);
        long totalSpaceRoot = getTotalSpace(systemDir);
        int leftProgressRoot = (int) (((float) spaceLeftRoot / totalSpaceRoot) * 100);
        int progressRoot = 100 - leftProgressRoot;
        totalStorages.add(new SectionItems(STORAGE_ROOT, storageSpace(spaceLeftRoot, totalSpaceRoot), R.drawable
                .ic_root_white, FileUtils.getAbsolutePath(rootDir), progressRoot));
    }

    private void addStorages() {

        List<String> storagePaths = getStorageDirectories();
        externalSDPaths = new ArrayList<>();

        for (String path : storagePaths) {
            File file = new File(path);
            int icon;
            String name;
            if (legacyPath.equals(path) || storagePath.equals(path)) {
                name = STORAGE_INTERNAL;
                icon = R.drawable.ic_phone_white;

            } else if (sdCardPath.equals(path)) {
                name = STORAGE_EXTERNAL;
                icon = R.drawable.ic_ext_white;
                externalSDPaths.add(path);
            } else {
                name = file.getName();
                icon = R.drawable.ic_ext_white;
                externalSDPaths.add(path);
            }
            if (!file.isDirectory() || file.canExecute()) {
                long spaceLeft = getSpaceLeft(file);
                long totalSpace = getTotalSpace(file);
                int leftProgress = (int) (((float) spaceLeft / totalSpace) * 100);
                int progress = 100 - leftProgress;
                String spaceText = storageSpace(spaceLeft, totalSpace);
                storagesList.add(new SectionItems(name, spaceText, icon, path, progress));
            }
        }
    }

    public ArrayList<SectionItems> getStoragesList() {
        if (storagesList.size() == 0) {
            addStorages();
        }
        return storagesList;
    }

    public ArrayList<String> getExternalSDList() {
        return externalSDPaths;
    }

    private String storageSpace(long spaceLeft, long totalSpace) {
        Context context = AceApplication.getAppContext();
        String freePlaceholder = " " + context.getResources().getString(R.string.msg_free) + " ";
        return FileUtils.formatSize(context, spaceLeft) + freePlaceholder +
                FileUtils.formatSize(context, totalSpace);
    }


}
