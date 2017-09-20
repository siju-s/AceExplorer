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

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;

import com.siju.acexplorer.view.DrawerListener;
import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.home.model.HomeLibraryInfo;
import com.siju.acexplorer.home.model.LibrarySortModel;
import com.siju.acexplorer.model.groups.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Siju on 02 September,2017
 */
public class HomeBridge implements HomeView {

    private HomeUiView uiView;
    private Context context;
    private HomeView.Listener listener;
    private Fragment fragment;

    HomeBridge(Fragment fragment, ViewGroup parent, DrawerListener drawerListener) {
        this.context = parent.getContext();
        this.fragment = fragment;
        uiView = HomeUiView.inflate(parent);
        uiView.setBridgeRef(this);
        uiView.setFragment(fragment);
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
    public void onLibrariesFetched(final List<HomeLibraryInfo> libraries) {
        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uiView.onLibrariesFetched(libraries);
            }
        });

    }

    @Override
    public void onDataLoaded(int id, ArrayList<FileInfo> data) {
        uiView.onDataLoaded(id, data);
    }

    @Override
    public void handleActivityResult(int requestCode, int resultCode, Intent intent) {
        uiView.handleActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void updateFavoritesCount(int size) {
        uiView.removeFavorite(size);
    }

    @Override
    public void init() {
        uiView.initialize();
    }

    @Override
    public void removeFavorites(int size) {
        uiView.removeFavorite(size);
    }

    @Override
    public void onPermissionGranted() {
        uiView.onPermissionGranted();
    }

    @Override
    public void showDualMode() {
        uiView.setDualMode();
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
}
