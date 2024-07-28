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

package com.siju.acexplorer.common

import java.security.InvalidParameterException

enum class ViewMode(val value : Int, val resourceId : Int) {

    LIST(0, R.string.action_view_list),
    GRID(1, R.string.action_view_grid),
    GALLERY(2, R.string.action_view_gallery);

    fun getValue(viewMode: ViewMode) = viewMode.value

    companion object {
        fun getViewModeFromValue(value: Int): ViewMode {
            return when (value) {
                0    -> LIST
                1    -> GRID
                2    -> GALLERY
                else -> throw InvalidParameterException(
                        "View mode value should be either ${LIST.value}, ${GRID.value} or" +
                                "${GALLERY.value}")
            }
        }
    }
}
