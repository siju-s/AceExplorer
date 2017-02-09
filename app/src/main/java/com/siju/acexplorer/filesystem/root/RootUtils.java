package com.siju.acexplorer.filesystem.root;

import android.content.Context;
import android.preference.PreferenceManager;

import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.helper.RootHelper;
import com.stericson.RootTools.RootTools;

import java.util.regex.Pattern;


public class RootUtils {
    public static final String DATA_APP_DIR = "/data/app";
    private static final String LS = "ls -lAnH \"%\" --color=never";
    private static final String LSDIR = "ls -land \"%\" --color=never";
    public static final String SYSTEM_APP_DIR = "/system/app";
    private static final Pattern mLsPattern;

    static {
        mLsPattern = Pattern.compile(".[rwxsStT-]{9}\\s+.*");
    }

    public static boolean isRooted(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(FileConstants.PREFS_ROOTED,
                false);
    }


    public static boolean isValid(String str) {
        return mLsPattern.matcher(str).matches();
    }

    public static boolean isUnixVirtualDirectory(String str) {
        return str.startsWith("/proc") || str.startsWith("/sys");
    }

    /**
     * Get a shell based listing
     * Context is superuser level shell
     *
     * @param str
     * @return
     */
/*    public static ArrayList<String> getDirListingSu(String str) throws RootDeniedException {
//        ArrayList<String> arrayLis = RootHelper.runAndWait(LS.replace("%", str));
        return arrayLis;
    }*/

    /**
     * Get a shell based listing
     * Context is an third-party context level shell
     *
     * @param str
     * @return
     */
/*
    public static List<String> getDirListing(String str) throws RootDeniedException {
//        return RootHelper.runNonRootShellCommand(LS.replace("%", str));
    }
*/

    /**
     * Change permissions (owner/group/others) of a specified path
     *
     * @param path
     * @param octalNotation octal notation of permission
     * @throws RootDeniedException
     */
    public static void chmod(String path, int octalNotation) throws RootDeniedException {
        if (!RootTools.isAccessGiven()) throw new RootDeniedException();
        String command = "chmod %s %s";
        Object[] args = new Object[2];
        args[0] = octalNotation;
        args[1] = path;
        RootHelper.runAndWait(String.format(command, args));
    }

    public static void mountRW(String path) throws RootDeniedException {
        if (!RootTools.isAccessGiven()) throw new RootDeniedException();
        String str = "mount -o %s,remount %s";
        String mountPoint = "/";
        if (path.startsWith("/system")) {
            mountPoint = "/system";
        }
        Object[] objArr = new Object[2];
        objArr[0] = "rw";
        objArr[1] = mountPoint;
        String cmd = String.format(str, objArr);
        Logger.log("RootUtils", "Command=" + cmd);
        RootHelper.runAndWait(cmd);
    }

    public static void mountRO(String path) throws RootDeniedException {
        if (!RootTools.isAccessGiven()) throw new RootDeniedException();
        String str = "mount -o %s,remount %s";
        String mountPoint = "/";
        if (path.startsWith("/system")) {
            mountPoint = "/system";
        }
        Object[] objArr = new Object[2];
        objArr[0] = "ro";
        objArr[1] = mountPoint;

        String cmd = String.format(str, objArr);
        Logger.log("RootUtils", "Command=" + cmd);
        RootHelper.runAndWait(cmd);

    }

/*
    *//**
     * Mount path for writable access (rw)
     *
     * @param path
     * @throws RootDeniedException
     *//*
    public static void mountOwnerRW(String path) throws RootDeniedException {
        if (!Shell.SU.available()) throw new RootDeniedException();
        chmod(path, 644);
    }

    *//**
     * Mount path for readable access (ro)
     *
     * @param path
     * @throws RootDeniedException
     *//*
    public static void mountOwnerRO(String path) throws RootDeniedException {
        if (!Shell.SU.available()) throw new RootDeniedException();
        chmod(path, 444);
    }*/

    /**
     * @param source
     * @param destination
     * @throws RootDeniedException
     */
    public static void copy(String source, String destination) throws RootDeniedException {
        RootHelper.runAndWait("cp " + source + " " + destination);
    }

    public static void mkDir(String path) throws RootDeniedException {
        RootHelper.runAndWait("mkdir " + path);
    }

    public static void mkFile(String path) throws RootDeniedException {
        RootHelper.runAndWait("touch " + path);
    }

    /**
     * Recursively removes a path with it's contents (if any)
     *
     * @param path
     * @throws RootDeniedException
     */
    public static void delete(String path) throws RootDeniedException {
        RootHelper.runAndWait("rm -r " + path);
    }

    /**
     * @param path
     * @param destination
     * @throws RootDeniedException
     */
    public static void move(String path, String destination) throws RootDeniedException {
        RootHelper.runAndWait("mv " + path + " " + destination);
    }

    public static void rename(String oldPath, String newPath) throws RootDeniedException {
        RootHelper.runAndWait("mv " + oldPath + " " + newPath);
    }

    public static String parsePermission(String permLine) {
        int owner = 0;
        int READ = 4;
        int WRITE = 2;
        int EXECUTE = 1;
        if (permLine.charAt(1) == 'r') {
            owner += READ;
        }
        if (permLine.charAt(2) == 'w') {
            owner += WRITE;
        }
        if (permLine.charAt(3) == 'x') {
            owner += EXECUTE;
        }
        int group = 0;
        if (permLine.charAt(4) == 'r') {
            group += READ;
        }
        if (permLine.charAt(5) == 'w') {
            group += WRITE;
        }
        if (permLine.charAt(6) == 'x') {
            group += EXECUTE;
        }
        int world = 0;
        if (permLine.charAt(7) == 'r') {
            world += READ;
        }
        if (permLine.charAt(8) == 'w') {
            world += WRITE;
        }
        if (permLine.charAt(9) == 'x') {
            world += EXECUTE;
        }
        String finalValue = owner + "" + group + "" + world;
        return finalValue;
    }
}