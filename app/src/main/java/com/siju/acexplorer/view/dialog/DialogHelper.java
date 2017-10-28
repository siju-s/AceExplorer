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
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AlertDialog;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
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
import com.siju.acexplorer.view.ConflictFileInfo;
import com.siju.acexplorer.view.PasteConflictAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.model.groups.Category.AUDIO;
import static com.siju.acexplorer.model.groups.Category.IMAGE;
import static com.siju.acexplorer.model.groups.Category.VIDEO;
import static com.siju.acexplorer.model.helper.AppUtils.getAppIcon;
import static com.siju.acexplorer.model.helper.AppUtils.getAppIconForFolder;
import static com.siju.acexplorer.model.helper.UriHelper.grantUriPermission;

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
        View dialogView = inflater.inflate(R.layout.dialog_rename, null);
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
    public static void showDeleteDialog(final Context context, final ArrayList<FileInfo> fileInfo,
                                        final DeleteDialogListener deleteDialogListener) {
        String title = context.getString(R.string.dialog_delete_title);
        String texts[] = new String[]{title, context.getString(R.string.msg_ok), "", context.getString(R.string
                .dialog_cancel)};


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_delete, null);
        builder.setView(dialogView);
        builder.setCancelable(true);

        final AlertDialog alertDialog = builder.create();


        TextView textTitle = dialogView.findViewById(R.id.textTitle);
        TextView textItems = dialogView.findViewById(R.id.textDeleteItems);
        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        textTitle.setText(title);
        positiveButton.setText(texts[1]);
        negativeButton.setText(texts[3]);


        StringBuilder items = new StringBuilder();
        for (int i = 0; i < fileInfo.size(); i++) {
            String path = fileInfo.get(i).getFilePath();
            items.append(path).append('\n');
            if (i == 9 && fileInfo.size() > 10) {
                int rem = fileInfo.size() - 10;
                items.append("+" + rem + " " + context.getString(R.string.more));
                break;
            }
        }

        textItems.setText(items);

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


        TextView textTitle = dialogView.findViewById(R.id.textTitle);
        final RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroupSort);
        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        textTitle.setText(title);
        positiveButton.setText(texts[1]);
        negativeButton.setText(texts[3]);
        Log.d(TAG, "showSortDialog: "+radioGroup + " sort:"+sortMode);
        View radioButton = radioGroup.getChildAt(sortMode);
        radioGroup.check(radioButton.getId());

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                View radioButton = radioGroup.findViewById(radioButtonID);
                int index = radioGroup.indexOfChild(radioButton);
                Log.d(TAG, "onClick: "+index);
                view.setTag(index);
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
        View dialogView = inflater.inflate(R.layout.dialog_rename, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        final AlertDialog alertDialog = builder.create();

        TextView title = dialogView.findViewById(R.id.textTitle);
        TextView msg = dialogView.findViewById(R.id.textMessage);
        final EditText inputText = dialogView.findViewById(R.id.editFileName);

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
                                          final List<FileInfo> destFiles, final String destinationDir, final boolean isMove,
                                          final PasteConflictListener pasteConflictListener) {
        String texts[] = new String[]{context.getString(R.string.dialog_title_paste_conflict),
                context.getString(R.string.dialog_skip), context.getString(R.string
                .dialog_keep_both), context.getString(R
                .string.dialog_replace)};


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_paste_conflict, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        final AlertDialog dialog = builder.create();

        final GridView gridView = dialogView.findViewById(R.id.gridFiles);

        List<ConflictFileInfo> fileInfoList = new ArrayList<>();

        String sourcePath = conflictFiles.get(0).getFilePath();
        String sourceFileName = conflictFiles.get(0).getFileName();
        File sourceFile = new File(sourcePath);
        long date = sourceFile.lastModified();
        String sourceFileDate = FileUtils.convertDate(date);
        long size = sourceFile.length();
        String sourceFileSize = Formatter.formatFileSize(context, size);

        fileInfoList.add(new ConflictFileInfo(sourceFileName, sourcePath, sourceFileDate, sourceFileSize));


        String destFileName = destFiles.get(0).getFileName();
        String destPath = destFiles.get(0).getFilePath();
        File destFile = new File(destFiles.get(0).getFilePath());
        long destFileDate = destFile.lastModified();
        String destFileModifiedDate = FileUtils.convertDate(destFileDate);
        long destSize = destFile.length();
        String destFileSize = Formatter.formatFileSize(context, destSize);

        fileInfoList.add(new ConflictFileInfo(destFileName, destPath, destFileModifiedDate, destFileSize));
        PasteConflictAdapter pasteConflictAdapter = new PasteConflictAdapter(context, fileInfoList);
        gridView.setAdapter(pasteConflictAdapter);


        final CheckBox checkBox = dialogView.findViewById(R.id.checkBox);
        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);
        Button neutralButton = dialogView.findViewById(R.id.buttonKeepBoth);

        positiveButton.setText(texts[1]);
        negativeButton.setText(texts[3]);

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

                pasteConflictListener.onPositiveButtonClick(dialog, Operations.COPY, destFiles, conflictFiles, destinationDir,
                                                            isMove, checkBox.isChecked());

            }
        });

        negativeButton.setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                pasteConflictListener.onNegativeButtonClick(dialog, Operations.COPY, destFiles, conflictFiles, destinationDir,
                        isMove, checkBox.isChecked());
            }
        });

        neutralButton.setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                pasteConflictListener.onNeutralButtonClick(dialog, Operations.COPY, destFiles, conflictFiles, destinationDir,
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

        final CheckBox readown =  dialogView.findViewById(R.id.creadown);
        final CheckBox readgroup =  dialogView.findViewById(R.id.creadgroup);
        final CheckBox readother =  dialogView.findViewById(R.id.creadother);
        final CheckBox writeown =  dialogView.findViewById(R.id.cwriteown);
        final CheckBox writegroup =  dialogView.findViewById(R.id.cwritegroup);
        final CheckBox writeother =  dialogView.findViewById(R.id.cwriteother);
        final CheckBox exeown =  dialogView.findViewById(R.id.cexeown);
        final CheckBox exegroup =  dialogView.findViewById(R.id.cexegroup);
        final CheckBox exeother =  dialogView.findViewById(R.id.cexeother);

        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

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

        ImageView imageFileIcon =  dialogView.findViewById(R.id.imageFileIcon);
        TextView textFileName =  dialogView.findViewById(R.id.textFileName);
        TextView textPath =  dialogView.findViewById(R.id.textPath);
        TextView textFileSize =  dialogView.findViewById(R.id.textFileSize);
        TextView textDateModified =  dialogView.findViewById(R.id.textDateModified);
        TextView textHidden =  dialogView.findViewById(R.id.textHidden);
        TextView textReadable =  dialogView.findViewById(R.id.textReadable);
        TextView textWriteable =  dialogView.findViewById(R.id.textWriteable);
        TextView textHiddenPlaceHolder =  dialogView.findViewById(R.id.textHiddenPlaceHolder);
        TextView textReadablePlaceHolder =  dialogView.findViewById(R.id
                .textReadablePlaceHolder);
        TextView textWriteablePlaceHolder =  dialogView.findViewById(R.id
                .textWriteablePlaceHolder);
        TextView textMD5 =  dialogView.findViewById(R.id.textMD5);
        TextView textMD5Placeholder =  dialogView.findViewById(R.id.textMD5PlaceHolder);

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

        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);

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
        View dialogView = inflater.inflate(R.layout.dialog_rename, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        final AlertDialog alertDialog = builder.create();

        TextView titleText = dialogView.findViewById(R.id.textTitle);
        TextView msg = dialogView.findViewById(R.id.textMessage);
        final EditText inputText = dialogView.findViewById(R.id.editFileName);

        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        titleText.setText(title);
        inputText.setText(zipName);
        positiveButton.setText(context.getString(R.string.create));
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
        String texts[] = new String[]{context.getString(R.string.extract),  context.getString(R.string.dialog_cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_extract, null);
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();

        final RadioButton radioButtonSpecify =  dialogView.findViewById(R.id
                .radioButtonSpecifyPath);
        final Button buttonPathSelect =  dialogView.findViewById(R.id.buttonPathSelect);
        RadioGroup radioGroupPath =  dialogView.findViewById(R.id.radioGroupPath);
        final EditText editFileName =  dialogView.findViewById(R.id.editFileName);
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

        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        positiveButton.setText(texts[0]);
        negativeButton.setText(texts[1]);

        positiveButton.setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = editFileName.getText().toString();
                extractDialogListener.onPositiveButtonClick(alertDialog, currentFilePath, fileName,
                        radioButtonSpecify.isChecked());
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
            destinationDir, final DragDialogListener dragDialogListener) {


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_drag, null);
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();

        final RadioButton radioButtonCopy =  dialogView.findViewById(R.id
                .radioCopy);

        final TextView textMessage = dialogView.findViewById(R.id.textMessage);

        textMessage.setText(String.format("%s %s", context.getString(R.string.dialog_to_placeholder), destinationDir));

        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        positiveButton.setText(context.getString(R.string.msg_ok));
        negativeButton.setText(context.getString(R.string.dialog_cancel));

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

    public static void openWith(final Uri uri, final Context context) {

        //Initializing a bottom sheet
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.dialog_open_as, null);
        bottomSheetDialog.setContentView(sheetView);

        ArrayList<String> items = new ArrayList<>();
        items.add(context.getString(R.string.text));
        items.add(context.getString(R.string.image));
        items.add(context.getString(R.string.audio));
        items.add(context.getString(R.string.other));


        TextView textTitle = sheetView.findViewById(R.id.textTitle);
        ListView listView = sheetView.findViewById(R.id.listOpenAs);

        textTitle.setText(context.getString(R.string.open_as));

        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, items) {

                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        TextView textView = (TextView) super.getView(position, convertView, parent);
                        textView.setTextColor(Color.BLACK);
                        return textView;
                    }
                };

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
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.show();

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


        void onPositiveButtonClick(Dialog dialog, Operations operation, List<FileInfo> destFiles, List<FileInfo> conflictFiles,
                                   String destinationDir, boolean isMove, boolean isChecked);

        void onNegativeButtonClick(Dialog dialog, Operations operation, List<FileInfo> destFiles, List<FileInfo> conflictFiles,
                                   String destinationDir, boolean isMove, boolean isChecked);

        void onNeutralButtonClick(Dialog dialog, Operations operation, List<FileInfo> destFiles, List<FileInfo> conflictFiles,
                                  String destinationDir, boolean isMove, boolean isChecked);
    }

    public interface DragDialogListener {

        void onPositiveButtonClick(ArrayList<FileInfo> filesToPaste, String destinationDir, boolean isMove);

    }


}

