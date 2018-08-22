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

package com.siju.acexplorer.storage.view;


import android.os.Bundle;

import com.siju.acexplorer.main.model.FileConstants;
import com.siju.acexplorer.main.model.groups.Category;

public class DualPaneList extends BaseFileList {

    public static DualPaneList newInstance(String path, Category category, boolean isDualMode) {
        Bundle args = new Bundle();
        args.putString(FileConstants.KEY_PATH, path);
        args.putSerializable(FileConstants.KEY_CATEGORY, category);
        args.putBoolean(FileConstants.KEY_DUAL_ENABLED, isDualMode);
        DualPaneList fileList = new DualPaneList();
        fileList.setArguments(args);
        return fileList;
    }
}
