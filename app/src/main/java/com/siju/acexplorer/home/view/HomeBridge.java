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

package com.siju.acexplorer.home.view;

import android.content.Intent;
import android.content.res.Configuration;
import android.view.ViewGroup;

import com.siju.acexplorer.billing.BillingManager;
import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.home.model.LibrarySortModel;
import com.siju.acexplorer.home.types.HomeLibraryInfo;
import com.siju.acexplorer.main.model.groups.Category;
import com.siju.acexplorer.main.view.DrawerListener;
import com.siju.acexplorer.storage.view.StoragesUiView;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;

/**
 * Created by Siju on 02 September,2017
 */
public class HomeBridge implements HomeView {

    private HomeUiView        homeUiView;
    private HomeView.Listener listener;
    private Fragment          fragment;

    HomeBridge(Fragment fragment, ViewGroup parent, DrawerListener drawerListener,
               StoragesUiView.FavoriteOperation favListener) {
        this.fragment = fragment;
        homeUiView = HomeUiView.inflate(parent);
        homeUiView.setBridgeRef(this);
        homeUiView.setFragment(fragment);
        homeUiView.setDrawerListener(drawerListener);
        homeUiView.setFavListener(favListener);
        parent.addView(homeUiView);
    }


    @Override
    public void onPause() {
        homeUiView.onPause();
    }

    @Override
    public void onResume() {
        homeUiView.onResume();
    }

    @Override
    public void onExit() {
        listener.onExit();
        homeUiView.onDestroy();
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onLibrariesFetched(final List<HomeLibraryInfo> libraries) {
        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                homeUiView.onLibrariesFetched(libraries);
            }
        });

    }

    @Override
    public void refreshList() {
        homeUiView.refreshList();
    }

    @Override
    public void onDataLoaded(int id, ArrayList<FileInfo> data) {
        homeUiView.onDataLoaded(id, data);
    }

    @Override
    public void handleActivityResult(int requestCode, int resultCode, Intent intent) {
        homeUiView.handleActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void updateFavoritesCount(int size) {
        homeUiView.updateFavoriteCount(size);
    }

    @Override
    public void init() {
        homeUiView.initialize();
    }

    @Override
    public void removeFavorites(int size) {
        homeUiView.removeFavorite(size);
    }

    @Override
    public void onPermissionGranted() {
        homeUiView.onPermissionGranted();
    }

    @Override
    public void showDualMode() {
        homeUiView.setDualMode();
    }

    @Override
    public void hideDualPane() {
        homeUiView.hideDualPane();
    }

    @Override
    public void setPremium() {
        homeUiView.setPremium();
    }

    @Override
    public void onConfigChanged(Configuration newConfig) {
        homeUiView.onConfigChanged(newConfig);
    }

    void getLibraries() {
        listener.getLibraries();
    }

    void loadData(Category category) {
        listener.loadData(category);
    }

    BillingStatus checkBillingStatus() {
        return listener.checkBillingStatus();
    }

    void reloadLibraries(List<LibrarySortModel> selectedLibs) {
        listener.reloadLibraries(selectedLibs);
    }

    boolean getDualModeState() {
        return listener.getDualModeState();
    }

    void saveLibs(List<LibrarySortModel> librarySortModels) {
        listener.saveLibs(librarySortModels);
    }

    public BillingManager getBillingManager() {
        return listener.getBillingManager();
    }
}
