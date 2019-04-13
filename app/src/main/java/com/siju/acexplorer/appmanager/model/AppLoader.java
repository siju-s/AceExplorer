package com.siju.acexplorer.appmanager.model;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.preference.PreferenceManager;

import com.siju.acexplorer.appmanager.helper.AppHelper;
import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.main.model.FileConstants;
import com.siju.acexplorer.main.model.groups.Category;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import static com.siju.acexplorer.appmanager.helper.AppHelper.isSystemPackage;
import static com.siju.acexplorer.main.model.helper.SortHelper.sortAppManager;

public class AppLoader extends AsyncTaskLoader<ArrayList<FileInfo>> {

    private ArrayList<FileInfo> fileInfoList;
    private PackageReceiver     packageReceiver;
    private int                 sortMode;


    public AppLoader(@NonNull Context context) {
        super(context);
        sortMode = PreferenceManager.getDefaultSharedPreferences(context).getInt(
                FileConstants.KEY_SORT_MODE, FileConstants.KEY_SORT_NAME);
    }

    @Override
    public void onCanceled(ArrayList<FileInfo> data) {
        super.onCanceled(data);
    }

    @Nullable
    @Override
    public ArrayList<FileInfo> loadInBackground() {
        getInstalledUserApps();
        sortAppManager(fileInfoList, sortMode);
        return fileInfoList;
    }

    private void getInstalledUserApps() {
        fileInfoList = new ArrayList<>();
        List<PackageInfo> packages = getContext().getPackageManager().getInstalledPackages(0);
        getAppPackageInfo(packages);
    }

    private void getAppPackageInfo(List<PackageInfo> packages) {
        for (PackageInfo packageInfo : packages) {
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;

            if (isSystemPackage(applicationInfo)) {
                continue;
            }
            File appDir = new File(applicationInfo.publicSourceDir);
            long size = appDir.length();
            long lastUpdateTime = packageInfo.lastUpdateTime;
            String appName = applicationInfo.loadLabel(getContext().getPackageManager()).toString();
            String packageName = applicationInfo.packageName;
            fileInfoList.add(FileInfo.createAppInfo(Category.APP_MANAGER, appName, packageName, lastUpdateTime, size));
        }
    }

    @Override
    protected void onStartLoading() {
        if (fileInfoList != null) {
            deliverResult(fileInfoList);
        }
        if (packageReceiver == null) {
            packageReceiver = new PackageReceiver(this);
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

        fileInfoList = null;
        if (packageReceiver != null) {
            getContext().unregisterReceiver(packageReceiver);
            packageReceiver = null;
        }
    }

    private static class PackageReceiver extends BroadcastReceiver {

        final AppLoader loader;

        PackageReceiver(AppLoader loader) {
            this.loader = loader;
            IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_ADDED);
            filter.addDataScheme(AppHelper.SCHEME_PACKAGE);
            this.loader.getContext().registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            loader.onContentChanged();
        }
    }
}
