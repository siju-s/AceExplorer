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

package com.siju.acexplorer.storage.model.task


import com.siju.acexplorer.main.model.helper.FileUtils
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

private const val BUFFER_SIZE_BYTES = 20480

class ExtractZipEntry {

    @Throws(IOException::class)
    fun unzipEntry(zipFile: ZipFile, entry: ZipEntry, outputDir: String, fileName: String,
                   zipFileViewCallback: ZipFileViewCallback) {
        val output = File(outputDir, fileName)
        if (entry.isDirectory) {
            File(outputDir, fileName).mkdir()
        }
        else {
            writeToFile(zipFile, entry, output)
        }
        val extension = FileUtils.getExtension(output.absolutePath)
        zipFileViewCallback.openZipFile(output.absolutePath, extension)
    }

    private fun writeToFile(zipfile: ZipFile, entry: ZipEntry,
                            output: File) {
        val inputStream = BufferedInputStream(zipfile.getInputStream(entry))
        val outputStream = BufferedOutputStream(FileOutputStream(output))

        try {
            val buffer = ByteArray(BUFFER_SIZE_BYTES)
            var len = inputStream.read(buffer)
            while (len > 0) {
                outputStream.write(buffer, 0, len)
                len = inputStream.read(buffer)
            }
        }
        finally {
            try {
                inputStream.close()
            }
            catch (e: IOException) {
                //closing quietly
            }

            try {
                outputStream.close()
            }
            catch (e: IOException) {
                //closing quietly
            }
        }
    }

    interface ZipFileViewCallback {
        fun openZipFile(outputDir: String, extension: String)
    }
}