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

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Formatter;
import android.util.Log;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import com.siju.acexplorer.R;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.storage.modules.zip.ZipUtils;
import com.siju.acexplorer.view.AceActivity;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.siju.acexplorer.model.helper.FileOperations.mkdir;
import static com.siju.acexplorer.model.helper.SdkHelper.isOreo;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_OP_FAILED;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_RELOAD_LIST;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILENAME;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH2;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_OPERATION;
import static com.siju.acexplorer.storage.model.operations.Operations.EXTRACT;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.EXTRACT_PROGRESS;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.KEY_COMPLETED;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.KEY_PROGRESS;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.KEY_TOTAL;

public class ExtractService extends IntentService {
    private final int NOTIFICATION_ID = 1000;

    private Context context;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private long copiedbytes = 0, totalbytes = 0;
    private final String CHANNEL_ID = "operation";


    public ExtractService() {
        super("ExtractService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        String file = intent.getStringExtra(KEY_FILEPATH);
        String newFile = intent.getStringExtra(KEY_FILEPATH2);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, AceActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        createChannelId();
        builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(getResources().getString(R.string.extracting))
                .setContentText(new File(file).getName())
                .setSmallIcon(R.drawable.ic_doc_compressed);
        builder.setOnlyAlertOnce(true);
        builder.setDefaults(0);

        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
        notificationManager.notify(NOTIFICATION_ID , notification);
        start(file, newFile);
    }


    @TargetApi(Build.VERSION_CODES.O)
    private void createChannelId() {
        if (isOreo()) {
            // The id of the channel.
//        String id = "ace_channel_01";
            CharSequence name = getString(R.string.operation);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void start(String zipFilePath, String newFile) {


        if (zipFilePath != null) {
            File zipFile = new File(zipFilePath);
            if (ZipUtils.isZipViewable(zipFilePath)) {
                extract(zipFile, newFile);
            } else if (zipFilePath.toLowerCase().endsWith(".rar")) {
                extractRar(zipFile, newFile);
            } else if (zipFilePath.toLowerCase().endsWith(".tar") || zipFile.getName().toLowerCase().endsWith
                    (".tar.gz")) {
                extractTar(zipFile, newFile);
            }
        }

        Logger.log("ExtractService", "ZIp file=" + zipFilePath + "new file=" + newFile);
    }

    void extract(File archive, String destinationPath) {
        try {
            ArrayList<ZipEntry> arrayList = new ArrayList<>();
            ZipFile zipfile = new ZipFile(archive);
//                calculateProgress(archive.getName(), id, copiedbytes, totalbytes);
            for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {

                ZipEntry entry = (ZipEntry) e.nextElement();
                arrayList.add(entry);

            }
            for (ZipEntry entry : arrayList) {
                totalbytes = totalbytes + entry.getSize();
            }
            for (ZipEntry entry : arrayList) {
                unzipEntry(zipfile, entry, destinationPath);
            }
            Intent intent = new Intent(ACTION_RELOAD_LIST);
            intent.putExtra(KEY_OPERATION, EXTRACT);
            sendBroadcast(intent);
            calculateProgress(archive.getName(), copiedbytes, totalbytes);
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "Error while extracting file " + archive, e);
            Intent intent = new Intent(ACTION_OP_FAILED);
            intent.putExtra(KEY_OPERATION, EXTRACT);
            sendBroadcast(intent);
            publishResults(archive.getName(), 100, totalbytes, copiedbytes);
        }

    }

    void extractTar(File archive, String destinationPath) {
        try {
            ArrayList<TarArchiveEntry> archiveEntries = new ArrayList<>();
            TarArchiveInputStream inputStream;
            if (archive.getName().endsWith(".tar"))
                inputStream = new TarArchiveInputStream(new BufferedInputStream(new FileInputStream(archive)));
            else
                inputStream = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(archive)));
            publishResults(archive.getName(), 0, totalbytes, copiedbytes);
            TarArchiveEntry tarArchiveEntry = inputStream.getNextTarEntry();
            while (tarArchiveEntry != null) {

                archiveEntries.add(tarArchiveEntry);
                tarArchiveEntry = inputStream.getNextTarEntry();

            }
            for (TarArchiveEntry entry : archiveEntries) {
                totalbytes = totalbytes + entry.getSize();
            }
            for (TarArchiveEntry entry : archiveEntries) {
                unzipTAREntry(inputStream, entry, destinationPath, archive.getName());

            }

            inputStream.close();

            Intent intent = new Intent(ACTION_RELOAD_LIST);
            intent.putExtra(KEY_OPERATION, EXTRACT);
            sendBroadcast(intent);
            publishResults(archive.getName(), 100, totalbytes, copiedbytes);

        } catch (Exception e) {
            Log.e("TAG", "Error while extracting file " + archive, e);
            Intent intent = new Intent(ACTION_RELOAD_LIST);
            intent.putExtra(KEY_OPERATION, EXTRACT);
            sendBroadcast(intent);
            publishResults(archive.getName(), 100, totalbytes, copiedbytes);

        }

    }

