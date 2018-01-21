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

import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.R;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.FileListLoader;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.model.helper.LargeBundleTransfer;
import com.siju.acexplorer.model.helper.RootHelper;
import com.siju.acexplorer.model.root.RootDeniedException;
import com.siju.acexplorer.model.root.RootUtils;
import com.siju.acexplorer.storage.model.CopyData;
import com.siju.acexplorer.storage.model.operations.OperationProgress;
import com.siju.acexplorer.storage.model.operations.OperationUtils;
import com.siju.acexplorer.trash.TrashDbHelper;
import com.siju.acexplorer.trash.TrashModel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.siju.acexplorer.model.helper.FileOperations.mkdir;
import static com.siju.acexplorer.model.helper.MediaStoreHelper.scanMultipleFiles;
import static com.siju.acexplorer.model.helper.SdkHelper.isOreo;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_OP_REFRESH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_CONFLICT_DATA;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_MOVE;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_OPERATION;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_RESULT;
import static com.siju.acexplorer.storage.model.operations.Operations.COPY;
import static com.siju.acexplorer.storage.model.operations.Operations.CUT;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.COPY_PROGRESS;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.KEY_COMPLETED;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.KEY_COUNT;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.KEY_PROGRESS;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.KEY_TOTAL;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.KEY_TOTAL_PROGRESS;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class CopyService extends Service {

    private static final String TAG      = "CopyService";
    private final int NOTIFICATION_ID = 1000;
    private final String SEPARATOR    = "/";
    private final String CHANNEL_ID   = "operation";


    private Context                    context;
    private NotificationManager        notificationManager;
    private NotificationCompat.Builder builder;

    private final ArrayList<String> filesToMediaIndex = new ArrayList<>();
    private final List<FileInfo>    failedFiles       = new ArrayList<>();
    private List<FileInfo> files;
    private List<CopyData> copyData;

    private long totalBytes = 0L, copiedBytes = 0L;
    private int     count     = 0;
    private boolean               move;
    private boolean               calculatingTotalSize;
    private boolean               isCopyToTrash;
    private boolean               isTrashRestore;
    private ArrayList<TrashModel> trashList;
    private ServiceHandler        serviceHandler;
    private boolean stopService;
    private boolean isCompleted;


    @Override
    public void onCreate() {
        super.onCreate();
        Logger.log("CopyService", "onCreate: ");
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
        builder.setContentTitle(getResources().getString(R.string.copying)).setSmallIcon(R.drawable.ic_copy_white);
        builder.setOnlyAlertOnce(true);
        builder.setDefaults(0);
        Intent cancelIntent = new Intent(context, CopyService.class);
        cancelIntent.setAction(OperationProgress.ACTION_STOP);
        PendingIntent pendingCancelIntent =
                PendingIntent.getService(context, NOTIFICATION_ID, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT) ;
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_cancel, getString(R.string.dialog_cancel), pendingCancelIntent));

        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
        notificationManager.notify(NOTIFICATION_ID, notification);
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


    private void startThread() {
        HandlerThread thread = new HandlerThread("CopyService",
                                                 Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        Looper serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.log(TAG, "onStartCommand: " + intent + "starId:" + startId);
        if (intent == null) {
            Logger.log(this.getClass().getSimpleName(), "Null intent");
            stopService();
            return START_NOT_STICKY;
        }
        String action = intent.getAction();
        if (action != null && action.equals(OperationProgress.ACTION_STOP)) {
            stopService = true;
            stopSelf();
            return START_NOT_STICKY;
        }

        isTrashRestore = intent.getBooleanExtra(OperationUtils.KEY_IS_RESTORE, false);
        if (isTrashRestore) {
            trashList = intent.getParcelableArrayListExtra(OperationUtils.KEY_TRASH_DATA);
            copyTrashFiles();
        } else {
            files = intent.getParcelableArrayListExtra(KEY_FILES);
            if (files == null) {
                files = LargeBundleTransfer.getFileData(context);
                if (files.size() == 0) {
                    stopService();
                    return START_NOT_STICKY;
                } else {
                    LargeBundleTransfer.removeFileData(context);
                }
            }
            isCopyToTrash = intent.getBooleanExtra(OperationUtils.KEY_IS_TRASH, false);
            copyData = intent.getParcelableArrayListExtra(KEY_CONFLICT_DATA);
            move = intent.getBooleanExtra(KEY_MOVE, false);

            String currentDir = intent.getStringExtra(KEY_FILEPATH);

            Message msg = serviceHandler.obtainMessage();
            msg.arg1 = startId;
            msg.obj = currentDir;
            serviceHandler.sendMessage(msg);
        }
        return START_STICKY;
    }

    private void stopService() {
        stopSelf();
    }
    private final class ServiceHandler extends Handler {

        ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            Logger.log(TAG, "handleMessage: " + msg.arg1);
            String currentDir = (String) msg.obj;
            checkWriteMode(currentDir);
            stopSelf();
        }
    }


    private void checkWriteMode(final String currentDir) {

        OperationUtils.WriteMode mode = OperationUtils.checkFolder(currentDir);
        switch (mode) {
            case INTERNAL:
                getTotalBytes(files);
                for (int i = 0; i < files.size(); i++) {
                    FileInfo sourceFile = files.get(i);
                    if (stopService) {
                        publishCompletionResult();
                        break;
                    }
                    try {

                        if (!new File(files.get(i).getFilePath()).canRead()) {
                            copyRoot(files.get(i).getFilePath(), files.get(i).getFileName(),
                                     currentDir);
                            continue;
                        }
                        FileInfo destFile = new FileInfo(sourceFile.getCategory(), sourceFile
                                .getFileName(),
                                                         sourceFile.getFilePath(), sourceFile.getDate(), sourceFile
                                                                 .getSize(), sourceFile.isDirectory(),
                                                         sourceFile.getExtension(), sourceFile.getPermissions(), false);
                        int action = FileUtils.ACTION_NONE;
                        if (copyData != null) {
                            for (CopyData copyData1 : copyData) {
                                if (copyData1.getFilePath().equals(sourceFile.getFilePath())) {
                                    action = copyData1.getAction();
                                    break;
                                }
                            }
                        }
                        String fileName = files.get(i).getFileName();
                        String path = currentDir + SEPARATOR + fileName;
                        if (action == FileUtils.ACTION_KEEP) {
                            if (new File(path).isDirectory()) {
                                path = currentDir + SEPARATOR + fileName + "(2)";
                            } else {
                                String fileNameWithoutExt = fileName.substring(0, fileName.
                                        lastIndexOf("."));
                                path = currentDir + SEPARATOR + fileNameWithoutExt + "(2)" + "." + files
                                        .get(i)
                                        .getExtension();
                            }
                        }
                        destFile.setFilePath(path);
                        Logger.log("CopyService", "Execute-Dest file path=" + destFile
                                .getFilePath());

                        startCopy(sourceFile, destFile, move);

                    } catch (Exception e) {
                        e.printStackTrace();

                        failedFiles.add(files.get(i));
                        for (int j = i + 1; j < files.size(); j++) {
                            failedFiles.add(files.get(j));
                        }
                        break;
                    }
                }
                publishCompletionResult();
                break;

            case ROOT:
                totalBytes = files.size();
                for (int i = 0; i < files.size(); i++) {
                    String path = files.get(i).getFilePath();
                    String name = files.get(i).getFileName();
                    copyRoot(path, name, currentDir);
                    FileInfo newFileInfo = files.get(i);
                    newFileInfo.setFilePath(currentDir + SEPARATOR + (newFileInfo.getFileName()));
                    if (checkFiles(files.get(i), newFileInfo)) {
                        failedFiles.add(files.get(i));
                    }
                }
                publishCompletionResult();
                break;
        }
        deleteCopiedFiles();
    }


    private void getTotalBytes(final List<FileInfo> files) {
        calculatingTotalSize = true;
        long totalBytes = 0L;
        for (int i = 0; i < files.size(); i++) {
            FileInfo f1 = (files.get(i));
            if (f1.isDirectory()) {
                totalBytes = totalBytes + FileUtils.getFolderSize(new File(f1.getFilePath()));
            } else {
                totalBytes = totalBytes + new File(f1.getFilePath()).length();
            }
        }

        this.totalBytes = totalBytes;
        calculatingTotalSize = false;
    }

    private void copyTrashFiles() {
        for (int i = 0; i < trashList.size(); i++) {
            String sourceFile = trashList.get(i).getDestination(); // Intentional : Destination will be source
            String destFile = trashList.get(i).getSource() + sourceFile.substring(sourceFile.lastIndexOf("/"), sourceFile.length());
            startCopy(sourceFile, destFile, move);
        }
    }

    private void startCopy(String sourceFile, String targetFile, boolean move) {
        if (new File(sourceFile).isDirectory()) {
            copyDirectory(sourceFile, targetFile, move);
        } else {
            copyFiles(sourceFile, targetFile);
        }
    }

    private void copyDirectory(String sourceFile, String targetFile, boolean move) {
        File destinationDir = new File(targetFile);
        boolean isExists = true;
        if (!destinationDir.exists()) {
            isExists = mkdir(destinationDir);
        }
        if (!isExists) {
            return;
        }

        ArrayList<FileInfo> filePaths = FileListLoader.getFilesList(sourceFile,
                                                                    false, true, false);
        for (FileInfo file : filePaths) {
            String path = file.getFilePath();
            String destFile = path + file.getFilePath().substring(sourceFile.lastIndexOf("/"), sourceFile.length());
            startCopy(file.getFilePath(), destFile, move);
        }

        if (filePaths.size() == 0) {
            deleteSourceFile(sourceFile, destinationDir);
            Intent intent = new Intent(COPY_PROGRESS);
            intent.putExtra(KEY_PROGRESS, 100);
            intent.putExtra(KEY_COMPLETED, 0L);
            intent.putExtra(KEY_TOTAL, totalBytes);
            intent.putExtra(KEY_COUNT, 1);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    // For trash
    private void copyFiles(String source, String destination) {

    }


    private void publishCompletionResult() {
        if (isCompleted) {
            return;
        }

        Logger.log(TAG, "publishCompletionResult: ");
        isCompleted = true;
        endNotification(NOTIFICATION_ID);
        Intent intent = new Intent(ACTION_OP_REFRESH);
        intent.putExtra(KEY_RESULT, failedFiles.size() == 0);
        intent.putExtra(KEY_OPERATION, move ? CUT : COPY);
        String newList[] = new String[filesToMediaIndex.size()];
        scanMultipleFiles(AceApplication.getAppContext(), filesToMediaIndex.toArray(newList));
        sendBroadcast(intent);
    }

    private void copyRoot(String path, String name, String destinationPath) {
        String targetPath;
        if (destinationPath.equals(File.separator)) {
            targetPath = destinationPath + name;
        } else {
            targetPath = destinationPath + File.separator + name;
        }
        try {
            RootUtils.mountRW(destinationPath);
            RootUtils.copy(path, targetPath);
            RootUtils.mountRO(destinationPath);
            if (FileUtils.isMediaScanningRequired(FileUtils.getMimeType(new File(targetPath)))) {
                filesToMediaIndex.add(targetPath);
            }
            copiedBytes++;
            calculateProgress(name);
        } catch (RootDeniedException e) {
            e.printStackTrace();
        }
    }

    private void startCopy(final FileInfo sourceFile, final FileInfo targetFile, final boolean move)
            throws IOException {
        if (sourceFile.isDirectory()) {
            copyDirectory(sourceFile, targetFile, move);
        } else {
            copyFiles(sourceFile, targetFile);
        }
    }

    private void copyDirectory(final FileInfo sourceFile, final FileInfo targetFile, boolean
            move) throws IOException {
        File destinationDir = new File(targetFile.getFilePath());
        boolean isExists = true;
        if (!destinationDir.exists()) {
            isExists = mkdir(destinationDir);
        }
        if (!isExists) {
            failedFiles.add(sourceFile);
            return;
        }

        ArrayList<FileInfo> filePaths = FileListLoader.getFilesList(sourceFile.getFilePath(),
                                                                    false, true, false);
        for (FileInfo file : filePaths) {
            if (stopService) {
                publishCompletionResult();
                break;
            }

            FileInfo destFile = new FileInfo(sourceFile.getCategory(), sourceFile.getFileName
                    (), sourceFile.getFilePath(),
                                             sourceFile.getDate(), sourceFile.getSize(), sourceFile.isDirectory(),
                                             sourceFile.getExtension(), sourceFile.getPermissions(), false);
            destFile.setFilePath(targetFile.getFilePath() + SEPARATOR + file.getFileName());
            startCopy(file, destFile, move);
        }
        if (filePaths.size() == 0) {
            deleteSourceFile(sourceFile.getFilePath(), destinationDir);
            Intent intent = new Intent(COPY_PROGRESS);
            intent.putExtra(KEY_PROGRESS, 100);
            intent.putExtra(KEY_COMPLETED, 0L);
            intent.putExtra(KEY_TOTAL, totalBytes);
            intent.putExtra(KEY_COUNT, 1);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

    }

    private void copyFiles(final FileInfo sourceFile, final FileInfo targetFile) throws
                                                                                 IOException {
        long size = new File(sourceFile.getFilePath()).length();

        Logger.log("Copy", "target file=" + targetFile.getFilePath());
        BufferedOutputStream out = null;
        try {
            File target = new File(targetFile.getFilePath());
            OutputStream outputStream = FileUtils.getOutputStream(target, context);
            if (outputStream != null) {
                out = new BufferedOutputStream(outputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        BufferedInputStream in = new BufferedInputStream(
                new FileInputStream(sourceFile.getFilePath()));
        if (out == null) {
            failedFiles.add(sourceFile);
            return;
        }
        copy(in, out, size, sourceFile, targetFile.getFilePath());

    }

    private long time = System.nanoTime() / 500000000;


    private void copy(BufferedInputStream in, BufferedOutputStream out, long size, FileInfo sourceFile,
                      String targetPath) throws IOException {
        String name = sourceFile.getFileName();
        long fileBytes = 0L;
        final int buffer = 2048; //2 KB
        byte[] data = new byte[2048];
        int length;
        //copy the file content in bytes
        while ((length = in.read(data, 0, buffer)) != -1 ) {
            if (stopService) {
                failedFiles.add(sourceFile);
                File targetFile = new File(targetPath);
                targetFile.delete();
                publishCompletionResult();
                break;
            }
            out.write(data, 0, length);
            copiedBytes += length;
            fileBytes += length;
            long time1 = System.nanoTime() / 500000000;
            if (((int) time1) > ((int) (time))) {
                calculateProgress(name);
                time = System.nanoTime() / 500000000;
            }
        }

        if (fileBytes == size) {
            count++;
            File targetFile = new File(targetPath);
            deleteSourceFile(sourceFile.getFilePath(), targetFile);
            if (FileUtils.isMediaScanningRequired(FileUtils.getMimeType(targetFile))) {
                filesToMediaIndex.add(targetPath);
            }
            Logger.log("CopyService", "Completed " + name + " KEY_COUNT=" + count);

            Intent intent = new Intent(COPY_PROGRESS);
            intent.putExtra(KEY_PROGRESS, 100);
            intent.putExtra(KEY_COMPLETED, copiedBytes);
            intent.putExtra(KEY_TOTAL, totalBytes);
            intent.putExtra(KEY_COUNT, count);
            int p1 = (int) ((copiedBytes / (float) totalBytes) * 100);
            intent.putExtra(KEY_TOTAL_PROGRESS, p1);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
        in.close();
        out.close();
    }

    private void deleteSourceFile(String sourcePath, File targetFile) {
        if (targetFile.exists() && (isCopyToTrash || isTrashRestore)) {
            File file = new File(sourcePath);
            if (isCopyToTrash) {
                TrashDbHelper.getInstance().addTrashData(new TrashModel(targetFile.getAbsolutePath(), file.getParent()));
            } else {
                TrashDbHelper.getInstance().deleteTrashData(file.getParent());
            }
            file.delete();
        }
    }

    private void calculateProgress(String name) {
        int p1 = (int) ((copiedBytes / (float) totalBytes) * 100);
        Logger.log("CopyService", "Copied=" + copiedBytes + " Totalbytes=" + totalBytes);
        if (calculatingTotalSize) {
            p1 = 0;
        }
        publishResults(name, p1, totalBytes, copiedBytes);
    }

    private void publishResults(String fileName, int p1, long total, long done) {
        //notification
        Logger.log("CopyService", "Total bytes=" + totalBytes + "Copied=" + copiedBytes +
                "Progress = " +
                p1);
        builder.setProgress(100, p1, false);
        builder.setOngoing(true);
        int title = R.string.copying;
        if (move) {
            title = R.string.moving;
        }
        builder.setContentTitle(getString(title));
        builder.setContentText(new File(fileName).getName() + " " + FileUtils.formatSize
                (context, done)  + SEPARATOR + FileUtils
                .formatSize(context, total));
        int id1 = NOTIFICATION_ID;
        notificationManager.notify(id1, builder.build());
        Intent intent = new Intent(COPY_PROGRESS);
        intent.putExtra(KEY_COMPLETED, copiedBytes);
        intent.putExtra(KEY_TOTAL, totalBytes);
        intent.putExtra(KEY_TOTAL_PROGRESS, p1);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        if (p1 == 100 || total == 0 || totalBytes == copiedBytes) {
            endNotification(id1);
        }
    }

    private void endNotification(int id1) {
        builder.setContentTitle("Copy completed");
        if (move) {
            builder.setContentTitle(getString(R.string.move_complete));
        }
        builder.setContentText("");
        builder.setProgress(0, 0, false);
        builder.setOngoing(false);
        builder.setAutoCancel(true);
        notificationManager.notify(id1, builder.build());
        try {
            notificationManager.cancel(id1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //check if copy is successful
    private boolean checkFiles(FileInfo oldFileInfo, FileInfo newFileInfo) {
        if (oldFileInfo.isDirectory()) {
            if (RootHelper.fileExists(newFileInfo.getFilePath())) {
                return false;
            }
            ArrayList<FileInfo> baseFiles = FileListLoader.getFilesList(oldFileInfo.getFilePath()
                    , true, true, false);
            if (baseFiles.size() > 0) {
                boolean b = true;
                for (FileInfo baseFile : baseFiles) {
                    baseFile.setFilePath(newFileInfo.getFilePath() + SEPARATOR + (baseFile
                            .getFileName
                                    ()));
                    if (!checkFiles(baseFile, baseFile)) {
                        b = false;
                    }
                }
                return b;
            }
            return RootHelper.fileExists(newFileInfo.getFilePath());
        } else {
            String parent = new File(oldFileInfo.getFilePath()).getParent();
            ArrayList<FileInfo> baseFiles = FileListLoader.getFilesList(parent, true, true, false);
            int i = -1;
            int index = -1;
            for (FileInfo b : baseFiles) {
                i++;
                if (b.getFilePath().equals(oldFileInfo.getFilePath())) {
                    index = i;
                    break;
                }
            }
            ArrayList<FileInfo> baseFiles1 = FileListLoader.getFilesList(parent, true, true, false);
            int i1 = -1;
            int index1 = -1;
            for (FileInfo b : baseFiles1) {
                i1++;
                if (b.getFilePath().equals(oldFileInfo.getFilePath())) {
                    index1 = i1;
                    break;
                }
            }
            return !(index == -1 || index1 == -1) && baseFiles.get(index).getSize() ==
                    (baseFiles1.get(index1)
                            .getSize());
        }
    }

    private void deleteCopiedFiles() {
        if (move) {
            ArrayList<FileInfo> toDelete = new ArrayList<>();
            for (FileInfo fileInfo : files) {
                if (!failedFiles.contains(fileInfo)) {
                    toDelete.add(fileInfo);
                }
            }
            Logger.log("Copy", "todel" + toDelete.size());

            DeleteTask deleteTask = new DeleteTask(context, toDelete);
            deleteTask.setmShowToast();
            deleteTask.delete();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        context = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
