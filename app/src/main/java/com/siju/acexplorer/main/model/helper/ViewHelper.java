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

package com.siju.acexplorer.main.model.helper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.siju.acexplorer.R;
import com.siju.acexplorer.analytics.Analytics;
import com.siju.acexplorer.main.view.dialog.DialogHelper;

import static android.webkit.MimeTypeMap.getSingleton;


public class ViewHelper {
    private static final String TAG = "ViewHelper";

    /**
     * View the file in external apps based on Mime Type
     *
     * @param context
     * @param path
     * @param extension
     */
    public static void viewFile(Context context, String path, String extension,
                                DialogHelper.AlertDialogListener alertDialogListener) {

        Uri uri = UriHelper.createContentUri(context, path);

        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (extension == null) {
            Analytics.getLogger().openAsDialogShown();
            DialogHelper.openWith(uri, context);
            return;
        }
        String ext = extension.toLowerCase();

        String texts[] = new String[]{context.getString(R.string.package_installer),
                context.getString(R.string.package_installer_content),
                context.getString(R.string.install), context.getString(R.string.dialog_cancel), context.getString(R.string.view),
        };

        if ("apk".equals(ext)) {
            DialogHelper.showAlertDialog(context, texts, alertDialogListener);
        } else {
            String mimeType = getSingleton().getMimeTypeFromExtension(ext);
            intent.setDataAndType(uri, mimeType);
            if (mimeType != null) {
                Analytics.getLogger().openFile();
                UriHelper.grantUriPermission(context, intent, uri);
            } else {
                Analytics.getLogger().openAsDialogShown();
                DialogHelper.openWith(uri, context);
            }
        }
    }

}
