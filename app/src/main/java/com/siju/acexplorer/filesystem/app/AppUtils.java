package com.siju.acexplorer.filesystem.app;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.siju.acexplorer.R;


public class AppUtils {

    public static Drawable getAppIcon(Context context, String url) {


        Drawable icon;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(url, PackageManager
                    .GET_ACTIVITIES);
            if (packageInfo == null)
                return ContextCompat.getDrawable(context, R.drawable.ic_apk_green);
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            appInfo.sourceDir = url;
            appInfo.publicSourceDir = url;

            icon = appInfo.loadIcon(context.getPackageManager());
            return icon;
        } catch (Exception e) {
            e.printStackTrace();
            return ContextCompat.getDrawable(context, R.drawable.ic_apk_green);

        }
    }

    public static Drawable getAppIconForFolder(Context context, String packageName) {

        try {
            return context.getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
