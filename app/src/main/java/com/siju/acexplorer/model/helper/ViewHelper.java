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
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.siju.acexplorer.R;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.utils.Dialogs;
import com.siju.acexplorer.view.dialog.DialogHelper;

import java.util.ArrayList;

import static android.webkit.MimeTypeMap.getSingleton;
import static com.siju.acexplorer.model.helper.UriHelper.grantUriPermission;



public class ViewHelper {
    private static final String TAG = "ViewHelper";

    /**
     * View the file in external apps based on Mime Type
     *
     * @param context
     * @param path
     * @param extension
     */
    public static void viewFile(Context context, String path, String extension, DialogHelper.AlertDialogListener alertDialogListener) {

        Uri uri = UriHelper.createContentUri(context, path);

        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (extension == null) {
            openWith(uri, context);
            return;
        }
        String ext = extension.toLowerCase();

        String texts[] = new String[]{context.getString(R.string.package_installer), context.getString(R.string
                .install), context.getString(R.string.dialog_cancel), context.getString(R.string.view),
                context.getString(R.string.package_installer_content)};

        if (ext.equals("apk")) {
            DialogHelper.showAlertDialog(context, texts, alertDialogListener);
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

    public static void openWith(final Uri uri, final Context context) {

        ArrayList<String> items = new ArrayList<>();
        items.add(context.getString(R.string.text));
        items.add(context.getString(R.string.image));
        items.add(context.getString(R.string.audio));
        items.add(context.getString(R.string.other));
        items.add(context.getString(R.string.text));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_open_as, null);
        builder.setView(dialogView);
        builder.setCancelable(true);

        final AlertDialog alertDialog = builder.create();


        TextView textTitle = alertDialog.findViewById(R.id.textTitle);
        ListView listView = alertDialog.findViewById(R.id.listOpenAs);

        textTitle.setText(context.getString(R.string.open_as));

        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, items);

        listView.setAdapter(itemsAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                switch (position) {
                    case 0:
                        intent.setDataAndType(uri, "text/*");
                        break;
                    case 1:
                        intent.setDataAndType(uri, "image/*");
                        break;
                    case 2:
                        intent.setDataAndType(uri, "video/*");
                        break;
                    case 3:
                        intent.setDataAndType(uri, "audio/*");
                        break;
                    case 4:
                        intent.setDataAndType(uri, "*/*");
                        break;
                }
                grantUriPermission(context, intent, uri);
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }
}
