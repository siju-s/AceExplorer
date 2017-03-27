package com.siju.acexplorer.filesystem.groups;

import android.content.Context;

import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.R;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.model.SectionItems;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.os.Environment.getRootDirectory;
import static com.siju.acexplorer.AceActivity.STORAGE_EXTERNAL;
import static com.siju.acexplorer.AceActivity.STORAGE_INTERNAL;
import static com.siju.acexplorer.AceActivity.STORAGE_ROOT;
import static com.siju.acexplorer.filesystem.storage.StorageUtils.getSpaceLeft;
import static com.siju.acexplorer.filesystem.storage.StorageUtils.getStorageDirectories;
import static com.siju.acexplorer.filesystem.storage.StorageUtils.getTotalSpace;


public class StoragesGroup {

    private static StoragesGroup storagesGroup;
    private ArrayList<SectionItems> totalStorages;
    private ArrayList<SectionItems> storagesList;
    private ArrayList<String> externalSDPaths;

    public static StoragesGroup getInstance() {
        if (storagesGroup == null) {
            storagesGroup = new StoragesGroup();
        }
        return storagesGroup;
    }

    private void clearStoragesList() {
        totalStorages = new ArrayList<>();
        storagesList = new ArrayList<>();
    }

    ArrayList<SectionItems> getStorageGroupData() {
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
            if ("/storage/emulated/legacy".equals(path) || "/storage/emulated/0".equals(path)) {
                name = STORAGE_INTERNAL;
                icon = R.drawable.ic_phone_white;

            } else if ("/storage/sdcard1".equals(path)) {
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
