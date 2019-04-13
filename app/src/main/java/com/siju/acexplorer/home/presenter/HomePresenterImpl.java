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

package com.siju.acexplorer.home.presenter;

import android.os.Bundle;

import com.siju.acexplorer.billing.BillingManager;
import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.home.model.HomeModel;
import com.siju.acexplorer.home.model.LibrarySortModel;
import com.siju.acexplorer.home.model.LoaderHelper;
import com.siju.acexplorer.home.types.HomeLibraryInfo;
import com.siju.acexplorer.home.view.HomeView;
import com.siju.acexplorer.main.model.groups.Category;

import java.util.ArrayList;
import java.util.List;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import static com.siju.acexplorer.main.model.FileConstants.KEY_CATEGORY;

/**
 * Created by Siju on 02 September,2017
 */
public class HomePresenterImpl implements
                               LoaderManager.LoaderCallbacks<ArrayList<FileInfo>>,
                               HomeView.Listener,
                               HomeModel.Listener
{

    private HomeView      homeView;
    private HomeModel     homeModel;
    private LoaderManager loaderManager;
    private LoaderHelper  loaderHelper;

    public HomePresenterImpl(HomeView homeView, HomeModel homeModel, LoaderHelper loaderHelper,
                             LoaderManager loaderManager) {
        this.homeView = homeView;
        this.homeModel = homeModel;
        this.loaderHelper = loaderHelper;
        this.loaderManager = loaderManager;
        homeView.setListener(this);
        homeModel.setListener(this);
    }

    @Override
    public void loadData(Category category) {
        Bundle args = new Bundle();
        args.putSerializable(KEY_CATEGORY, category);
//        Log.d(TAG, "loadData: Category:"+category.getValue());
        loaderManager.restartLoader(category.getValue(), args, this);
    }

    @Override
    public BillingStatus checkBillingStatus() {
        return homeModel.getBillingStatus();
    }

    @Override
    public BillingManager getBillingManager() {
        return homeModel.getBillingManager();
    }

    @Override
    public void reloadLibraries(List<LibrarySortModel> selectedLibs) {
        homeModel.reloadLibraries(selectedLibs);
    }

    @Override
    public boolean getDualModeState() {
        return homeModel.getDualModeState();
    }

    @Override
    public void saveLibs(List<LibrarySortModel> librarySortModels) {
        homeModel.saveLibs(librarySortModels);
    }

    @Override
    public void onExit() {
//        if (loaderManager != null) {
//        }
    }


    @Override
    public Loader<ArrayList<FileInfo>> onCreateLoader(int id, Bundle args) {
        Category category = (Category) args.getSerializable(KEY_CATEGORY);
        return loaderHelper.createLoader(category, true);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<FileInfo>> loader, ArrayList<FileInfo> data) {
        homeView.onDataLoaded(loader.getId(), data);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<FileInfo>> loader) {

    }

    @Override
    public void getLibraries() {
        homeModel.getLibraries();
    }

    @Override
    public void onLibrariesFetched(List<HomeLibraryInfo> libraries) {
        homeView.onLibrariesFetched(libraries);
    }
}
