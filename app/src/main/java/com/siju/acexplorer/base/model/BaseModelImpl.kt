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
package com.siju.acexplorer.base.model

import android.content.Context
import com.siju.acexplorer.AceApplication.Companion.appContext
import com.siju.acexplorer.theme.Theme
import com.siju.acexplorer.theme.Theme.Companion.getTheme

class BaseModelImpl : BaseModel {
    private val context: Context
    override val theme: Theme
        get() = getTheme(context)

    init {
        context = appContext
    }
}