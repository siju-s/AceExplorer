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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.siju.acexplorer.R;
import com.siju.acexplorer.home.model.HomeModel;
import com.siju.acexplorer.home.model.HomeModelImpl;
import com.siju.acexplorer.home.model.LoaderHelper;
import com.siju.acexplorer.home.presenter.HomePresenterImpl;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.FileConstants;
import com.siju.acexplorer.storage.view.StoragesUiView;
import com.siju.acexplorer.view.DrawerListener;

import static com.siju.acexplorer.model.FileConstants.PREFS_FIRST_RUN;

public class HomeScreenFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    private HomeView homeView;
    private DrawerListener drawerListener;
    private StoragesUiView.FavoriteOperation favListener;

    public static HomeScreenFragment newInstance(boolean isFirstRun, boolean isDualMode) {
        Bundle args = new Bundle();
        args.putBoolean(FileConstants.KEY_HOME, true);
        args.putBoolean(PREFS_FIRST_RUN, isFirstRun);
        args.putBoolean(FileConstants.KEY_DUAL_ENABLED, isDualMode);
        HomeScreenFragment homeScreenFragment = new HomeScreenFragment();
        homeScreenFragment.setArguments(args);
        return homeScreenFragment;
    }

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
        setHasOptionsMenu(false);
        Logger.log(TAG, "onActivityCreated" + savedInstanceState);

        LinearLayout linearLayout = getView().findViewById(R.id.home_base);
        homeView = new HomeBridge(this, linearLayout, drawerListener, favListener);
        HomeModel homeModel = new HomeModelImpl();
        homeModel.setActivityContext(getActivity());
        LoaderHelper loaderHelper = new LoaderHelper(this);

        new HomePresenterImpl(homeView, homeModel, loaderHelper,
                getActivity().getSupportLoaderManager());

        homeView.init();
    }


    @Override
    public void onPause() {
        homeView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        homeView.onResume();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        homeView.handleActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        homeView.onConfigChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

     @Override
    public void onDestroy() {
        homeView.onExit();
        super.onDestroy();
    }

    public void updateFavoritesCount(int size) {
        homeView.updateFavoritesCount(size);
    }

    public void setDrawerListener(DrawerListener drawerListener) {
        this.drawerListener = drawerListener;
    }

    public void setFavListener(StoragesUiView.FavoriteOperation favoriteListener) {
        this.favListener = favoriteListener;
    }

    public void removeFavorites(int size) {
        homeView.removeFavorites(size);
    }

    public void onPermissionGranted() {
        if (homeView != null) {
            homeView.onPermissionGranted();
        }
    }


    public void showDualMode() {
        homeView.showDualMode();
    }

    public void hideDualPane() {
        homeView.hideDualPane();
    }

    public void setPremium() {
        homeView.setPremium();
    }
}