    void extractRar(File archive, String destinationPath) {
        try {
            ArrayList<FileHeader> arrayList = new ArrayList<>();
            Archive zipfile = new Archive(archive);
            FileHeader fh = zipfile.nextFileHeader();
            publishResults(archive.getName(), 0, totalbytes, copiedbytes);
            while (fh != null) {


                arrayList.add(fh);
                fh = zipfile.nextFileHeader();

            }
            for (FileHeader header : arrayList) {
                totalbytes = totalbytes + header.getFullUnpackSize();
            }
            for (FileHeader header : arrayList) {

                unzipRAREntry(archive.getName(), zipfile, header, destinationPath);

            }
            Intent intent = new Intent(ACTION_RELOAD_LIST);
            intent.putExtra(KEY_OPERATION, EXTRACT);
            sendBroadcast(intent);
            calculateProgress(archive.getName(), copiedbytes, totalbytes);

        } catch (Exception e) {
            Log.e("TAG", "Error while extracting file " + archive, e);
            Intent intent = new Intent(ACTION_RELOAD_LIST);
            intent.putExtra(KEY_OPERATION, EXTRACT);
            sendBroadcast(intent);
            calculateProgress(archive.getName(), copiedbytes, totalbytes);

        }
    }


    private void publishResults(String fileName, int progress, long total, long done) {
        builder.setContentTitle(getResources().getString(R.string.extracting));
        builder.setProgress(100, progress, false);
        builder.setOngoing(true);
        builder.setContentText(new File(fileName).getName() + " " + Formatter.formatFileSize
                (context, done) + "/" + Formatter.formatFileSize(context, total));
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        if (progress == 100) {
            builder.setContentTitle(getResources().getString(R.string.extract_complete));
            builder.setContentText(new File(fileName).getName() + " " + Formatter.formatFileSize(context, total));
            builder.setProgress(0, 0, false);
            builder.setOngoing(false);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            publishCompletedResult(NOTIFICATION_ID);
        }

        Logger.log(ExtractService.this.getClass().getSimpleName(), "Progress=" + progress + " done=" + done + " total="
                + total);
        Intent intent = new Intent(EXTRACT_PROGRESS);
        intent.putExtra(KEY_PROGRESS, progress);
        intent.putExtra(KEY_COMPLETED, done);
        intent.putExtra(KEY_TOTAL, total);
        intent.putExtra(KEY_FILENAME, fileName);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

    }

    private void publishCompletedResult(int id) {
        try {
            notificationManager.cancel(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void createDir(File dir) {
        mkdir(dir);
    }


    void calculateProgress(final String name, final long
            copiedbytes, final long totalbytes) {

        int progress = (int) ((copiedbytes / (float) totalbytes) * 100);
        publishResults(name, progress, totalbytes, copiedbytes);

    }

    long time = System.nanoTime() / 500000000;

    private void unzipEntry(ZipFile zipfile, ZipEntry entry, String outputDir)
            throws Exception {
        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
            return;
        }
        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }

        BufferedInputStream inputStream = new BufferedInputStream(
                zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(
                FileUtils.getOutputStream(outputFile, context));
//            Logger.log("ExtractService", "zipfile=" + zipfile + " zipentry=" + entry + " stream=" + inputStream);

        try {
            int len;
            byte buf[] = new byte[20480];
            while ((len = inputStream.read(buf)) > 0) {

                outputStream.write(buf, 0, len);
                copiedbytes = copiedbytes + len;

                long time1 = System.nanoTime() / 500000000;
                if (((int) time1) > ((int) (time))) {
                    calculateProgress(zipfile.getName(), copiedbytes, totalbytes);
                    time = System.nanoTime() / 500000000;
                }
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

    private void unzipRAREntry(String fileName, Archive zipfile, FileHeader entry, String outputDir)
            throws Exception {
        String name = entry.getFileNameString();
        name = name.replaceAll("\\\\", "/");
        if (entry.isDirectory()) {
            createDir(new File(outputDir, name));
            return;
        }
        File outputFile = new File(outputDir, name);
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }
        BufferedInputStream inputStream = new BufferedInputStream(
                zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(
                FileUtils.getOutputStream(outputFile, context));
        try {
            int len;
            byte buf[] = new byte[20480];
            while ((len = inputStream.read(buf)) > 0) {

                outputStream.write(buf, 0, len);
                copiedbytes = copiedbytes + len;
                long time1 = System.nanoTime() / 500000000;
                if (((int) time1) > ((int) (time))) {
                    calculateProgress(fileName, copiedbytes, totalbytes);
                    time = System.nanoTime() / 500000000;
                }

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

    private void unzipTAREntry(TarArchiveInputStream zipfile, TarArchiveEntry entry, String outputDir,
                               String fileName)
            throws Exception {
        String name = entry.getName();
        if (entry.isDirectory()) {
            createDir(new File(outputDir, name));
            return;
        }
        File outputFile = new File(outputDir, name);
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }

        BufferedOutputStream outputStream = new BufferedOutputStream(
                FileUtils.getOutputStream(outputFile, getBaseContext()));
        try {
            int len;
            byte buf[] = new byte[20480];
            while ((len = zipfile.read(buf)) > 0) {

                outputStream.write(buf, 0, len);
                copiedbytes = copiedbytes + len;
                long time1 = System.nanoTime() / 500000000;
                if (((int) time1) > ((int) (time))) {
                    calculateProgress(fileName, copiedbytes, totalbytes);
                    time = System.nanoTime() / 500000000;
                }

            }
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                //close
            }

        }
    }
}


