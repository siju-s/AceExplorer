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

package com.siju.acexplorer.storage.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.siju.acexplorer.R;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.FileConstants;
import com.siju.acexplorer.model.helper.helper.ShareHelper;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.storage.model.ViewMode;
import com.siju.acexplorer.storage.model.operations.Operations;
import com.siju.acexplorer.model.root.RootUtils;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.model.helper.RootHelper;
import com.siju.acexplorer.model.helper.root.RootTools;
import com.siju.acexplorer.model.helper.root.rootshell.execution.Command;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.theme.Theme;
import com.siju.acexplorer.utils.Dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.R.string.hide;
import static com.siju.acexplorer.model.helper.helper.AppUtils.getAppIcon;
import static com.siju.acexplorer.model.helper.helper.AppUtils.getAppIconForFolder;
import static com.siju.acexplorer.model.helper.helper.MediaStoreHelper.removeMedia;
import static com.siju.acexplorer.model.helper.helper.MediaStoreHelper.scanFile;
import static com.siju.acexplorer.model.helper.helper.PermissionsHelper.parse;
import static com.siju.acexplorer.model.helper.helper.UriHelper.getUriForCategory;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH2;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_POSITION;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_RESULT;
import static com.siju.acexplorer.model.groups.Category.AUDIO;
import static com.siju.acexplorer.model.groups.Category.FAVORITES;
import static com.siju.acexplorer.model.groups.Category.FILES;
import static com.siju.acexplorer.model.groups.Category.IMAGE;
import static com.siju.acexplorer.model.groups.Category.VIDEO;

/**
 * Created by Siju on 04 September,2017
 */
