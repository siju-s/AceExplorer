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
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.R;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.main.model.data.FileDataFetcher;
import com.siju.acexplorer.main.model.helper.FileUtils;
import com.siju.acexplorer.main.model.helper.LargeBundleTransfer;
import com.siju.acexplorer.main.model.helper.RootHelper;
import com.siju.acexplorer.main.model.root.RootDeniedException;
import com.siju.acexplorer.main.model.root.RootUtils;
import com.siju.acexplorer.storage.model.CopyData;
import com.siju.acexplorer.storage.model.operations.OperationProgress;
import com.siju.acexplorer.storage.model.operations.OperationUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.siju.acexplorer.main.model.helper.FileOperations.mkdir;
import static com.siju.acexplorer.main.model.helper.MediaStoreHelper.scanMultipleFiles;
import static com.siju.acexplorer.main.model.helper.SdkHelper.isAtleastOreo;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_OP_REFRESH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_CONFLICT_DATA;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_END;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES_COUNT;
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

    private static final String TAG             = "CopyService";
    private final        int    NOTIFICATION_ID = 1000;
    private final        String SEPARATOR       = "/";
    private final        String CHANNEL_ID      = "operation";


    private Context                    context;
    private NotificationManager        notificationManager;
    private NotificationCompat.Builder builder;

    private final ArrayList<String> filesToMediaIndex = new ArrayList<>();
    private final List<FileInfo>    failedFiles       = new ArrayList<>();
    private List<FileInfo> files;
    private List<CopyData> copyData;

    private long totalBytes = 0L, copiedBytes = 0L;
    private int count = 0;
    private boolean        move;
    private boolean        calculatingTotalSize;
    private ServiceHandler serviceHandler;
    private boolean        stopService;
    private boolean        isCompleted;
    private int filesCopied;


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
                PendingIntent.getService(context, NOTIFICATION_ID, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_cancel, getString(R.string.dialog_cancel), pendingCancelIntent));

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
        copyData = intent.getParcelableArrayListExtra(KEY_CONFLICT_DATA);
        move = intent.getBooleanExtra(KEY_MOVE, false);

        String currentDir = intent.getStringExtra(KEY_FILEPATH);

        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = currentDir;
        serviceHandler.sendMessage(msg);
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
                onInternalStorage(currentDir);
                publishCompletionResult();
                break;

            case ROOT:
                onRoot(currentDir);
                publishCompletionResult();
                break;
        }
        deleteCopiedFiles();
    }

    private void onRoot(String currentDir) {
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
    }

    private void onInternalStorage(String currentDir) {
        getTotalBytes(files);
        for (int index = 0; index < files.size(); index++) {
            FileInfo sourceFile = files.get(index);
            if (stopService) {
                publishCompletionResult();
                break;
            }
            try {
                if (isNonReadable(index)) {
                    copyRoot(files.get(index).getFilePath(), files.get(index).getFileName(),
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
                    action = getAction(sourceFile, action);
                }
                String path = getNewPathForAction(currentDir, index, action);
                destFile.setFilePath(path);
                Logger.log("CopyService", "Execute-Dest file path=" + destFile
                        .getFilePath());

                startCopy(sourceFile, destFile, move);

            } catch (Exception e) {
                e.printStackTrace();
                populateFailedFiles(index);
                break;
            }
        }
    }

    private String getNewPathForAction(String currentDir, int index, int action) {
        String fileName = files.get(index).getFileName();
        String path = currentDir + SEPARATOR + fileName;

        if (action == FileUtils.ACTION_KEEP) {
            boolean isDirectory = new File(path).isDirectory();
            if (isDirectory) {
                path = getNewFileName(currentDir, fileName, true, files.get(index).getExtension());
            }
            else {
                String fileNameWithoutExt = fileName.substring(0, fileName.
                        lastIndexOf("."));
                path = currentDir + SEPARATOR + getNewFileName(currentDir, fileNameWithoutExt, false, files.get(index).getExtension());
            }
        }
        return path;
    }

    private int getAction(FileInfo sourceFile, int action) {
        for (CopyData copyData1 : copyData) {
            if (copyData1.getFilePath().equals(sourceFile.getFilePath())) {
                action = copyData1.getAction();
                break;
            }
        }
        return action;
    }

    private void populateFailedFiles(int index) {
        failedFiles.add(files.get(index));
        for (int j = index + 1; j < files.size(); j++) {
            failedFiles.add(files.get(j));
        }
    }

    private boolean isNonReadable(int i) {
        return !new File(files.get(i).getFilePath()).canRead();
    }

    private String getNewFileName(String currentDir, String fileName, boolean isDirectory, String extension) {
       File file = new File(currentDir);
       List<String> files = Arrays.asList(file.list());
       int suffix = 1;
        String newFileName = fileName + " " + "(" + suffix + ")";
        if (isDirectory) {
           return getNewDirectoryName(files, suffix, newFileName, fileName);
       }
       else {
           return getFileName(files, suffix, newFileName, fileName, extension);
       }
    }

    private String getFileName(List<String> files, int suffix, String fileName, String originalFileName, String extension) {
        if (files.contains(fileName + "." + extension)) {
            suffix++;
            String newFileName = originalFileName + " " + "(" + suffix + ")";
            return getFileName(files, suffix, newFileName, originalFileName, extension);
        }
        else {
            return fileName + "." + extension;
        }
    }

    private String getNewDirectoryName(List<String> files, int suffix, String fileName, String originalFileName) {
        if (files.contains(fileName)) {
            suffix++;
            String newFileName = originalFileName + " " + "(" + suffix + ")";
            return getNewDirectoryName(files, suffix, newFileName, originalFileName);
        }
        else {
            return fileName;
        }
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

        ArrayList<FileInfo> filePaths = FileDataFetcher.Companion.getFilesList(sourceFile,
                                                                    false, true, false);
        for (FileInfo file : filePaths) {
            String path = file.getFilePath();
            String destFile = path + file.getFilePath().substring(sourceFile.lastIndexOf("/"), sourceFile.length());
            startCopy(file.getFilePath(), destFile, move);
        }

        if (filePaths.size() == 0) {
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

    private void dismissProgressDialog() {
        Intent intent = new Intent(COPY_PROGRESS);
        intent.putExtra(KEY_END, true);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    private void publishCompletionResult() {
        if (isCompleted) {
            return;
        }
        Logger.log(TAG, "publishCompletionResult: ");
        isCompleted = true;
        endNotification();
        if (stopService) {
            dismissProgressDialog();
        }
        Intent intent = new Intent(ACTION_OP_REFRESH);
        intent.putExtra(KEY_FILES_COUNT, filesCopied);
        intent.putExtra(KEY_RESULT, failedFiles.size() == 0);
        intent.putExtra(KEY_OPERATION, move ? CUT : COPY);
        String[] newList = new String[filesToMediaIndex.size()];
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

        ArrayList<FileInfo> filePaths = FileDataFetcher.Companion.getFilesList(sourceFile.getFilePath(),
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
        while ((length = in.read(data, 0, buffer)) != -1) {
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
            filesCopied++;
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
                (context, done) + SEPARATOR + FileUtils
                .formatSize(context, total));
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        Intent intent = new Intent(COPY_PROGRESS);
        intent.putExtra(KEY_COMPLETED, copiedBytes);
        intent.putExtra(KEY_TOTAL, totalBytes);
        intent.putExtra(KEY_TOTAL_PROGRESS, p1);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        if (p1 == 100 || total == 0 || totalBytes == copiedBytes) {
            endNotification();
        }
    }

    private void endNotification() {
       notificationManager.cancel(NOTIFICATION_ID);
    }

    //check if copy is successful
    private boolean checkFiles(FileInfo oldFileInfo, FileInfo newFileInfo) {
        if (oldFileInfo.isDirectory()) {
            if (RootHelper.fileExists(newFileInfo.getFilePath())) {
                return false;
            }
            ArrayList<FileInfo> baseFiles = FileDataFetcher.Companion.getFilesList(oldFileInfo.getFilePath()
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
            ArrayList<FileInfo> baseFiles = FileDataFetcher.Companion.getFilesList(parent, true, true, false);
            int i = -1;
            int index = -1;
            for (FileInfo b : baseFiles) {
                i++;
                if (b.getFilePath().equals(oldFileInfo.getFilePath())) {
                    index = i;
                    break;
                }
            }
            ArrayList<FileInfo> baseFiles1 = FileDataFetcher.Companion.getFilesList(parent, true, true, false);
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
