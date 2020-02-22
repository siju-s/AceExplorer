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

import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.main.model.helper.FileUtils.isFileNonWritable
import com.siju.acexplorer.main.model.helper.FileUtils.isWritable
import java.io.File

object OperationUtils {

    const val KEY_FILENAME = "filename"
    const val KEY_FILEPATH = "filepath"
    const val KEY_FILEPATH2 = "filepath2"
    const val KEY_OPERATION = "operation"
    const val KEY_FILES = "op_files"

    const val KEY_OLD_FILES = "old_op_files"
    const val KEY_CONFLICT_DATA = "conflict_data"
    const val ACTION_OP_REFRESH = "refresh"
    const val ACTION_OP_FAILED = "failed"
    const val KEY_RESULT = "result"
    const val KEY_FILES_COUNT = "files_count"

    const val KEY_END = "end"

    enum class WriteMode {
        ROOT,
        INTERNAL,
        EXTERNAL
    }


    fun getWriteMode(directory: String?): WriteMode {
        if (directory == null) {
            return WriteMode.ROOT
        }
        val folder = File(directory)
        return when {
            StorageUtils.isOnExtSdCard(folder) -> when {
                !folder.exists() || !folder.isDirectory -> WriteMode.ROOT
                isFileNonWritable(folder) -> WriteMode.EXTERNAL
                else -> WriteMode.INTERNAL
            }
            isWritable(File(folder, "DummyFile")) -> WriteMode.INTERNAL
            else -> WriteMode.ROOT
        }
    }

}
