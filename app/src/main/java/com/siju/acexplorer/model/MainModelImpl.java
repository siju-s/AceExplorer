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

package com.siju.acexplorer.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.billing.BillingHelper;
import com.siju.acexplorer.billing.BillingResultCallback;
import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.model.groups.DrawerItems;
import com.siju.acexplorer.theme.Theme;
import com.siju.acexplorer.theme.ThemeUtils;
import com.siju.acexplorer.utils.Utils;

import java.util.ArrayList;

import static com.siju.acexplorer.filesystem.FileConstants.PREFS_FIRST_RUN;

/**
 * Created by Siju on 02 September,2017
 */
public class MainModelImpl implements MainModel, BillingResultCallback, DrawerItems.DrawerItemsCallback {

    private final String TAG = this.getClass().getSimpleName();
    private MainModel.Listener listener;
    private Context context;

    public MainModelImpl() {
        context = AceApplication.getAppContext();
    }

    @Override
    public void getUserSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isTablet = false;
        boolean isFirstRun = true;
        if (preferences.getBoolean(PREFS_FIRST_RUN, true)) {
            preferences.edit().putInt(FileConstants.KEY_SORT_MODE, FileConstants
                    .KEY_SORT_NAME).apply();
            isTablet = Utils.isTablet(context);
            if (isTablet) {
                Logger.log(TAG, "Istab");
                preferences.edit().putBoolean(FileConstants.PREFS_DUAL_PANE, true).apply();
            }
        }
        boolean isHomeScreenEnabled = preferences.getBoolean(FileConstants.PREFS_HOMESCREEN, true);
        boolean isDualPaneEnabled = preferences.getBoolean(FileConstants.PREFS_DUAL_PANE,
                isTablet);

        Bundle bundle = new Bundle();
        bundle.putBoolean(FileConstants.PREFS_HOMESCREEN, isHomeScreenEnabled);
        bundle.putBoolean(FileConstants.PREFS_DUAL_PANE, isDualPaneEnabled);
        bundle.putBoolean(PREFS_FIRST_RUN, isFirstRun);
// TODO: 02/09/17 Where to set IS_FIRST_RUN to true??
        listener.passUserPrefs(bundle);
    }

    @Override
    public void getBillingStatus() {
        BillingHelper.getInstance().setupBilling(AceApplication.getAppContext(), this);
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Theme getTheme() {
        return Theme.getTheme(ThemeUtils.getTheme(context));
    }

    @Override
    public void getTotalGroupData() {
        DrawerItems drawerItems = new DrawerItems();
        drawerItems.getTotalGroupData(this);
    }




    @Override
    public void onBillingResult(BillingStatus billingStatus) {
        switch (billingStatus) {
            case UNSUPPORTED:
                listener.onBillingUnSupported();
                break;
            case FREE:
                listener.onFreeVersion();
                break;
            case PREMIUM:
                listener.onPremiumVersion();
                break;

        }
    }

    @Override
    public void onTotalGroupDataFetched(ArrayList<SectionGroup> totalData) {
        listener.onTotalGroupDataFetched(totalData);
    }
}
