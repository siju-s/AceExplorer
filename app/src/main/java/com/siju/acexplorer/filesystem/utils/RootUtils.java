package com.siju.acexplorer.filesystem.utils;

import android.content.Context;
import android.preference.PreferenceManager;

import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.helper.RootHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import eu.chainfire.libsuperuser.Shell;


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
    public static ArrayList<String> getDirListingSu(String str) throws RootNotPermittedException {
        ArrayList<String> arrayLis = RootHelper.runShellCommand(LS.replace("%", str));
        return arrayLis;
    }

    /**
     * Get a shell based listing
     * Context is an third-party context level shell
     *
     * @param str
     * @return
     */
    public static List<String> getDirListing(String str) throws RootNotPermittedException {
        return RootHelper.runNonRootShellCommand(LS.replace("%", str));
    }

    /**
     * Change permissions (owner/group/others) of a specified path
     *
     * @param path
     * @param octalNotation octal notation of permission
     * @throws RootNotPermittedException
     */
    public static void chmod(String path, int octalNotation) throws RootNotPermittedException {
        if (!Shell.SU.available()) throw new RootNotPermittedException();
        String command = "chmod %s %s";
        Object[] args = new Object[2];
        args[0] = octalNotation;
        args[1] = path;
        RootHelper.runShellCommand(String.format(command, args));
    }

    public static void mountRW(String path) throws RootNotPermittedException {
        if (!Shell.SU.available()) throw new RootNotPermittedException();
        RootHelper.runShellCommand("mount -o rw,remount " + path);
    }

    public static void mountRO(String path) throws RootNotPermittedException {
        if (!Shell.SU.available()) throw new RootNotPermittedException();
        RootHelper.runShellCommand("mount -o ro,remount " + path);
    }


    /**
     * Mount path for writable access (rw)
     *
     * @param path
     * @throws RootNotPermittedException
     */
    public static void mountOwnerRW(String path) throws RootNotPermittedException {
        if (!Shell.SU.available()) throw new RootNotPermittedException();
        chmod(path, 644);
    }

    /**
     * Mount path for readable access (ro)
     *
     * @param path
     * @throws RootNotPermittedException
     */
    public static void mountOwnerRO(String path) throws RootNotPermittedException {
        if (!Shell.SU.available()) throw new RootNotPermittedException();
        chmod(path, 444);
    }

    /**
     * @param source
     * @param destination
     * @throws RootNotPermittedException
     */
    public static void copy(String source, String destination) throws RootNotPermittedException {
        RootHelper.runShellCommand("cp " + source + " " + destination);
    }

    public static void mkDir(String path, String name) throws RootNotPermittedException {
        RootHelper.runShellCommand("mkdir " + path + "/" + name);
    }

    /**
     * Recursively removes a path with it's contents (if any)
     *
     * @param path
     * @throws RootNotPermittedException
     */
    public static void delete(String path) throws RootNotPermittedException {
        RootHelper.runShellCommand("rm -r " + path);
    }

    /**
     * @param path
     * @param destination
     * @throws RootNotPermittedException
     */
    public static void move(String path, String destination) throws RootNotPermittedException {
        RootHelper.runShellCommand("mv " + path + " " + destination);
    }

    public static void rename(String oldPath, String newPath) throws RootNotPermittedException {
        RootHelper.runShellCommand("mv " + oldPath + " " + newPath);
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
