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

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.kobakei.ratethisapp.RateThisApp;
import com.siju.acexplorer.R;
import com.siju.acexplorer.analytics.Analytics;
import com.siju.acexplorer.base.view.BaseActivity;
import com.siju.acexplorer.billing.BillingManager;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.MainModel;
import com.siju.acexplorer.model.MainModelImpl;
import com.siju.acexplorer.presenter.MainPresenter;
import com.siju.acexplorer.presenter.MainPresenterImpl;

import static com.siju.acexplorer.settings.SettingsPreferenceFragment.PREFS_ANALYTICS;


public class AceActivity extends BaseActivity {

    private final String TAG = this.getClass().getSimpleName();

    private MainUi mainUi;


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
        MainModel mainModel = new MainModelImpl();
        MainPresenter mainPresenter = new MainPresenterImpl(mainUi, mainModel);

        mainUi.init();
        mainPresenter.getUserPreferences();

        BillingManager billingManager = BillingManager.getInstance();
        billingManager.setContext(this);
        mainPresenter.getBillingStatus();
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
        RateThisApp.onStart(this);
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
        Log.d(TAG, "handleActivityResult(" + requestCode + "," + resultCode + ","
                + intent);

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
        Logger.log(TAG, "onDestroy");
        mainUi.onExit();
        super.onDestroy();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: ");
        mainUi.onPreferenceChange();
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
        Log.d("Ace", "onConfigChanged: "+newConfig.orientation);
        configuration = newConfig;
        mainUi.onConfigChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    public Configuration getConfiguration() {
        Log.d("Ace", "getConfiguration: configuration:"+configuration);
        return configuration != null ? configuration : getResources().getConfiguration();
    }

    public void switchView(int viewMode, boolean isDual) {
        configuration = getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mainUi.switchView(viewMode, isDual);
        }
    }
}
