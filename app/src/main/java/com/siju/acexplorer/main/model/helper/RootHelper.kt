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
@file:Suppress("RegExpRedundantEscape")
package com.siju.acexplorer.main.model.helper

import android.annotation.SuppressLint
import android.util.Log
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.logging.Logger.log
import com.siju.acexplorer.main.model.data.FileDataFetcher.Companion.getFilesList
import com.siju.acexplorer.main.model.groups.Category
import com.stericson.RootShell.exceptions.RootDeniedException
import com.stericson.RootShell.execution.Command
import com.stericson.RootTools.RootTools
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicReference

private const val TAG = "RootHelper"
private const val COMMAND_WAIT_MS = 2000
private const val UNIX_ESCAPE_EXPRESSION = "(\\(|\\)|\\[|\\]|\\s|\'|\"|`|\\{|\\}|&|\\\\|\\?)"

@SuppressLint("StaticFieldLeak")
object RootHelper {
    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)

    @Synchronized
    fun executeCommand(cmd: String): ArrayList<String> {
        Log.d(TAG, "executeCommand: $cmd")
        val list = ArrayList<String>()
        //        final CountDownLatch countDownLatch = new CountDownLatch(1);
        val resultRef = AtomicReference<ArrayList<String>>()
        val command: Command = object : Command(0, cmd) {
            override fun commandOutput(id: Int, line: String) {
                super.commandOutput(id, line)
                list.add(line)
                Log.d(TAG, "command commandOutput:$line")
            }

            override fun commandTerminated(id: Int, reason: String) {
                super.commandTerminated(id, reason)
                Log.d(TAG, "command terminated:$reason")
            }

            override fun commandCompleted(id: Int, exitcode: Int) {
                super.commandCompleted(id, exitcode)
                Log.d(TAG, "command commandCompleted:" + list.size)
                resultRef.set(list)
                //                countDownLatch.countDown();
            }
        }
        try {
            RootTools.getShell(true).add(command)
            waitForCommand(command,1)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: RootDeniedException) {
            e.printStackTrace()
        } catch (e: TimeoutException) {
            e.printStackTrace()
        }
        return resultRef.get()
    }

    fun runAndWait(cmd: String?) {
        val c: Command = object : Command(0, cmd) {
            override fun commandOutput(i: Int, s: String) {
                log(TAG, "commandOutput: i=$i s=$s")
            }

            override fun commandTerminated(i: Int, s: String) {
                log(TAG, "commandTerminated: i=$i s=$s")
            }

            override fun commandCompleted(i: Int, i2: Int) {
                log(TAG, "commandCompleted: i=$i i2=$i2")
            }
        }
        try {
            RootTools.getShell(true).add(c)
        } catch (e: Exception) {
            return
        }
        waitForCommand(c, -1)
    }

    private fun waitForCommand(command: Command, time: Long = -1L): Boolean {
        var t: Long = 0
        while (!command.isFinished) {
            synchronized(command) {
                try {
                    if (!command.isFinished) {
                        t += COMMAND_WAIT_MS
                        if (t != -1L && t >= time) {
                            return true
                        }
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            if (!command.isExecuting && !command.isFinished) {
                return false
            }
        }
        log(TAG, "Command Finished!$command")
        return true
    }

    fun getCommandLineString(input: String): String {
        return input.replace(UNIX_ESCAPE_EXPRESSION.toRegex(), "\\\\$1")
    }

    fun parseFilePermission(f: File): String {
        var per = ""
        if (f.canRead()) {
            per += "r"
        }
        if (f.canWrite()) {
            per += "w"
        }
        if (f.canExecute()) {
            per += "x"
        }
        return per
    }

    fun getRootedList(path: String, root: Boolean,
                      showHidden: Boolean): ArrayList<FileInfo> {
        val fileInfoArrayList = ArrayList<FileInfo>()
        var hidden = " "
        if (showHidden) {
            hidden = "a "
        }
        val list: ArrayList<String>
        val time = System.currentTimeMillis()
        Log.d(TAG, "getRootedList: time:$time")
        val rootAccessGiven = RootTools.isAccessGiven()
        val rooted = root || rootAccessGiven
        if (rooted) {
            list = executeCommand("ls -l" + hidden + getCommandLineString(path))
            val newTime = System.currentTimeMillis()
            Log.d(TAG, "getRootedList: time taken for ls:" + (newTime - time) + " list:" + list)
            for (i in list.indices) {
                val file1 = list[i]
                parseFileNew(path, file1, fileInfoArrayList)
            }
        }
        return fileInfoArrayList
    }

    fun fileExists(path: String?): Boolean {
        path ?: return true
        val file = File(path)
        val parent = file.parent
        if (parent != null && parent.isNotEmpty()) {
            val ls = getFilesList(parent, root = false, showHidden = true, ringtonePicker = false)
            for (strings in ls) {
                if (strings.filePath != null && strings.filePath == path) {
                    return true
                }
            }
        }
        return false
    }

    private fun parseFileNew(path: String, result: String, fileInfoArrayList: ArrayList<FileInfo>) {
        val array = result.trim { it <= ' ' }.split("\\s+").toTypedArray()
        val arrayLength = array.size
        Log.d(TAG, "parseFileNew: arrayLength:$arrayLength")
        if (array.size > 3) {
            val trimName: String
            val date: String
            val permission = array[0]
            val name: String
            var size = 0L
            val isDirectory = permission.startsWith("d")
            val isLink = permission.startsWith("l")
            when {
                isDirectory -> {
                    trimName = array[arrayLength - 1]
                    name = trimName
                    date = array[arrayLength - 3] + " " + array[arrayLength - 2]
                }
                isLink -> {
                    trimName = array[arrayLength - 3]
                    name = trimName
                    date = array[arrayLength - 5] + " " + array[arrayLength - 4]
                }
                else -> {
                    trimName = array[arrayLength - 1]
                    name = trimName
                    size = java.lang.Long.valueOf(array[arrayLength - 4])
                    date = array[arrayLength - 3] + " " + array[arrayLength - 2]
                }
            }
            val lastModified = getTimeInMillis(date)
            val filePath = fixSlashes(path + File.separator + name)
            fileInfoArrayList.add(FileInfo(Category.FILES, name, filePath,
                    lastModified, size, isDirectory, null,
                    permission, true))
        }
    }

    private fun getTimeInMillis(date: String): Long {
        var timeInMillis = 0L
        try {
            val dateTime = simpleDateFormat.parse(date)
            dateTime?.let {
                timeInMillis = it.time
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return timeInMillis
    }

    // Removes duplicate adjacent slashes and any trailing slash.
    private fun fixSlashes(origPath: String): String {
        var lastWasSlash = false
        val newPath = origPath.toCharArray()
        val length = newPath.size
        var newLength = 0
        for (i in 0 until length) {
            val ch = newPath[i]
            if (ch == '/') {
                if (!lastWasSlash) {
                    newPath[newLength++] = File.separatorChar
                    lastWasSlash = true
                }
            } else {
                newPath[newLength++] = ch
                lastWasSlash = false
            }
        }
        // Remove any trailing slash (unless this is the root of the file system).
        if (lastWasSlash && newLength > 1) {
            newLength--
        }
        return if (newLength != length) String(newPath, 0, newLength) else origPath
    }
}