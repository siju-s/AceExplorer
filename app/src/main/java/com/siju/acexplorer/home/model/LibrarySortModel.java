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

import android.os.Parcel;
import android.os.Parcelable;

public class LibrarySortModel implements Parcelable {
    private boolean  isChecked;
    private int categoryId;

    public LibrarySortModel(int categoryId) {
        this.categoryId = categoryId;
        this.isChecked = true;
    }

    public LibrarySortModel() {

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LibrarySortModel)) {
            return false;
        }
        LibrarySortModel o = (LibrarySortModel) obj;
        return o.categoryId == (this.categoryId);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }


    protected LibrarySortModel(Parcel in) {
        categoryId = in.readInt();
        isChecked = in.readByte() != 0;
    }

    public static final Creator<LibrarySortModel> CREATOR = new Creator<LibrarySortModel>() {
        @Override
        public LibrarySortModel createFromParcel(Parcel in) {
            return new LibrarySortModel(in);
        }

        @Override
        public LibrarySortModel[] newArray(int size) {
            return new LibrarySortModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(categoryId);
        dest.writeByte((byte) (isChecked ? 1 : 0));
    }
}
