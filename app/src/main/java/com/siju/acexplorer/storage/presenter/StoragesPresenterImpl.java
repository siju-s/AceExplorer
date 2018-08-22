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

package com.siju.acexplorer.storage.presenter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.home.model.LoaderHelper;
import com.siju.acexplorer.main.model.FavInfo;
import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.main.model.groups.Category;
import com.siju.acexplorer.storage.model.CopyData;
import com.siju.acexplorer.storage.model.StoragesModel;
import com.siju.acexplorer.storage.model.operations.Operations;
import com.siju.acexplorer.storage.view.StoragesUi;
import com.siju.acexplorer.main.view.dialog.DialogHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Siju on 04 September,2017
 */
@SuppressWarnings("FieldCanBeLocal")
public class StoragesPresenterImpl implements StoragesUi.Listener,
                                              StoragesModel.Listener,
                                              LoaderManager
                                                      .LoaderCallbacks<ArrayList<FileInfo>>
{

    private static final int    LOADER_ID  = 1000;
    private static final String KEY_PICKER = "picker";
    private static final String KEY_ID = "id";

    private static final String KEY_PATH = "path";

    private StoragesUi    storagesUi;
    private StoragesModel storagesModel;
    private LoaderManager loaderManager;
    private LoaderHelper  loaderHelper;
    private Category      category;


    public StoragesPresenterImpl(StoragesUi storagesUi, StoragesModel storagesModel, LoaderHelper loaderHelper,
                                 LoaderManager loaderManager) {
        this.storagesUi = storagesUi;
        this.storagesModel = storagesModel;
        this.loaderHelper = loaderHelper;
        this.loaderManager = loaderManager;
        storagesUi.setListener(this);
        storagesModel.setListener(this);
    }


    @Override
    public void loadData(String currentDir, Category category, long id) {
        this.category = category;
        Bundle args = new Bundle();
        args.putString(KEY_PATH, currentDir);
        args.putBoolean(KEY_PICKER, false);
        args.putLong(KEY_ID, id);
        loaderManager.restartLoader(LOADER_ID, args, this);
    }

    @Override
    public BillingStatus checkBillingStatus() {
        return storagesModel.getBillingStatus();
    }

    @Override
    public Bundle getUserPrefs() {
        return storagesModel.getUserPrefs();
    }

    @Override
    public void startPasteOperation(String currentDir, boolean isMove, boolean rooted, ArrayList<FileInfo> info) {
        storagesModel.startPasteOperation(currentDir, isMove, rooted, info);
    }

    @Override
    public void moveToTrash(ArrayList<FileInfo> filesToDelete, String trashDir) {
        storagesModel.moveToTrash(filesToDelete, trashDir);
    }


    @Override
    public void handleSAFResult(Intent operationIntent, Uri treeUri, boolean rooted, int flags) {
        storagesModel.handleSAFResult(operationIntent, treeUri, rooted, flags);
    }

    @Override
    public void saveOldSAFUri(String path) {
        storagesModel.saveOldSAFUri(path);
    }

    @Override
    public void createDir(String currentDir, String name, boolean rooted) {
        storagesModel.createDir(currentDir, name, rooted);
    }

    @Override
    public void createFile(String currentDir, String name, boolean rooted) {
        storagesModel.createFile(currentDir, name, rooted);
    }

    @Override
    public void deleteFiles(ArrayList<FileInfo> filesToDelete) {
        storagesModel.deleteFiles(filesToDelete);

    }

    @Override
    public void onExtractPositiveClick(String currentFilePath, String newFileName, boolean isChecked,
                                       String selectedPath) {

        storagesModel.onExtractPositiveClick(currentFilePath, newFileName, isChecked, selectedPath);
    }

    @Override
    public void hideUnHideFiles(ArrayList<FileInfo> infoList, ArrayList<Integer> pos) {
        storagesModel.hideUnHideFiles(infoList, pos);
    }

    @Override
    public void getFilePermissions(String filePath, boolean directory) {
        storagesModel.getFilePermissions(filePath, directory);

    }

    @Override
    public int getSortMode() {
        return storagesModel.getSortMode();
    }

    @Override
    public void persistSortMode(int position) {
        storagesModel.persistSortMode(position);

    }

    @Override
    public void persistTrashState(boolean value) {
        storagesModel.persistTrashState(value);
    }

    @Override
    public void onCompressPosClick(String newFilePath, ArrayList<FileInfo> paths) {
        storagesModel.onCompressPosClick(newFilePath, paths);

    }

    @Override
    public void setPermissions(String path, boolean isDir, String permissions) {
        storagesModel.setPermissions(path, isDir, permissions);

    }

    @Override
    public void saveSettingsOnExit(int gridCols, int viewMode) {
        storagesModel.saveSettingsOnExit(gridCols, viewMode);

    }

    @Override
    public void updateFavorites(ArrayList<FavInfo> favInfoArrayList) {
        storagesModel.updateFavorites(favInfoArrayList);

    }

    @Override
    public void renameFile(String filePath, String newFilePath, String name, boolean rooted) {
        storagesModel.renameFile(filePath, newFilePath, name, rooted);
    }

    @Override
    public void showSAFDialog(String path, Intent data) {
        storagesUi.showSAFDialog(path, data);
    }

    @Override
    public void onFileExists(Operations operation, String msg) {
        storagesUi.onFileExists(operation, msg);

    }

    @Override
    public void showConflictDialog(final List<FileInfo> conflictFiles,
                                   List<FileInfo> destFiles, final String destinationDir, final boolean isMove,
                                   final DialogHelper.PasteConflictListener pasteConflictListener) {
        storagesUi.showConflictDialog(conflictFiles, destFiles, destinationDir, isMove, pasteConflictListener);
    }

    @Override
    public void onLowSpace() {
        storagesUi.onLowSpace();
    }

    @Override
    public void showPasteProgressDialog(String destinationDir, List<FileInfo> files,
                                        List<CopyData> copyData, boolean isMove) {

        storagesUi.showPasteProgressDialog(destinationDir, files, copyData, isMove);

    }

    @Override
    public void onOperationFailed(Operations operation) {
        storagesUi.onOperationFailed(operation);
    }

    @Override
    public void onInvalidName(Operations operation) {
        storagesUi.onInvalidName(operation);
    }

    @Override
    public void dismissDialog(Operations operation) {
        storagesUi.dismissDialog(operation);

    }

    @Override
    public void onPermissionsFetched(ArrayList<Boolean[]> permissionList) {
        storagesUi.onPermissionsFetched(permissionList);

    }

    @Override
    public void onPermissionSetError() {
        storagesUi.onPermissionSetError();
    }

    @Override
    public void onPermissionsSet() {
        storagesUi.onPermissionsSet();
    }


    @Override
    public void showZipProgressDialog(ArrayList<FileInfo> files, String destinationPath) {
        storagesUi.showZipProgressDialog(files, destinationPath);
    }

    @Override
    public void showExtractDialog(Intent intent) {
        storagesUi.showExtractDialog(intent);
    }

    @Override
    public void onFavExists() {
        storagesUi.onFavExists();
    }

    @Override
    public void onFavAdded(int count) {
        storagesUi.onFavAdded(count);
    }

    @Override
    public Loader<ArrayList<FileInfo>> onCreateLoader(int id, Bundle args) {

        return loaderHelper.createLoader(args.getString(KEY_PATH), category, args.getBoolean(KEY_PICKER),
                                         args.getLong(KEY_ID));
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<FileInfo>> loader, ArrayList<FileInfo> data) {
        storagesUi.onDataLoaded(data);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<FileInfo>> loader) {

    }

}
