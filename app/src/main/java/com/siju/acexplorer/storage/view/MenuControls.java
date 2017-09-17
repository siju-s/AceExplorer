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
import android.app.Dialog;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.siju.acexplorer.R;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.model.helper.ShareHelper;
import com.siju.acexplorer.storage.model.ViewMode;
import com.siju.acexplorer.storage.model.operations.Operations;
import com.siju.acexplorer.theme.Theme;
import com.siju.acexplorer.view.dialog.DialogHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.R.string.hide;
import static com.siju.acexplorer.model.groups.Category.FAVORITES;
import static com.siju.acexplorer.model.groups.Category.FILES;

/**
 * Created by Siju on 04 September,2017
 */
public class MenuControls implements View.OnClickListener,
        Toolbar.OnMenuItemClickListener,
        PopupMenu.OnMenuItemClickListener {

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
        bottomToolbar = storagesUiView.findViewById(R.id.toolbar_bottom);
        toolbar = storagesUiView.findViewById(R.id.toolbar);
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

        imgNavigationIcon = actionBar.findViewById(R.id.imgNavigationIcon);
        toolbarTitle = actionBar.findViewById(R.id.toolbarTitle);
        imgNavigationIcon.setOnClickListener(this);
        imgOverflow = actionBar.findViewById(R.id.imgButtonOverflow);
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


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgNavigationIcon:
                if (searchHelper.isExpanded()) {
                    searchHelper.disableSearch();
                } else {
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
        updateMenuTitle(storagesUiView.getViewMode());
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

        } else {
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
                } else {
                    mHideItem.setIcon(R.drawable.ic_unhide_black);

                }
            } else {
                mHideItem.setTitle(context.getString(hide));
                if (theme.equals(Theme.DARK)) {
                    mHideItem.setIcon(R.drawable.ic_hide_white);
                } else {
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
                        storagesUiView.removeFavorite(filesToDelete);
                        Toast.makeText(context, context.getString(R.string.fav_removed), Toast
                                .LENGTH_SHORT).show();
                    } else {
                        DialogHelper.showDeleteDialog(context, filesToDelete, deleteDialogListener);
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
                    DialogHelper.showInfoDialog(context, fileInfo, category.equals(FILES));
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
                    DialogHelper.showCompressDialog(context, currentDir, paths, compressDialogListener);
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
                        storagesUiView.updateFavouritesGroup(favList);
                    }
                    storagesUiView.finishActionMode();
                }
                break;

            case R.id.action_extract:

                if (selectedItems != null && selectedItems.size() > 0) {
                    FileInfo fileInfo = fileInfoList.get(selectedItems.keyAt(0));
                    String currentFile = fileInfo.getFilePath();
                    DialogHelper.showExtractOptions(context, currentFile, currentDir, extractDialogListener);
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
                    storagesUiView.hideUnHideFiles(infoList, pos);
                    storagesUiView.finishActionMode();
                }
                break;

            case R.id.action_permissions:

                if (selectedItems != null && selectedItems.size() > 0) {
                    FileInfo info = fileInfoList.get(selectedItems.keyAt(0));
                    isDirectory = info.isDirectory();
                    storagesUiView.getPermissions(info.getFilePath(), isDirectory);
                    storagesUiView.finishActionMode();
                }
                break;


            case R.id.action_view:
                int mode = storagesUiView.switchView();
                updateMenuTitle(mode);
                break;

            case R.id.action_sort:
                showSortDialog();
                break;
        }
        return false;
    }

    private boolean isDirectory;


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

        storagesUiView.showRenameDialog(fileName);
        storagesUiView.finishActionMode();
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

    private void updateMenuTitle(int viewMode) {
        mViewItem.setTitle(viewMode == ViewMode.LIST ? R.string.action_view_grid : R.string
                .action_view_list);
    }


    private void showSortDialog() {
        DialogHelper.showSortDialog(context, storagesUiView.getSortMode(), alertDialogListener);
    }


    void showBottomToolbar() {
        bottomToolbar.setVisibility(View.VISIBLE);
    }


    private DialogHelper.ExtractDialogListener extractDialogListener = new DialogHelper.ExtractDialogListener() {

        @Override
        public void onPositiveButtonClick(Dialog dialog, String currentFile, String newFileName, boolean isChecked) {
            storagesUiView.onExtractPositiveClick(dialog, currentFile, newFileName, isChecked);
        }

        @Override
        public void onSelectButtonClick() {
            storagesUiView.showSelectPathDialog();
        }
    };


    private DialogHelper.AlertDialogListener alertDialogListener = new DialogHelper.AlertDialogListener() {

        @Override
        public void onPositiveButtonClick(View view) {
            storagesUiView.sortFiles((int) view.getTag());
        }

        @Override
        public void onNegativeButtonClick(View view) {

        }

        @Override
        public void onNeutralButtonClick(View view) {

        }
    };

    private DialogHelper.DeleteDialogListener deleteDialogListener = new DialogHelper.DeleteDialogListener() {
        @Override
        public void onPositiveButtonClick(View view, ArrayList<FileInfo> filesToDelete) {
            storagesUiView.deleteFiles(filesToDelete);
        }
    };

    private DialogHelper.PermissionDialogListener permissionDialogListener = new DialogHelper.PermissionDialogListener() {

        @Override
        public void onPositiveButtonClick(String path, boolean isDir, String permissions) {

        }
    };

    private DialogHelper.CompressDialogListener compressDialogListener = new DialogHelper.CompressDialogListener() {

        @Override
        public void onPositiveButtonClick(Dialog dialog, Operations operation, String newFileName, String extension, ArrayList<FileInfo> paths) {
            storagesUiView.onCompressPosClick(dialog, operation, newFileName, extension, paths);
        }

        @Override
        public void onNegativeButtonClick(Operations operation) {

        }
    };


    public void onPermissionsFetched(ArrayList<Boolean[]> permissionList) {
        DialogHelper.showPermissionsDialog(context, currentDir, isDirectory, permissionList, permissionDialogListener);
    }
}
