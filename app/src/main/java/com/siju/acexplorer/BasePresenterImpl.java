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

package com.siju.acexplorer;

import android.content.Context;

import com.siju.acexplorer.filesystem.groups.DrawerItems;
import com.siju.acexplorer.model.SectionGroup;

import java.util.ArrayList;

public class BasePresenterImpl implements BasePresenter {
    private BaseView baseView;
    private Context context;
    private DrawerItems drawerItems;


    public BasePresenterImpl(Context context) {
        this.context = context;
        if (context instanceof BaseView) {
            this.baseView = (BaseView) context;
        }
        drawerItems = new DrawerItems(context);
    }


    @Override
    public ArrayList<SectionGroup> getTotalGroupData() {
        return drawerItems.getTotalGroupData();
    }
}
