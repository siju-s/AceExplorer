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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.siju.acexplorer.R;
import com.siju.acexplorer.home.model.LoaderHelper;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.storage.model.StorageModelImpl;
import com.siju.acexplorer.storage.model.StoragesModel;
import com.siju.acexplorer.storage.presenter.StoragesPresenterImpl;
import com.siju.acexplorer.view.DrawerListener;


public class BaseFileList extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    private StoragesUi storagesUi;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_base, container, false);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        Logger.log(TAG, "onActivityCreated" + savedInstanceState);

        LinearLayout linearLayout = getView().findViewById(R.id.home_base);
        storagesUi = new StorageBridge(this, linearLayout, favListener, drawerListener);
        StoragesModel storagesModel = new StorageModelImpl();
        LoaderHelper loaderHelper = new LoaderHelper(this);

        new StoragesPresenterImpl(storagesUi, storagesModel, loaderHelper,
                getLoaderManager());

        storagesUi.init();
    }


    @Override
    public void onDestroy() {
        storagesUi.onExit();
        super.onDestroy();
    }


    public boolean onBackPressed() {
        return storagesUi.onBackPress();
    }


    public void onPermissionGranted() {
        storagesUi.refreshList();
    }


    @Override
    public void onResume() {
        super.onResume();
        storagesUi.onResume();
    }


    @Override
    public void onPause() {
        storagesUi.onPause();
        super.onPause();

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        storagesUi.handleActivityResult(requestCode, resultCode, intent);
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onDestroyView() {
        storagesUi.onViewDestroyed();
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
        storagesUi.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        storagesUi.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    public void performVoiceSearch(String query) {
        storagesUi.performVoiceSearch(query);
    }

    public boolean isFabExpanded() {
        return storagesUi.isFabExpanded();
    }

    public void collapseFab() {
        storagesUi.collapseFab();
    }

    public void reloadList(String directory, Category category) {
        storagesUi.reloadList(directory, category);
    }

    public void refreshList() {
        storagesUi.refreshList();
    }

    public void removeHomeFromNavPath() {
        storagesUi.removeHomeFromNavPath();
    }

    public void refreshSpan() {
        storagesUi.refreshSpan();
    }

    public void showDualPane() {
        storagesUi.showDualPane();
    }

    private StoragesUiView.FavoriteOperation favListener;

    public void setFavoriteListener(StoragesUiView.FavoriteOperation favoriteListener) {
        this.favListener = favoriteListener;
    }

    private DrawerListener drawerListener;

    public void setDrawerListener(DrawerListener drawerListener) {
        this.drawerListener = drawerListener;
    }

    public void setPremium() {
        storagesUi.setPremium();
    }

    public void setHidden(boolean showHidden) {
        storagesUi.setHidden(showHidden);
    }
}
