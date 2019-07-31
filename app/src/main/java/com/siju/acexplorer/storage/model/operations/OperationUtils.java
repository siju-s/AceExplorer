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

package com.siju.acexplorer.storage.model.operations;

import com.siju.acexplorer.main.model.StorageUtils;

import java.io.File;

import static com.siju.acexplorer.main.model.helper.FileUtils.isFileNonWritable;
import static com.siju.acexplorer.main.model.helper.FileUtils.isWritable;

public class OperationUtils {

    public static final String ACTION_SAF         = "ACTION_SAF";
    public static final String KEY_FILENAME       = "filename";
    public static final String KEY_FILEPATH       = "filepath";
    public static final String KEY_FILEPATH2      = "filepath2";
    public static final String KEY_OPERATION      = "operation";
    public static final String KEY_FILES          = "op_files";
    public static final String KEY_MEDIA_INDEX_FILES          = "mediastore_files";

    public static final String KEY_OLD_FILES      = "old_op_files";
    public static final String KEY_POSITION       = "pos";
    public static final String KEY_CONFLICT_DATA  = "conflict_data";
    public static final String ACTION_OP_REFRESH  = "refresh";
    public static final String ACTION_RELOAD_LIST = "reload";
    public static final String ACTION_OP_FAILED   = "failed";
    public static final String KEY_RESULT         = "result";
    public static final String KEY_FILES_COUNT    = "files_count";

    public static final String KEY_END            = "end";

    public static final String KEY_MOVE           = "move";
    public static final String KEY_COUNT          = "count";
    public static final String KEY_SHOW_RESULT    = "show_result";
    public static final String KEY_IS_TRASH       = "trash";
    public static final String KEY_IS_RESTORE       = "restore";

    public static final String KEY_TRASH_DATA        = "trash_data";


    public enum WriteMode {
        ROOT,
        INTERNAL,
        EXTERNAL
    }


    public static WriteMode getWriteMode(final String directory) {
        if (directory == null) {
            return WriteMode.ROOT;
        }
        File folder = new File(directory);
        if (StorageUtils.INSTANCE.isOnExtSdCard(folder)) {
            if (!folder.exists() || !folder.isDirectory()) {
                return WriteMode.ROOT;
            }

            // On Android 5 and above, trigger storage access framework.
            if (isFileNonWritable(folder)) {
                return WriteMode.EXTERNAL;
            }
            return WriteMode.INTERNAL;
        } else if (isWritable(new File(folder, "DummyFile"))) {
            return WriteMode.INTERNAL;
        } else {
            return WriteMode.ROOT;
        }
    }

}
