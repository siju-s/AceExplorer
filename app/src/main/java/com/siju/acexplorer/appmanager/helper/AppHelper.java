package com.siju.acexplorer.appmanager.helper;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import static com.siju.acexplorer.appmanager.view.AppDetailUi.REQUEST_CODE_UNINSTALL;


public class AppHelper {

    public static final  String SCHEME_PACKAGE     = "package";
    private static final String PREFIX_PACKAGE_URI = "package:";

    public static void uninstallApp(Activity activity, String packageName) {
        Uri packageUri = Uri.parse(PREFIX_PACKAGE_URI + packageName);
        Intent uninstallIntent =
                new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
        uninstallIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        activity.startActivityForResult(uninstallIntent, REQUEST_CODE_UNINSTALL);
    }


    public static void openAppSettings(Context context, String packageName) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts(SCHEME_PACKAGE, packageName, null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static boolean isPackageNotExisting(Context context, String packageName) {
        if (packageName == null) {
            return true;
        }
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_META_DATA);
            return false;
        }
        catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }

    public static boolean isSystemPackage(ApplicationInfo applicationInfo) {
        return (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

}