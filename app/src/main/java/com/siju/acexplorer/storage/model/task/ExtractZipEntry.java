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


import android.content.Context;
import android.os.AsyncTask;

import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.main.model.helper.FileUtils;
import com.siju.acexplorer.main.view.dialog.DialogHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.siju.acexplorer.main.model.helper.ViewHelper.viewFile;


public class ExtractZipEntry extends AsyncTask<Void, Void, Void> {
    private final String outputDir;
    private ZipFile zipFile;
    private final String fileName;
    private final boolean zip;
    private ZipEntry entry;
    private File output;
    private DialogHelper.AlertDialogListener alertDialogListener;
    private Context context;

    public ExtractZipEntry(Context context, ZipFile zipFile, String outputDir, String fileName,
                           ZipEntry zipEntry, DialogHelper.AlertDialogListener alertDialogListener) {
        this.context = context;
        this.zip = true;
        this.outputDir = outputDir;
        this.zipFile = zipFile;
        this.fileName = fileName;
        this.entry = zipEntry;
        this.alertDialogListener = alertDialogListener;
    }


    @Override
    protected Void doInBackground(Void... zipEntries) {

        try {
            if (zip) {
                unzipEntry(zipFile, entry, outputDir);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        String outputPath = output.getPath();
        String extension = FileUtils.getExtension(outputPath);
        viewFile(context, outputPath, extension, alertDialogListener, true);
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
}