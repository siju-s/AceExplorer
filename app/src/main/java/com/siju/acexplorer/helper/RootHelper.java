package com.siju.acexplorer.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.filesystem.model.BaseFile;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Command;

import java.io.File;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RootHelper {

    public static void runAndWait(String cmd, boolean root) {

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
            return;
        }

        waitForCommand(c, -1);

    }

    private static ArrayList<String> runAndWait1(String cmd, final boolean root) {
        final ArrayList<String> output = new ArrayList<>();
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

    @SuppressLint("SdCardPath")
    public static ArrayList<FileInfo> getFilesList(String path, boolean root,
                                                   boolean showHidden, boolean isRingtonePicker) {
        ArrayList<FileInfo> fileInfoArrayList = new ArrayList<>();
        File file = new File(path);
//        Logger.log("RootHelper", "Starting time FILES=");
        if (file.canRead()) {
            File[] listFiles = file.listFiles();

            if (listFiles != null) {
                for (File file1 : listFiles) {
                    String filePath = file1.getAbsolutePath();
                    boolean isDirectory = false;
//                    String noOfFilesOrSize = null;
                    long size;
                    String extension = null;
                    int type = 0;


                    // Dont show hidden files by default
                    if (file1.isHidden() && !showHidden) {
                        continue;
                    }
                    if (file1.isDirectory()) {

                        isDirectory = true;
                        int childFileListSize = 0;
                        String[] list = file1.list();
                        if (list != null) {
                            childFileListSize = list.length;
                        }
                        // Saves us 200 ms by avoiding filtering of hidden files
                      /*  if (childFileListSize == 0) {
                            noOfFilesOrSize = context.getResources().getString(R.string.empty);
                        } else {
                            noOfFilesOrSize = context.getResources().getQuantityString(R.plurals.number_of_files,
                                    childFileListSize, childFileListSize);
                        }*/
                        size = childFileListSize;
//                        }
                    } else {
                        size = file1.length();
//                        noOfFilesOrSize = Formatter.formatFileSize(context, size);
                        extension = filePath.substring(filePath.lastIndexOf(".") + 1);
                        type = checkMimeType(extension);
                        if (isRingtonePicker && !FileUtils.isFileMusic(filePath)) {
                            continue;
                        }
                    }

                    long date = file1.lastModified();
//                    String fileModifiedDate = convertDate(date);

                    FileInfo fileInfo = new FileInfo(file1.getName(), filePath, date, size,
                            isDirectory, extension, type, parseFilePermission(file1));
                    fileInfoArrayList.add(fileInfo);
                }
//                Logger.log("RootHelper", "END time FILES=");

            }
        } else {
            String p = " ";
            if (showHidden) p = "a ";
            ArrayList<String> ls;
            if (root) {
//            if (!path.startsWith("/storage") && !path.startsWith("/sdcard")) {
                String cpath = getCommandLineString(path);
                ls = runAndWait1("ls -l" + p + cpath, true);
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
//                                    Logger.log("RootHelper", "Path==" + path);

//                                    Logger.log("RootHelper", "path1==" + path1);
//                                    array.setName(array.getPath());
//                                    array.setPath();
                                    boolean isDirectory;
                                    if (array.getLink().trim().length() > 0) {
                                        isDirectory = isDirectory(array.getLink(), true, 0);
                                    } else isDirectory = isDirectory(array);
                                    long size1 = array.getSize();
                                    fileInfoArrayList.add(new FileInfo(name, path1,
                                            array.getDate(),
                                            size1, isDirectory, null,
                                            FileConstants.CATEGORY
                                                    .FILES.getValue(), array.getPermission()));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }
            }
//            }

        }
        return fileInfoArrayList;

    }

    private final ArrayList<FileInfo> fileInfoArrayList = new ArrayList<>();

    @SuppressLint("SdCardPath")
    public ArrayList<FileInfo> getFilesListRecursively(Context context, String path, boolean root) {

        File file = new File(path);
//        Logger.log("RootHelper", "Starting time FILES=");

        if (file.canRead() && (path.startsWith("/storage") || path.startsWith("/sdcard"))) {
            File[] listFiles = file.listFiles();

            if (listFiles != null) {
                for (File file1 : listFiles) {
                    String filePath = file1.getAbsolutePath();
                    boolean isDirectory = false;
//                    String noOfFilesOrSize = null;
                    long size;
                    String extension = null;
                    int type = 0;

                    if (file1.isDirectory()) {

                        isDirectory = true;
                        int childFileListSize = 0;
                        String[] list = file1.list();
                        if (list != null) {
                            childFileListSize = list.length;
                        }
                        getFilesListRecursively(context, filePath, true);
                        // Saves us 200 ms by avoiding filtering of hidden files
                      /*  if (childFileListSize == 0) {
                            noOfFilesOrSize = context.getResources().getString(R.string.empty);
                        } else {
                            noOfFilesOrSize = context.getResources().getQuantityString(R.plurals.number_of_files,
                                    childFileListSize, childFileListSize);
                        }*/
                        size = childFileListSize;
//                        }
                    } else {
                        size = file1.length();
//                        noOfFilesOrSize = Formatter.formatFileSize(context, size);
                        extension = filePath.substring(filePath.lastIndexOf(".") + 1);
                        type = checkMimeType(extension);
                    }
                    long date = file1.lastModified();
//                    String fileModifiedDate = convertDate(date);

                    FileInfo fileInfo = new FileInfo(file1.getName(), filePath, date, size,
                            isDirectory, extension, type, parseFilePermission(file1));
                    fileInfoArrayList.add(fileInfo);
//                    Logger.log("RootHelper", "fileInfoArrayList element="+fileInfo.getFilePath());

                }
//                Logger.log("RootHelper", "END fileInfoArrayList size="+fileInfoArrayList.size());

            }
        } else {
            String p;
            p = "a ";
            ArrayList<String> ls;
            if (root) {
//            if (!path.startsWith("/storage") && !path.startsWith("/sdcard")) {
                String cpath = getCommandLineString(path);
                ls = runAndWait1("ls -l" + p + cpath, true);
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
//                                    Logger.log("RootHelper", "Path==" + path);

//                                    Logger.log("RootHelper", "path1==" + path1);
//                                    array.setName(array.getPath());
//                                    array.setPath();
                                    boolean isDirectory;
                                    if (array.getLink().trim().length() > 0) {
                                        isDirectory = isDirectory(array.getLink(), true, 0);
//                                        array.setDirectory(isdirectory);
                                    } else isDirectory = isDirectory(array);
                                    long size1 = array.getSize();
                                    fileInfoArrayList.add(new FileInfo(name, path1,
                                            array.getDate(),
                                            size1, isDirectory, null,
                                            FileConstants.CATEGORY
                                                    .FILES.getValue(), array.getPermission()));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }
            }
//            }

        }
        return fileInfoArrayList;

    }


    private static int checkMimeType(String extension) {
//        String mimeType = URLConnection.guessContentTypeFromName(path);

        int value = 0;
        if (extension == null) return value;
        extension = extension.toLowerCase(); // necessary
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        if (mimeType != null) {
            if (mimeType.indexOf("image") == 0) {
                value = FileConstants.CATEGORY.IMAGE.getValue();
            } else if (mimeType.indexOf("video") == 0) {
                value = FileConstants.CATEGORY.VIDEO.getValue();
            } else if (mimeType.indexOf("audio") == 0) {
                value = FileConstants.CATEGORY.AUDIO.getValue();
            }
        }
        return value;
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
