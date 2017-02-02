package com.siju.acexplorer.filesystem.helper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import com.siju.acexplorer.R;
import com.siju.acexplorer.filesystem.groups.Category;
import com.siju.acexplorer.utils.Utils;

import java.io.File;
import java.util.List;

import static com.siju.acexplorer.filesystem.utils.FileUtils.showMessage;

/**
 * Created by SJ on 27-01-2017.
 */

public class UriHelper {

    public static Uri createContentUri(Context context, String path) {

        if (Utils.isAtleastNougat()) {
            String authority = context.getPackageName() + ".fileprovider";
            return FileProvider.getUriForFile(context, authority, new File(path));
        } else {
            return Uri.fromFile(new File(path));
        }
    }

    public static void grantUriPermission(Context context, Intent intent, Uri uri) {
        PackageManager packageManager = context.getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            if (Utils.isAtleastLollipop()) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                List<ResolveInfo> resInfoList = packageManager.queryIntentActivities(intent, PackageManager
                        .MATCH_DEFAULT_ONLY);

                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }
            context.startActivity(intent);
        } else {
            showMessage(context, context.getString(R.string.msg_error_not_supported));
        }
    }

    public static Uri getUriForCategory(Category category) {
        switch (category) {
            case FILES:
            case DOCS:
            case DOWNLOADS:
            case COMPRESSED:
            case PDF:
            case APPS:
            case LARGE_FILES:
                return MediaStore.Files.getContentUri("external");
            case AUDIO:
                return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            case VIDEO:
                return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            case IMAGE:
                return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        return MediaStore.Files.getContentUri("external");

    }

}