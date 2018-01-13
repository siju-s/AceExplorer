package com.siju.acexplorer.appmanager;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import static com.siju.acexplorer.appmanager.AppInfoActivity.REQUEST_CODE_UNINSTALL;

public class AppHelper {

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
        Uri uri = Uri.fromParts("package", packageName, null);
        intent.setData(uri);
        context.startActivity(intent);
    }

}
