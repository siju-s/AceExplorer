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

package com.siju.acexplorer.model.helper;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import com.siju.acexplorer.R;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.utils.Utils;

import java.io.File;
import java.util.List;

import static com.siju.acexplorer.model.helper.FileUtils.showMessage;

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

    public static Uri getUriFromFile(final String path, Context context) {
        ContentResolver resolver = context.getContentResolver();

        Cursor filecursor = resolver.query(MediaStore.Files.getContentUri("external"),
                new String[]{BaseColumns._ID}, MediaStore.MediaColumns.DATA + " = ?",
                new String[]{path}, MediaStore.MediaColumns.DATE_ADDED + " desc");

        if (filecursor != null) {
            filecursor.moveToFirst();
            if (filecursor.isAfterLast()) {
                filecursor.close();
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DATA, path);
                return resolver.insert(MediaStore.Files.getContentUri("external"), values);
            } else {
                int imageId = filecursor.getInt(filecursor.getColumnIndex(BaseColumns._ID));
                Uri uri = MediaStore.Files.getContentUri("external").buildUpon().appendPath(
                        Integer.toString(imageId)).build();
                filecursor.close();
                return uri;
            }
        }
        return null;

    }

}
