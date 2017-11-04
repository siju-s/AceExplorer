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

package com.siju.acexplorer.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.siju.acexplorer.model.SectionGroup;
import com.siju.acexplorer.theme.Theme;

import java.util.ArrayList;

/**
 * Created by Siju on 02 September,2017
 */
public class MainBridge implements MainUi {

    private MainUiView uiView;
    private Context context;
    private MainUi.Listener listener;
    private AppCompatActivity activity;

    MainBridge(AppCompatActivity activity, ViewGroup parent) {
        this.context = parent.getContext();
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
    public void onPreferenceChange() {
        uiView.onPreferenceChange();
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
}