public class MenuControls implements View.OnClickListener,
        Toolbar.OnMenuItemClickListener,
        PopupMenu.OnMenuItemClickListener{

    private final String TAG = this.getClass().getSimpleName();

    private StoragesUiView storagesUiView;
    private Context context;
    private Activity activity;
    private Theme theme;
    private Toolbar toolbar;
    private ImageButton imgNavigationIcon;
    private TextView toolbarTitle;
    private ImageButton imgOverflow;
    private Toolbar bottomToolbar;
    private SearchHelper searchHelper;
    private MenuItem mPasteItem;
    private MenuItem cancelItem;
    private MenuItem createItem;
    private MenuItem mRenameItem;
    private MenuItem mInfoItem;
    private MenuItem mArchiveItem;
    private MenuItem mFavItem;
    private MenuItem mExtractItem;
    private MenuItem mHideItem;
    private MenuItem mPermissionItem;
    private boolean mIsMoveOperation = false;
    private MenuItem mViewItem;
    private Category category;
    private String currentDir;


    MenuControls(Activity activity, StoragesUiView storagesUiView, Theme theme) {
        this.activity = activity;
        this.storagesUiView = storagesUiView;
        this.context = storagesUiView.getContext();
        init();
        setTheme(theme);
    }


    void setCategory(Category category) {
        this.category = category;
    }

    void setCurrentDir(String currentDir) {
        this.currentDir = currentDir;
    }

    private void init() {
        bottomToolbar =  storagesUiView.findViewById(R.id.toolbar_bottom);
        toolbar =  storagesUiView.findViewById(R.id.toolbar);
        setToolbar();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                aceActivity.openDrawer();
            }
        });
        searchHelper = new SearchHelper(this, context);

    }

    private void setToolbar() {
        View actionBar = LayoutInflater.from(context).inflate(R.layout.actionbar_custom, null);
        toolbar.addView(actionBar);
        toolbar.setTitle(R.string.app_name);

        imgNavigationIcon =  actionBar.findViewById(R.id.imgNavigationIcon);
        toolbarTitle =  actionBar.findViewById(R.id.toolbarTitle);
        imgNavigationIcon.setOnClickListener(this);
        imgOverflow =  actionBar.findViewById(R.id.imgButtonOverflow);
        imgOverflow.setOnClickListener(this);
    }

    private void setTheme(Theme theme) {
        this.theme = theme;
        switch (theme) {
            case DARK:
                bottomToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color
                        .colorPrimary));
                toolbar.setPopupTheme(R.style.Dark_AppTheme_PopupOverlay);
                bottomToolbar.setPopupTheme(R.style.Dark_AppTheme_PopupOverlay);
                break;
            case LIGHT:
                toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay);
                bottomToolbar.setPopupTheme(R.style.AppTheme_PopupOverlay);
                toolbarTitle.setTextColor(ContextCompat.getColor(context, R.color.white));
                searchHelper.setSearchHintColor();
                break;
        }
    }


    void setTitleForCategory(Category category) {
        switch (category) {
            case FILES:
                toolbar.setTitle(context.getString(R.string.app_name));
                break;
            case AUDIO:
                toolbar.setTitle(context.getString(R.string.nav_menu_music));
                break;
            case VIDEO:
                toolbar.setTitle(context.getString(R.string.nav_menu_video));
                break;
            case IMAGE:
                toolbar.setTitle(context.getString(R.string.nav_menu_image));
                break;
            case DOCS:
                toolbar.setTitle(context.getString(R.string.nav_menu_docs));
                break;
            default:
                toolbar.setTitle(context.getString(R.string.app_name));
        }
    }

    void onOperationResult(Intent intent, Operations operation) {

        switch (operation) {
            case DELETE:

                ArrayList<FileInfo> deletedFilesList = intent.getParcelableArrayListExtra
                        (KEY_FILES);

                for (FileInfo info : deletedFilesList) {
                    scanFile(getActivity().getApplicationContext(), info.getFilePath());
                }

                Uri uri = getUriForCategory(category);
                getContext().getContentResolver().notifyChange(uri, null);
                for (int i = 0; i < deletedFilesList.size(); i++) {
                    fileInfoList.remove(deletedFilesList.get(i));
                }
                fileListAdapter.setStopAnimation(true);
                fileListAdapter.updateAdapter(fileInfoList);

                break;

            case RENAME:

                final int position = intent.getIntExtra(KEY_POSITION, -1);
                String oldFile = intent.getStringExtra(KEY_FILEPATH);
                String newFile = intent.getStringExtra(KEY_FILEPATH2);
                int type = fileInfoList.get(position).getType();
                removeMedia(getActivity(), new File(oldFile), type);
                scanFile(getActivity().getApplicationContext(), newFile);
                fileInfoList.get(position).setFilePath(newFile);
                fileInfoList.get(position).setFileName(new File(newFile).getName());
                fileListAdapter.setStopAnimation(true);
                Logger.log(TAG, "Position changed=" + position);
                scanFile(getActivity().getApplicationContext(), newFile);
                fileListAdapter.notifyItemChanged(position);
                break;

            case CUT:
            case COPY:
                ArrayList<String> copiedFiles = intent.getStringArrayListExtra(KEY_FILES);

                if (copiedFiles != null) {
                    for (String path : copiedFiles) {
                        scanFile(getActivity().getApplicationContext(), path);
                    }
                }
            case FOLDER_CREATION:
            case FILE_CREATION:
                boolean isSuccess = intent.getBooleanExtra(KEY_RESULT, true);

                if (!isSuccess) {
                    Toast.makeText(getActivity(), getString(R.string.msg_operation_failed), Toast
                            .LENGTH_LONG).show();
                }
                else {
                    computeScroll();
                    refreshList();
                }
                break;

        }
    }





    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgNavigationIcon:
                if (searchHelper.isExpanded()) {
                    searchHelper.disableSearch();
                }
                else {
//                    aceActivity.openDrawer();
                }
                break;
            case R.id.imgButtonOverflow:
                showOptionsPopup(imgOverflow);
                break;
        }
    }

    private void showOptionsPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.file_base, popupMenu.getMenu());
        mViewItem = popupMenu.getMenu().findItem(R.id.action_view);
        updateMenuTitle();
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }



    private void setupMenu() {
        Menu menu = bottomToolbar.getMenu();
        mRenameItem = menu.findItem(R.id.action_edit);
        mInfoItem = menu.findItem(R.id.action_info);
        mArchiveItem = menu.findItem(R.id.action_archive);
        mFavItem = menu.findItem(R.id.action_fav);
        mExtractItem = menu.findItem(R.id.action_extract);
        mHideItem = menu.findItem(R.id.action_hide);
        mPermissionItem = menu.findItem(R.id.action_permissions);
        // Dont show Fav and Archive option for Non file mode
        if (!category.equals(FILES)) {
            mArchiveItem.setVisible(false);
            mFavItem.setVisible(false);
            mHideItem.setVisible(false);
        }

        switch (theme) {
            case LIGHT:
                mInfoItem.setIcon(R.drawable.ic_info_black);
                mArchiveItem.setIcon(R.drawable.ic_archive_black);
                mExtractItem.setIcon(R.drawable.ic_extract_black);
                mPermissionItem.setIcon(R.drawable.ic_permissions_black);
                mFavItem.setIcon(R.drawable.ic_favorite_black);
                break;
            case DARK:
                break;
        }

    }

    private void setupPasteMenu() {
        Menu menu = bottomToolbar.getMenu();
        mPasteItem = menu.findItem(R.id.action_paste);
        createItem = menu.findItem(R.id.action_create);
        cancelItem = menu.findItem(R.id.action_cancel);

    }

    void setupMenuVisibility(SparseBooleanArray selectedItemPos) {
        Log.d(TAG, "setupMenuVisibility: " + selectedItemPos.size());
        List<FileInfo> fileInfoList = storagesUiView.getFileList();
        if (selectedItemPos.size() > 1) {
            mRenameItem.setVisible(false);
            mInfoItem.setVisible(false);

        }
        else {
            mRenameItem.setVisible(true);
            mInfoItem.setVisible(true);
            if (selectedItemPos.size() == 1) {

                boolean isDirectory = fileInfoList.get(selectedItemPos.keyAt(0))
                        .isDirectory();
                String filePath = fileInfoList.get(selectedItemPos.keyAt(0))
                        .getFilePath();

                boolean isRoot = fileInfoList.get(selectedItemPos.keyAt(0)).isRootMode();
                if (FileUtils.isFileCompressed(filePath)) {
                    mExtractItem.setVisible(true);
                    mArchiveItem.setVisible(false);
                }
                if (isRoot) {
                    mPermissionItem.setVisible(true);
                }
                if (!isDirectory) {
                    mFavItem.setVisible(false);
                }

            }
            String fileName = fileInfoList.get(selectedItemPos.keyAt(0)).getFileName();

            if (fileName.startsWith(".")) {
                mHideItem.setTitle(context.getString(R.string.unhide));
                if (theme.equals(Theme.DARK)) {
                    mHideItem.setIcon(R.drawable.ic_unhide_white);
                }
                else {
                    mHideItem.setIcon(R.drawable.ic_unhide_black);

                }
            }
            else {
                mHideItem.setTitle(context.getString(hide));
                if (theme.equals(Theme.DARK)) {
                    mHideItem.setIcon(R.drawable.ic_hide_white);
                }
                else {
                    mHideItem.setIcon(R.drawable.ic_hide_black);
                }
            }
        }
    }


    void startActionMode() {

//        toggleDummyView(true);
        bottomToolbar.inflateMenu(R.menu.action_mode_bottom);
        bottomToolbar.getMenu().clear();
        EnhancedMenuInflater.inflate(activity.getMenuInflater(), bottomToolbar.getMenu(),
                category);
        setupMenu();
        bottomToolbar.startActionMode(storagesUiView.getActionModeCallback());
        bottomToolbar.setOnMenuItemClickListener(this);

    }

    private final ArrayList<FileInfo> copiedData = new ArrayList<>();
    private boolean isPasteVisible;

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        List<FileInfo> fileInfoList = storagesUiView.getFileList();
        SparseBooleanArray selectedItems = storagesUiView.getSelectedItems();
        boolean isRooted = storagesUiView.isRooted();

        switch (item.getItemId()) {
            case R.id.action_cut:
                if (selectedItems != null && selectedItems.size() > 0) {
                    FileUtils.showMessage(context, selectedItems.size() + " " +
                            context.getString(R.string.msg_cut_copy));
                    copiedData.clear();
                    for (int i = 0; i < selectedItems.size(); i++) {
                        copiedData.add(fileInfoList.get(selectedItems.keyAt(i)));
                    }
                    isPasteVisible = true;
                    mIsMoveOperation = true;
                    showPasteIcon();
                }
                break;
            case R.id.action_copy:

                if (selectedItems != null && selectedItems.size() > 0) {
                    mIsMoveOperation = false;
                    FileUtils.showMessage(context, selectedItems.size() + " " +
                            context.getString(R.string
                                    .msg_cut_copy));
                    copiedData.clear();
                    for (int i = 0; i < selectedItems.size(); i++) {
                        copiedData.add(fileInfoList.get(selectedItems.keyAt(i)));
                    }
                    isPasteVisible = true;
                    showPasteIcon();
                }
                break;

            case R.id.action_paste:
                isPasteVisible = false;
                if (copiedData.size() > 0) {
                    ArrayList<FileInfo> info = new ArrayList<>();
                    info.addAll(copiedData);
                    storagesUiView.onPasteAction(mIsMoveOperation, info);
                    copiedData.clear();
                    storagesUiView.endActionMode();
                }
                break;

            case R.id.action_delete:

                if (selectedItems != null && selectedItems.size() > 0) {
                    ArrayList<FileInfo> filesToDelete = new ArrayList<>();
                    for (int i = 0; i < selectedItems.size(); i++) {
                        FileInfo info = fileInfoList.get(selectedItems.keyAt(i));
                        filesToDelete.add(info);
                    }
                    if (category.equals(FAVORITES)) {
                        removeFavorite(filesToDelete);
                        Toast.makeText(context, context.getString(R.string.fav_removed), Toast
                                .LENGTH_SHORT).show();
                    }
                    else {
                        dialogs.showDeleteDialog(this, filesToDelete, isRooted);
                    }
                    storagesUiView.finishActionMode();
                }
                break;
            case R.id.action_share:
                if (selectedItems != null && selectedItems.size() > 0) {
                    ArrayList<FileInfo> filesToShare = new ArrayList<>();
                    for (int i = 0; i < selectedItems.size(); i++) {
                        FileInfo info = fileInfoList.get(selectedItems.keyAt(i));
                        if (!info.isDirectory()) {
                            filesToShare.add(info);
                        }
                    }
                    ShareHelper.shareFiles(context, filesToShare, category);
                    storagesUiView.finishActionMode();
                }
                break;

            case R.id.action_edit:

                if (selectedItems != null && selectedItems.size() > 0) {
                    final String oldFilePath = fileInfoList.get(selectedItems.keyAt(0)).
                            getFilePath();
                    int renamedPosition = selectedItems.keyAt(0);
                    String newFilePath = new File(oldFilePath).getParent();
                    renameDialog(oldFilePath, newFilePath, renamedPosition);
                }
                break;

            case R.id.action_info:

                if (selectedItems != null && selectedItems.size() > 0) {
                    FileInfo fileInfo = fileInfoList.get(selectedItems.keyAt(0));
                    showInfoDialog(fileInfo);
                    storagesUiView.finishActionMode();
                }
                break;
            case R.id.action_archive:

                if (selectedItems != null && selectedItems.size() > 0) {
                    ArrayList<FileInfo> paths = new ArrayList<>();
                    for (int i = 0; i < selectedItems.size(); i++) {
                        FileInfo info = fileInfoList.get(selectedItems.keyAt(i));
                        paths.add(info);
                    }
                    dialogs.showCompressDialog(BaseFileList.this, currentDir, paths);
                    storagesUiView.finishActionMode();
                }
                break;
            case R.id.action_fav:

                if (selectedItems != null && selectedItems.size() > 0) {
                    int count = 0;
                    ArrayList<FileInfo> favList = new ArrayList<>();
                    for (int i = 0; i < selectedItems.size(); i++) {
                        FileInfo info = fileInfoList.get(selectedItems.keyAt(i));
                        // Fav option meant only for directories
                        if (info.isDirectory()) {
                            favList.add(info);
                            count++;
                        }
                    }


                    if (count > 0) {
                        FileUtils.showMessage(context, context.getString(R.string.msg_added_to_fav));
                        updateFavouritesGroup(favList);
                    }
                    storagesUiView.finishActionMode();
                }
                break;

            case R.id.action_extract:

                if (selectedItems != null && selectedItems.size() > 0) {
                    FileInfo fileInfo = fileInfoList.get(selectedItems.keyAt(0));
                    String currentFile = fileInfo.getFilePath();
                    showExtractOptions(currentFile, currentDir);
                    storagesUiView.finishActionMode();
                }

                break;

            case R.id.action_hide:

                if (selectedItems != null && selectedItems.size() > 0) {
                    ArrayList<FileInfo> infoList = new ArrayList<>();
                    ArrayList<Integer> pos = new ArrayList<>();
                    for (int i = 0; i < selectedItems.size(); i++) {
                        infoList.add(fileInfoList.get(selectedItems.keyAt(i)));
                        pos.add(selectedItems.keyAt(i));

                    }
                    hideUnHideFiles(infoList, pos);
                    storagesUiView.finishActionMode();
                }
                break;

            case R.id.action_permissions:

                if (selectedItems != null && selectedItems.size() > 0) {
                    FileInfo info = fileInfoList.get(selectedItems.keyAt(0));
                    showPermissionsDialog(info);
                    storagesUiView.finishActionMode();
                }
                break;


            case R.id.action_view:
                if (viewMode == ViewMode.LIST) {
                    viewMode = ViewMode.GRID;
                }
                else {
                    viewMode = ViewMode.LIST;
                }
                sharedPreferenceWrapper.savePrefs(getActivity(), viewMode);
                storagesUiView.switchView();
                updateMenuTitle();
                break;

            case R.id.action_sort:
                showSortDialog();
                break;
        }
        return false;
    }



    boolean isPasteOp() {
        return isPasteVisible;
    }

    void removeSearchTask() {
        searchHelper.removeSearchTask();
    }

    void hideBottomToolbar() {
        bottomToolbar.setVisibility(View.GONE);
    }



    boolean isSearch() {
        return searchHelper.endSearch();
    }


    private void renameDialog(final String oldFilePath, final String newFilePath, final int
            position) {
        String fileName = oldFilePath.substring(oldFilePath.lastIndexOf("/") + 1, oldFilePath
                .length());
        boolean file = false;
        String extension = null;
        if (new File(oldFilePath).isFile()) {
            String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
            fileName = tokens[0];
            extension = tokens[1];
            file = true;
        }
        final boolean isFile = file;
        final String ext = extension;

        String title = getString(R.string.action_rename);
        String texts[] = new String[]{"", fileName, title, title, "",
                getString(R.string.dialog_cancel)};
        final MaterialDialog materialDialog = new Dialogs().showEditDialog(getActivity(), texts);

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = materialDialog.getInputEditText().getText().toString();
                if (FileUtils.isFileNameInvalid(fileName)) {
                    materialDialog.getInputEditText().setError(getResources().getString(R.string
                            .msg_error_valid_name));
                    return;
                }


                fileName = fileName.trim();
                String renamedName = fileName;
                if (isFile) {
                    renamedName = fileName + "." + ext;
                }

                File newFile = new File(newFilePath + "/" + renamedName);

                if (exists(newFile.getAbsolutePath())) {
                    materialDialog.getInputEditText().setError(getResources().getString(R.string
                            .dialog_title_paste_conflict));
                    return;
                }
                File oldFile = new File(oldFilePath);
                fileOpHelper.renameFile(oldFile, newFile, position, mIsRootMode);
                materialDialog.dismiss();
            }
        });

        materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });

        materialDialog.show();
        actionMode.finish();
    }

    public boolean exists(String filePath) {
        for (FileInfo fileInfo : fileInfoList) {
            if (fileInfo.getFilePath().equals(filePath)) {
                return true;
            }
        }
        return false;
    }


    private void hideUnHideFiles(ArrayList<FileInfo> fileInfo, ArrayList<Integer> pos) {
        for (int i = 0; i < fileInfo.size(); i++) {
            String fileName = fileInfo.get(i).getFileName();
            String renamedName;
            if (fileName.startsWith(".")) {
                renamedName = fileName.substring(1);
            }
            else {
                renamedName = "." + fileName;
            }
            String path = fileInfo.get(i).getFilePath();
            File oldFile = new File(path);
            String temp = path.substring(0, path.lastIndexOf(File.separator));

            File newFile = new File(temp + File.separator + renamedName);
            fileOpHelper.renameFile(oldFile, newFile, pos.get(i), mIsRootMode);
        }
    }


    private void showPermissionsDialog(final FileInfo fileInfo) {

        String texts[] = new String[]{getString(R.string.permissions), getString(R.string.msg_ok),
                "", getString(R.string.dialog_cancel)};
        final MaterialDialog materialDialog = new Dialogs().showCustomDialog(getActivity(),
                R.layout.dialog_permission, texts);
        final CheckBox readown = (CheckBox) materialDialog.findViewById(R.id.creadown);
        final CheckBox readgroup = (CheckBox) materialDialog.findViewById(R.id.creadgroup);
        final CheckBox readother = (CheckBox) materialDialog.findViewById(R.id.creadother);
        final CheckBox writeown = (CheckBox) materialDialog.findViewById(R.id.cwriteown);
        final CheckBox writegroup = (CheckBox) materialDialog.findViewById(R.id.cwritegroup);
        final CheckBox writeother = (CheckBox) materialDialog.findViewById(R.id.cwriteother);
        final CheckBox exeown = (CheckBox) materialDialog.findViewById(R.id.cexeown);
        final CheckBox exegroup = (CheckBox) materialDialog.findViewById(R.id.cexegroup);
        final CheckBox exeother = (CheckBox) materialDialog.findViewById(R.id.cexeother);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String perm = RootHelper.getPermissions(fileInfo.getFilePath(), fileInfo
                        .isDirectory());
                ArrayList<Boolean[]> arrayList = parse(perm);
                final Boolean[] read = arrayList.get(0);
                final Boolean[] write = arrayList.get(1);
                final Boolean[] exe = arrayList.get(2);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        readown.setChecked(read[0]);
                        readgroup.setChecked(read[1]);
                        readother.setChecked(read[2]);
                        writeown.setChecked(write[0]);
                        writegroup.setChecked(write[1]);
                        writeother.setChecked(write[2]);
                        exeown.setChecked(exe[0]);
                        exegroup.setChecked(exe[1]);
                        exeother.setChecked(exe[2]);
                    }
                });
            }
        };

        new Thread(runnable).start();

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDialog.dismiss();
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

                String command = "chmod " + finalValue + " " + fileInfo.getFilePath();
                if (fileInfo.isDirectory()) {
                    command = "chmod -R " + finalValue + " \"" + fileInfo.getFilePath() + "\"";
                }
                Command com = new Command(1, command) {
                    @Override
                    public void commandOutput(int i, String s) {
                        Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void commandTerminated(int i, String s) {
                        Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void commandCompleted(int i, int i2) {
                        Toast.makeText(getActivity(), getResources().getString(R.string
                                .completed), Toast
                                .LENGTH_LONG).show();
                    }
                };
                try {
                    RootUtils.mountRW(fileInfo.getFilePath());
                    RootTools.getShell(true).add(com);
                    RootUtils.mountRO(fileInfo.getFilePath());
                    refreshList();
                } catch (Exception e1) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error), Toast
                            .LENGTH_LONG)
                            .show();
                    e1.printStackTrace();
                }

            }
        });
        materialDialog.show();

    }


    @SuppressWarnings("ConstantConditions")
    private void showExtractOptions(final String currentFilePath, final String currentDir) {

        mSelectedPath = null;
        final String currentFileName = currentFilePath.substring(currentFilePath.lastIndexOf("/")
                + 1, currentFilePath.lastIndexOf("."));
        String texts[] = new String[]{getString(R.string.extract), getString(R.string.extract),
                "", getString(R.string.dialog_cancel)};
        final MaterialDialog materialDialog = new Dialogs().showCustomDialog(getActivity(),
                R.layout.dialog_extract, texts);

        final RadioButton radioButtonSpecify = (RadioButton) materialDialog.findViewById(R.id
                .radioButtonSpecifyPath);
        buttonPathSelect = (Button) materialDialog.findViewById(R.id.buttonPathSelect);
        RadioGroup radioGroupPath = (RadioGroup) materialDialog.findViewById(R.id.radioGroupPath);
        final EditText editFileName = (EditText) materialDialog.findViewById(R.id.editFileName);
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
                DialogBrowseFragment dialogFragment = new DialogBrowseFragment();
                dialogFragment.setTargetFragment(BaseFileList.this, DIALOG_FRAGMENT);
                dialogFragment.setStyle(DialogFragment.STYLE_NORMAL, getThemeStyle());
                dialogFragment.show(getFragmentManager(), "Browse Fragment");
            }
        });

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = editFileName.getText().toString();
                if (FileUtils.isFileNameInvalid(fileName)) {
                    editFileName.setError(getResources().getString(R.string
                            .msg_error_valid_name));
                    return;
                }
                if (radioButtonSpecify.isChecked()) {
                    File newFile = new File(mSelectedPath + "/" + currentFileName);
                    File currentFile = new File(currentFilePath);
                    if (FileUtils.isFileExisting(mSelectedPath, newFile.getName())) {
                        editFileName.setError(getResources().getString(R.string
                                .dialog_title_paste_conflict));
                        return;
                    }
                    fileOpHelper.extractFile(currentFile, newFile);
                }
                else {
                    File newFile = new File(currentDir + "/" + fileName);
                    File currentFile = new File(currentFilePath);
                    if (FileUtils.isFileExisting(currentDir, newFile.getName())) {
                        editFileName.setError(getResources().getString(R.string
                                .dialog_title_paste_conflict));
                        return;
                    }
                    fileOpHelper.extractFile(currentFile, newFile);
                }
                materialDialog.dismiss();
            }
        });

        materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });

        materialDialog.show();


    }

    private void showPasteIcon() {
        bottomToolbar.setVisibility(View.VISIBLE);
        bottomToolbar.getMenu().clear();
        bottomToolbar.inflateMenu(R.menu.action_mode_paste);
/*        EnhancedMenuInflater.inflate(getActivity().getMenuInflater(), bottomToolbar.getMenu(),
                category);*/
        setupPasteMenu();
/*        fabCreateMenu.setVisibility(View.GONE);
        fabOperation.setVisibility(View.VISIBLE);*/
    }

    void hidePasteIcon() {
   /*     fabOperation.setVisibility(View.GONE);
        fabCreateMenu.setVisibility(View.VISIBLE);*/
    }

    void onStartActionMode() {
        showBottomToolbar();
    }

    void onActionModeEnd() {
        isPasteVisible = false;
        hidePasteIcon();
        hideBottomToolbar();
    }


    @SuppressWarnings("ConstantConditions")
    private void showInfoDialog(FileInfo fileInfo) {
        String title = getString(R.string.properties);
        String texts[] = new String[]{title, getString(R.string.msg_ok), "", null};
        final MaterialDialog materialDialog = new Dialogs().showCustomDialog(getActivity(),
                R.layout.dialog_file_properties, texts);
        View view = materialDialog.getCustomView();
        ImageView imageFileIcon = (ImageView) view.findViewById(R.id.imageFileIcon);
        TextView textFileName = (TextView) view.findViewById(R.id.textFileName);
        TextView textPath = (TextView) view.findViewById(R.id.textPath);
        TextView textFileSize = (TextView) view.findViewById(R.id.textFileSize);
        TextView textDateModified = (TextView) view.findViewById(R.id.textDateModified);
        TextView textHidden = (TextView) view.findViewById(R.id.textHidden);
        TextView textReadable = (TextView) view.findViewById(R.id.textReadable);
        TextView textWriteable = (TextView) view.findViewById(R.id.textWriteable);
        TextView textHiddenPlaceHolder = (TextView) view.findViewById(R.id.textHiddenPlaceHolder);
        TextView textReadablePlaceHolder = (TextView) view.findViewById(R.id
                .textReadablePlaceHolder);
        TextView textWriteablePlaceHolder = (TextView) view.findViewById(R.id
                .textWriteablePlaceHolder);
        TextView textMD5 = (TextView) view.findViewById(R.id.textMD5);
        TextView textMD5Placeholder = (TextView) view.findViewById(R.id.textMD5PlaceHolder);

        String path = fileInfo.getFilePath();
        String fileName = fileInfo.getFileName();
        String fileDate;
        if (Category.checkIfFileCategory(category)) {
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
                fileNoOrSize = getResources().getString(R.string.empty);
            }
            else if (childFileListSize == -1) {
                fileNoOrSize = "";
            }
            else {
                fileNoOrSize = getResources().getQuantityString(R.plurals.number_of_files,
                        childFileListSize, childFileListSize);
            }
        }
        else {
            long size = fileInfo.getSize();
            fileNoOrSize = Formatter.formatFileSize(getActivity(), size);
        }
        boolean isReadable = new File(path).canRead();
        boolean isWriteable = new File(path).canWrite();
        boolean isHidden = new File(path).isHidden();

        textFileName.setText(fileName);
        textPath.setText(path);
        textFileSize.setText(fileNoOrSize);
        textDateModified.setText(fileDate);

        if (!isFilesCategory()) {
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
            textReadable.setText(isReadable ? getString(R.string.yes) : getString(R.string.no));
            textWriteable.setText(isWriteable ? getString(R.string.yes) : getString(R.string.no));
            textHidden.setText(isHidden ? getString(R.string.yes) : getString(R.string.no));
        }

        if (new File(path).isDirectory()) {
            textMD5.setVisibility(View.GONE);
            textMD5Placeholder.setVisibility(View.GONE);
            Drawable apkIcon = getAppIconForFolder(getActivity(), fileName);
            if (apkIcon != null) {
                imageFileIcon.setImageDrawable(apkIcon);
            }
            else {
                imageFileIcon.setImageResource(R.drawable.ic_folder);
            }
        }
        else {
            if (isFilesCategory()) {
                String md5 = FileUtils.getFastHash(path);
                textMD5.setText(md5);
            }

            if (fileInfo.getType() == VIDEO.getValue()) {
                Uri videoUri = Uri.fromFile(new File(path));
                Glide.with(getActivity()).load(videoUri).centerCrop()
                        .placeholder(R.drawable.ic_movie)
                        .crossFade(2)
                        .into(imageFileIcon);
            }
            else if (fileInfo.getType() == IMAGE.getValue()) {
                Uri imageUri = Uri.fromFile(new File(path));
                Glide.with(getActivity()).load(imageUri).centerCrop()
                        .crossFade(2)
                        .placeholder(R.drawable.ic_image_default)
                        .into(imageFileIcon);
            }
            else if (fileInfo.getType() == AUDIO.getValue()) {
                imageFileIcon.setImageResource(R.drawable.ic_music_default);
            }
            else if (fileInfo.getExtension().equals(FileConstants.APK_EXTENSION)) {
                Drawable apkIcon = getAppIcon(getActivity(), path);
                imageFileIcon.setImageDrawable(apkIcon);
            }
            else {
                imageFileIcon.setImageResource(R.drawable.ic_doc_white);
            }
        }

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });

        materialDialog.show();

    }

    private void updateMenuTitle() {
        mViewItem.setTitle(viewMode == ViewMode.LIST ? R.string.action_view_grid : R.string
                .action_view_list);
    }



    private void showSortDialog() {
        int color = new Dialogs().getCurrentThemePrimary(getActivity());

        final MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        CharSequence items[] = new String[]{getString(R.string.sort_name), getString(R.string
                .sort_name_desc),
                getString(R.string.sort_type), getString(R.string.sort_type_desc),
                getString(R.string.sort_size), getString(R.string.sort_size_desc),
                getString(R.string.sort_date), getString(R.string.sort_date_desc)};
        builder.title(getString(R.string.action_sort));
        builder.positiveText(getString(R.string.dialog_cancel));
        builder.positiveColor(color);
        builder.items(items);

        builder.alwaysCallSingleChoiceCallback();
        builder.itemsCallbackSingleChoice(getSortMode(), new MaterialDialog
                .ListCallbackSingleChoice() {
            @Override
            public boolean onSelection(MaterialDialog dialog, View itemView, int position,
                                       CharSequence text) {
                persistSortMode(position);
                refreshList();
                dialog.dismiss();
                return true;
            }
        });

        final MaterialDialog materialDialog = builder.build();
        materialDialog.show();
    }

    private void persistSortMode(int sortMode) {
        preferences.edit().putInt(FileConstants.KEY_SORT_MODE, sortMode).apply();
    }

    private int getSortMode() {
        return preferences.getInt(FileConstants.KEY_SORT_MODE, FileConstants.KEY_SORT_NAME);
    }


    void showBottomToolbar() {
        bottomToolbar.setVisibility(View.VISIBLE);
    }

    private Dialogs.DialogCallback dialogCallback = new Dialogs.DialogCallback() {

        @Override
        public void onError(int error) {

        }

        @Override
        public void onPositiveButtonClick(MaterialDialog dialog, Operations operation, String fileName) {
            if (FileUtils.isFileNameInvalid(fileName)) {
                dialog.getInputEditText().setError(context.getResources().getString(R.string
                        .msg_error_valid_name));
            } else {

                fileName = fileName.trim();
                String newPath = currentDir + File.separator + fileName;
                storagesUiView.
                if (baseFileList.exists(newPath)) {
                    materialDialog.getInputEditText().setError(context.getResources().getString(R.string
                            .file_exists));
                    return;
                }
                baseFileList.getFileOpHelper().mkDir(new File(newPath), isRooted);
                dialog.dismiss();
            }
        }

        @Override
        public void onNegativeButtonClick(Operations operations) {

        }
    };

}
