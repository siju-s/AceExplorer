package com.siju.acexplorer.filesystem.helper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

import com.siju.acexplorer.common.Logger;

import static android.webkit.MimeTypeMap.getSingleton;
import static com.siju.acexplorer.filesystem.helper.UriHelper.createContentUri;
import static com.siju.acexplorer.filesystem.helper.UriHelper.grantUriPermission;
import static com.siju.acexplorer.utils.Dialogs.openWith;
import static com.siju.acexplorer.utils.Dialogs.showApkOptionsDialog;


public class ViewHelper {
    private static final String TAG = "ViewHelper";

    /**
     * View the file in external apps based on Mime Type
     *
     * @param fragment
     * @param path
     * @param extension
     */
    public static void viewFile(Fragment fragment, String path, String extension) {

        Context context = fragment.getContext();
        Uri uri = createContentUri(fragment.getContext(), path);

        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (extension == null) {
            openWith(uri, context);
            return;
        }
        String ext = extension.toLowerCase();

        if (ext.equals("apk")) {
            showApkOptionsDialog(fragment, path, ext);
        } else {
            String mimeType = getSingleton().getMimeTypeFromExtension(ext);
            Logger.log(TAG, " uri==" + uri + "MIME=" + mimeType);
            intent.setDataAndType(uri, mimeType);
            if (mimeType != null) {
                grantUriPermission(context, intent, uri);
            } else {
                openWith(uri, context);
            }
        }

    }
}
