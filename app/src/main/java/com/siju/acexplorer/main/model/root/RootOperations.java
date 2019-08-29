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


import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.File;

public class RootOperations {


    public static void renameRoot(File sourceFile, String newFileName) throws RootDeniedException {
        String destinationPath = sourceFile.getParent() + File.separator + newFileName;
        RootUtils.mountRW(sourceFile.getPath());
        RootUtils.move(sourceFile.getPath(), destinationPath);
    }

    public static boolean fileExists(String path, boolean isDir) {
        return RootTools.exists(path, isDir);
    }

    public static boolean setPermissions(String path, boolean isDir, String permissions) {

        String command = "chmod " + permissions + " " + path;
        if (isDir) {
            command = "chmod -R " + permissions + " \"" + path + "\"";
        }
        Command com = new Command(1, command);
        try {
            RootUtils.mountRW(path);
            RootTools.getShell(true).add(com);
            return true;
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return false;
    }


}
