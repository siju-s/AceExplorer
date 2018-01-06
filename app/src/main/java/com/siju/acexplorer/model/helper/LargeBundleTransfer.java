package com.siju.acexplorer.model.helper;

import android.content.Context;

import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.SharedPreferenceWrapper;

import java.util.List;

public class LargeBundleTransfer {

    public static final int LARGE_BUNDLE_LIMIT = 300;

    public static void storeFileData(Context context , List<FileInfo> files) {
        SharedPreferenceWrapper sharedPreferenceWrapper = new SharedPreferenceWrapper();
        sharedPreferenceWrapper.storeFileData(context, files);
    }

    public static List<FileInfo> getFileData(Context context) {
        SharedPreferenceWrapper sharedPreferenceWrapper = new SharedPreferenceWrapper();
        return sharedPreferenceWrapper.getFileData(context);
    }

    public static void removeFileData(Context context) {
        SharedPreferenceWrapper sharedPreferenceWrapper = new SharedPreferenceWrapper();
        sharedPreferenceWrapper.removeFileData(context);
    }

    public static void storeStringData(Context context , List<String> files) {
        SharedPreferenceWrapper sharedPreferenceWrapper = new SharedPreferenceWrapper();
        sharedPreferenceWrapper.storeStringData(context, files);
    }

    public static List<String> getStringData(Context context) {
        SharedPreferenceWrapper sharedPreferenceWrapper = new SharedPreferenceWrapper();
        return sharedPreferenceWrapper.getStringData(context);
    }

    public static void removeStringData(Context context) {
        SharedPreferenceWrapper sharedPreferenceWrapper = new SharedPreferenceWrapper();
        sharedPreferenceWrapper.removeStringData(context);
    }
}
