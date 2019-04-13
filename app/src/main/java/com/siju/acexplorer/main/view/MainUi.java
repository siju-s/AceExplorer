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
import androidx.annotation.NonNull;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;

import com.siju.acexplorer.billing.BillingManager;
import com.siju.acexplorer.main.model.SectionGroup;

import java.util.ArrayList;

/**
 * Created by Siju on 02 September,2017
 */
public interface MainUi {


    void onIntentReceived(Intent intent);

    void setListener(Listener listener);

    void passUserPrefs(Bundle userPrefs);

    void onExit();

    void onPermissionResult(int requestCode, @NonNull String[] permissions,
                            @NonNull int[] grantResults);

    boolean handleActivityResult(int requestCode, int resultCode, Intent intent);

    void passActivityResult(int requestCode, int resultCode, Intent intent);

    void checkForPreferenceChanges();

    void onForeground();

    void onBillingUnSupported();

    void onFreeVersion();

    void onPremiumVersion();

    boolean onBackPressed();

    void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo);

    void onContextItemSelected(MenuItem item);

    void getTotalGroupData();

    void onTotalGroupDataFetched(ArrayList<SectionGroup> totalData);

    void init();

    void showDualFrame();

    void setDualPaneFocusState(boolean isDualPaneInFocus);

    void onConfigChanged(Configuration newConfig);

    void switchView(int viewMode, boolean isDual);

    void refreshList(boolean isDual);

    void onMultiWindowChanged(boolean isInMultiWindowMode, Configuration newConfig);

    void onSearchClicked();

    interface Listener {

        void getTotalGroupData();

        void onExit();

        BillingManager getBillingManager();
    }
}
