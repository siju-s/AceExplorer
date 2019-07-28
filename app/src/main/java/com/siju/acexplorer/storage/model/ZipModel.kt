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

import java.util.zip.ZipEntry

class ZipModel(val entry: ZipEntry?, date: Long, size: Long, directory: Boolean) {

    val isDirectory: Boolean = directory
    var name: String? = null
        private set
    var time: Long = 0
        private set
    var size: Long = 0
        private set

    init {
        entry?.let {
            name = entry.name
            this.time = date
            this.size = size
        }
    }


}
