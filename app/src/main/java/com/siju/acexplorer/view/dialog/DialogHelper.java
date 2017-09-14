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
import android.support.v7.app.AlertDialog;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.siju.acexplorer.R;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.storage.model.CopyData;
import com.siju.acexplorer.storage.model.operations.Operations;

import java.io.File;
import java.util.List;

import static com.siju.acexplorer.model.helper.AppUtils.getAppIcon;

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


    *//**
     * @param context
     * @param text           0->title, 1->msg, 2->Positive button 3->Negative button
     * @param dialogListener
     */
    public static void showInputDialog(final Context context, String[] text,
                                       final Operations operation,
                                       final DialogCallback dialogListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.rename_dialog, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        final AlertDialog alertDialog = builder.create();

        TextView title =  dialogView.findViewById(R.id.textTitle);
        TextView msg =  dialogView.findViewById(R.id.textMessage);
        final EditText inputText =  dialogView.findViewById(R.id.editTextName);

        Button positiveButton =  dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton =  dialogView.findViewById(R.id.buttonNegative);

        title.setText(text[0]);
        msg.setText(text[1]);
        positiveButton.setText(text[2]);
        negativeButton.setText(text[3]);


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
            }
            else {
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

    public void showPasteProgress(String destinationDir, List<FileInfo> files,
                                         List<CopyData> copyData, boolean isMove) {
    }


    public interface DialogListener {

        void onPositiveButtonClick(View view, String name);

        void onNegativeButtonClick(View view);

        void onErrorInvalidName();

        void onError(String error);

    }

    public interface AlertDialogListener {

        void onPositiveButtonClick(View view);

        void onNegativeButtonClick(View view);
    }


    public interface DialogCallback {
        int INVALID_ERROR = 1;
        int DUPLICATE_ERROR = 2;
        void onError(int error);

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


}

