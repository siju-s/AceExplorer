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

import com.stericson.RootShell.execution.Command
import com.stericson.RootTools.RootTools
import java.io.File

object RootOperations {
    @Throws(RootDeniedException::class)
    fun renameRoot(sourceFile: File, newFileName: String) {
        val parent = sourceFile.parent
        parent?.let {
            val destinationPath = it + File.separator + newFileName
            RootUtils.mountRW(sourceFile.path)
            RootUtils.move(sourceFile.path, destinationPath)
        }
    }

    fun fileExists(path: String?, isDir: Boolean): Boolean {
        return RootTools.exists(path, isDir)
    }

    fun setPermissions(path: String, isDir: Boolean, permissions: String): Boolean {
        var command = "chmod $permissions $path"
        if (isDir) {
            command = "chmod -R $permissions \"$path\""
        }
        val com = Command(1, command)
        try {
            RootUtils.mountRW(path)
            RootTools.getShell(true).add(com)
            return true
        } catch (e1: Exception) {
            e1.printStackTrace()
        }
        return false
    }
}