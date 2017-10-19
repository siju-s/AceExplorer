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

import com.siju.acexplorer.R;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.storage.model.operations.OperationUtils;
import com.siju.acexplorer.view.AceActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_OPERATION;
import static com.siju.acexplorer.storage.model.operations.Operations.COMPRESS;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.KEY_COMPLETED;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.KEY_PROGRESS;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.KEY_TOTAL;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.ZIP_PROGRESS;


public class CreateZipService extends IntentService {

    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private Context context;
    private final int NOTIFICATION_ID = 1000;

    private int lastpercent;
    private long size, totalBytes;
    private String filePath;

    private ZipOutputStream zipOutputStream;
    private String name;

    public CreateZipService() {
        super("CreateZipService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        name = intent.getStringExtra(KEY_FILEPATH);
        ArrayList<FileInfo> zipFiles = intent.getParcelableArrayListExtra(KEY_FILES);
        File zipName = new File(name);
        if (!zipName.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                zipName.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, AceActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        builder = new NotificationCompat.Builder(context, formChannel());
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(getResources().getString(R.string.zip_progress_title))
                .setSmallIcon(R.drawable.ic_archive_white);
        String channelId = getResources().getString(R.string.operation);
        builder.setChannelId(channelId);
        startForeground(NOTIFICATION_ID, builder.build());
        execute(toFileArray(zipFiles), name);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private String formChannel() {
        // The id of the channel.
        String id = "ace_channel_01";
        CharSequence name = getString(R.string.operation);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        notificationManager.createNotificationChannel(channel);
        return id;
    }



    void execute(ArrayList<File> zipFiles, String fileOut) {
        for (File f1 : zipFiles) {
            if (f1.isDirectory()) {
                totalBytes = totalBytes + FileUtils.getFolderSize(f1);
            } else {
                totalBytes = totalBytes + f1.length();
            }
        }
        OutputStream out;
        filePath = fileOut;
        File zipDirectory = new File(fileOut);

        try {
            out = FileUtils.getOutputStream(zipDirectory, context);
            zipOutputStream = new ZipOutputStream(new BufferedOutputStream(out));
        } catch (Exception ignored) {
        }
        for (File file : zipFiles) {
            try {
                compressFile(file, "");
            } catch (Exception ignored) {
            }
        }
        try {
            zipOutputStream.flush();
            zipOutputStream.close();

        } catch (Exception ignored) {
        }
    }


    private void compressFile(File file, String path) throws IOException, NullPointerException {

        if (!file.isDirectory()) {
            byte[] buf = new byte[2048];
            int len;
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
            zipOutputStream.putNextEntry(new ZipEntry(path + "/" + file.getName()));
            while ((len = in.read(buf)) > 0) {
                zipOutputStream.write(buf, 0, len);
                size += len;
                int p = (int) ((size / (float) totalBytes) * 100);
                if (p != lastpercent || lastpercent == 0) {
                    calculateProgress(filePath, size, totalBytes);
                }
                lastpercent = p;
            }
            in.close();
            return;
        }
        if (file.list() == null) {
            return;
        }
        for (String fileName : file.list()) {

            File f = new File(file.getAbsolutePath() + File.separator
                    + fileName);
            compressFile(f, path + File.separator + file.getName());

        }
    }



    private ArrayList<File> toFileArray(ArrayList<FileInfo> a) {
        ArrayList<File> b = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            b.add(new File(a.get(i).getFilePath()));
        }
        return b;
    }

    private void publishResults(String filePath, int i, long done, long total) {


        builder.setProgress(100, i, false);
        builder.setOngoing(true);
        int title = R.string.zip_progress_title;
        builder.setContentTitle(getResources().getString(title));
        builder.setContentText(new File(filePath).getName() + " " + Formatter.formatFileSize
                (context, done) + "/" + Formatter.formatFileSize(context, total));
        int id1 = NOTIFICATION_ID;
        notificationManager.notify(id1, builder.build());
        //Log.d("CreateZip", "publishResults: progress:"+i + " total:"+total);
        if (i == 100 || total == 0) {
            builder.setContentTitle("Zip completed");
            builder.setContentText("");
            builder.setProgress(0, 0, false);
            builder.setOngoing(false);
            notificationManager.notify(id1, builder.build());
            publishCompletedResult(id1);
        }

        Intent intent = new Intent(ZIP_PROGRESS);
        if (i == 100 || total == 0) {
            intent.putExtra(KEY_PROGRESS, 100);
        } else {
            intent.putExtra(KEY_PROGRESS, i);
        }
        intent.putExtra(KEY_COMPLETED, done);
        intent.putExtra(KEY_TOTAL, total);
        intent.putExtra(KEY_FILEPATH, filePath);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

    }

    private void publishCompletedResult(int id1) {
        Intent intent = new Intent(OperationUtils.ACTION_RELOAD_LIST);
        intent.putExtra(KEY_OPERATION, COMPRESS);
        intent.putExtra(KEY_FILEPATH, name);
        sendBroadcast(intent);
        try {
            notificationManager.cancel(id1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculateProgress(final String name, final long
            copiedbytes, final long totalbytes) {
        int progress = (int) ((copiedbytes / (float) totalbytes) * 100);
        lastpercent = (int) copiedbytes;
        publishResults(name, progress, copiedbytes, totalbytes);
    }


}

