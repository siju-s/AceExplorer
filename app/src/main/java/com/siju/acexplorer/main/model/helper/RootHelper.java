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

package com.siju.acexplorer.main.model.helper;

import android.util.Log;

import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.main.model.data.FileDataFetcher;
import com.siju.acexplorer.storage.model.BaseFile;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.siju.acexplorer.main.model.groups.Category.FILES;

public class RootHelper {

    private static final String TAG = "RootHelper";

    public static void runAndWait(String cmd) {

        Command c = new Command(0, cmd) {
            @Override
            public void commandOutput(int i, String s) {
                Logger.log(TAG, "commandOutput: i=" + i + " s=" + s);

            }

            @Override
            public void commandTerminated(int i, String s) {
                Logger.log(TAG, "commandTerminated: i=" + i + " s=" + s);
            }

            @Override
            public void commandCompleted(int i, int i2) {
                Logger.log(TAG, "commandCompleted: i=" + i + " i2=" + i2);
            }
        };
        try {
            RootTools.getShell(true).add(c);
        } catch (Exception e) {
            return;
        }

        waitForCommand(c, -1);

    }

    private static ArrayList<String> runAndWait1(String cmd, final boolean root) {
        final ArrayList<String> output = new ArrayList<>();
        Command cc = new Command(1, cmd) {
            @Override
            public void commandOutput(int i, String s) {
                System.out.println("commandOutput" + i + s);

                output.add(s);
                System.out.println("output " + output);

            }

            @Override
            public void commandTerminated(int i, String s) {

                System.out.println("error" + root + s);

            }

            @Override
            public void commandCompleted(int i, int i2) {
                System.out.println("commandCompleted" + i + i2);

            }
        };
        try {
            RootTools.getShell(root).add(cc);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (!waitForCommand(cc, -1)) {
            return null;
        }
        return output;
    }

    private static boolean waitForCommand(Command cmd, long time) {
        long t = 0;
        while (!cmd.isFinished()) {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (cmd) {
                try {
                    if (!cmd.isFinished()) {
                        cmd.wait(2000);
                        t += 2000;
                        if (t != -1 && t >= time) {
                            return true;
                        }

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (!cmd.isExecuting() && !cmd.isFinished()) {
                return false;
            }
        }

        //Logger.debug("Command Finished!");
        return true;
    }

    private static String getCommandLineString(String input) {
        return input.replaceAll(UNIX_ESCAPE_EXPRESSION, "\\\\$1");
    }

    private static final String UNIX_ESCAPE_EXPRESSION = "(\\(|\\)|\\[|\\]|\\s|\'|\"|`|\\{|\\}|&|\\\\|\\?)";


    public static String parseFilePermission(File f) {
        String per = "";
        if (f.canRead()) {
            per = per + "r";
        }
        if (f.canWrite()) {
            per = per + "w";
        }
        if (f.canExecute()) {
            per = per + "x";
        }
        return per;
    }


    public static ArrayList<FileInfo> getRootedList(String path, boolean root,
                                                    boolean showHidden) {
        ArrayList<FileInfo> fileInfoArrayList = new ArrayList<>();

        String hidden = " ";
        if (showHidden) {
            hidden = "a ";
        }
        ArrayList<String> ls;
        long time = System.currentTimeMillis();
        Log.e(TAG, "getRootedList: time:"+time);
        boolean rootAccessGiven = RootTools.isAccessGiven();
        boolean rooted = root || rootAccessGiven;
        if (rooted) {
            String cpath = getCommandLineString(path);
            ls = runAndWait1("ls -l" + hidden + cpath, true);
            long newTime = System.currentTimeMillis();
            Log.e(TAG, "getRootedList: time taken for ls:"+(newTime - time));
            if (ls != null) {
                for (int i = 0; i < ls.size(); i++) {
                    String file1 = ls.get(i);
                    if (!file1.contains("Permission denied")) {
                        try {
                            BaseFile baseFile = parseFile(file1);
                            Log.e(TAG, "getRootedList: parse time taken:"+(System.currentTimeMillis() - newTime));
                            if (baseFile != null) {
                                String name = baseFile.getPath();
                                String path1;

                                if (!path.equals("/")) {
                                    path1 = path + "/" + name;
                                } else {
                                    path1 = "/" + name;
                                }
                                boolean isDirectory;
                                if (baseFile.getLink().trim().length() > 0) {
                                    isDirectory = isDirectory(baseFile.getLink(), true, 0);
                                } else {
                                    isDirectory = isDirectory(baseFile);
                                }
                                long size1 = baseFile.getSize();
                                fileInfoArrayList.add(new FileInfo(FILES, name, path1,
                                                                   baseFile.getDate(), size1, isDirectory, null,
                                                                   baseFile.getPermission(), true));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return fileInfoArrayList;
    }

    public static String getPermissions(String filePath, boolean isDirectory) {
        final String cmdPath = getCommandLineString(filePath);
        String directory = " ";
        if (isDirectory) {
            directory = "d ";
        }
        final String[] permissionString = {null};
        final String finalDirectory = directory;

        ArrayList<String> contents = runAndWait1("ls -l" + finalDirectory + cmdPath, true);
        System.out.println("getPermissions " + contents);
        if (contents != null && contents.size() == 1) {
            permissionString[0] = getPermissionFile(contents.get(0));

        }
        return permissionString[0];
    }


    private static String getPermissionFile(String line) {
        String[] array = line.split(" ");
        return array[0];
    }


    public static boolean fileExists(String path) {
        File f = new File(path);
        String p = f.getParent();
        if (p != null && p.length() > 0) {
            ArrayList<FileInfo> ls = FileDataFetcher.Companion.getFilesList(p, false, true, false);
            for (FileInfo strings : ls) {
                if (strings.getFilePath() != null && strings.getFilePath().equals(path)) {
                    return true;
                }
            }
        }
        return false;
    }


    private static BaseFile parseFile(String line) {
        boolean linked = false;
        StringBuilder name = new StringBuilder();
        StringBuilder link = new StringBuilder();
        String size = "-1";
        String date = "";
        String[] array = line.split(" ");
        if (array.length < 6) {
            return null;
        }
        for (String anArray : array) {
            if (anArray.contains("->") && array[0].startsWith("l")) {
                linked = true;
            }
        }
        int p = getColonPosition(array);
        if (p != -1) {
            date = array[p - 1] + " | " + array[p];
            size = array[p - 2];
        }
        if (!linked) {
            for (int i = p + 1; i < array.length; i++) {
                name.append(" ").append(array[i]);
            }
            name = new StringBuilder(name.toString().trim());
        } else {
            int q = getLinkPosition(array);
            for (int i = p + 1; i < q; i++) {
                name.append(" ").append(array[i]);
            }
            name = new StringBuilder(name.toString().trim());
            for (int i = q + 1; i < array.length; i++) {
                link.append(" ").append(array[i]);
            }
        }
        long Size = (size == null || size.trim().length() == 0) ? -1 : Long.parseLong(size);
        if (date.trim().length() > 0) {
            ParsePosition pos = new ParsePosition(0);
            SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd | HH:mm", Locale.getDefault());
            Date stringDate = simpledateformat.parse(date, pos);
            BaseFile baseFile = new BaseFile(name.toString(), array[0], stringDate.getTime(), Size);
            baseFile.setLink(link.toString());
            return baseFile;
        } else {
            BaseFile baseFile = new BaseFile(name.toString(), array[0], new File("/").lastModified(), Size);
            baseFile.setLink(link.toString());
            return baseFile;
        }

    }

    private static int getLinkPosition(String[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].contains("->")) {
                return i;
            }
        }
        return 0;
    }

    private static int getColonPosition(String[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].contains(":")) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isDirectory(String filePath, boolean root, int count) {
        File file = new File(filePath);
        String name = file.getName();
        String parentFile = file.getParent();
        if (parentFile != null && parentFile.length() > 1) {
            ArrayList<String> ls = runAndWait1("ls -la " + parentFile, root);
            if (ls == null) {
                return file.isDirectory();
            }
            for (String s : ls) {
                if (contains(s.split(" "), name)) {
                    try {
                        BaseFile path = parseFile(s);
                        if (path == null) {
                            return file.isDirectory();
                        }
                        if (path.getPermission().trim().startsWith("d")) {
                            return true;
                        } else if (path.getPermission().trim().startsWith("l")) {
                            if (count > 5) {
                                return file.isDirectory();
                            } else {
                                return isDirectory(path.getLink().trim(), root, ++count);
                            }
                        } else {
                            return file.isDirectory();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }

        }
        return file.isDirectory();
    }

    private static boolean isDirectory(BaseFile path) {
        return path.getPermission().startsWith("d") || new File(path.getPath()).isDirectory();
    }


    private static boolean contains(String[] a, String name) {
        for (String s : a) {
            if (s.equals(name)) {
                return true;
            }
        }
        return false;
    }

}
