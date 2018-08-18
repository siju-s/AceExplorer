package com.siju.acexplorer.model.data;

import android.content.Context;

import com.siju.acexplorer.model.FavInfo;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.HiddenFileHelper;
import com.siju.acexplorer.model.SharedPreferenceWrapper;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.model.helper.RootHelper;

import java.io.File;
import java.util.ArrayList;

import static com.siju.acexplorer.model.groups.Category.FILES;
import static com.siju.acexplorer.model.helper.FileUtils.getCategoryFromExtension;
import static com.siju.acexplorer.model.helper.RootHelper.parseFilePermission;
import static com.siju.acexplorer.model.helper.SortHelper.sortFiles;

public class FileDataFetcher {

    static ArrayList<FileInfo> fetchFiles(String currentDir, int sortMode, boolean showHidden, boolean isRingtonePicker,
                                          boolean isRooted)
    {
        ArrayList<FileInfo> fileInfoList = getFilesList(currentDir, isRooted, showHidden, isRingtonePicker);
        sortFiles(fileInfoList, sortMode);
        return fileInfoList;
    }

    public static ArrayList<FileInfo> getFilesList(String path, boolean root,
                                                    boolean showHidden, boolean isRingtonePicker)
    {
        File file = new File(path);
        ArrayList<FileInfo> fileInfoArrayList;
        if (file.canRead()) {
            fileInfoArrayList = getNonRootedList(file, showHidden, isRingtonePicker);
        } else {
            fileInfoArrayList = RootHelper.getRootedList(path, root, showHidden);
        }
        return fileInfoArrayList;
    }

    private static ArrayList<FileInfo> getNonRootedList(File file, boolean showHidden,
                                                        boolean isRingtonePicker)
    {
        File[] listFiles = file.listFiles();

        return getFilesList(listFiles, showHidden, isRingtonePicker);
    }

    private static ArrayList<FileInfo> getFilesList(File[] listFiles, boolean showHidden, boolean isRingtonePicker)
    {
        ArrayList<FileInfo> filesList = new ArrayList<>();
        if (listFiles == null) {
            return filesList;
        }
        for (File file1 : listFiles) {
            String filePath = file1.getAbsolutePath();
            boolean isDirectory = false;
            long size;
            String extension = null;
            Category category = FILES;

            // Don't show hidden files by default
            if (HiddenFileHelper.shouldSkipHiddenFiles(file1, showHidden)) {
                continue;
            }
            if (file1.isDirectory()) {
                isDirectory = true;
                int childFileListSize = 0;
                String[] list = file1.list();
                if (list != null) {
                    childFileListSize = list.length;
                }
                size = childFileListSize;
            } else {
                size = file1.length();
                extension = filePath.substring(filePath.lastIndexOf(".") + 1);
                category = getCategoryFromExtension(extension);
                if (isRingtonePicker && !FileUtils.isFileMusic(filePath)) {
                    continue;
                }
            }
            long date = file1.lastModified();

            FileInfo fileInfo = new FileInfo(category, file1.getName(), filePath, date, size,
                                             isDirectory, extension, parseFilePermission(file1), false);
            filesList.add(fileInfo);
        }
        return filesList;
    }

    static ArrayList<FileInfo> fetchFavorites(Context context, Category category, int sortMode,
                                              boolean showOnlyCount)
    {
        SharedPreferenceWrapper wrapper = new SharedPreferenceWrapper();
        ArrayList<FavInfo> favList = wrapper.getFavorites(context);
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();
        if (showOnlyCount) {
            fileInfoList.add(new FileInfo(category, favList.size()));
            return fileInfoList;
        }
        for (FavInfo favInfo : favList) {
            String path = favInfo.getFilePath();
            File file = new File(path);
            String fileName = file.getName();
            long childFileListSize = 0;
            String[] filesList = file.list();
            if (filesList != null) {
                childFileListSize = filesList.length;
            }
            long date = file.lastModified();

            FileInfo fileInfo = new FileInfo(FILES, fileName, path, date, childFileListSize,
                                             true, null, parseFilePermission(new File(path)), false);
            fileInfoList.add(fileInfo);
        }
        return sortFiles(fileInfoList, sortMode);
    }
}
