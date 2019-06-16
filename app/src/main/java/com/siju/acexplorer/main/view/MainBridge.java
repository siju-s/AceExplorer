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

package com.siju.acexplorer.main.view;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.siju.acexplorer.billing.repository.BillingManager;
import com.siju.acexplorer.main.AceActivity;
import com.siju.acexplorer.main.model.FavInfo;
import com.siju.acexplorer.main.model.SectionGroup;
import com.siju.acexplorer.theme.Theme;

import java.util.ArrayList;


public class MainBridge implements MainUi {

    private MainUiView uiView;
    private MainUi.Listener listener;
    private AppCompatActivity activity;

    MainBridge(AppCompatActivity activity, ViewGroup parent) {
        this.activity = activity;
        uiView = MainUiView.inflate(parent);
        uiView.setActivity(activity);
        uiView.setBridgeRef(this);
        parent.addView(uiView);
    }


    @Override
    public void onIntentReceived(Intent intent) {
        uiView.handleIntent(intent);
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void passUserPrefs(Bundle userPrefs) {
        uiView.passUserPrefs(userPrefs);
    }


    @Override
    public void onExit() {
        listener.onExit();
        uiView.cleanUp();
    }

    @Override
    public boolean onBackPressed() {
        return uiView.handleBackPress();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo
            menuInfo) {
        uiView.onCreateContextMenu(menu, menuInfo);
    }

    @Override
    public void onContextItemSelected(MenuItem item) {
        uiView.onContextItemSelected(item);
    }

    @Override
    public void getTotalGroupData() {
        listener.getTotalGroupData();
    }

    @Override
    public void onTotalGroupDataFetched(final ArrayList<SectionGroup> totalData) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uiView.onTotalGroupDataFetched(totalData);
            }
        });
    }

    @Override
    public void init() {
        uiView.initialize();
    }

    @Override
    public void showDualFrame() {
        uiView.showDualFrame();
    }

    @Override
    public void setDualPaneFocusState(boolean isDualPaneInFocus) {
        uiView.setDualPaneFocusState(isDualPaneInFocus);
    }

    @Override
    public void onSearchClicked() {
        uiView.onSearchClicked();
    }

    @Override
    public void onDrawerIconClicked(boolean dualPane) {
        uiView.onDrawerIconClicked(dualPane);
    }

    @Override
    public void syncDrawer() {
        uiView.syncDrawer();
    }

    @Override
    public void updateFavorites(ArrayList<FavInfo> favList) {
        uiView.updateFavorites(favList);
    }

    @Override
    public void removeFavorites(ArrayList<FavInfo> favList) {
        uiView.removeFavorites(favList);
    }

    @Override
    public void onConfigChanged(Configuration newConfig) {
        uiView.onConfigChanged(newConfig);
    }

    @Override
    public void onMultiWindowChanged(boolean isInMultiWindowMode, Configuration newConfig) {
        uiView.onMultiWindowChanged(isInMultiWindowMode, newConfig);
    }

    @Override
    public void switchView(int viewMode, boolean isDual) {
        uiView.switchView(viewMode, isDual);
    }

    @Override
    public void refreshList(boolean isDual) {
        uiView.refreshList(isDual);
    }

    @Override
    public void onPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        uiView.handlePermissionResult(requestCode);
    }

    @Override
    public boolean handleActivityResult(int requestCode, int resultCode, Intent intent) {
        return uiView.handleActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void passActivityResult(int requestCode, int resultCode, Intent intent) {
    }

    @Override
    public void checkForPreferenceChanges() {
        uiView.checkForPreferenceChanges();
    }

    @Override
    public void onForeground() {
        uiView.onForeground();
    }

    @Override
    public void onBillingUnSupported() {
        uiView.onBillingUnSupported();
    }

    @Override
    public void onFreeVersion() {
        uiView.onFreeVersion();
    }

    @Override
    public void onPremiumVersion() {
        uiView.onPremiumVersion();
    }

    Theme getCurrentTheme() {
        return ((AceActivity) activity).getCurrentTheme();
    }

    BillingManager getBillingManager() {
        return listener.getBillingManager();
    }
}
