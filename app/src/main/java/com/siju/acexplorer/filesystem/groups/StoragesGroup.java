package com.siju.acexplorer.filesystem.groups;

import android.content.Context;

import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.model.SectionGroup;
import com.siju.acexplorer.model.SectionItems;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.filesystem.groups.StoragesInfo.getSpaceLeft;
import static com.siju.acexplorer.filesystem.groups.StoragesInfo.getTotalSpace;


public class StoragesGroup {
    private String STORAGE_ROOT;
    private String STORAGE_INTERNAL;
    private String STORAGE_EXTERNAL;
    private final ArrayList<SectionItems> totalStorages = new ArrayList<>();
    private final ArrayList<SectionItems> storagesList = new ArrayList<>();

    private Context context;

    public StoragesGroup(Context context) {
        this.context = context;
    }


    private void initConstants() {
        STORAGE_ROOT = context.getResources().getString(R.string.nav_menu_root);
        STORAGE_INTERNAL = context.getResources().getString(R.string.nav_menu_internal_storage);
        STORAGE_EXTERNAL = context.getResources().getString(R.string.nav_menu_ext_storage);
    }

    public void initializeStorageGroup() {
        initConstants();
        addRootDir();
        addStorages();
        totalStorages.addAll(storagesList);
    }


    private void addRootDir() {
        File systemDir = FileUtils.getRootDirectory();
        File rootDir = systemDir.getParentFile();

        long spaceLeftRoot = getSpaceLeft(systemDir);
        long totalSpaceRoot = getTotalSpace(systemDir);
        int leftProgressRoot = (int) (((float) spaceLeftRoot / totalSpaceRoot) * 100);
        int progressRoot = 100 - leftProgressRoot;
        totalStorages.add(new SectionItems(STORAGE_ROOT, storageSpace(spaceLeftRoot, totalSpaceRoot), R.drawable
                .ic_root_white, FileUtils.getAbsolutePath(rootDir), progressRoot));
    }

    private void addStorages() {

        List<String> storagePaths = FileUtils.getStorageDirectories(context);

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
                mExternalSDPaths.add(path);
            } else {
                name = file.getName();
                icon = R.drawable.ic_ext_white;
                mExternalSDPaths.add(path);
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
        return storagesList;
    }

    public ArrayList<SectionItems> getTotalStorages() {
        return totalStorages;
    }

    private String storageSpace(long spaceLeft, long totalSpace) {
        String freePlaceholder = " " + context.getResources().getString(R.string.msg_free) + " ";
        return FileUtils.formatSize(context, spaceLeft) + freePlaceholder +
                FileUtils.formatSize(context, totalSpace);
    }

}
