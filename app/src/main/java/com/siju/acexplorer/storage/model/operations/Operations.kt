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

package com.siju.acexplorer.storage.model.operations


enum class Operations constructor(val value: Int) {

    CUT(0),
    COPY(1),
    FILE_CREATION(2),
    FOLDER_CREATION(3),
    RENAME(4),
    DELETE(5),
    EXTRACT(6),
    COMPRESS(7),
    HIDE(8),
    INFO(9),
    SHARE(10),
    PASTE(11),
    FAVORITE(12),
    DELETE_FAVORITE(13),
    PERMISSIONS(14),
    PICKER(15)

}
