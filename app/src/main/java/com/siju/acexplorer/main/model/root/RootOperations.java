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

package com.siju.acexplorer.main.model.root;


import java.io.File;

public class RootOperations {


    public static void renameRoot(File sourceFile, String newFileName) throws RootDeniedException {
        String destinationPath = sourceFile.getParent() + File.separator + newFileName;
        RootUtils.mountRW(sourceFile.getPath());
        RootUtils.move(sourceFile.getPath(), destinationPath);
        RootUtils.mountRO(sourceFile.getPath());
    }

    public static boolean fileExists(String path, boolean isDir) throws RootDeniedException {
        RootUtils.mountRW(path);
        boolean exists = RootUtils.fileExists(path, isDir);
        RootUtils.mountRO(path);
        return exists;
    }
}
