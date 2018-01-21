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
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Formatter;

import com.siju.acexplorer.R;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.model.helper.LargeBundleTransfer;
import com.siju.acexplorer.storage.model.operations.OperationProgress;
import com.siju.acexplorer.storage.model.operations.OperationUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.siju.acexplorer.model.helper.SdkHelper.isOreo;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_OPERATION;
import static com.siju.acexplorer.storage.model.operations.Operations.COMPRESS;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.KEY_COMPLETED;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.KEY_PROGRESS;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.KEY_TOTAL;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.ZIP_PROGRESS;


public class CreateZipService extends Service {

    private static final String TAG             = "CreateZipService";
    private final        String CHANNEL_ID      = "operation";
    private final static int    NOTIFICATION_ID = 1000;

    private NotificationManager        notificationManager;
    private NotificationCompat.Builder builder;
    private Context                    context;
    private ServiceHandler             serviceHandler;
    private ZipOutputStream            zipOutputStream;

    private List<FileInfo> zipFiles;

    private String  filePath;
    private String  name;
    private boolean stopService;
    private boolean isCompleted;
    private int     lastpercent;
    private long    size, totalBytes;


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        createNotification();
        startThread();
    }


    private void createNotification() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }

        createChannelId();
        builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setContentTitle(getResources().getString(R.string.zip_progress_title))
                .setSmallIcon(R.drawable.ic_archive_white);
        builder.setOnlyAlertOnce(true);
        builder.setDefaults(0);
        Intent cancelIntent = new Intent(context, CreateZipService.class);
        cancelIntent.setAction(OperationProgress.ACTION_STOP);
        PendingIntent pendingCancelIntent =
                PendingIntent.getService(context, NOTIFICATION_ID, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_cancel, getString(R.string.dialog_cancel), pendingCancelIntent));

        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void startThread() {
        HandlerThread thread = new HandlerThread("CreateZipService",
                                                 Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        Looper serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    // Handler that receives messages from the thread
    @TargetApi(Build.VERSION_CODES.O)
    private void createChannelId() {
        if (isOreo()) {
            CharSequence name = getString(R.string.operation);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.log(TAG, "onStartCommand: " + intent + "startId:" + startId);
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        String action = intent.getAction();
        if (action != null && action.equals(OperationProgress.ACTION_STOP)) {
            stopService = true;
            stopSelf();
            return START_NOT_STICKY;
        }

        name = intent.getStringExtra(KEY_FILEPATH);
        zipFiles = intent.getParcelableArrayListExtra(KEY_FILES);
        if (zipFiles == null) {
            zipFiles = LargeBundleTransfer.getFileData(context);
            if (zipFiles.size() == 0) {
                return START_NOT_STICKY;
            } else {
                LargeBundleTransfer.removeFileData(context);
            }
        }
        File zipName = new File(name);
        if (!zipName.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                zipName.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final class ServiceHandler extends Handler {

        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Logger.log(CreateZipService.this.getClass().getSimpleName(), "handleMessage: " + msg.arg1);
            execute(toFileArray(zipFiles), name);
            stopSelf();
        }
    }


    private void execute(ArrayList<File> zipFiles, String fileOut) {
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
            if (stopService) {
                publishCompletedResult(NOTIFICATION_ID);
                break;
            }
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
                if (stopService) {
                    publishCompletedResult(NOTIFICATION_ID);
                    break;
                }
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
        String[] files = file.list();
        if (files == null) {
            return;
        }
        for (String fileName : files) {

            File f = new File(file.getAbsolutePath() + File.separator
                                      + fileName);
            compressFile(f, path + File.separator + file.getName());

        }
    }


    private ArrayList<File> toFileArray(List<FileInfo> a) {
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
        if (isCompleted) {
            return;
        }
        isCompleted = true;
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

