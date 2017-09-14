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

package com.siju.acexplorer.model.helper.root.internal;

import com.siju.acexplorer.model.helper.root.containers.Mount;
import com.siju.acexplorer.model.helper.root.containers.Permissions;
import com.siju.acexplorer.model.helper.root.containers.Symlink;

import java.util.ArrayList;
import java.util.regex.Pattern;



public class InternalVariables
{

    // ----------------------
    // # Internal Variables #
    // ----------------------


    protected static boolean nativeToolsReady = false;
    protected static boolean found = false;
    protected static boolean processRunning = false;

    protected static String[] space;
    protected static String getSpaceFor;
    protected static String busyboxVersion;
    protected static String pid_list = "";
    protected static ArrayList<Mount> mounts;
    protected static ArrayList<Symlink> symlinks;
    protected static String inode = "";
    protected static Permissions permissions;

    // regex to get pid out of ps line, example:
    // root 2611 0.0 0.0 19408 2104 pts/2 S 13:41 0:00 bash
    protected static final String PS_REGEX = "^\\S+\\s+([0-9]+).*$";
    protected static Pattern psPattern;

    static
    {
        psPattern = Pattern.compile(PS_REGEX);
    }
}
