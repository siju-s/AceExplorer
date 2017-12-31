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

package com.siju.acexplorer.storage.modules.picker.presenter;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.siju.acexplorer.home.model.LoaderHelper;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.storage.modules.picker.model.PickerModel;
import com.siju.acexplorer.storage.modules.picker.view.PickerUi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Siju on 04 September,2017
 */
@SuppressWarnings("FieldCanBeLocal")
public class PickerPresenterImpl implements PickerPresenter, LoaderManager
        .LoaderCallbacks<ArrayList<FileInfo>>, PickerModel.Listener {

    private final String TAG = this.getClass().getSimpleName();
    private static final int LOADER_ID = 1000;
    private static final String KEY_PICKER = "picker";
    private static final String KEY_PATH = "path";

    private PickerUi pickerUi;
    private PickerModel pickerModel;
    private LoaderManager loaderManager;
    private LoaderHelper loaderHelper;
    private Category category;


    public PickerPresenterImpl(PickerUi pickerUi, PickerModel pickerModel, LoaderHelper loaderHelper,
                               LoaderManager loaderManager) {
        this.pickerUi = pickerUi;
        this.pickerModel = pickerModel;
        this.loaderHelper = loaderHelper;
        this.loaderManager = loaderManager;
//        pickerUi.setListener(this);
        pickerModel.setListener(this);
    }


    @Override
    public void loadData(String path, boolean isRingtonePicker) {
        Bundle args = new Bundle();
        args.putString(KEY_PATH, path);
        args.putBoolean(KEY_PICKER, isRingtonePicker);
        loaderManager.restartLoader(LOADER_ID, args, this);
    }

    @Override
    public void getStoragesList() {
        pickerModel.getStoragesList();
    }

    @Override
    public void saveLastRingtoneDir(String currentPath) {
        pickerModel.saveLastRingtoneDir(currentPath);
    }

    @Override
    public String getLastSavedRingtoneDir() {
        return pickerModel.getLastSavedRingtoneDir();
    }


    @Override
    public Loader<ArrayList<FileInfo>> onCreateLoader(int id, Bundle args) {

        return loaderHelper.createLoader(args.getString(KEY_PATH), Category.FILES, args.getBoolean(KEY_PICKER));
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<FileInfo>> loader, ArrayList<FileInfo> data) {
        pickerUi.onDataLoaded(data);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<FileInfo>> loader) {

    }

    @Override
    public void onStoragesFetched(List<String> storagesList) {
        pickerUi.onStoragesFetched(storagesList);
    }
}
