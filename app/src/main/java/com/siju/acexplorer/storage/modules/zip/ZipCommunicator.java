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

package com.siju.acexplorer.storage.modules.zip;

import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.groups.Category;

import java.util.ArrayList;


public interface ZipCommunicator {

    void removeZipScrollPos(String newPath);

    void endZipMode();

    void calculateZipScroll(String dir);

    void onZipContentsLoaded(ArrayList<FileInfo> data);

    void openZipViewer(String currentDir);

    void setNavDirectory(String path, boolean isHomeScreenEnabled, Category category);

    void addToBackStack(String path, Category category);

    void removeFromBackStack();

    void setInitialDir(String path);
}
