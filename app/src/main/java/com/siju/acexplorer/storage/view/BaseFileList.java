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
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.siju.acexplorer.view.DrawerListener;
import com.siju.acexplorer.R;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.home.model.LoaderHelper;
import com.siju.acexplorer.storage.model.StorageModelImpl;
import com.siju.acexplorer.storage.model.StoragesModel;
import com.siju.acexplorer.storage.presenter.StoragesPresenter;
import com.siju.acexplorer.storage.presenter.StoragesPresenterImpl;


public class BaseFileList extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    private StoragesPresenter storagesPresenter;
    private StoragesUi storagesUi;
    private StoragesModel storagesModel;
    private DrawerListener drawerListener;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_base, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        Logger.log(TAG, "onActivityCreated" + savedInstanceState);

        LinearLayout linearLayout = getActivity().findViewById(R.id.home_base);
        storagesUi = new StorageBridge(this, linearLayout, drawerListener);
        storagesModel = new StorageModelImpl();
        LoaderHelper loaderHelper = new LoaderHelper(this);

        storagesPresenter = new StoragesPresenterImpl(storagesUi, storagesModel, loaderHelper,
                getActivity().getSupportLoaderManager());

        storagesUi.init();
    }


    @Override
    public void onDestroy() {
        storagesUi.onExit();
        super.onDestroy();
    }

    public void updateFavoritesCount(int size) {
        storagesUi.updateFavoritesCount(size);
    }

    public void setDrawerListener(DrawerListener drawerListener) {
        this.drawerListener = drawerListener;
    }


    public boolean onBackPressed() {
        return storagesUi.onBackPress();
    }


    public void onPermissionGranted() {
        refreshList();
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


}
