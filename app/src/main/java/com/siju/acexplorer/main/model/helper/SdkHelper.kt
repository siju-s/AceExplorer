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

package com.siju.acexplorer.main.model.helper


import android.os.Build

object SdkHelper {

    val isAtleastAndroid10: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    val isAtleastOreo: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    val isAtleastNougat: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

    val isAtleastMarsh: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    val isAtleastPie: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    val isAtleastAndroid11: Boolean
        get() = Build.VERSION.SDK_INT >= 30

    val isAtleastAPI33: Boolean
        get() = Build.VERSION.SDK_INT >= 33

    val isAtleastAndroid15: Boolean
        get() = Build.VERSION.SDK_INT >= 35

}
