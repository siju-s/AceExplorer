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

import java.util.*

object PermissionsHelper {
    fun parse(permLine: String?): ArrayList<Array<Boolean>> {
        val arrayList = ArrayList<Array<Boolean>>()
        if (permLine == null) {
            return arrayList
        }
        val read = arrayOf(false, false, false)
        val write = arrayOf(false, false, false)
        val execute = arrayOf(false, false, false)
        if (permLine[1] == 'r') {
            read[0] = true
        }
        if (permLine[2] == 'w') {
            write[0] = true
        }
        if (permLine[3] == 'x') {
            execute[0] = true
        }
        if (permLine[4] == 'r') {
            read[1] = true
        }
        if (permLine[5] == 'w') {
            write[1] = true
        }
        if (permLine[6] == 'x') {
            execute[1] = true
        }
        if (permLine[7] == 'r') {
            read[2] = true
        }
        if (permLine[8] == 'w') {
            write[2] = true
        }
        if (permLine[9] == 'x') {
            execute[2] = true
        }
        arrayList.add(read)
        arrayList.add(write)
        arrayList.add(execute)
        return arrayList
    }
}