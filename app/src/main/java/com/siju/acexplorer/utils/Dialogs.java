package com.siju.acexplorer.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.siju.acexplorer.R;
import com.siju.acexplorer.filesystem.BaseFileList;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;

import static android.webkit.MimeTypeMap.getSingleton;
import static com.siju.acexplorer.filesystem.helper.UriHelper.createContentUri;
import static com.siju.acexplorer.filesystem.helper.UriHelper.grantUriPermission;

public class Dialogs {


    @SuppressWarnings("ConstantConditions")
    public void createFileDialog(final BaseFileList baseFileList, final boolean isRootMode, final String path) {

        String title = baseFileList.getString(R.string.new_file);
        String texts[] = new String[]{baseFileList.getString(R.string.enter_name), "", title, baseFileList.getString(R.string
                .create),
                "",
                baseFileList.getString(R.string.dialog_cancel)};
        final MaterialDialog materialDialog = showEditDialog(baseFileList.getContext(), texts);

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = materialDialog.getInputEditText().getText().toString();
                if (FileUtils.isFileNameInvalid(fileName)) {
                    materialDialog.getInputEditText().setError(baseFileList.getResources().getString(R.string
                            .msg_error_valid_name));
                    return;
                }

                fileName = fileName.trim() + ".txt";
                String filePath = path + File.separator + fileName;
                if (baseFileList.exists(filePath)) {
                    materialDialog.getInputEditText().setError(baseFileList.getResources().getString(R.string
                            .file_exists));
                    return;
                }
                baseFileList.getFileOpHelper().mkFile(new File(filePath), isRootMode);
                materialDialog.dismiss();
            }
        });

        materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });

        materialDialog.show();
    }

    @SuppressWarnings("ConstantConditions")
    public void createDirDialog(final BaseFileList baseFileList, final boolean isRooted, final String path) {

        String title = baseFileList.getString(R.string.new_folder);
        String texts[] = new String[]{baseFileList.getString(R.string.enter_name), "", title, baseFileList.getString(R.string
                .create), "", baseFileList.getString(R.string.dialog_cancel)};
        final MaterialDialog materialDialog = showEditDialog(baseFileList.getContext(), texts);

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = materialDialog.getInputEditText().getText().toString();
                if (FileUtils.isFileNameInvalid(fileName)) {
                    materialDialog.getInputEditText().setError(baseFileList.getResources().getString(R.string
                            .msg_error_valid_name));
                    return;
                }

                fileName = fileName.trim();
                String newPath = path + File.separator + fileName;
                if (baseFileList.exists(newPath)) {
                    materialDialog.getInputEditText().setError(baseFileList.getResources().getString(R.string
                            .file_exists));
                    return;
                }
                baseFileList.getFileOpHelper().mkDir(new File(newPath), isRooted);
                materialDialog.dismiss();
            }
        });

        materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });

        materialDialog.show();
    }


    /**
     * @param fileInfo Paths to delete
     */
    public void showDeleteDialog(final BaseFileList baseFileList, final ArrayList<FileInfo> fileInfo, final boolean
            isRooted) {
        String title = baseFileList.getString(R.string.dialog_delete_title);
        String texts[] = new String[]{title, baseFileList.getString(R.string.msg_ok), "", baseFileList.getString(R.string
                .dialog_cancel)};

        ArrayList<String> items = new ArrayList<>();
        for (int i = 0; i < fileInfo.size(); i++) {
            String path = fileInfo.get(i).getFilePath();
            items.add(path);
            if (i == 9 && fileInfo.size() > 10) {
                int rem = fileInfo.size() - 10;
                items.add("+" + rem + " " + baseFileList.getString(R.string.more));
                break;
            }
        }
        final MaterialDialog materialDialog = showListDialog(baseFileList.getContext(), texts, items);


        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                baseFileList.getFileOpHelper().deleteFiles(fileInfo, isRooted);
                materialDialog.dismiss();
            }
        });

        materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });

        materialDialog.show();
    }

    public static void showApkOptionsDialog(final Fragment fragment, final String path, final String extension) {
        final Context context = fragment.getContext();

        String texts[] = new String[]{context.getString(R.string.package_installer), context.getString(R.string
                .install), context.getString(R.string.dialog_cancel), context.getString(R.string.view),
                context.getString(R.string.package_installer_content)};

        final MaterialDialog materialDialog = new Dialogs().showDialog(context, texts);


        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = createContentUri(fragment.getContext(), path);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_INSTALL_PACKAGE);

                String mimeType = getSingleton().getMimeTypeFromExtension(extension);
                intent.setData(uri);

                if (mimeType != null) {
                    grantUriPermission(context, intent, uri);
                } else {
                    openWith(uri, context);
                }
                materialDialog.dismiss();

            }
        });

        materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             /*   if (fragment instanceof FileListDualFragment)
                    ((FileListDualFragment) fragment).openZipViewer(path);
                else*/
                ((BaseFileList) fragment).openZipViewer(path);

                materialDialog.dismiss();
            }
        });

        materialDialog.show();

    }

    public static void openWith(final Uri uri, final Context context) {

        String texts[] = new String[]{context.getString(R.string.open_as), null, null, null};
        ArrayList<String> items = new ArrayList<>();
        items.add(context.getString(R.string.text));
        items.add(context.getString(R.string.image));
        items.add(context.getString(R.string.audio));
        items.add(context.getString(R.string.other));
        items.add(context.getString(R.string.text));

        final MaterialDialog materialDialog = new Dialogs().showListDialog(context, texts, items);

        materialDialog.getBuilder().itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
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
                materialDialog.dismiss();
            }
        });
        materialDialog.show();
    }

    @SuppressWarnings("ConstantConditions")
    public void showCompressDialog(final BaseFileList baseFileList, final String currentDir, final ArrayList<FileInfo>
            paths) {

        final String ext = ".zip";
        String fileName = paths.get(0).getFileName();
        String filePath = paths.get(0).getFilePath();
        String zipName = fileName;
        if (!(new File(filePath).isDirectory())) {
            zipName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        String title = baseFileList.getString(R.string.create);
        String texts[] = new String[]{"", zipName, title, title, "",
                baseFileList.getString(R.string.dialog_cancel)};
        final MaterialDialog materialDialog = showEditDialog(baseFileList.getContext(), texts);

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = materialDialog.getInputEditText().getText().toString();
                if (FileUtils.isFileNameInvalid(fileName)) {
                    materialDialog.getInputEditText().setError(baseFileList.getResources().getString(R.string
                            .msg_error_valid_name));
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
        });

        materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });

        materialDialog.show();
    }

    public MaterialDialog showEditDialog(final Context context, String[] texts) {
        int color = getCurrentThemePrimary(context);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.input(texts[0], texts[1], false, new
                MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog materialDialog, CharSequence charSequence) {

                    }
                });
        builder.widgetColor(ContextCompat.getColor(context, R.color.colorAccent));
  /*      if(m.theme1==1)
            a.theme(Theme.DARK);*/
        builder.title(texts[2]);
        builder.positiveText(texts[3]);
        builder.positiveColor(color);
        builder.neutralText(texts[4]);
        if (texts[5] != (null)) {
            builder.negativeText(texts[5]);
            builder.negativeColor(color);
        }
        return builder.build();
    }


    public MaterialDialog showCustomDialog(final Context context, int resourceId, String[] texts) {
        int color = getCurrentThemePrimary(context);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);

        builder.customView(resourceId, true);
        builder.title(texts[0]);
        builder.positiveText(texts[1]);
        builder.positiveColor(color);
        builder.neutralText(texts[2]);
        builder.neutralColor(color);
        if (texts[3] != (null)) {
            builder.negativeText(texts[3]);
            builder.negativeColor(color);
        }
        return builder.build();
    }

    public int getCurrentThemePrimary(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.titleTextColor, typedValue, true);
        return typedValue.data;
    }

    private MaterialDialog showListDialog(final Context context, String[] texts, ArrayList<String> items) {
        int color = getCurrentThemePrimary(context);
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(texts[0]);
        builder.positiveText(texts[1]);
        builder.positiveColor(color);
        builder.neutralText(texts[2]);
        builder.items(items);
        if (texts[3] != (null)) {
            builder.negativeText(texts[3]);
            builder.negativeColor(color);
        }
        return builder.build();
    }

    public MaterialDialog showDialog(final Context context, String[] texts) {
        int color = getCurrentThemePrimary(context);
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(texts[0]);
        builder.positiveText(texts[1]);
        builder.positiveColor(color);
        builder.neutralText(texts[2]);
        builder.neutralColor(color);
        if (texts[3] != (null)) {
            builder.negativeText(texts[3]);
            builder.negativeColor(color);
        }
        builder.content(texts[4]);

        return builder.build();
    }


}
