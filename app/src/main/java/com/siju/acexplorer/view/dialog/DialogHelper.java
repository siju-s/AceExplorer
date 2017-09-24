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

package com.siju.acexplorer.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.siju.acexplorer.R;
import com.siju.acexplorer.model.FileConstants;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.storage.model.operations.Operations;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.model.groups.Category.AUDIO;
import static com.siju.acexplorer.model.groups.Category.IMAGE;
import static com.siju.acexplorer.model.groups.Category.VIDEO;
import static com.siju.acexplorer.model.helper.AppUtils.getAppIcon;
import static com.siju.acexplorer.model.helper.AppUtils.getAppIconForFolder;

/**
 * Created by Siju on 29 August,2017
 */
public class DialogHelper {

    private static final String TAG = "DialogHelper";

   /* public static void showRenameDialog(final Context context, String name,
                                        String [] text,
                                        final int position, final DialogListener dialogListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.rename_dialog, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        final AlertDialog alertDialog = builder.create();

        TextView title = (TextView) dialogView.findViewById(R.id.textTitle);
        TextView msg = (TextView) dialogView.findViewById(R.id.textMessage);
        final EditText inputText = (EditText) dialogView.findViewById(R.id.editTextName);

        Button positiveButton = (Button) dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = (Button) dialogView.findViewById(R.id.buttonNegative);

        title.setText(context.getString(R.string.txt_edit_marker_rename_title));
        msg.setText(context.getString(R.string.txt_edit_marker_rename_message));
        positiveButton.setText(context.getString(R.string.txt_rename_action));
        negativeButton.setText(context.getString(R.string.txt_cancel_action));
        inputText.setText(name);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = inputText.getText().toString().trim();

                if (name.isEmpty()) {
                    dialogListener.onErrorInvalidName();
                } else {
                    v.setTag(position);
                    dialogListener.onPositiveButtonClick(v, name);
                    alertDialog.dismiss();
                }
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogListener.onNegativeButtonClick(v);
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }


    */

