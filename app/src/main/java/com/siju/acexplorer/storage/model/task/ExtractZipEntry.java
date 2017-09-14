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

package com.siju.acexplorer.storage.model.task;


import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.helper.RootHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.siju.acexplorer.model.helper.helper.ViewHelper.viewFile;

public class ExtractZipEntry extends AsyncTask<Void, Void, Void> {
    private final String outputDir;
    private ZipFile zipFile;
    private final Fragment fragment;
    private final String fileName;
    private final boolean zip;
    private ZipEntry entry;
    private Archive rar;
    private FileHeader header;
    private File output;

    public ExtractZipEntry(ZipFile zipFile, String outputDir, Fragment fragment, String fileName,
                           ZipEntry zipEntry) {
        this.zip = true;
        this.outputDir = outputDir;
        this.zipFile = zipFile;
        this.fragment = fragment;
        this.fileName = fileName;
        this.entry = zipEntry;
    }

    public ExtractZipEntry(Archive rar, String outputDir, Fragment fragment, String fileName,
                           FileHeader fileHeader) {
        this.zip = false;
        this.outputDir = outputDir;
        this.rar = rar;
        this.fragment = fragment;
        this.fileName = fileName;
        this.header = fileHeader;
    }

    @Override
    protected Void doInBackground(Void... zipEntries) {

        try {
            if (zip) unzipEntry(zipFile, entry, outputDir);
            else unzipRAREntry(rar, header, outputDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        String cmd = "chmod 777 " + output.getPath();
        Log.d("change permissions", cmd);
        RootHelper.runAndWait(cmd);
        String outputPath = output.getPath();
        String extension = outputPath.substring(outputPath.lastIndexOf(".") + 1, outputPath.length());
        viewFile(fragment, outputPath, extension);

    }

    private void unzipEntry(ZipFile zipfile, ZipEntry entry, String outputDir)
            throws IOException {

        output = new File(outputDir, fileName);
        if (entry.isDirectory()) {
            //noinspection ResultOfMethodCallIgnored
            new File(outputDir, fileName).mkdir();
            return;
        }
        BufferedInputStream inputStream = new BufferedInputStream(
                zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(
                new FileOutputStream(output));
        Logger.log("Extract", "zipfile=" + zipfile.getName() + " zipentry=" + entry + " stream=" + inputStream);
        Logger.log("Extract", "Bytes START=" + inputStream.available());

        try {
            int len;
            byte buf[] = new byte[20480];
            while ((len = inputStream.read(buf)) > 0) {
                //System.out.println(id + " " + hash.get(id));
                outputStream.write(buf, 0, len);
            }
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                //closing quietly
            }

            try {
                outputStream.close();
            } catch (IOException e) {
                //closing quietly
            }

        }
    }

    private void unzipRAREntry(Archive zipfile, FileHeader header, String outputDir)
            throws IOException, RarException {

        output = new File(outputDir + "/" + header.getFileNameString().trim());
        FileOutputStream fileOutputStream = new FileOutputStream(output);
        zipfile.extractFile(header, fileOutputStream);
    }

}