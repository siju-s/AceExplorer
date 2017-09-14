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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.model.FileInfo;

import java.util.ArrayList;

import static com.siju.acexplorer.model.groups.Category.checkIfFileCategory;


public class ShareHelper {

    public static void shareFiles(Context context, ArrayList<FileInfo> fileInfo, Category category) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        if (checkIfFileCategory(category)) {
            intent.setType("*/*");
        } else {
            String extension = fileInfo.get(0).getExtension();
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            intent.setType(mimeType);
        }

        ArrayList<Uri> files = new ArrayList<>();

        for (FileInfo info : fileInfo) {
            Uri uri = UriHelper.createContentUri(context, info.getFilePath());
            System.out.println("shareuri==" + uri);
            files.add(uri);
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        context.startActivity(intent);
    }

}
