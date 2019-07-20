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
import android.text.format.Formatter;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.siju.acexplorer.R;
import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.main.model.helper.FileUtils;
import com.siju.acexplorer.main.model.helper.LargeBundleTransfer;
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

import static com.siju.acexplorer.main.model.helper.SdkHelper.isAtleastOreo;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_END;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_OPERATION;
import static com.siju.acexplorer.storage.model.operations.Operations.COMPRESS;
import static com.siju.acexplorer.storage.model.operations.ProgressConstantsKt.KEY_COMPLETED;
import static com.siju.acexplorer.storage.model.operations.ProgressConstantsKt.KEY_PROGRESS;
import static com.siju.acexplorer.storage.model.operations.ProgressConstantsKt.KEY_TOTAL;
import static com.siju.acexplorer.storage.model.operations.ProgressConstantsKt.ZIP_PROGRESS;


public class CreateZipService extends Service {

    private static final String TAG = "CreateZipService";
    private final String CHANNEL_ID = "operation";
    private final static int NOTIFICATION_ID = 1000;

    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private Context context;
    private ServiceHandler serviceHandler;
    private ZipOutputStream zipOutputStream;

    private List<FileInfo> zipFiles;

    private String filePath;
    private String name;
    private boolean stopService;
    private boolean isCompleted;
    private int lastpercent;
    private long size, totalBytes;


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
        createBuilder();
        addCancelAction();

        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannelId() {
        if (isAtleastOreo()) {
            CharSequence name = getString(R.string.operation);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createBuilder() {
        builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setContentTitle(getResources().getString(R.string.zip_progress_title))
                .setSmallIcon(R.drawable.ic_archive_white);
        builder.setOnlyAlertOnce(true);
        builder.setDefaults(0);
    }

    private void addCancelAction() {
        Intent cancelIntent = new Intent(context, CreateZipService.class);
        cancelIntent.setAction(OperationProgress.ACTION_STOP);
        PendingIntent pendingCancelIntent =
                PendingIntent.getService(context, NOTIFICATION_ID, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_cancel, getString(R.string.dialog_cancel), pendingCancelIntent));
    }

    private void startThread() {
        HandlerThread thread = new HandlerThread("CreateZipService",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        Looper serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
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
        createZipFile();

        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);
        return START_STICKY;
    }

    private void createZipFile() {
        File zipName = new File(name);
        if (!zipName.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                zipName.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
            calculateTotalSize(f1);
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
                publishCompletedResult();
                break;
            }
            try {
                compress(file, "");
            } catch (Exception ignored) {
            }
        }
        try {
            zipOutputStream.flush();
            zipOutputStream.close();

        } catch (Exception ignored) {
        }
    }

    private void calculateTotalSize(File file) {
        if (file.isDirectory()) {
            totalBytes = totalBytes + FileUtils.getFolderSize(file);
        } else {
            totalBytes = totalBytes + file.length();
        }
    }


    private void compress(File file, String path) throws IOException, NullPointerException {

        if (file.isFile()) {
            compressFile(file, path);
        } else {
            compressDirectory(file, path);
        }
    }

    private void compressFile(File file, String path) throws IOException {
        if (file.length() == 0) {
            calculateProgress(filePath, size, totalBytes);
        } else {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
            zipOutputStream.putNextEntry(new ZipEntry(path + "/" + file.getName()));
            zipFile(in);
            in.close();
        }
    }

    private void compressDirectory(File file, String path) throws IOException {
        String[] files = file.list();
        if (files == null) {
            return;
        } else if (files.length == 0) {
            compressEmptyFolder(path + File.separator + file.getName() + "/");
        }
        for (String fileName : files) {

            File f = new File(file.getAbsolutePath() + File.separator
                    + fileName);
            compress(f, path + File.separator + file.getName());

        }
    }

    private void zipFile(BufferedInputStream in) throws IOException {
        byte[] buf = new byte[2048];
        int len;
        while ((len = in.read(buf)) > 0) {
            if (stopService) {
                publishCompletedResult();
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
    }

    private void compressEmptyFolder(String path) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(path));
        calculateProgress(filePath, size, totalBytes);
    }


    private ArrayList<File> toFileArray(List<FileInfo> a) {
        ArrayList<File> b = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            b.add(new File(a.get(i).getFilePath()));
        }
        return b;
    }

    private void publishResults(String filePath, int progress, long done, long total) {

        builder.setProgress(100, progress, false);
        builder.setOngoing(true);
        int title = R.string.zip_progress_title;
        builder.setContentTitle(getResources().getString(title));
        builder.setContentText(new File(filePath).getName() + " " + Formatter.formatFileSize
                (context, done) + "/" + Formatter.formatFileSize(context, total));
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        Log.e("CreateZip", "publishResults: progress:" + progress + " total:" + total);
        if (progress == 100 || total == 0) {
            publishCompletedResult();
        }

        sendBroadcast(filePath, progress, done, total);
    }

    private void sendBroadcast(String filePath, int progress, long done, long total) {
        Intent intent = new Intent(ZIP_PROGRESS);
        if (progress == 100 || total == 0) {
            intent.putExtra(KEY_PROGRESS, 100);
        } else {
            intent.putExtra(KEY_PROGRESS, progress);
        }
        intent.putExtra(KEY_COMPLETED, done);
        intent.putExtra(KEY_TOTAL, total);
        intent.putExtra(KEY_FILEPATH, filePath);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void dismissProgressDialog() {
        Intent intent = new Intent(ZIP_PROGRESS);
        intent.putExtra(KEY_END, true);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    private void publishCompletedResult() {
        if (isCompleted) {
            return;
        }
        isCompleted = true;
        if (stopService) {
            dismissProgressDialog();
        }
        Intent intent = new Intent(OperationUtils.ACTION_RELOAD_LIST);
        intent.putExtra(KEY_OPERATION, COMPRESS);
        intent.putExtra(KEY_FILEPATH, name);
        sendBroadcast(intent);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void calculateProgress(final String name, final long
            copiedbytes, final long totalbytes) {
        int progress = (int) ((copiedbytes / (float) totalbytes) * 100);
        lastpercent = (int) copiedbytes;
        publishResults(name, progress, copiedbytes, totalbytes);
    }
}