    /**
     * @param fileInfo Paths to delete
     */
    public static void showDeleteDialog(final Context context, final ArrayList<FileInfo> fileInfo, final DeleteDialogListener deleteDialogListener) {
        String title = context.getString(R.string.dialog_delete_title);
        String texts[] = new String[]{title, context.getString(R.string.msg_ok), "", context.getString(R.string
                .dialog_cancel)};

        ArrayList<String> items = new ArrayList<>();
        for (int i = 0; i < fileInfo.size(); i++) {
            String path = fileInfo.get(i).getFilePath();
            items.add(path);
            if (i == 9 && fileInfo.size() > 10) {
                int rem = fileInfo.size() - 10;
                items.add("+" + rem + " " + context.getString(R.string.more));
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_open_as, null);
        builder.setView(dialogView);
        builder.setCancelable(true);

        final AlertDialog alertDialog = builder.create();


        TextView textTitle = alertDialog.findViewById(R.id.textTitle);
        ListView listView = alertDialog.findViewById(R.id.listOpenAs);
        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        textTitle.setText(title);
        positiveButton.setText(texts[1]);
        negativeButton.setText(texts[3]);

        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, items);

        listView.setAdapter(itemsAdapter);


        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDialogListener.onPositiveButtonClick(view, fileInfo);
                alertDialog.dismiss();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    /**
     * @param fileInfo Paths to delete
     */
    public static void showSortDialog(final Context context, int sortMode, final AlertDialogListener alertDialogListener) {
        String title = context.getString(R.string.action_sort);
        String texts[] = new String[]{title, context.getString(R.string.msg_ok), "", context.getString(R.string
                .dialog_cancel)};


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_sort, null);
        builder.setView(dialogView);
        builder.setCancelable(true);

        final AlertDialog alertDialog = builder.create();


        TextView textTitle = alertDialog.findViewById(R.id.textTitle);
        final ListView listView = alertDialog.findViewById(R.id.listSortOptions);
        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        textTitle.setText(title);
        positiveButton.setText(texts[1]);
        negativeButton.setText(texts[3]);

        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<>(context, R.layout.sort_options);

        listView.setAdapter(itemsAdapter);

        final int[] position = new int[1];
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                position[0] = i;
            }
        });


        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setTag(position[0]);
                alertDialogListener.onPositiveButtonClick(view);
                alertDialog.dismiss();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    /**
     * @param context
     * @param text           0->title, 1->msg, 2->Positive button 3->Negative button
     * @param textEdit
     * @param dialogListener
     */
    public static void showInputDialog(final Context context, String[] text,
                                       final Operations operation,
                                       String textEdit, final DialogCallback dialogListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.rename_dialog, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        final AlertDialog alertDialog = builder.create();

        TextView title = dialogView.findViewById(R.id.textTitle);
        TextView msg = dialogView.findViewById(R.id.textMessage);
        final EditText inputText = dialogView.findViewById(R.id.editTextName);

        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        title.setText(text[0]);
        msg.setText(text[1]);
        positiveButton.setText(text[2]);
        negativeButton.setText(text[3]);


        if (textEdit != null) {
            inputText.setText(textEdit);
        }
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = inputText.getText().toString().trim();
                dialogListener.onPositiveButtonClick(alertDialog, operation, name);

            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogListener.onNegativeButtonClick(operation);
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }


    /**
     * @param context
     * @param text           0->title, 1->msg, 2->Positive button 3->Negative button
     * @param dialogListener
     */
    public static void showAlertDialog(final Context context, String text[],
                                       final AlertDialogListener dialogListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.alert_dialog, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        final AlertDialog alertDialog = builder.create();

        TextView title = dialogView.findViewById(R.id.textTitle);
        TextView msg = dialogView.findViewById(R.id.textMessage);

        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);
        Button neutralButton = dialogView.findViewById(R.id.buttonNeutral);


        title.setText(text[0]);
        msg.setText(text[1]);
        positiveButton.setText(text[2]);


        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogListener.onPositiveButtonClick(v);
                alertDialog.dismiss();
            }
        });

        if (text.length > 3) {
            negativeButton.setVisibility(View.VISIBLE);
            negativeButton.setText(text[3]);
            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogListener.onNegativeButtonClick(v);
                    alertDialog.dismiss();
                }
            });
        }

        if (text.length > 4) {
            neutralButton.setVisibility(View.VISIBLE);
            neutralButton.setText(text[4]);
            neutralButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogListener.onNeutralButtonClick(v);
                    alertDialog.dismiss();
                }
            });
        }

        alertDialog.show();
    }

    /**
     * @param context
     * @param text           0->title, 1->msg, 2->Positive button 3->Negative button
     * @param dialogListener
     */
    public static void showCustomDialog(final Context context, String text[],
                                        int resourceId,
                                        final AlertDialogListener dialogListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(resourceId, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        final AlertDialog alertDialog = builder.create();

        TextView title = dialogView.findViewById(R.id.textTitle);
        TextView msg = dialogView.findViewById(R.id.textMessage);

        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        title.setText(text[0]);
        msg.setText(text[1]);
        positiveButton.setText(text[2]);


        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogListener.onPositiveButtonClick(v);
                alertDialog.dismiss();
            }
        });

        if (text.length > 3) {
            negativeButton.setVisibility(View.VISIBLE);
            negativeButton.setText(text[3]);
            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogListener.onNegativeButtonClick(v);
                    alertDialog.dismiss();
                }
            });
        }

        alertDialog.show();
    }

    public static void showSAFDialog(Context context, int resourceId, String[] text, final AlertDialogListener dialogListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(resourceId, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        final AlertDialog alertDialog = builder.create();

        TextView title = dialogView.findViewById(R.id.textTitle);
        TextView msg = dialogView.findViewById(R.id.textMessage);

        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        title.setText(text[0]);
        msg.setText(text[1]);
        positiveButton.setText(text[2]);


        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogListener.onPositiveButtonClick(v);
                alertDialog.dismiss();
            }
        });

        if (text.length > 3) {
            negativeButton.setVisibility(View.VISIBLE);
            negativeButton.setText(text[3]);
            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogListener.onNegativeButtonClick(v);
                    alertDialog.dismiss();
                }
            });
        }

        alertDialog.show();

    }

    public static void showConflictDialog(Context context, final List<FileInfo> conflictFiles,
                                          final String destinationDir, final boolean isMove,
                                          final PasteConflictListener pasteConflictListener) {
        String texts[] = new String[]{context.getString(R.string.dialog_title_paste_conflict),
                context.getString(R.string.dialog_skip), context.getString(R.string
                .dialog_keep_both), context.getString(R
                .string.dialog_replace)};

        int counter = 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_paste_conflict, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        final AlertDialog dialog = builder.create();

        final CheckBox checkBox = dialog.findViewById(R.id.checkBox);
        ImageView icon = dialog.findViewById(R.id.imageFileIcon);
        TextView textFileName = dialog.findViewById(R.id.textFileName);
        TextView textFileDate = dialog.findViewById(R.id.textFileDate);
        TextView textFileSize = dialog.findViewById(R.id.textFileSize);
        Button positiveButton = dialog.findViewById(R.id.buttonSkip);
        Button negativeButton = dialog.findViewById(R.id.buttonReplace);
        Button neutralButton = dialog.findViewById(R.id.buttonKeepBoth);


        String fileName = conflictFiles.get(0).getFileName();
        textFileName.setText(fileName);
        File sourceFile = new File(conflictFiles.get(0).getFilePath());
        long date = sourceFile.lastModified();
        String fileModifiedDate = FileUtils.convertDate(date);
        long size = sourceFile.length();
        String fileSize = Formatter.formatFileSize(context, size);
        textFileDate.setText(fileModifiedDate);
        textFileSize.setText(fileSize);
        Drawable drawable = getAppIcon(context, conflictFiles.get(0).getFilePath());
        if (drawable != null) {
            icon.setImageDrawable(drawable);
        }

        // POSITIVE BUTTON ->SKIP   NEGATIVE ->REPLACE    NEUTRAL ->KEEP BOTH
        if (sourceFile.getParent().equals(destinationDir)) {
            if (isMove) {
                neutralButton.setEnabled(false);
                negativeButton.setEnabled(false);
            } else {
                negativeButton.setEnabled(false);
            }
        }

        if (new File(conflictFiles.get(0).getFilePath()).isDirectory()) {
            neutralButton.setEnabled(false);
        }


        positiveButton.setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {

                pasteConflictListener.onPositiveButtonClick(dialog, Operations.COPY, conflictFiles, destinationDir,
                        isMove, checkBox.isChecked());

            }
        });

        negativeButton.setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                pasteConflictListener.onNegativeButtonClick(dialog, Operations.COPY, conflictFiles, destinationDir,
                        isMove, checkBox.isChecked());
            }
        });

        neutralButton.setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                pasteConflictListener.onNeutralButtonClick(dialog, Operations.COPY, conflictFiles, destinationDir,
                        isMove, checkBox.isChecked());
            }
        });


        dialog.show();
    }



    public static void showPermissionsDialog(Context context, final String path, final boolean isDir, final ArrayList<Boolean[]> permissions,
                                       final PermissionDialogListener permissionDialogListener) {

        String texts[] = new String[]{context.getString(R.string.permissions), context.getString(R.string.msg_ok),
                "", context.getString(R.string.dialog_cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_permission, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        final AlertDialog alertDialog = builder.create();

        final CheckBox readown =  alertDialog.findViewById(R.id.creadown);
        final CheckBox readgroup =  alertDialog.findViewById(R.id.creadgroup);
        final CheckBox readother =  alertDialog.findViewById(R.id.creadother);
        final CheckBox writeown =  alertDialog.findViewById(R.id.cwriteown);
        final CheckBox writegroup =  alertDialog.findViewById(R.id.cwritegroup);
        final CheckBox writeother =  alertDialog.findViewById(R.id.cwriteother);
        final CheckBox exeown =  alertDialog.findViewById(R.id.cexeown);
        final CheckBox exegroup =  alertDialog.findViewById(R.id.cexegroup);
        final CheckBox exeother =  alertDialog.findViewById(R.id.cexeother);

        Button positiveButton = alertDialog.findViewById(R.id.buttonPositive);
        Button negativeButton = alertDialog.findViewById(R.id.buttonNegative);

        final Boolean[] read = permissions.get(0);
        final Boolean[] write = permissions.get(1);
        final Boolean[] exe = permissions.get(2);

        readown.setChecked(read[0]);
        readgroup.setChecked(read[1]);
        readother.setChecked(read[2]);
        writeown.setChecked(write[0]);
        writegroup.setChecked(write[1]);
        writeother.setChecked(write[2]);
        exeown.setChecked(exe[0]);
        exegroup.setChecked(exe[1]);
        exeother.setChecked(exe[2]);



        positiveButton.setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                int a = 0, b = 0, c = 0;
                if (readown.isChecked()) {
                    a = 4;
                }
                if (writeown.isChecked()) {
                    b = 2;
                }
                if (exeown.isChecked()) {
                    c = 1;
                }
                int owner = a + b + c;
                int d = 0, e = 0, f = 0;
                if (readgroup.isChecked()) {
                    d = 4;
                }
                if (writegroup.isChecked()) {
                    e = 2;
                }
                if (exegroup.isChecked()) {
                    f = 1;
                }
                int group = d + e + f;
                int g = 0, h = 0, i = 0;
                if (readother.isChecked()) {
                    g = 4;
                }
                if (writeother.isChecked()) {
                    h = 2;
                }
                if (exeother.isChecked()) {
                    i = 1;
                }
                int other = g + h + i;
                String finalValue = owner + "" + group + "" + other;

                permissionDialogListener.onPositiveButtonClick(path, isDir, finalValue);

            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }


    public static void showInfoDialog(Context context, FileInfo fileInfo, boolean isFileCategory) {
        String title = context.getString(R.string.properties);
        String texts[] = new String[]{title, context.getString(R.string.msg_ok), "", null};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_file_properties, null);
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();

        ImageView imageFileIcon =  alertDialog.findViewById(R.id.imageFileIcon);
        TextView textFileName =  alertDialog.findViewById(R.id.textFileName);
        TextView textPath =  alertDialog.findViewById(R.id.textPath);
        TextView textFileSize =  alertDialog.findViewById(R.id.textFileSize);
        TextView textDateModified =  alertDialog.findViewById(R.id.textDateModified);
        TextView textHidden =  alertDialog.findViewById(R.id.textHidden);
        TextView textReadable =  alertDialog.findViewById(R.id.textReadable);
        TextView textWriteable =  alertDialog.findViewById(R.id.textWriteable);
        TextView textHiddenPlaceHolder =  alertDialog.findViewById(R.id.textHiddenPlaceHolder);
        TextView textReadablePlaceHolder =  alertDialog.findViewById(R.id
                .textReadablePlaceHolder);
        TextView textWriteablePlaceHolder =  alertDialog.findViewById(R.id
                .textWriteablePlaceHolder);
        TextView textMD5 =  alertDialog.findViewById(R.id.textMD5);
        TextView textMD5Placeholder =  alertDialog.findViewById(R.id.textMD5PlaceHolder);

        String path = fileInfo.getFilePath();
        String fileName = fileInfo.getFileName();
        String fileDate;
        if (isFileCategory) {
            fileDate = FileUtils.convertDate(fileInfo.getDate());
        }
        else {
            fileDate = FileUtils.convertDate(fileInfo.getDate() * 1000);
        }
        boolean isDirectory = fileInfo.isDirectory();
        String fileNoOrSize;
        if (isDirectory) {
            int childFileListSize = (int) fileInfo.getSize();
            if (childFileListSize == 0) {
                fileNoOrSize = context.getString(R.string.empty);
            }
            else if (childFileListSize == -1) {
                fileNoOrSize = "";
            }
            else {
                fileNoOrSize = context.getResources().getQuantityString(R.plurals.number_of_files,
                        childFileListSize, childFileListSize);
            }
        }
        else {
            long size = fileInfo.getSize();
            fileNoOrSize = Formatter.formatFileSize(context, size);
        }
        boolean isReadable = new File(path).canRead();
        boolean isWriteable = new File(path).canWrite();
        boolean isHidden = new File(path).isHidden();

        textFileName.setText(fileName);
        textPath.setText(path);
        textFileSize.setText(fileNoOrSize);
        textDateModified.setText(fileDate);

        if (!isFileCategory) {
            textMD5.setVisibility(View.GONE);
            textMD5Placeholder.setVisibility(View.GONE);
            textReadablePlaceHolder.setVisibility(View.GONE);
            textWriteablePlaceHolder.setVisibility(View.GONE);
            textHiddenPlaceHolder.setVisibility(View.GONE);
            textReadable.setVisibility(View.GONE);
            textWriteable.setVisibility(View.GONE);
            textHidden.setVisibility(View.GONE);
        }
        else {
            String yes = context.getString(R.string.yes);
            String no = context.getString(R.string.no);

            textReadable.setText(isReadable ? yes : no);
            textWriteable.setText(isWriteable ? yes : no);
            textHidden.setText(isHidden ? yes : no);
        }

        if (new File(path).isDirectory()) {
            textMD5.setVisibility(View.GONE);
            textMD5Placeholder.setVisibility(View.GONE);
            Drawable apkIcon = getAppIconForFolder(context, fileName);
            if (apkIcon != null) {
                imageFileIcon.setImageDrawable(apkIcon);
            }
            else {
                imageFileIcon.setImageResource(R.drawable.ic_folder);
            }
        }
        else {
            if (isFileCategory) {
                String md5 = FileUtils.getFastHash(path);
                textMD5.setText(md5);
            }

            if (fileInfo.getType() == VIDEO.getValue()) {
                Uri videoUri = Uri.fromFile(new File(path));
                Glide.with(context).load(videoUri).centerCrop()
                        .placeholder(R.drawable.ic_movie)
                        .crossFade(2)
                        .into(imageFileIcon);
            }
            else if (fileInfo.getType() == IMAGE.getValue()) {
                Uri imageUri = Uri.fromFile(new File(path));
                Glide.with(context).load(imageUri).centerCrop()
                        .crossFade(2)
                        .placeholder(R.drawable.ic_image_default)
                        .into(imageFileIcon);
            }
            else if (fileInfo.getType() == AUDIO.getValue()) {
                imageFileIcon.setImageResource(R.drawable.ic_music_default);
            }
            else if (fileInfo.getExtension().equals(FileConstants.APK_EXTENSION)) {
                Drawable apkIcon = getAppIcon(context, path);
                imageFileIcon.setImageDrawable(apkIcon);
            }
            else {
                imageFileIcon.setImageResource(R.drawable.ic_doc_white);
            }
        }

        Button positiveButton = alertDialog.findViewById(R.id.buttonPositive);

        positiveButton.setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }


    public static void showCompressDialog(Context context, final String currentDir, final ArrayList<FileInfo>
            paths, final CompressDialogListener dialogListener) {

        final String ext = ".zip";
        String fileName = paths.get(0).getFileName();
        String filePath = paths.get(0).getFilePath();
        String zipName = fileName;
        if (!(new File(filePath).isDirectory())) {
            zipName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        String title = context.getString(R.string.create);
        String texts[] = new String[]{"", zipName, title, title, "",
                context.getString(R.string.dialog_cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.rename_dialog, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        final AlertDialog alertDialog = builder.create();

        TextView titleText = dialogView.findViewById(R.id.textTitle);
        TextView msg = dialogView.findViewById(R.id.textMessage);
        final EditText inputText = dialogView.findViewById(R.id.editTextName);

        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        titleText.setText(title);
        inputText.setText(zipName);
        positiveButton.setText(context.getString(R.string.background));
        negativeButton.setText(context.getString(R.string.dialog_cancel));


        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = inputText.getText().toString().trim();
                dialogListener.onPositiveButtonClick(alertDialog, Operations.COMPRESS, name, ext, paths);

            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogListener.onNegativeButtonClick(Operations.COMPRESS);
                alertDialog.dismiss();
            }
        });

/*
        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = materialDialog.getInputEditText().getText().toString();
                if (FileUtils.isFileNameInvalid(fileName)) {
                    materialDialog.getInputEditText().setError(baseFileList.getResources().getString(R.string
                            .msg_error_invalid_name));
                    return;
                }
                String newFilePath = currentDir + "/" + fileName + ext;

                if (FileUtils.isFileExisting(currentDir, fileName + ext)) {
                    materialDialog.getInputEditText().setError(baseFileList.getResources().getString(R.string
                            .dialog_title_paste_conflict));
                    return;
                }
                baseFileList.getFileOpHelper().compressFile(new File(newFilePath), paths);
                materialDialog.dismiss();
            }
        });*/

        alertDialog.show();
    }

    public static void showExtractOptions(Context context, final String currentFilePath, final String currentDir,
                                    final ExtractDialogListener extractDialogListener) {

        final String currentFileName = currentFilePath.substring(currentFilePath.lastIndexOf("/")
                + 1, currentFilePath.lastIndexOf("."));
        String texts[] = new String[]{context.getString(R.string.extract), context.getString(R.string.extract),
                "", context.getString(R.string.dialog_cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_extract, null);
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();

        final RadioButton radioButtonSpecify =  alertDialog.findViewById(R.id
                .radioButtonSpecifyPath);
        final Button buttonPathSelect =  alertDialog.findViewById(R.id.buttonPathSelect);
        RadioGroup radioGroupPath =  alertDialog.findViewById(R.id.radioGroupPath);
        final EditText editFileName =  alertDialog.findViewById(R.id.editFileName);
        editFileName.setText(currentFileName);
        radioGroupPath.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.radioButtonCurrentPath) {
                    buttonPathSelect.setVisibility(View.GONE);
                }
                else {
                    buttonPathSelect.setVisibility(View.VISIBLE);
                }
            }
        });

        buttonPathSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                extractDialogListener.onSelectButtonClick(buttonPathSelect);
            }
        });

        Button positiveButton = alertDialog.findViewById(R.id.buttonPositive);
        Button negativeButton = alertDialog.findViewById(R.id.buttonNegative);

        positiveButton.setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = editFileName.getText().toString();
                extractDialogListener.onPositiveButtonClick(alertDialog, currentFilePath, fileName, radioButtonSpecify.isChecked());
            }
        });

        negativeButton.setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }


    public static void showDragDialog(Context context, final ArrayList<FileInfo> filesToPaste, final String
            destinationDir,
                                          final DragDialogListener dragDialogListener) {


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_drag, null);
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();

        final RadioButton radioButtonCopy =  alertDialog.findViewById(R.id
                .radioCopy);

        final TextView textMessage = alertDialog.findViewById(R.id.textMessage);

        textMessage.setText(context.getString(R.string.dialog_to_placeholder) + " " + destinationDir);

        Button positiveButton = alertDialog.findViewById(R.id.buttonPositive);
        Button negativeButton = alertDialog.findViewById(R.id.buttonNegative);

        positiveButton.setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                dragDialogListener.onPositiveButtonClick(filesToPaste, destinationDir,
                        !radioButtonCopy.isChecked());
            }
        });

        negativeButton.setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }



    public interface ExtractDialogListener {

        void onPositiveButtonClick(Dialog dialog , String currentFile, String newFileName, boolean isChecked);

        void onSelectButtonClick(Button path);


    }

    public interface PermissionDialogListener {

        void onPositiveButtonClick(String path, boolean isDir, String permissions);


    }


    public interface CompressDialogListener {

        void onPositiveButtonClick(Dialog dialog, Operations operation, String newFileName,
                                   String extension, ArrayList<FileInfo> paths);

        void onNegativeButtonClick(Operations operation);

    }

    public interface AlertDialogListener {

        void onPositiveButtonClick(View view);

        void onNegativeButtonClick(View view);

        void onNeutralButtonClick(View view);

    }


    public interface DeleteDialogListener {

        void onPositiveButtonClick(View view, ArrayList<FileInfo> filesToDelete);

    }


    public interface DialogCallback {

        void onPositiveButtonClick(Dialog dialog, Operations operation, String name);

        void onNegativeButtonClick(Operations operations);
    }


    public interface PasteConflictListener {


        void onPositiveButtonClick(Dialog dialog, Operations operation, List<FileInfo> conflictFiles,
                                   String destinationDir, boolean isMove, boolean isChecked);

        void onNegativeButtonClick(Dialog dialog, Operations operation, List<FileInfo> conflictFiles,
                                   String destinationDir, boolean isMove, boolean isChecked);

        void onNeutralButtonClick(Dialog dialog, Operations operation, List<FileInfo> conflictFiles,
                                  String destinationDir, boolean isMove, boolean isChecked);
    }

    public interface DragDialogListener {

        void onPositiveButtonClick(ArrayList<FileInfo> filesToPaste, String destinationDir, boolean isMove);

    }


}
