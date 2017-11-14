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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.model.FavInfo;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.storage.model.CopyData;
import com.siju.acexplorer.storage.model.operations.Operations;
import com.siju.acexplorer.view.AceActivity;
import com.siju.acexplorer.view.DrawerListener;
import com.siju.acexplorer.view.dialog.DialogHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Siju on 02 September,2017
 */
public class StorageBridge implements StoragesUi {

    private StoragesUiView storagesUiView;
    private Context        context;
    private Listener       listener;
    private Fragment       fragment;

    StorageBridge(Fragment fragment, ViewGroup parent, StoragesUiView.FavoriteOperation favListener,
                  DrawerListener drawerListener) {
        this.context = parent.getContext();
        this.fragment = fragment;
        storagesUiView = StoragesUiView.inflate(parent);
        storagesUiView.setBridgeRef(this);
        storagesUiView.setFragment(fragment);
        storagesUiView.setFavListener(favListener);
        storagesUiView.setDrawerListener(drawerListener);
        parent.addView(storagesUiView);
    }


    @Override
    public void onPause() {
        storagesUiView.onPause();
    }

    @Override
    public void onResume() {
        storagesUiView.onResume();
    }

    @Override
    public void onExit() {
        storagesUiView.onDestroy();
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }


    @Override
    public void onDataLoaded(ArrayList<FileInfo> data) {
        storagesUiView.onDataLoaded(data);
    }

    @Override
    public void handleActivityResult(int requestCode, int resultCode, Intent intent) {
        storagesUiView.handleActivityResult(requestCode, resultCode, intent);
    }


    @Override
    public void init() {
        storagesUiView.initialize();
    }

    @Override
    public boolean onBackPress() {
        return storagesUiView.onBackPressed();
    }

    @Override
    public void onViewDestroyed() {
        storagesUiView.onViewDestroyed();
    }

