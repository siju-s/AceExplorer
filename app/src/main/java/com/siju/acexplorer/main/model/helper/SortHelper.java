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

package com.siju.acexplorer.main.model.helper;

import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.storage.model.ZipModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;



public class SortHelper {

    public static final Comparator<? super FileInfo> comparatorByNameZip = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {
            // sort folders first
            if ((file1.isDirectory()) && (!file2.isDirectory()))
                return -1;
            if ((!file1.isDirectory()) && (file2.isDirectory()))
                return 1;

            // here both are folders or both are files : sort alpha
            return file1.getFileName().toLowerCase()
                    .compareTo(file2.getFileName().toLowerCase());
        }

    };

    public static final Comparator<? super ZipModel> comparatorByNameZip1 = new Comparator<ZipModel>() {

        public int compare(ZipModel file1, ZipModel file2) {
            // sort folders first
            if ((file1.isDirectory()) && (!file2.isDirectory()))
                return -1;
            if ((!file1.isDirectory()) && (file2.isDirectory()))
                return 1;

            // here both are folders or both are files : sort alpha
            return file1.getName().toLowerCase()
                    .compareTo(file2.getName().toLowerCase());
        }

    };

    public static ArrayList<FileInfo> sortFiles(ArrayList<FileInfo> files, int sortMode) {

        switch (sortMode) {
            case 0:
                Collections.sort(files, comparatorByName);
                break;
            case 1:
                Collections.sort(files, comparatorByNameDesc);
                break;
            case 2:
                Collections.sort(files, comparatorByType);
                break;
            case 3:
                Collections.sort(files, comparatorByTypeDesc);
                break;
            case 4:
                Collections.sort(files, comparatorBySize);
                break;
            case 5:
                Collections.sort(files, comparatorBySizeDesc);
                break;
            case 6:
                Collections.sort(files, comparatorByDate);
                break;
            case 7:
                Collections.sort(files, comparatorByDateDesc);
                break;

        }
        return files;
    }


    public static ArrayList<FileInfo> sortAppManager(ArrayList<FileInfo> files, int sortMode) {

        switch (sortMode) {
            case 0:
                Collections.sort(files, comparatorByName);
                break;
            case 1:
                Collections.sort(files, comparatorByNameDesc);
                break;
            case 2:
                Collections.sort(files, comparatorByType);
                break;
            case 3:
                Collections.sort(files, comparatorByTypeDesc);
                break;
            case 4:
                Collections.sort(files, comparatorBySizeApk);
                break;
            case 5:
                Collections.sort(files, comparatorBySizeApkDesc);
                break;
            case 6:
                Collections.sort(files, comparatorByDateApk);
                break;
            case 7:
                Collections.sort(files, comparatorByDateApkDesc);
                break;

        }
        return files;
    }


    private static final Comparator<? super FileInfo> comparatorByName = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            if ((file1.isDirectory()) && (!file2.isDirectory()))
                return -1;
            if ((!file1.isDirectory()) && (file2.isDirectory()))
                return 1;
            // here both are folders or both are files : sort alpha
            return file1.getFileName().toLowerCase()
                    .compareTo(file2.getFileName().toLowerCase());
        }

    };

    private static final Comparator<? super FileInfo> comparatorByNameDesc = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            if ((file1.isDirectory()) && (!file2.isDirectory()))
                return -1;
            if ((!file1.isDirectory()) && (file2.isDirectory()))
                return 1;
            // here both are folders or both are files : sort alpha
            return file2.getFileName().toLowerCase()
                    .compareTo(file1.getFileName().toLowerCase());
        }

    };

    private static final Comparator<? super FileInfo> comparatorBySize = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            if ((file1.isDirectory()) && (!file2.isDirectory())) {
                return -1;
            }
            if ((!file1.isDirectory()) && (file2.isDirectory())) {
                return 1;
            }


            Long first = getSize(new File(file1.getFilePath()));
            Long second = getSize(new File(file2.getFilePath()));
            return first.compareTo(second);
        }
    };

    private static final Comparator<? super FileInfo> comparatorBySizeDesc = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            if ((file1.isDirectory()) && (!file2.isDirectory())) {
                return -1;
            }
            if ((!file1.isDirectory()) && (file2.isDirectory())) {
                return 1;
            }

            Long first = getSize(new File(file1.getFilePath()));
            Long second = getSize(new File(file2.getFilePath()));
            return second.compareTo(first);
        }
    };

    private static final Comparator<? super FileInfo> comparatorBySizeApk = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            Long first = file1.getSize();
            Long second = file2.getSize();
            return first.compareTo(second);
        }
    };

    private static final Comparator<? super FileInfo> comparatorBySizeApkDesc = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            Long first = file1.getSize();
            Long second = file2.getSize();
            return second.compareTo(first);
        }
    };

    private static final Comparator<? super FileInfo> comparatorByDateApk = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            Long date1 = file1.getDate();
            Long date2 = file2.getDate();
            return date1.compareTo(date2);
        }
    };

    private static final Comparator<? super FileInfo> comparatorByDateApkDesc = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            Long date1 = file1.getDate();
            Long date2 = file2.getDate();
            return date2.compareTo(date1);
        }
    };

    private static long getSize(File file) {

        long size = 0;
        if (file.isFile()) {
            size = file.length();
        } else if (file.isDirectory()) {
            File[] list = file.listFiles();
            if (list != null) {
                size = list.length;
            }
        }
        return size;
    }

    private static final Comparator<? super FileInfo> comparatorByType = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            String arg0 = file1.getFileName();
            String arg1 = file2.getFileName();

            final int s1Dot = arg0.lastIndexOf('.');
            final int s2Dot = arg1.lastIndexOf('.');

            if ((s1Dot == -1) == (s2Dot == -1)) { // both or neither

                arg0 = arg0.substring(s1Dot + 1);
                arg1 = arg1.substring(s2Dot + 1);
                return (arg0.toLowerCase()).compareTo((arg1.toLowerCase()));
            } else if (s1Dot == -1) { // only s2 has an extension, so s1 goes
                // first
                return -1;
            } else { // only s1 has an extension, so s1 goes second
                return 1;
            }
        }

    };

    private static final Comparator<? super FileInfo> comparatorByTypeDesc = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            String arg0 = file2.getFileName();
            String arg1 = file1.getFileName();

            final int s1Dot = arg0.lastIndexOf('.');
            final int s2Dot = arg1.lastIndexOf('.');

            if ((s1Dot == -1) == (s2Dot == -1)) { // both or neither

                arg0 = arg0.substring(s1Dot + 1);
                arg1 = arg1.substring(s2Dot + 1);
                return (arg0.toLowerCase()).compareTo((arg1.toLowerCase()));
            } else if (s1Dot == -1) { // only s2 has an extension, so s1 goes
                // first
                return -1;
            } else { // only s1 has an extension, so s1 goes second
                return 1;
            }
        }

    };


    private static final Comparator<? super FileInfo> comparatorByDate = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            Long date1 = new File(file1.getFilePath()).lastModified();
            Long date2 = new File(file2.getFilePath()).lastModified();
            return date1.compareTo(date2);
        }
    };

    private static final Comparator<? super FileInfo> comparatorByDateDesc = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            Long date1 = new File(file1.getFilePath()).lastModified();
            Long date2 = new File(file2.getFilePath()).lastModified();
            return date2.compareTo(date1);
        }
    };


}
