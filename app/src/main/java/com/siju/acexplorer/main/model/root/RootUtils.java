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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.siju.acexplorer.main.model.StorageUtils;
import com.siju.acexplorer.main.model.helper.RootHelper;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static com.siju.acexplorer.settings.SettingsPreferenceFragment.PREF_ROOT;


@SuppressWarnings("unused")
public class RootUtils {
    public static final String DATA_APP_DIR = "/data/app";
    private static final String LS = "ls -lAnH \"%\" --color=never";
    private static final String LSDIR = "ls -land \"%\" --color=never";
    public static final String SYSTEM_APP_DIR = "/system/app";
    private static final String PREFS_ROOTED = "is_rooted";
    private static final Pattern mLsPattern;

    static {
        mLsPattern = Pattern.compile(".[rwxsStT-]{9}\\s+.*");
    }

    public static boolean isRooted(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREF_ROOT, false);
    }

    public static void setRooted(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(PREFS_ROOTED, true).apply();
    }

    public static boolean isRootDir(String path, ArrayList<String> externalSdList) {
        if (path == null) {
            return false;
        }
        boolean isPathOnExt = false;
        for (String extSD : externalSdList) {
            if (path.startsWith(extSD)) {
                isPathOnExt = true;
                break;
            }
        }
        return !path.startsWith(StorageUtils.INSTANCE.getInternalStorage()) && !isPathOnExt;
    }

    public static boolean hasRootAccess() {
        try {
            return RootTools.isAccessGiven();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValid(String str) {
        return mLsPattern.matcher(str).matches();
    }

    public static boolean isUnixVirtualDirectory(String str) {
        return str.startsWith("/proc") || str.startsWith("/sys");
    }

    private static boolean isRWMounted() {
        File mountFile = new File("/proc/mounts");
        StringBuilder procData = new StringBuilder();
        if (mountFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(mountFile.toString());
                DataInputStream dis = new DataInputStream(fis);
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        dis));
                String data;
                while ((data = br.readLine()) != null) {
                    procData.append(data).append("\n");
                }

                br.close();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            String[] tmp = procData.toString().split("\n");
            for (String aTmp : tmp) {
                // Kept simple here on purpose different devices have
                // different blocks
                if (aTmp.contains("/dev/block")
                        && aTmp.contains("/system")) {
                    if (aTmp.contains("rw")) {
                        // system is rw
                        return true;
                    } else if (aTmp.contains("ro")) {
                        // system is ro
                        return false;
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Change permissions (owner/group/others) of a specified path
     */
    public static void chmod(String path, int octalNotation) {
        String command = "chmod ";
        RootHelper.executeCommand(octalNotation + " " + RootHelper.getCommandLineString(path));
    }

    public static void mountRW(String path) {
        if (!isRWMounted()) {
            Log.e("RootUtils", "mountRW() called with: path = [" + path + "]");
            RootTools.remount(path, "RW");
        }
    }

    public static boolean fileExists(String path, boolean isDir) {
        return RootTools.exists(path, isDir);
    }

    public static void copy(String source, String destination) {
        RootHelper.executeCommand("cp -fr " + RootHelper.getCommandLineString(source) + " " +
                RootHelper.getCommandLineString(destination));
    }

    public static void mkDir(String path) {
        String parentPath = new File(path).getParent();
        RootUtils.mountRW(parentPath);
        RootHelper.runAndWait("mkdir " + RootHelper.getCommandLineString(path));
    }

    public static void mkFile(String path) {
        Log.e("RootUtils", "mkFile: " + path);
        String parentPath = new File(path).getParent();
        RootUtils.mountRW(parentPath);
        RootHelper.runAndWait("touch " + RootHelper.getCommandLineString(path));
    }

    public static void delete(String path) {
        RootUtils.mountRW(path);
        if (new File(path).isDirectory()) {
            RootHelper.executeCommand("rm -f -r " + RootHelper.getCommandLineString(path));
        } else {
            RootHelper.executeCommand("rm -r " + RootHelper.getCommandLineString(path));
        }
    }

    public static void move(String source, String destination) {
        RootHelper.executeCommand("mv " + RootHelper.getCommandLineString(source) + " " +
                RootHelper.getCommandLineString(destination));
    }
}
