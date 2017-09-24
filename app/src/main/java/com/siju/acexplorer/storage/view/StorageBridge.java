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
import android.view.ViewGroup;

import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.model.FavInfo;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.storage.model.CopyData;
import com.siju.acexplorer.storage.model.operations.Operations;
import com.siju.acexplorer.view.DrawerListener;
import com.siju.acexplorer.view.MainUiView;
import com.siju.acexplorer.view.dialog.DialogHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Siju on 02 September,2017
 */
public class StorageBridge implements StoragesUi {

    private StoragesUiView uiView;
    private Context context;
    private Listener listener;
    private Fragment fragment;

    StorageBridge(Fragment fragment, ViewGroup parent, StoragesUiView.FavoriteOperation favListener,
                  DrawerListener drawerListener) {
        this.context = parent.getContext();
        this.fragment = fragment;
        uiView = StoragesUiView.inflate(parent);
        uiView.setBridgeRef(this);
        uiView.setFragment(fragment);
        uiView.setFavListener(favListener);
        uiView.setDrawerListener(drawerListener);
        parent.addView(uiView);
    }


    @Override
    public void onPause() {
        uiView.onPause();
    }

    @Override
    public void onResume() {
        uiView.onResume();
    }

    @Override
    public void onExit() {
        uiView.onDestroy();
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }


    @Override
    public void onDataLoaded(ArrayList<FileInfo> data) {
        uiView.onDataLoaded(data);
    }

    @Override
    public void handleActivityResult(int requestCode, int resultCode, Intent intent) {
        uiView.handleActivityResult(requestCode, resultCode, intent);
    }


    @Override
    public void init() {
        uiView.initialize();
    }

    @Override
    public boolean onBackPress() {
        return uiView.onBackPressed();
    }

    @Override
    public void onViewDestroyed() {
        uiView.onViewDestroyed();
    }

    @Override
    public void showSAFDialog(String path, Intent data) {
        uiView.showSAFDialog(path, data);

    }

    @Override
    public void onFileExists(final Operations operation, final String msg) {
        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uiView.onFileExists(operation, msg);
            }
        });
    }

    @Override
    public void showConflictDialog(final List<FileInfo> conflictFiles,
                                   final String destinationDir, final boolean isMove,
                                   final DialogHelper.PasteConflictListener pasteConflictListener) {
        uiView.showConflictDialog(conflictFiles, destinationDir, isMove, pasteConflictListener);

    }

    @Override
    public void onLowSpace() {

    }

    @Override
    public void showPasteProgressDialog(String destinationDir, List<FileInfo> files, List<CopyData> copyData, boolean isMove) {
        uiView.showPasteProgressDialog(destinationDir, files, copyData, isMove);

    }

    @Override
    public void onInvalidName(Operations operation) {
        uiView.onInvalidName(operation);
    }

    @Override
    public void dismissDialog(Operations operation) {
        uiView.dismissDialog(operation);

    }

    @Override
    public void onPermissionsFetched(ArrayList<Boolean[]> permissionList) {
        uiView.onPermissionsFetched(permissionList);

    }

    @Override
    public void onPermissionsSet() {
        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uiView.onPermissionsSet();
            }
        });
    }

    @Override
    public void onPermissionSetError() {
        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uiView.onPermissionSetError();
            }
        });
    }

    @Override
    public void refreshList() {
        uiView.refreshList();
    }

    @Override
    public boolean isFabExpanded() {
        return uiView.isFabExpanded();
    }

    @Override
    public void collapseFab() {
        uiView.collapseFab();

    }

    @Override
    public void showDualPane() {
        uiView.showDualPane();
    }

    @Override
    public void reloadList(String directory, Category category) {
        uiView.reloadList(directory, category);
    }

    @Override
    public void removeHomeFromNavPath() {
        uiView.removeHomeFromNavPath();
    }

    @Override
    public void refreshSpan() {
        uiView.refreshSpan();
    }

    @Override
    public void performVoiceSearch(String query) {
        uiView.performVoiceSearch(query);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        uiView.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void setPremium() {
        uiView.setPremium();
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

    public void deleteFiles(ArrayList<FileInfo> filesToDelete) {
        listener.deleteFiles(filesToDelete);
    }

    public void onExtractPositiveClick(String currentFilePath, String newFileName, boolean isChecked,
                                       String selectedPath) {
        listener.onExtractPositiveClick(currentFilePath, newFileName, isChecked, selectedPath);

    }

    public void hideUnHideFiles(ArrayList<FileInfo> infoList, ArrayList<Integer> pos) {
        listener.hideUnHideFiles(infoList, pos);
    }

    public void getFilePermissions(String filePath, boolean directory) {
        listener.getFilePermissions(filePath, directory);

    }

    public int getSortMode() {
        return listener.getSortMode();
    }

    public void persistSortMode(int position) {
        listener.persistSortMode(position);
    }

    public void onCompressPosClick(String newFilePath, ArrayList<FileInfo> paths) {
        listener.onCompressPosClick(newFilePath, paths);

    }

    public void setPermissions(String path, boolean isDir, String permissions) {
        listener.setPermissions(path, isDir, permissions);
    }

    public void saveSettingsOnExit(int gridCols, int viewMode) {
        listener.saveSettingsOnExit(gridCols, viewMode);
    }

    public void updateFavorites(ArrayList<FavInfo> favInfoArrayList) {
        listener.updateFavorites(favInfoArrayList);

    }
}