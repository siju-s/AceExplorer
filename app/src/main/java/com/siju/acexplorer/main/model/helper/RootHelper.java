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

import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.main.model.data.FileDataFetcher;
import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static com.siju.acexplorer.main.model.groups.Category.FILES;

public class RootHelper {

    private static final String TAG = "RootHelper";

    public static synchronized ArrayList<String> executeCommand(String cmd) {
        Log.e(TAG, "executeCommand: "+cmd);
        final ArrayList<String> list = new ArrayList<>();
//        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final AtomicReference<ArrayList<String>> resultRef = new AtomicReference<>();
        Command command = new Command(0, cmd) {
            @Override
            public void commandOutput(int id, String line) {
                super.commandOutput(id, line);
                list.add(line);
                Log.e(TAG, "command commandOutput:"+line);
            }

            @Override
            public void commandTerminated(int id, String reason) {
                super.commandTerminated(id, reason);
                Log.e(TAG, "command terminated:"+reason);
            }

            @Override
            public void commandCompleted(int id, int exitcode) {
                super.commandCompleted(id, exitcode);
                Log.e(TAG, "command commandCompleted:"+list.size());
                resultRef.set(list);
//                countDownLatch.countDown();
            }
        };
        try {
            RootTools.getShell(true).add(command);
            waitForCommand(command, -1);
//            countDownLatch.await();
        } catch (IOException | RootDeniedException | TimeoutException e) {
            e.printStackTrace();
        }
        return resultRef.get();
    }

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

        Logger.log(TAG, "Command Finished!"+cmd);
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
        ArrayList<String> list;
        long time = System.currentTimeMillis();
        Log.e(TAG, "getRootedList: time:" + time);
        boolean rootAccessGiven = RootTools.isAccessGiven();
        boolean rooted = root || rootAccessGiven;
        if (rooted) {
            list = executeCommand("ls -l " + getCommandLineString(path));
            long newTime = System.currentTimeMillis();
            Log.e(TAG, "getRootedList: time taken for ls:" + (newTime - time) +" list:"+list);
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    String file1 = list.get(i);
                    parseFileNew(path, file1, fileInfoArrayList);
                }
            }
        }
        return fileInfoArrayList;
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


    private static SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);

    private static void parseFileNew(String path, String result, ArrayList<FileInfo> fileInfoArrayList) {
        String[] array = result.trim().split("\\s+");
        int arrayLength = array.length;
        Log.e(TAG, "parseFileNew: arrayLength:" + arrayLength);
        if (array.length > 3) {
            String trimName;
            String date;
            String permission = array[0];
            String name;
            long size = 0L;

            boolean isDirectory = permission.startsWith("d");
            boolean isLink = permission.startsWith("l");

            if (isDirectory) {
                trimName = array[arrayLength - 1];
                name = trimName;
                date = array[arrayLength - 3] + " " + array[arrayLength - 2];
            } else if (isLink) {
                trimName = array[arrayLength - 3];
                name = trimName;
                date = array[arrayLength - 5] + " " + array[arrayLength - 4];
            } else {
                trimName = array[arrayLength - 1];
                name = trimName;
                size = Long.valueOf(array[arrayLength - 4]);
                date = array[arrayLength - 3] + " " + array[arrayLength - 2];
            }
            long lastModified = getTimeinMillis(date);
            String filePath = fixSlashes(path + File.separator + name);
            fileInfoArrayList.add(new FileInfo(FILES, name, filePath,
                    lastModified, size, isDirectory, null,
                    permission, true));
        }
    }

    private static long getTimeinMillis(String date) {
        long timeInMillis = 0;
        try {
            timeInMillis = simpledateformat.parse(date).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timeInMillis;
    }

    // Removes duplicate adjacent slashes and any trailing slash.
    private static String fixSlashes(String origPath) {
        boolean lastWasSlash = false;
        char[] newPath = origPath.toCharArray();
        int length = newPath.length;
        int newLength = 0;
        for (int i = 0; i < length; ++i) {
            char ch = newPath[i];
            if (ch == '/') {
                if (!lastWasSlash) {
                    newPath[newLength++] = File.separatorChar;
                    lastWasSlash = true;
                }
            } else {
                newPath[newLength++] = ch;
                lastWasSlash = false;
            }
        }
        // Remove any trailing slash (unless this is the root of the file system).
        if (lastWasSlash && newLength > 1) {
            newLength--;
        }
        return (newLength != length) ? new String(newPath, 0, newLength) : origPath;
    }

}
