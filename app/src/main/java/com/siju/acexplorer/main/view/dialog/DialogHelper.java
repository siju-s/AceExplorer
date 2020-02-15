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

package com.siju.acexplorer.main.view.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.siju.acexplorer.R;
import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.main.model.helper.FileUtils;
import com.siju.acexplorer.main.model.helper.UriHelper;
import com.siju.acexplorer.main.view.PasteConflictAdapter;
import com.siju.acexplorer.storage.model.SortMode;
import com.siju.acexplorer.storage.model.operations.Operations;
import com.siju.acexplorer.storage.model.operations.PasteConflictCheckData;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Siju on 29 August,2017
 */
@SuppressWarnings("ConstantConditions")
@SuppressLint("InflateParams")
public class DialogHelper {

    public static void showDeleteDialog(final Context context, final ArrayList<FileInfo> files,
                                        final boolean trashEnabled, final DeleteDialogListener deleteDialogListener) {
        String title = context.getString(R.string.dialog_delete_title, files.size());
        String[] texts = new String[]{title, context.getString(R.string.msg_ok), "", context
                .getString(R.string
                .dialog_cancel)};


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_delete, null);
        builder.setView(dialogView);
        builder.setCancelable(true);

        final AlertDialog alertDialog = builder.create();

        TextView textTitle = dialogView.findViewById(R.id.textTitle);
        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        final CheckBox checkBoxTrash = dialogView.findViewById(R.id.checkBoxTrash);
        checkBoxTrash.setChecked(trashEnabled);

        textTitle.setText(title);
        positiveButton.setText(texts[1]);
        negativeButton.setText(texts[3]);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isChecked = false;
                deleteDialogListener.onPositiveButtonClick(view, isChecked, files);
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

