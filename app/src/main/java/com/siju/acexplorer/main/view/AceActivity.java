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
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.kobakei.ratethisapp.RateThisApp;
import com.siju.acexplorer.R;
import com.siju.acexplorer.analytics.Analytics;
import com.siju.acexplorer.base.view.BaseActivity;
import com.siju.acexplorer.billing.BillingManager;
import com.siju.acexplorer.main.model.FavInfo;
import com.siju.acexplorer.main.model.MainModel;
import com.siju.acexplorer.main.model.MainModelImpl;
import com.siju.acexplorer.main.presenter.MainPresenter;
import com.siju.acexplorer.main.presenter.MainPresenterImpl;
import com.siju.acexplorer.storage.view.StoragesUiView;

import java.util.ArrayList;

import static com.siju.acexplorer.settings.SettingsPreferenceFragment.PREFS_ANALYTICS;


public class AceActivity extends BaseActivity implements ActivityFragmentCommunicator {

    private final String TAG = this.getClass().getSimpleName();

    private MainUi mainUi;
    private MainModel mainModel = new MainModelImpl();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base);
        boolean sendAnalytics = PreferenceManager.getDefaultSharedPreferences(this).
                getBoolean(PREFS_ANALYTICS, true);

        Analytics.getLogger().sendAnalytics(sendAnalytics);
        Analytics.getLogger().register(this);
        Analytics.getLogger().reportDeviceName();

        LinearLayout linearLayout = findViewById(R.id.base);
        mainUi = new MainBridge(this, linearLayout);
        MainPresenter mainPresenter = new MainPresenterImpl(mainUi, mainModel);

        mainUi.init();
        mainPresenter.getUserPreferences();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        mainUi.onIntentReceived(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mainUi.onPermissionResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Monitor launch times and interval from installation
        RateThisApp.onCreate(this);
        // If the criteria is satisfied, "Rate this app" dialog will be shown
        RateThisApp.showRateDialogIfNeeded(this);
    }

    @Override
    protected void onResume() {
        mainUi.onForeground();
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (!mainUi.handleActivityResult(requestCode, resultCode, intent)) {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }


    @Override
    public void onBackPressed() {
        if (mainUi.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        mainUi.onExit();
        super.onDestroy();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        mainUi.checkForPreferenceChanges();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        mainUi.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        mainUi.onContextItemSelected(item);
        return super.onContextItemSelected(item);
    }

    public void showDualFrame() {
        mainUi.showDualFrame();
    }

    public void setDualPaneFocusState(boolean isDualPaneInFocus) {
        mainUi.setDualPaneFocusState(isDualPaneInFocus);
    }

    private Configuration configuration;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        configuration = newConfig;
        mainUi.onConfigChanged(newConfig);
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode, Configuration newConfig) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig);
        mainUi.onMultiWindowChanged(isInMultiWindowMode, newConfig);
    }

    public Configuration getConfiguration() {
        return configuration != null ? configuration : getResources().getConfiguration();
    }

    public void switchView(int viewMode, boolean isDual) {
        configuration = getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mainUi.switchView(viewMode, isDual);
        }
    }

    public void refreshList(boolean isDual) {
        configuration = getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mainUi.refreshList(isDual);
        }
    }

    public void onSearchClicked() {
        mainUi.onSearchClicked();
    }

    @Override
    public BillingManager getBillingManager() {
        return mainModel.getBillingManager();
    }

    @Override
    public DrawerListener getDrawerListener() {
        return drawerListener;
    }

    @Override
    public StoragesUiView.FavoriteOperation getFavListener() {
        return favListener;
    }

    private DrawerListener drawerListener = new DrawerListener() {
        @Override
        public void onDrawerIconClicked(boolean dualPane) {
            mainUi.onDrawerIconClicked(dualPane);
        }

        @Override
        public void syncDrawer() {
            mainUi.syncDrawer();
        }
    };

    private StoragesUiView.FavoriteOperation favListener = new StoragesUiView.FavoriteOperation() {
        @Override
        public void updateFavorites(ArrayList<FavInfo> favList) {
            mainUi.updateFavorites(favList);
        }

        @Override
        public void removeFavorites(ArrayList<FavInfo> favList) {
            mainUi.removeFavorites(favList);
        }
    };


}
