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
import com.siju.acexplorer.home.model.LibrarySortModel;
import com.siju.acexplorer.home.model.LoaderHelper;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.storage.model.CopyData;
import com.siju.acexplorer.storage.model.StoragesModel;
import com.siju.acexplorer.storage.model.operations.Operations;
import com.siju.acexplorer.storage.view.StoragesUi;
import com.siju.acexplorer.view.dialog.DialogHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Siju on 04 September,2017
 */
public class StoragesPresenterImpl implements StoragesPresenter, StoragesUi.Listener,
        StoragesModel.Listener, LoaderManager
                .LoaderCallbacks<ArrayList<FileInfo>> {

    private final String TAG = this.getClass().getSimpleName();
    private final int LOADER_ID = 1000;
    private final String KEY_PICKER = "picker";


    private final String KEY_PATH = "path";
    private StoragesUi storagesUi;
    private StoragesModel storagesModel;
    private LoaderManager loaderManager;
    private LoaderHelper loaderHelper;
    private Category category;


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
    public void loadData(String currentDir, Category category, boolean isPicker) {
        this.category = category;
        Bundle args = new Bundle();
        args.putString(KEY_PATH, currentDir);
        args.putBoolean(KEY_PICKER, isPicker);
        loaderManager.initLoader(LOADER_ID, null, this);
    }

    @Override
    public BillingStatus checkBillingStatus() {
        return null;
    }

    @Override
    public void reloadLibraries(List<LibrarySortModel> selectedLibs) {

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
    public void handleSAFResult(Intent operationIntent, Uri treeUri, boolean rooted, int flags) {
        storagesModel.handleSAFResult(operationIntent, treeUri, rooted, flags);
    }

    @Override
    public void saveOldSAFUri(String path) {
        storagesModel.saveOldSAFUri(path);
    }

    @Override
    public void createDir(String currentDir, String name, boolean rooted) {
        storagesModel.createDir(currentDir,name, rooted);
    }

    @Override
    public void createFile(String currentDir, String name, boolean rooted) {
        storagesModel.createFile(currentDir,name, rooted);
    }

    @Override
    public void onFilesFetched(List<FileInfo> libraries) {

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
                                   final String destinationDir, final boolean isMove,
                                   final DialogHelper.PasteConflictListener pasteConflictListener) {
        storagesUi.showConflictDialog(conflictFiles, destinationDir, isMove, pasteConflictListener);
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

    }

    @Override
    public Loader<ArrayList<FileInfo>> onCreateLoader(int id, Bundle args) {

        return loaderHelper.createLoader(args.getString(KEY_PATH), category, args.getBoolean(KEY_PICKER));
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<FileInfo>> loader, ArrayList<FileInfo> data) {
         storagesUi.onDataLoaded(data);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<FileInfo>> loader) {

    }
}
