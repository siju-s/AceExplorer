package com.siju.acexplorer.helper;

import android.content.Context;
import android.text.format.Formatter;
import android.util.Log;

import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.filesystem.model.BaseFile;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Command;

import java.io.File;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Siju on 03-08-2016.
 */
public class RootHelper {

    public static String runAndWait(String cmd, boolean root) {

        Command c = new Command(0, cmd) {
            @Override
            public void commandOutput(int i, String s) {

            }

            @Override
            public void commandTerminated(int i, String s) {

            }

            @Override
            public void commandCompleted(int i, int i2) {

            }
        };
        try {
            RootTools.getShell(root).add(c);
        } catch (Exception e) {
            return null;
        }

        if (!waitForCommand(c, -1)) {
            return null;
        }

        return c.toString();
    }

    public static ArrayList<String> runAndWait1(String cmd, final boolean root) {
        final ArrayList<String> output = new ArrayList<String>();
        Command cc = new Command(1, cmd) {
            @Override
            public void commandOutput(int i, String s) {
                output.add(s);
            }

            @Override
            public void commandTerminated(int i, String s) {

                System.out.println("error" + root + s);

            }

            @Override
            public void commandCompleted(int i, int i2) {

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

    public static ArrayList<FileInfo> getFilesList(Context context, String path, boolean root,
                                                   boolean
                                                           showHidden) {
        ArrayList<FileInfo> fileInfoArrayList = new ArrayList<>();
        String p = " ";
        int mode = 0;
        if (showHidden) p = "a ";
        ArrayList<BaseFile> a = new ArrayList<>();
        ArrayList<String> ls = new ArrayList<>();
        if (root) {
//            if (!path.startsWith("/storage") && !path.startsWith("/sdcard")) {
            String cpath = getCommandLineString(path);
            ls = runAndWait1("ls -l" + p + cpath, root);
            if (ls != null) {
                for (int i = 0; i < ls.size(); i++) {
                    String file = ls.get(i);
                    if (!file.contains("Permission denied"))
                        try {
                            BaseFile array = parseName(file);
                            if (array != null) {
                                String name = array.getPath();
                                String path1 = path + "/" + array.getPath();
//                                    array.setName(array.getPath());
//                                    array.setPath();
                                boolean isDirectory;
                                if (array.getLink().trim().length() > 0) {
                                    isDirectory = isDirectory(array.getLink(), root, 0);
//                                        array.setDirectory(isdirectory);
                                } else isDirectory = isDirectory(array);
                                long size1 = array.getSize();
                                String size;
                                if (size1 != -1) {
                                    size = Formatter.formatFileSize(context, array.getSize());
                                } else {
                                    size = null;
                                }
                                fileInfoArrayList.add(new FileInfo(name, path1,
                                        convertDate(array.getDate()),
                                        size, isDirectory, null,
                                        FileConstants.CATEGORY
                                        .FILES.getValue()));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                }
                return fileInfoArrayList;

            }
//            }

        }
        return null;

    }

    public static String convertDate(long dateInMs) {
        SimpleDateFormat df2 = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
        String dateText = df2.format(dateInMs);
        return dateText;
    }


    public static BaseFile parseName(String line) {
        boolean linked = false;
        String name = "", link = "", size = "-1", date = "";
        String[] array = line.split(" ");
        if (array.length < 6) return null;
        for (int i = 0; i < array.length; i++) {
            if (array[i].contains("->") && array[0].startsWith("l")) {
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
            SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd | HH:mm");
            Date stringDate = simpledateformat.parse(date, pos);
            BaseFile baseFile = new BaseFile(name, array[0], stringDate.getTime(), Size, true);
            baseFile.setLink(link);
            return baseFile;
        } else {
            BaseFile baseFile = new BaseFile(name, array[0], new File("/").lastModified(), Size, true);
            baseFile.setLink(link);
            return baseFile;
        }

    }

    public static int getLinkPosition(String[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].contains("->")) return i;
        }
        return 0;
    }

    public static int getColonPosition(String[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].contains(":")) return i;
        }
        return -1;
    }

    public static boolean isDirectory(String a, boolean root, int count) {
        File f = new File(a);
        String name = f.getName();
        String p = f.getParent();
        if (p != null && p.length() > 1) {
            ArrayList<String> ls = runAndWait1("ls -la " + p, root, 2000);
            for (String s : ls) {
                if (contains(s.split(" "), name)) {
                    try {
                        BaseFile path = parseName(s);
                        if (path.getPermisson().trim().startsWith("d")) return true;
                        else if (path.getPermisson().trim().startsWith("l")) {
                            if (count > 5)
                                return f.isDirectory();
                            else
                                return isDirectory(path.getLink().trim(), root, ++count);
                        } else return f.isDirectory();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }

        }
        return f.isDirectory();
    }

    static boolean isDirectory(BaseFile path) {
        if (path.getPermisson().startsWith("d")) return true;
        else return new File(path.getPath()).isDirectory();
    }

    public static ArrayList<String> runAndWait1(String cmd, final boolean root, final long time) {
        final ArrayList<String> output = new ArrayList<String>();
        Command cc = new Command(1, cmd) {
            @Override
            public void commandOutput(int i, String s) {
                output.add(s);
            }

            @Override
            public void commandTerminated(int i, String s) {

                System.out.println("error" + root + s + time);

            }

            @Override
            public void commandCompleted(int i, int i2) {

            }
        };
        try {
            RootTools.getShell(root).add(cc);
        } catch (Exception e) {
            //       Logger.errorST("Exception when trying to run shell command", e);
            e.printStackTrace();
            return null;
        }

        if (!waitForCommand(cc, time)) {
            return null;
        }

        return output;
    }

    static boolean contains(String[] a, String name) {
        for (String s : a) {
            Log.e("checking", s);
            if (s.equals(name)) return true;
        }
        return false;
    }

}
