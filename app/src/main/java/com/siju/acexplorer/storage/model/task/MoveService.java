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
import android.util.Log;

import com.siju.acexplorer.R;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.model.helper.LargeBundleTransfer;
import com.siju.acexplorer.model.root.RootDeniedException;
import com.siju.acexplorer.model.root.RootUtils;
import com.siju.acexplorer.storage.model.CopyData;
import com.siju.acexplorer.storage.model.operations.OperationProgress;
import com.siju.acexplorer.storage.model.operations.OperationUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.model.FileConstants.KEY_CATEGORY;
import static com.siju.acexplorer.model.helper.FileUtils.checkMimeType;
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

public class MoveService extends Service {

    private static final String TAG = "MoveService";

    private Context                    context;
    private NotificationManager        notificationManager;
    private NotificationCompat.Builder builder;

    private List<FileInfo>     filesToMove;
    private List<CopyData>     copyData;
    private ArrayList<String>  filesMovedList;
    private ArrayList<String>  oldFileList;
    private ArrayList<Integer> categories;

    private final int    NOTIFICATION_ID = 1000;
    private final String SEPARATOR       = "/";
    private final String CHANNEL_ID      = "operation";

    private Looper         serviceLooper;
    private ServiceHandler serviceHandler;
    private final int STOP_REQUEST = 200;
    private boolean stopService;


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        startThread();
    }

    private void startThread() {
        HandlerThread thread = new HandlerThread("MoveService",
                                                 Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.log("CopyService", "onHandleIntent: " + intent + "starId:" + startId);
        if (intent == null) {
            Log.e(this.getClass().getSimpleName(), "Null intent");
            stopService();
            return START_NOT_STICKY;
        }
        String action = intent.getAction();
        if (action != null && action.equals(OperationProgress.ACTION_STOP)) {
            stopService = true;
            stopSelf();
            return START_NOT_STICKY;
        }

        filesToMove = intent.getParcelableArrayListExtra(KEY_FILES);
        if (filesToMove == null) {
            filesToMove = LargeBundleTransfer.getFileData(context);
            if (filesToMove == null) {
                return START_NOT_STICKY;
            } else {
                LargeBundleTransfer.removeFileData(context);
            }
        }
        copyData = intent.getParcelableArrayListExtra(KEY_CONFLICT_DATA);
        String destDir = intent.getStringExtra(KEY_FILEPATH);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }

        createChannelId();
        builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setContentTitle(getResources().getString(R.string.moving)).setSmallIcon(R.drawable.ic_cut_white);
        builder.setOnlyAlertOnce(true);
        builder.setDefaults(0);
        Intent cancelIntent = new Intent(context, MoveService.class);
        cancelIntent.setAction(OperationProgress.ACTION_STOP);
        PendingIntent pendingCancelIntent =
                PendingIntent.getService(context, NOTIFICATION_ID, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT) ;
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_cancel, getString(R.string.dialog_cancel), pendingCancelIntent));

        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
        notificationManager.notify(NOTIFICATION_ID, notification);

        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = destDir;
        serviceHandler.sendMessage(msg);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void stopService() {
        stopSelf();
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Logger.log("CopyService", "handleMessage: " + msg.arg1);

                String currentDir = (String) msg.obj;
                checkWriteMode(currentDir);

                // Stop the service using the startId, so that we don't stop
                // the service in the middle of handling another job
                stopSelf(msg.arg1);
        }
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

    private void checkWriteMode(final String destinationDir) {
        int totalFiles = filesToMove.size();
        if (totalFiles == 0 || stopService) {
            sendCompletedResult();
            return;
        }
        filesMovedList = new ArrayList<>();
        oldFileList = new ArrayList<>();
        categories = new ArrayList<>();
        OperationUtils.WriteMode mode = OperationUtils.checkFolder(destinationDir);
        switch (mode) {
            case INTERNAL:
                for (int i = 0; i < filesToMove.size(); i++) {
                    if (stopService) {
                        sendCompletedResult();
                       break;
                    }
                    FileInfo sourceFile = filesToMove.get(i);
                    String sourcePath = sourceFile.getFilePath();
                    try {

                        if (!new File(sourcePath).canRead()) {
                            moveRoot(sourcePath, sourceFile.getFileName(),
                                     destinationDir);
                            continue;
                        }

                        int action = FileUtils.ACTION_NONE;

                        if (copyData != null) {
                            for (CopyData copyData1 : copyData) {
                                if (copyData1.getFilePath().equals(sourcePath)) {
                                    action = copyData1.getAction();
                                    break;
                                }
                            }
                        }
                        String fileName = sourceFile.getFileName();
                        String destPath = destinationDir + SEPARATOR + fileName;
                        if (action == FileUtils.ACTION_KEEP) {
                            String fileNameWithoutExt = fileName.substring(0, fileName.
                                    lastIndexOf("."));
                            destPath = destinationDir + "/" + fileNameWithoutExt + "(1)" + "." + sourceFile.getExtension();
                        }
                        moveFiles(sourceFile, fileName, destPath);

                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
                sendCompletedResult();
                break;

            case ROOT:
                for (int i = 0; i < filesToMove.size(); i++) {
                    String path = filesToMove.get(i).getFilePath();
                    String name = filesToMove.get(i).getFileName();
                    moveRoot(path, name, destinationDir);
                }
                sendCompletedResult();
                break;
        }
    }

    private void moveFiles(FileInfo sourceFileInfo, String fileName, String destinationPath) {

        String sourcePath = sourceFileInfo.getFilePath();
        File newFile = new File(destinationPath);
        File oldFile = new File(sourcePath);
        if (oldFile.renameTo(newFile)) {
            oldFileList.add(sourcePath);
            filesMovedList.add(newFile.getAbsolutePath());
            categories.add(sourceFileInfo.getCategory().getValue());
        }
        publishResults(fileName, filesToMove.size(), filesMovedList.size());
    }

    private void moveRoot(String path, String name, String destinationPath) {
        String targetPath;
        if (destinationPath.equals(File.separator)) {
            targetPath = destinationPath + name;
        } else {
            targetPath = destinationPath + File.separator + name;
        }
        try {
            RootUtils.mountRW(destinationPath);
            RootUtils.move(path, targetPath);
            RootUtils.mountRO(destinationPath);
            oldFileList.add(path);
            filesMovedList.add(targetPath);
            String extension = name.substring(name.lastIndexOf(".") + 1);
            int category = checkMimeType(extension).getValue();
            categories.add(category);
            publishResults(name, filesToMove.size(), filesMovedList.size());
        } catch (RootDeniedException e) {
            e.printStackTrace();
        }
    }

    private void publishResults(String fileName, long total, long done) {
        int progress = (int) ((((float) done / total)) * 100);
        builder.setProgress(100, progress, false);
        builder.setOngoing(true);
        int title = R.string.moving;
        builder.setContentTitle(getString(title));
        builder.setContentText(new File(fileName).getName() + " " + FileUtils.formatSize
                (context, done) + SEPARATOR + FileUtils.formatSize(context, total));
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        Intent intent = new Intent(MOVE_PROGRESS);
        intent.putExtra(KEY_COMPLETED, done);
        intent.putExtra(KEY_TOTAL, total);
        intent.putExtra(KEY_TOTAL_PROGRESS, progress);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void sendCompletedResult() {
        Logger.log(TAG, "sendCompletedResult" + filesMovedList.size());
        boolean isMoveSuccess = filesMovedList.size() == filesToMove.size();
        builder.setContentTitle(getString(R.string.move_complete));
        builder.setProgress(0, 0, false);
        builder.setOngoing(false);
        builder.setAutoCancel(true);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        if (!isMoveSuccess) {
            Intent intent = new Intent(MOVE_PROGRESS);
            intent.putExtra(KEY_COMPLETED, 0L);
            intent.putExtra(KEY_TOTAL, (long) filesToMove.size());
            intent.putExtra(KEY_TOTAL_PROGRESS, 100);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

        Intent intent = new Intent(ACTION_OP_REFRESH);
        intent.putExtra(KEY_RESULT, isMoveSuccess);
        intent.putExtra(KEY_OPERATION, CUT);
        intent.putStringArrayListExtra(KEY_FILES, filesMovedList);
        intent.putStringArrayListExtra(KEY_OLD_FILES, oldFileList);
        intent.putIntegerArrayListExtra(KEY_CATEGORY, categories);
        sendBroadcast(intent);
    }
}
