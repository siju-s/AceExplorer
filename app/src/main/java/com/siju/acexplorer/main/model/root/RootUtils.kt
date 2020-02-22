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
package com.siju.acexplorer.main.model.root

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import com.siju.acexplorer.main.model.StorageUtils.internalStorage
import com.siju.acexplorer.main.model.helper.RootHelper.executeCommand
import com.siju.acexplorer.main.model.helper.RootHelper.getCommandLineString
import com.siju.acexplorer.main.model.helper.RootHelper.runAndWait
import com.siju.acexplorer.settings.SettingsPreferenceFragment
import com.stericson.RootTools.RootTools
import java.io.*
import java.util.*

object RootUtils {

    fun isRooted(context: Context?): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getBoolean(SettingsPreferenceFragment.PREF_ROOT, false)
    }

    fun isRootDir(path: String?, externalSdList: ArrayList<String>): Boolean {
        if (path == null) {
            return false
        }
        var isPathOnExt = false
        for (extSD in externalSdList) {
            if (path.startsWith(extSD)) {
                isPathOnExt = true
                break
            }
        }
        return !path.startsWith(internalStorage) && !isPathOnExt
    }

    fun hasRootAccess(): Boolean {
        return try {
            RootTools.isAccessGiven()
        } catch (e: Exception) {
            false
        }
    }

    // Kept simple here on purpose different devices have
// different blocks
    private fun isRWMounted(): Boolean {
        val mountFile = File("/proc/mounts")
        val procData = StringBuilder()
        if (mountFile.exists()) {
            try {
                val fis = FileInputStream(mountFile.toString())
                val dis = DataInputStream(fis)
                val br = BufferedReader(InputStreamReader(
                        dis))
                var data: String?
                while (br.readLine().also { data = it } != null) {
                    procData.append(data).append("\n")
                }
                br.close()
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
            val tmp = procData.toString().split("\n").toTypedArray()
            for (aTmp in tmp) { // Kept simple here on purpose different devices have
// different blocks
                if (aTmp.contains("/dev/block")
                        && aTmp.contains("/system")) {
                    return when {
                        aTmp.contains("rw") -> { // system is rw
                            true
                        }
                        aTmp.contains("ro") -> { // system is ro
                            false
                        }
                        else -> {
                            false
                        }
                    }
                }
            }
        }
        return false
    }

    fun mountRW(path: String?) {
        if (!isRWMounted()) {
            Log.d("RootUtils", "mountRW() called with: path = [$path]")
            RootTools.remount(path, "RW")
        }
    }

    fun copy(source: String?, destination: String?) {
        executeCommand("cp -fr " + getCommandLineString(source!!) + " " +
                getCommandLineString(destination!!))
    }

    fun mkDir(path: String?) {
        path ?: return
        val parentPath = File(path).parent
        mountRW(parentPath)
        runAndWait("mkdir " + getCommandLineString(path))
    }

    fun mkFile(path: String?) {
        Log.d("RootUtils", "mkFile: $path")
        path ?: return
        val parentPath = File(path).parent
        mountRW(parentPath)
        runAndWait("touch " + getCommandLineString(path))
    }

    fun delete(path: String) {
        mountRW(path)
        if (File(path).isDirectory) {
            executeCommand("rm -f -r " + getCommandLineString(path))
        } else {
            executeCommand("rm -r " + getCommandLineString(path))
        }
    }

    fun move(source: String?, destination: String?) {
        executeCommand("mv " + getCommandLineString(source!!) + " " +
                getCommandLineString(destination!!))
    }


}