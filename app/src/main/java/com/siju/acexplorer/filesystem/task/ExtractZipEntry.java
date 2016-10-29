package com.siju.acexplorer.filesystem.task;


import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.helper.RootHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ExtractZipEntry extends AsyncTask<Void, Void, Void> {
    private String outputDir;
    private ZipFile zipFile;
    private Fragment fragment;
    private String fileName;
    boolean zip;
    ZipEntry entry;
    Archive rar;
    FileHeader header;
    File output;

    public ExtractZipEntry(ZipFile zipFile, String outputDir, Fragment fragment, String fileName, boolean zip,
                           ZipEntry zipEntry) {
        this.zip = zip;
        this.outputDir = outputDir ; //FileUtils.getInternalStorage().getAbsolutePath() + "/" + "Testing";
        this.zipFile = zipFile;
        this.fragment = fragment;
        this.fileName = fileName;
        this.entry = zipEntry;
    }

    public ExtractZipEntry(Archive rar, String outputDir, Fragment fragment, String fileName, boolean zip,
                           FileHeader fileHeader) {
        this.zip = zip;
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
        RootHelper.runAndWait(cmd, false);
        String outputPath = output.getPath();
        String extension = outputPath.substring(outputPath.lastIndexOf(".") + 1, outputPath.length());
        FileUtils.viewFile(fragment, outputPath, extension);

    }

    private void unzipEntry(ZipFile zipfile, ZipEntry entry, String outputDir)
            throws IOException {

        output = new File(outputDir, fileName);
        BufferedInputStream inputStream = new BufferedInputStream(
                zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(
                new FileOutputStream(output));
        Logger.log("Extract", "zipfile=" + zipfile + " zipentry=" + entry + " stream=" + inputStream);
        Logger.log("Extract", "Bytes START=" + inputStream.available());


        try {
            int len;
            byte buf[] = new byte[20480];
            while ((len = inputStream.read(buf)) > 0) {
                //System.out.println(id + " " + hash.get(id));
                outputStream.write(buf, 0, len);
            }
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }

    private void unzipRAREntry(Archive zipfile, FileHeader header, String outputDir)
            throws IOException, RarException {

        output = new File(outputDir + "/" + header.getFileNameString().trim());
        FileOutputStream fileOutputStream = new FileOutputStream(output);
        zipfile.extractFile(header, fileOutputStream);
    }

}