    public static void showAlertDialog(final Context context, String[] text,
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
        alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup
                .LayoutParams.WRAP_CONTENT);
    }

    public static void showSortDialog(final Context context, final SortMode sortMode, final
    SortDialogListener sortDialogListener) {
        String title = context.getString(R.string.action_sort);
        String[] texts = new String[]{title, context.getString(R.string.msg_ok), "", context
                .getString(R.string
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
        final CheckBox ascendingOrderCheckbox = dialogView.findViewById(R.id.checkBoxOrder);
        ascendingOrderCheckbox.setChecked(SortMode.Companion.isAscending(sortMode));

        textTitle.setText(title);
        positiveButton.setText(texts[1]);
        negativeButton.setText(texts[3]);
        View radioButton = radioGroup.getChildAt(indexToCheck(sortMode));
        radioGroup.check(radioButton.getId());

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                View radioButton = radioGroup.findViewById(radioButtonID);
                int index = radioGroup.indexOfChild(radioButton);
                SortMode sortModeNew = SortMode.Companion.getSortModeFromValue(newSortMode(index, ascendingOrderCheckbox.isChecked()));
                sortDialogListener.onPositiveButtonClick(sortModeNew);
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

    private static int indexToCheck(SortMode sortMode) {
        return sortMode.getValue() / 2;
    }

    private static int newSortMode(int selectedOption, boolean isAscending) {
        int newSortMode = selectedOption * 2;
        if (!isAscending) {
            newSortMode += 1;
        }
        return newSortMode;
    }


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

        String fileName = FileUtils.getFileNameWithoutExt(textEdit);
        if (fileName != null) {
            inputText.setText(fileName);
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

    public static void showApkDialog(final Context context, String[] text, final String path,
                                     final ApkDialogListener dialogListener) {

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
                dialogListener.onInstallClicked(path);
                alertDialog.dismiss();
            }
        });

        if (text.length > 3) {
            negativeButton.setVisibility(View.VISIBLE);
            negativeButton.setText(text[3]);
            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogListener.onCancelClicked();
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
                    dialogListener.onOpenApkClicked(path);
                    alertDialog.dismiss();
                }
            });
        }

        alertDialog.show();
        alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup
                .LayoutParams.WRAP_CONTENT);
    }


    public static void showSAFDialog(Context context, String path, final AlertDialogListener dialogListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_saf, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        String dialogTitle = context.getString(R.string.needsaccess);
        String[] text = new String[]{dialogTitle, context.getString(R.string
                .needs_access_summary, path),
                context.getString(R.string.open), context.getString(R.string
                .dialog_cancel)};

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

        negativeButton.setVisibility(View.VISIBLE);
        negativeButton.setText(text[3]);
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogListener.onNegativeButtonClick(v);
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }

    public static void showConflictDialog(Context context, final PasteConflictCheckData pasteConflictCheckData,
                                          final PasteConflictListener pasteConflictListener) {
        String[] texts = new String[]{context.getString(R.string.msg_file_exists),
                context.getString(R.string.dialog_skip), context.getString(R.string.dialog_keep_both),
                context.getString(R.string.dialog_replace)};


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_paste_conflict, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        final AlertDialog dialog = builder.create();

        final ListView listView = dialogView.findViewById(R.id.listFiles);
        View footerView = inflater.inflate(R.layout.paste_conflict_footer, null, false);
        listView.addFooterView(footerView);

        List<FileInfo> fileInfoList = new ArrayList<>();

        final List<FileInfo> conflictFiles = pasteConflictCheckData.getConflictFiles();
        final List<FileInfo> destFiles = pasteConflictCheckData.getDestFiles();

        final String destinationDir = pasteConflictCheckData.getDestinationDir();
        final Operations operation = pasteConflictCheckData.getOperations();
        FileInfo fileToPaste = conflictFiles.get(0);

        String sourcePath = fileToPaste.getFilePath();
        File sourceFile = new File(sourcePath);

        fileInfoList.add(fileToPaste);
        fileInfoList.add(destFiles.get(0));

        PasteConflictAdapter pasteConflictAdapter = new PasteConflictAdapter(context, fileInfoList);
        listView.setAdapter(pasteConflictAdapter);

        final CheckBox checkBox = footerView.findViewById(R.id.checkBox);
        if (conflictFiles.size() == 1) {
            checkBox.setVisibility(View.GONE);
        }
        Button positiveButton = footerView.findViewById(R.id.buttonPositive);
        Button negativeButton = footerView.findViewById(R.id.buttonNegative);
        Button neutralButton = footerView.findViewById(R.id.buttonKeepBoth);

        positiveButton.setText(texts[1]);
        negativeButton.setText(texts[3]);

        // POSITIVE BUTTON ->SKIP   NEGATIVE ->REPLACE    NEUTRAL ->KEEP BOTH
        if (sourceFile.getParent().equals(destinationDir)) {
            if (operation == Operations.CUT) {
                neutralButton.setVisibility(View.GONE);
                negativeButton.setVisibility(View.GONE);
            } else {
                negativeButton.setVisibility(View.GONE);
            }
        }

        if (new File(fileToPaste.getFilePath()).isDirectory()) {
            neutralButton.setVisibility(View.GONE);
        }


        final boolean checked = checkBox.isChecked();
        positiveButton.setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                pasteConflictListener.onSkipClicked(pasteConflictCheckData, checked);
            }
        });

        negativeButton.setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                pasteConflictListener.onReplaceClicked(pasteConflictCheckData, checked);
            }
        });

        neutralButton.setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                pasteConflictListener.onKeepBothClicked(pasteConflictCheckData, checked);
            }
        });


        dialog.show();
    }


    public static void showPermissionsDialog(Context context, final String path, final boolean
            isDir, final ArrayList<Boolean[]> permissions,
                                             final PermissionDialogListener
                                                     permissionDialogListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_permission, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        final AlertDialog alertDialog = builder.create();

        final CheckBox readown = dialogView.findViewById(R.id.readOwnerState);
        final CheckBox readgroup = dialogView.findViewById(R.id.readGroupState);
        final CheckBox readother = dialogView.findViewById(R.id.readOtherState);
        final CheckBox writeown = dialogView.findViewById(R.id.writeOwnerState);
        final CheckBox writegroup = dialogView.findViewById(R.id.writeGroupState);
        final CheckBox writeother = dialogView.findViewById(R.id.writeOtherState);
        final CheckBox exeown = dialogView.findViewById(R.id.execOwnerState);
        final CheckBox exegroup = dialogView.findViewById(R.id.execGroupState);
        final CheckBox exeother = dialogView.findViewById(R.id.execOtherState);

        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        positiveButton.setText(context.getString(R.string.msg_ok));
        negativeButton.setText(context.getString(R.string.dialog_cancel));

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
                if (!RootTools.isAccessGiven()) {
                    return;
                }
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

    public static void showCompressDialog(Context context, final ArrayList<FileInfo> paths,
                                          final CompressDialogListener dialogListener) {

        String fileName = paths.get(0).getFileName();
        String filePath = paths.get(0).getFilePath();
        String zipName = fileName;
        if (!(new File(filePath).isDirectory())) {
            zipName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        String title = context.getString(R.string.action_archive);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_rename, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        final AlertDialog alertDialog = builder.create();

        TextView titleText = dialogView.findViewById(R.id.textTitle);
        TextView msgText = dialogView.findViewById(R.id.textMessage);
        final EditText inputText = dialogView.findViewById(R.id.editFileName);

        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        msgText.setVisibility(View.GONE);
        titleText.setText(title);
        inputText.setText(zipName);
        positiveButton.setText(context.getString(R.string.create));
        negativeButton.setText(context.getString(R.string.dialog_cancel));

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = inputText.getText().toString().trim();
                dialogListener.onPositiveButtonClick(alertDialog, Operations.COMPRESS, name,
                        paths);

            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogListener.onNegativeButtonClick(Operations.COMPRESS);
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    public static void showExtractDialog(Context context, final String zipFilePath,
                                         final String destinationDir, final ExtractDialogListener extractDialogListener) {

        final String currentFileName = zipFilePath.substring(zipFilePath.lastIndexOf("/")
                + 1, zipFilePath
                .lastIndexOf("."));
        String[] texts = new String[]{context.getString(R.string.extract), context.getString(R.string.dialog_cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_extract, null);
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();

        final RadioButton extractToPathButton = dialogView.findViewById(R.id
                .radioButtonSpecifyPath);
        final Button selectPathButton = dialogView.findViewById(R.id.buttonPathSelect);
        RadioGroup radioGroupPath = dialogView.findViewById(R.id.radioGroupPath);
        final EditText editFileName = dialogView.findViewById(R.id.editFileName);
        editFileName.setText(currentFileName);
        radioGroupPath.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.radioButtonCurrentPath) {
                    selectPathButton.setVisibility(View.GONE);
                } else {
                    selectPathButton.setVisibility(View.VISIBLE);
                }
            }
        });

        selectPathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                extractDialogListener.onSelectButtonClicked(alertDialog);
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
                String newDir = destinationDir;
                if (extractToPathButton.isChecked()) {
                    newDir = selectPathButton.getText().toString();
                }
                extractDialogListener.onPositiveButtonClick(Operations.EXTRACT, zipFilePath, fileName,
                        newDir);
                alertDialog.dismiss();
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


    public static void showDragDialog(Context context, final ArrayList<FileInfo> filesToPaste,
                                      final String
                                              destinationDir, final DragDialogListener
                                              dragDialogListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_drag, null);
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();

        final RadioButton radioButtonCopy = dialogView.findViewById(R.id
                .radioCopy);

        final TextView textMessage = dialogView.findViewById(R.id.textMessage);

        textMessage.setText(String.format("%s %s", context.getString(R.string.dialog_to_placeholder), destinationDir));

        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        positiveButton.setText(context.getString(R.string.msg_ok));
        negativeButton.setText(context.getString(R.string.dialog_cancel));

        final Operations operation;
        if (radioButtonCopy.isChecked()) {
            operation = Operations.COPY;
        }
        else {
            operation = Operations.CUT;
        }
        positiveButton.setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                dragDialogListener.onPositiveButtonClick(filesToPaste, destinationDir,
                        operation);
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
                    public View getView(int position, @Nullable View convertView, @NonNull
                            ViewGroup parent) {
                        return super.getView(position, convertView, parent);
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
                boolean canGrant = UriHelper.INSTANCE.canGrantUriPermission(context, intent);
                if (canGrant) {
                    context.startActivity(intent);
                }
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.show();
    }


    public interface ExtractDialogListener {

        void onPositiveButtonClick(Operations operation, String sourceFilePath, String newFileName,
                                   String destinationDir);

        void onSelectButtonClicked(Dialog dialog);


    }

    public interface PermissionDialogListener {

        void onPositiveButtonClick(String path, boolean isDir, String permissions);


    }


    public interface CompressDialogListener {

        void onPositiveButtonClick(Dialog dialog, Operations operation, String newFileName,
                                  ArrayList<FileInfo> paths);

        void onNegativeButtonClick(Operations operation);

    }

    public interface AlertDialogListener {

        void onPositiveButtonClick(View view);

        void onNegativeButtonClick(View view);

        void onNeutralButtonClick(View view);
    }

    public interface ApkDialogListener {

        void onInstallClicked(String path);

        void onCancelClicked();

        void onOpenApkClicked(String path);

    }

    public interface SortDialogListener {

        void onPositiveButtonClick(SortMode sortMode);

    }


    public interface DeleteDialogListener {

        void onPositiveButtonClick(View view, boolean isTrashEnabled, ArrayList<FileInfo> filesToDelete);

    }


    public interface DialogCallback {

        void onPositiveButtonClick(Dialog dialog, Operations operation, String name);

        void onNegativeButtonClick(Operations operations);
    }


    public interface PasteConflictListener {

        void onSkipClicked(PasteConflictCheckData pasteConflictCheckData, boolean isChecked);

        void onReplaceClicked(PasteConflictCheckData pasteConflictCheckData, boolean isChecked);

        void onKeepBothClicked(PasteConflictCheckData pasteConflictCheckData, boolean isChecked);
    }

    public interface DragDialogListener {

        void onPositiveButtonClick(ArrayList<FileInfo> filesToPaste, String destinationDir,
                                   Operations operation);

    }
}

