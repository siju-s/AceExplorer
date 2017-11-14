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
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.siju.acexplorer.R;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.storage.model.CopyData;
import com.siju.acexplorer.view.AceActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.model.FileConstants.KEY_CATEGORY;
import static com.siju.acexplorer.model.helper.SdkHelper.isOreo;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_OP_REFRESH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_CONFLICT_DATA;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_OLD_FILES;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_OPERATION;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_RESULT;
import static com.siju.acexplorer.storage.model.operations.Operations.CUT;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.KEY_COMPLETED;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.KEY_TOTAL;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.KEY_TOTAL_PROGRESS;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.MOVE_PROGRESS;

public class MoveFiles extends IntentService {

    private List<FileInfo> filesToMove;
    private List<CopyData> copyData;
    private ArrayList<String> filesMovedList;
    private ArrayList<String> oldFileList;
    private ArrayList<Integer> categories;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private final int NOTIFICATION_ID = 1000;
    private final String SEPARATOR = "/";
    private final String CHANNEL_ID = "operation";

    private Context context;

    public MoveFiles() {
        super("MoveFiles");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        filesToMove = intent.getParcelableArrayListExtra(KEY_FILES);
        copyData = intent.getParcelableArrayListExtra(KEY_CONFLICT_DATA);
        String currentDir = intent.getStringExtra(KEY_FILEPATH);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, AceActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        createChannelId();
        builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(getResources().getString(R.string.moving)).setSmallIcon(R
                .drawable.ic_cut_white);
        builder.setOnlyAlertOnce(true);
        builder.setDefaults(0);

        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
        notificationManager.notify(NOTIFICATION_ID , notification);

        moveFiles(currentDir);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannelId() {
        if (isOreo()) {
            CharSequence name = getString(R.string.operation);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void moveFiles(String destinationDir) {

        int totalFiles = filesToMove.size();
        if (totalFiles == 0) {
            return;
        }
        filesMovedList = new ArrayList<>();
        oldFileList = new ArrayList<>();
        categories = new ArrayList<>();

        for (FileInfo fileInfo : filesToMove) {
            int action = FileUtils.ACTION_NONE;

            if (copyData != null) {
                for (CopyData copyData1 : copyData) {
                    if (copyData1.getFilePath().equals(fileInfo.getFilePath())) {
                        action = copyData1.getAction();
                        break;
                    }
                }
            }
            String fileName = fileInfo.getFileName();
            String path = destinationDir + "/" + fileName;
            if (action == FileUtils.ACTION_KEEP) {
                String fileNameWithoutExt = fileName.substring(0, fileName.
                        lastIndexOf("."));
                path = destinationDir + "/" + fileNameWithoutExt + "(2)" + "." + fileInfo.getExtension();
            }
            File newFile = new File(path);
            File oldFile = new File(fileInfo.getFilePath());
            if (oldFile.renameTo(newFile)) {
                oldFileList.add(fileInfo.getFilePath());
                filesMovedList.add(newFile.getAbsolutePath());
                categories.add(fileInfo.getCategory().getValue());
            }
            publishResults(fileName, totalFiles, filesMovedList.size());
        }
        sendCompletedResult();
    }

    private void publishResults(String fileName, long total, long done) {
        //notification
        int progress = (int) (((float) (done / total)) * 100);
        builder.setProgress(100, progress, false);
        builder.setOngoing(true);
        int title = R.string.moving;
        builder.setContentTitle(getString(title));
        builder.setContentText(new File(fileName).getName() + " " + FileUtils.formatSize
                (context, done)
                + SEPARATOR + FileUtils
                .formatSize(context, total));
        int id1 = NOTIFICATION_ID;
        notificationManager.notify(id1, builder.build());
        if (total == done) {
            builder.setContentTitle("Move Completed");
            builder.setContentText("");
            builder.setProgress(0, 0, false);
            builder.setOngoing(false);
            builder.setAutoCancel(true);
            notificationManager.notify(id1, builder.build());
        }

        Intent intent = new Intent(MOVE_PROGRESS);
        intent.putExtra(KEY_COMPLETED, done);
        intent.putExtra(KEY_TOTAL, total);
        intent.putExtra(KEY_TOTAL_PROGRESS, progress);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void sendCompletedResult() {
        Intent intent = new Intent(ACTION_OP_REFRESH);
        intent.putExtra(KEY_RESULT, filesMovedList.size() != 0);
        intent.putExtra(KEY_OPERATION, CUT);
        intent.putStringArrayListExtra(KEY_FILES, filesMovedList);
        intent.putStringArrayListExtra(KEY_OLD_FILES, oldFileList);
        intent.putIntegerArrayListExtra(KEY_CATEGORY, categories);
        sendBroadcast(intent);
    }
}
