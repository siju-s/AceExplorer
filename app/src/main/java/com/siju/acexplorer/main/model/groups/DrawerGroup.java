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

package com.siju.acexplorer.main.model.groups;

import android.content.Context;

import com.siju.acexplorer.R;

public enum DrawerGroup {

    STORAGE(0),
    FAVORITES(1),
    LIBRARY(2),
    OTHERS(3),
    TOOLS(4);

    private final int value;

    DrawerGroup(int value) {
        this.value = value;
    }

    public int getValue() {

        return value;
    }

    public static DrawerGroup getGroupFromPos(int position) {
        switch (position) {
            case 0:
                return STORAGE;
            case 1:
                return FAVORITES;
            case 2:
                return LIBRARY;
            case 3:
                return TOOLS;
            case 4 :
                return OTHERS;
        }
        return STORAGE;
    }

    public static String getDrawerGroupName(Context context, DrawerGroup group) {
        switch (group) {
            case STORAGE:
                return context.getString(R.string.nav_header_storages);
            case FAVORITES:
                return context.getString(R.string.nav_header_favourites);
            case LIBRARY:
                return context.getString(R.string.nav_header_collections);
            case TOOLS:
                return context.getString(R.string.nav_header_tools);
        }
        return null;
    }


}
