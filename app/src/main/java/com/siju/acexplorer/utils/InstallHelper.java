package com.siju.acexplorer.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.Fragment;

import com.siju.acexplorer.R;
import com.siju.acexplorer.model.helper.SdkHelper;

import static com.siju.acexplorer.model.helper.FileUtils.showMessage;

public class InstallHelper {

    private static final String PACKAGE = "package";
    public  static final int UNKNOWN_APPS_INSTALL_REQUEST = 300;

    public static boolean canInstallApp(Context context) {
        if (SdkHelper.isAtleastOreo()) {
            return context.getPackageManager().canRequestPackageInstalls();
        }
        return true;
    }

    public static void requestUnknownAppsInstallPermission(Fragment fragment) {
        Context context = fragment.getContext();
        if (context == null) {
            return;
        }
        if (SdkHelper.isAtleastOreo()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            Uri uri = Uri.fromParts(PACKAGE, context.getPackageName(), null);
            intent.setData(uri);
            fragment.startActivityForResult(intent, UNKNOWN_APPS_INSTALL_REQUEST);
        }
    }

    public static void openInstallAppScreen(Context context, Uri uri) {
        if (uri == null) {
            return;
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_INSTALL_PACKAGE);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            showMessage(context, context.getString(R.string.msg_error_not_supported));
        }
    }
}
