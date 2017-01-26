package com.siju.acexplorer.filesystem.helper;

import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.model.ZipModel;

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

    public static Comparator<? super ZipModel> comparatorByNameZip1 = new Comparator<ZipModel>() {

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

}
