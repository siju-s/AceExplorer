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

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.model.LibrarySortModel;
import com.siju.acexplorer.home.model.LoaderHelper;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.storage.model.StoragesModel;
import com.siju.acexplorer.storage.view.StoragesUi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Siju on 04 September,2017
 */
public class StoragesPresenterImpl implements StoragesPresenter, StoragesUi.Listener,
        StoragesModel.Listener, LoaderManager
                .LoaderCallbacks<ArrayList<FileInfo>> {

    private final int LOADER_ID = 1000;
    private final String KEY_PICKER = "picker";
    private final String KEY_PATH = "path";


    private StoragesUi storagesUi;
    private StoragesModel storagesModel;
    private LoaderManager loaderManager;
    private LoaderHelper loaderHelper;
    private Category category;
    private final String TAG = this.getClass().getSimpleName();


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
    public void getLibraries() {

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
    public void onFilesFetched(List<FileInfo> libraries) {

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
