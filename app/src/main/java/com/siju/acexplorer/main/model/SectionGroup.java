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

import com.siju.acexplorer.main.model.groups.DrawerGroup;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class SectionGroup {
    private String                  mHeader;
    private ArrayList<SectionItems> mChildItems;
    private DrawerGroup             groups;


    public SectionGroup(ArrayList<SectionItems> mChildItems, DrawerGroup drawerGroup) {
        this.groups = drawerGroup;
        this.mChildItems = mChildItems;
    }

    public DrawerGroup getGroups() {
        return groups;
    }

    public void setGroups(DrawerGroup groups) {
        this.groups = groups;
    }

    public String getmHeader() {
        return mHeader;
    }

    public void setmHeader(String mHeader) {
        this.mHeader = mHeader;
    }

    public ArrayList<SectionItems> getmChildItems() {
        return mChildItems;
    }

    public void setmChildItems(ArrayList<SectionItems> mChildItems) {
        this.mChildItems = mChildItems;
    }


}
