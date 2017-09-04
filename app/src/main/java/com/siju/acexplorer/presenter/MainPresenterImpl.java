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

package com.siju.acexplorer.presenter;

import android.os.Bundle;

import com.siju.acexplorer.model.MainModel;
import com.siju.acexplorer.model.SectionGroup;
import com.siju.acexplorer.view.MainUi;

import java.util.ArrayList;

/**
 * Created by Siju on 02 September,2017
 */
public class MainPresenterImpl implements MainPresenter, MainUi.Listener, MainModel.Listener {

    private MainUi mainUi;
    private MainModel mainModel;

    public MainPresenterImpl(MainUi mainUi, MainModel mainModel) {
        this.mainUi = mainUi;
        this.mainModel = mainModel;
        mainUi.setListener(this);
        mainModel.setListener(this);
    }


    @Override
    public void setListener(Listener listener) {

    }

    @Override
    public void getUserPreferences() {
        mainModel.getUserSettings();
    }

    @Override
    public void getBillingStatus() {
       mainModel.getBillingStatus();
    }

    @Override
    public void passUserPrefs(Bundle userPrefs) {
        mainUi.passUserPrefs(userPrefs);
    }

    @Override
    public void onBillingUnSupported() {
        mainUi.onBillingUnSupported();
    }

    @Override
    public void onFreeVersion() {
       mainUi.onFreeVersion();
    }

    @Override
    public void onPremiumVersion() {
        mainUi.onPremiumVersion();
    }

    @Override
    public void onTotalGroupDataFetched(ArrayList<SectionGroup> totalData) {
        mainUi.onTotalGroupDataFetched(totalData);
    }

    @Override
    public void getTotalGroupData() {
        mainModel.getTotalGroupData();
    }
}
