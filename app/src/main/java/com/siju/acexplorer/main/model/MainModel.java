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

package com.siju.acexplorer.main.model;

import android.os.Bundle;

import com.siju.acexplorer.billing.BillingManager;
import com.siju.acexplorer.theme.Theme;

import java.util.ArrayList;

/**
 * Created by Siju on 02 September,2017
 */
public interface MainModel {

    void getUserSettings();

    void setupBilling();

    void setListener(Listener listener);

    Theme getTheme();

    void getTotalGroupData();

    void cleanup();

    BillingManager getBillingManager();

    interface Listener {
        void passUserPrefs(Bundle userPrefs);

        void onBillingUnSupported();

        void onFreeVersion();

        void onPremiumVersion();

        void onTotalGroupDataFetched(ArrayList<SectionGroup> totalData);
    }
}
