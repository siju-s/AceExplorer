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

package com.siju.acexplorer.dialog;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.siju.acexplorer.R;

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
     * @param newFilePath
     * @param position
     * @param dialogListener
     *//*
    public static void showInputDialog(final Context context, String[] text,
                                       final String filePath, String newFilePath, final int
                                       position,
                                       final DialogListener dialogListener) {

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

        title.setText(text[0]);
        msg.setText(text[1]);
        positiveButton.setText(text[2]);
        negativeButton.setText(text[3]);

        final String extension = FileHelper.getExtension(newFilePath);
        Log.d(TAG, "showInputDialog: "+newFilePath);
        inputText.setText(FileHelper.getNameWithoutExt(newFilePath));

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = inputText.getText().toString().trim();
                String errorMsg = Utils.checkFileNameValidations(context, name);

                if (errorMsg != null) {
                    dialogListener.onError(errorMsg);
                } else {
                    if (FileHelper.isExists(filePath, name + extension)) {
                        dialogListener.onError(context.getString(R.string
                        .txt_filename_duplicate_message));
                        return;
                    }
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
    }*/


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


}

