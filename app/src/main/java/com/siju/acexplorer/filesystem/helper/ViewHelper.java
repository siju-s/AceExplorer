/*
 * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
     * @param context
     * @param path
     * @param extension
     */
    public static void viewFile(Context context, String path, String extension) {

        Uri uri = createContentUri(context, path);

        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (extension == null) {
            openWith(uri, context);
            return;
        }
        String ext = extension.toLowerCase();

        if (ext.equals("apk")) {
            showApkOptionsDialog(context, path, ext);
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
