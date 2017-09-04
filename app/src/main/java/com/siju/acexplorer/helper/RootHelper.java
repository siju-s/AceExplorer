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

package com.siju.acexplorer.helper;

import android.util.Log;

import com.siju.acexplorer.filesystem.model.BaseFile;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.root.RootDeniedException;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Command;

import java.io.File;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.siju.acexplorer.filesystem.FileListLoader.getFilesList;
import static com.siju.acexplorer.model.groups.Category.FILES;

/*import com.siju.acexplorer.helper.root.RootTools;
import com.siju.acexplorer.helper.root.rootshell.execution.Command;*/

public class RootHelper {

    /**
     * Runs the command and stores output in a list. The listener is set on the caller thread,
     * thus any code run in callback must be thread safe.
     * Command is run from the root context (u:r:SuperSU0)
     *
     * @param cmd the command
     * @return a list of results. Null only if the command passed is a blocking call or no output is
     * there for the command passed
     * @throws RootDeniedException
     */
   /* public static ArrayList<String> runShellCommand(String cmd) throws RootDeniedException {
        if (!Shell.SU.available()) throw new RootDeniedException();
        final ArrayList<String> result = new ArrayList<>();

        // setting STDOUT listener so as to avoid extra buffer and possible memory loss by superuser
        AceActivity.shellInteractive.addCommand(cmd, 0, new Shell.OnCommandResultListener() {
            @Override
            public void onCommandResult(int commandCode, int exitCode, List<String> output) {

                for (String line : output) {
                    result.add(line);
                }
            }
        });
        AceActivity.shellInteractive.waitForIdle();
        return result;
    }

    *//**
     * Runs the command and stores output in a list. The listener is set on the caller thread,
     * thus any code run in callback must be thread safe.
     * Command is run from superuser context (u:r:SuperSU0)
     *
     * @param cmd      the command
     * @param callback
     * @return a list of results. Null only if the command passed is a blocking call or no output is
     * there for the command passed
     * @throws RootDeniedException
     *//*
    public static void runShellCommand(String cmd, Shell.OnCommandResultListener callback)
            throws RootDeniedException {
        if (!Shell.SU.available()) throw new RootDeniedException();
        AceActivity.shellInteractive.addCommand(cmd, 0, callback);
    }

    *//**
     * Runs the command and stores output in a list. The listener is set on the caller thread,
     * thus any code run in callback must be thread safe.
     * Command is run from a third-party level context (u:r:init_shell0)
     * Not callback supported as the shell is not interactive
     *
     * @param cmd the command
     * @return a list of results. Null only if the command passed is a blocking call or no output is
     * there for the command passed
     * @throws RootDeniedException
     *//*
    public static List<String> runNonRootShellCommand(String cmd) {
        return Shell.SH.run(cmd);
    }*/

     private static final String TAG = "RootHelper";
    public static void runAndWait(String cmd) {

        Command c = new Command(0, cmd) {
            @Override
            public void commandOutput(int i, String s) {
                Log.d(TAG, "commandOutput: i="+i+ " s="+s);

            }

            @Override
            public void commandTerminated(int i, String s) {
                Log.d(TAG, "commandTerminated: i="+i+ " s="+s);
            }

            @Override
            public void commandCompleted(int i, int i2) {
                Log.d(TAG, "commandCompleted: i="+i+ " i2="+i2);
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
                        if (t != -1 && t >= time)
                            return true;

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

    public static String getCommandLineString(String input) {
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

        boolean rootAccessGiven = RootTools.isAccessGiven();
        boolean rooted = root || rootAccessGiven;
        if (rooted) {
            String cpath = getCommandLineString(path);
            ls = runAndWait1("ls -l" + hidden + cpath, true);
            if (ls != null) {
                for (int i = 0; i < ls.size(); i++) {
                    String file1 = ls.get(i);
                    if (!file1.contains("Permission denied"))
                        try {
                            BaseFile array = parseName(file1);
                            if (array != null) {
                                String name = array.getPath();
                                String path1;

                                if (!path.equals("/")) {
                                    path1 = path + "/" + name;
                                } else {
                                    path1 = "/" + name;
                                }
                                boolean isDirectory;
                                if (array.getLink().trim().length() > 0) {
                                    isDirectory = isDirectory(array.getLink(), true, 0);
                                } else isDirectory = isDirectory(array);
                                long size1 = array.getSize();
                                fileInfoArrayList.add(new FileInfo(FILES, name, path1,
                                        array.getDate(), size1, isDirectory, null,
                                        array.getPermission(), true));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
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
            ArrayList<FileInfo> ls = getFilesList(p, true, true, false);
            for (FileInfo strings : ls) {
                if (strings.getFilePath() != null && strings.getFilePath().equals(path)) {
                    return true;
                }
            }
        }
        return false;
    }


    private static BaseFile parseName(String line) {
        boolean linked = false;
        String name = "", link = "", size = "-1", date = "";
        String[] array = line.split(" ");
        if (array.length < 6) return null;
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
                name = name + " " + array[i];
            }
            name = name.trim();
        } else {
            int q = getLinkPosition(array);
            for (int i = p + 1; i < q; i++) {
                name = name + " " + array[i];
            }
            name = name.trim();
            for (int i = q + 1; i < array.length; i++) {
                link = link + " " + array[i];
            }
        }
        long Size = (size == null || size.trim().length() == 0) ? -1 : Long.parseLong(size);
        if (date.trim().length() > 0) {
            ParsePosition pos = new ParsePosition(0);
            SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd | HH:mm", Locale.getDefault());
            Date stringDate = simpledateformat.parse(date, pos);
            BaseFile baseFile = new BaseFile(name, array[0], stringDate.getTime(), Size);
            baseFile.setLink(link);
            return baseFile;
        } else {
            BaseFile baseFile = new BaseFile(name, array[0], new File("/").lastModified(), Size);
            baseFile.setLink(link);
            return baseFile;
        }

    }

    private static int getLinkPosition(String[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].contains("->")) return i;
        }
        return 0;
    }

    private static int getColonPosition(String[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].contains(":")) return i;
        }
        return -1;
    }

    public static boolean isDirectory(String filePath, boolean root, int count) {
        File file = new File(filePath);
        String name = file.getName();
        String parentFile = file.getParent();
        if (parentFile != null && parentFile.length() > 1) {
            ArrayList<String> ls = runAndWait1("ls -la " + parentFile, root);
            if (ls == null) return file.isDirectory();
            for (String s : ls) {
                if (contains(s.split(" "), name)) {
                    try {
                        BaseFile path = parseName(s);
                        if (path == null) return file.isDirectory();
                        if (path.getPermission().trim().startsWith("d")) return true;
                        else if (path.getPermission().trim().startsWith("l")) {
                            if (count > 5)
                                return file.isDirectory();
                            else
                                return isDirectory(path.getLink().trim(), root, ++count);
                        } else return file.isDirectory();
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
            Log.e("checking", s);
            if (s.equals(name)) return true;
        }
        return false;
    }

}
