package com.siju.acexplorer.appmanager;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.siju.acexplorer.model.FileConstants;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.groups.Category;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.model.helper.SortHelper.sortAppManager;

public class AppLoader extends AsyncTaskLoader<ArrayList<FileInfo>> {

    private ArrayList<FileInfo> fileInfoList;
    private       int     sortMode;
    private PackageReceiver packageReceiver;


    public AppLoader(@NonNull Context context) {
        super(context);
        sortMode = PreferenceManager.getDefaultSharedPreferences(context).getInt(
                FileConstants.KEY_SORT_MODE, FileConstants.KEY_SORT_NAME);
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


    @Override
    public void onCanceled(ArrayList<FileInfo> data) {
        super.onCanceled(data);
    }

    @Nullable
    @Override
    public ArrayList<FileInfo> loadInBackground() {
        getInstalledPackages();
        return fileInfoList;
    }

    private void getInstalledPackages() {
        fileInfoList = new ArrayList<>();
        List<PackageInfo> packages = getContext().getPackageManager().getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;

            if (isSystemPackage(applicationInfo)) {
                continue;
            }
            String name = applicationInfo.loadLabel(getContext().getPackageManager()).toString();
            long date = packageInfo.lastUpdateTime;
            File file = new File(applicationInfo.publicSourceDir);
            long size = file.length();
            String packageName = applicationInfo.packageName;
            fileInfoList.add(new FileInfo(Category.APP_MANAGER, name, packageName, date, size));
        }
        if (fileInfoList.size() != 0) {
            fileInfoList = sortAppManager(fileInfoList, sortMode);
        }

    }

    private boolean isSystemPackage(ApplicationInfo applicationInfo) {
        return (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    private static class PackageReceiver extends BroadcastReceiver {

        final AppLoader loader;

        PackageReceiver(AppLoader loader) {
            this.loader = loader;
            IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_ADDED);
            filter.addDataScheme("package");
            this.loader.getContext().registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            loader.onContentChanged();
        }
    }
}
