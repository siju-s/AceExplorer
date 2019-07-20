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

package com.siju.acexplorer.storage.model

import android.os.Parcel
import android.os.Parcelable
import com.siju.acexplorer.main.model.helper.FileUtils

class PasteActionInfo(val filePath: String?, val action : Int = FileUtils.ACTION_KEEP) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readInt())


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PasteActionInfo

        if (filePath != other.filePath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filePath.hashCode()
        result = 31 * result + action
        return result
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(filePath)
        parcel.writeInt(action)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PasteActionInfo> {
        override fun createFromParcel(parcel: Parcel): PasteActionInfo {
            return PasteActionInfo(parcel)
        }

        override fun newArray(size: Int): Array<PasteActionInfo?> {
            return arrayOfNulls(size)
        }
    }
}
