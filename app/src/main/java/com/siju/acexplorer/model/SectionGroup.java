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

import java.util.ArrayList;

public class SectionGroup {
    private final String mHeader;
    private final ArrayList<SectionItems> mChildItems;

    public SectionGroup(String mHeader, ArrayList<SectionItems> mChildItems) {

        this.mHeader = mHeader;
        this.mChildItems = mChildItems;
    }

    public String getmHeader() {
        return mHeader;
    }

// --Commented out by Inspection START (06-11-2016 11:07 PM):
//    public void setmHeader(String mHeader) {
//        this.mHeader = mHeader;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:07 PM)

    public ArrayList<SectionItems> getmChildItems() {
        return mChildItems;
    }

// --Commented out by Inspection START (06-11-2016 11:07 PM):
//    public void setmChildItems(ArrayList<SectionItems> mChildItems) {
//        this.mChildItems = mChildItems;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:07 PM)


}
