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

package com.siju.acexplorer.home.model;

import android.support.v4.app.FragmentActivity;

import com.siju.acexplorer.billing.BillingManager;
import com.siju.acexplorer.billing.BillingStatus;
import com.siju.acexplorer.home.types.HomeLibraryInfo;

import java.util.List;

/**
 * Created by Siju on 02 September,2017
 */
public interface HomeModel {

    void getLibraries();

    void setListener(Listener listener);

    BillingStatus getBillingStatus();

    void reloadLibraries(List<LibrarySortModel> selectedLibs);

    boolean getDualModeState();

    void saveLibs(List<LibrarySortModel> librarySortModels);

    void setActivityContext(FragmentActivity activity);

    BillingManager getBillingManager();


    interface Listener {

        void onLibrariesFetched(List<HomeLibraryInfo> libraries);
    }
}