    @Override
    public void showSAFDialog(final String path, final Intent data) {
        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                storagesUiView.showSAFDialog(path, data);
            }
        });

    }

    @Override
    public void onFileExists(final Operations operation, final String msg) {
        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                storagesUiView.onFileExists(operation, msg);
            }
        });
    }

    @Override
    public void showConflictDialog(final List<FileInfo> conflictFiles,
                                   final List<FileInfo> destFiles, final String destinationDir, final boolean isMove,
                                   final DialogHelper.PasteConflictListener pasteConflictListener) {
        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                storagesUiView.showConflictDialog(conflictFiles, destFiles, destinationDir, isMove, pasteConflictListener);
            }
        });

    }

    @Override
    public void onLowSpace() {

    }

    @Override
    public void showPasteProgressDialog(final String destinationDir, final List<FileInfo> files,
                                        final List<CopyData> copyData, final boolean isMove) {
        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                storagesUiView.showPasteProgressDialog(destinationDir, files, copyData, isMove);
            }
        });

    }

    @Override
    public void onInvalidName(final Operations operation) {
        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                storagesUiView.onInvalidName(operation);
            }
        });
    }

    @Override
    public void dismissDialog(final Operations operation) {
        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                storagesUiView.dismissDialog();
            }
        });

    }

    @Override
    public void onPermissionsFetched(final ArrayList<Boolean[]> permissionList) {
        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                storagesUiView.onPermissionsFetched(permissionList);
            }
        });

    }

    @Override
    public void onPermissionsSet() {
        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                storagesUiView.onPermissionsSet();
            }
        });
    }

    @Override
    public void onPermissionSetError() {
        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                storagesUiView.onPermissionSetError();
            }
        });
    }

    @Override
    public void refreshList() {
        storagesUiView.refreshList();
    }

    @Override
    public boolean isFabExpanded() {
        return storagesUiView.isFabExpanded();
    }

    @Override
    public void collapseFab() {
        storagesUiView.collapseFab();

    }

    @Override
    public void showDualPane() {
        storagesUiView.showDualPane();
    }

    @Override
    public void reloadList(String directory, Category category) {
        storagesUiView.reloadList(directory, category);
    }

    @Override
    public void removeHomeFromNavPath() {
        storagesUiView.removeHomeFromNavPath();
    }

    @Override
    public void refreshSpan() {
        storagesUiView.refreshSpan();
    }

    @Override
    public void performVoiceSearch(String query) {
        storagesUiView.performVoiceSearch(query);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        storagesUiView.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onOptionsItemSelected(MenuItem menuItem) {
        storagesUiView.onOptionsItemSelected(menuItem);
    }

    @Override
    public void setPremium() {
        storagesUiView.setPremium();
    }

    @Override
    public void showZipProgressDialog(Intent zipIntent) {
        storagesUiView.showZipProgressDialog(zipIntent);
    }

    @Override
    public void onOperationFailed(final Operations operation) {
        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                storagesUiView.onOperationFailed(operation);
            }
        });
    }

    @Override
    public void showExtractDialog(Intent intent) {
        storagesUiView.showExtractDialog(intent);
    }

    @Override
    public void setHidden(boolean showHidden) {
        storagesUiView.setHidden(showHidden);
    }

    @Override
    public void onFavAdded(final int count) {
        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                storagesUiView.onFavAdded(count);
            }
        });

    }

    @Override
    public void onFavExists() {
        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                storagesUiView.onFavExists();
            }
        });
    }

    @Override
    public void hideDualPane() {
        storagesUiView.hideDualPane();
    }

    @Override
    public void addHomeNavPath() {
        storagesUiView.addHomeNavPath();
    }


    void loadData(String currentDir, Category category, boolean isPicker) {
        listener.loadData(currentDir, category, isPicker);
    }

    BillingStatus checkBillingStatus() {
        return listener.checkBillingStatus();
    }


    Bundle getUserPrefs() {
        return listener.getUserPrefs();
    }

    void startPasteOperation(String currentDir, boolean isMove, boolean rooted,
                             ArrayList<FileInfo> info) {
        listener.startPasteOperation(currentDir, isMove, rooted, info);
    }

    void handleSAFResult(Intent operationIntent, Uri treeUri, boolean rooted, int flags) {
        listener.handleSAFResult(operationIntent, treeUri, rooted, flags);
    }

    void saveOldSAFUri(String path) {
        listener.saveOldSAFUri(path);
    }

    void createDir(String currentDir, String name, boolean rooted) {
        listener.createDir(currentDir, name, rooted);
    }

    void createFile(String currentDir, String name, boolean rooted) {
        listener.createFile(currentDir, name, rooted);
    }

    void deleteFiles(ArrayList<FileInfo> filesToDelete) {
        listener.deleteFiles(filesToDelete);
    }

    void onExtractPositiveClick(String currentFilePath, String newFileName, boolean isChecked,
                                String selectedPath) {
        listener.onExtractPositiveClick(currentFilePath, newFileName, isChecked, selectedPath);

    }

    void hideUnHideFiles(ArrayList<FileInfo> infoList, ArrayList<Integer> pos) {
        listener.hideUnHideFiles(infoList, pos);
    }

    void getFilePermissions(String filePath, boolean directory) {
        listener.getFilePermissions(filePath, directory);

    }

    int getSortMode() {
        return listener.getSortMode();
    }

    void persistSortMode(int position) {
        listener.persistSortMode(position);
    }

    void onCompressPosClick(String newFilePath, ArrayList<FileInfo> paths) {
        listener.onCompressPosClick(newFilePath, paths);

    }

    public void setPermissions(String path, boolean isDir, String permissions) {
        listener.setPermissions(path, isDir, permissions);
    }

    void saveSettingsOnExit(int gridCols, int viewMode) {
        listener.saveSettingsOnExit(gridCols, viewMode);
    }

    void updateFavorites(ArrayList<FavInfo> favInfoArrayList) {
        listener.updateFavorites(favInfoArrayList);

    }

    void renameFile(String filePath, String parentDir, String name, int position,
                    boolean rooted) {
        listener.renameFile(filePath, parentDir, name, position, rooted);
    }

    public void showDualFrame() {
        ((AceActivity)fragment.getActivity()).showDualFrame();
    }
}
