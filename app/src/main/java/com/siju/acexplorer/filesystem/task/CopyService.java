package com.siju.acexplorer.filesystem.task;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.SparseBooleanArray;

import com.siju.acexplorer.AceActivity;
import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.filesystem.model.CopyData;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.operations.OperationUtils;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.filesystem.utils.RootNotPermittedException;
import com.siju.acexplorer.filesystem.utils.RootUtils;
import com.siju.acexplorer.helper.RootHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import static com.siju.acexplorer.filesystem.operations.OperationProgress.COPY_PROGRESS;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.ACTION_OP_REFRESH;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_CONFLICT_DATA;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_FILEPATH;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_FILES;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_OPERATION;
import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_RESULT;
import static com.siju.acexplorer.filesystem.operations.Operations.COPY;
import static com.siju.acexplorer.filesystem.operations.Operations.CUT;

public class CopyService extends Service {
    private final SparseBooleanArray hash = new SparseBooleanArray();
    private boolean rootmode;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private Context mContext;
    private final int NOTIFICATION_ID = 1000;
    private boolean foreground = true;
    private final ArrayList<String> filesToMediaIndex = new ArrayList<>();


    @Override
    public void onCreate() {
        mContext = getApplicationContext();
        rootmode = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(FileConstants.PREFS_ROOTED,
                false);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle b = new Bundle();
        ArrayList<FileInfo> files = intent.getParcelableArrayListExtra(KEY_FILES);
        ArrayList<CopyData> copyData = intent.getParcelableArrayListExtra(KEY_CONFLICT_DATA);

        String currentDir = intent.getStringExtra(KEY_FILEPATH);
        int mode = intent.getIntExtra("MODE", 0);
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        b.putInt("id", startId);
        Intent notificationIntent = new Intent(this, AceActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setContentTitle(getResources().getString(R.string.copying)).setSmallIcon(R.drawable.ic_copy_white);
        if (foreground) {
            startForeground(NOTIFICATION_ID + startId, mBuilder.build());
            foreground = false;
        }
        b.putBoolean("move", intent.getBooleanExtra("move", false));
        b.putString(KEY_FILEPATH, currentDir);
        b.putInt("MODE", mode);
        b.putParcelableArrayList(KEY_FILES, files);
        b.putParcelableArrayList(KEY_CONFLICT_DATA, copyData);
        hash.put(startId, true);
        new DoInBackground().execute(b);

        return START_STICKY;
    }

    private class DoInBackground extends AsyncTask<Bundle, Void, Integer> {
        ArrayList<FileInfo> files;
        ArrayList<CopyData> copyData;
        boolean move;
        Copy copy;


        protected Integer doInBackground(Bundle... p1) {
            String currentDir = p1[0].getString(KEY_FILEPATH);
            int id = p1[0].getInt("id");
            files = p1[0].getParcelableArrayList(KEY_FILES);
            copyData = p1[0].getParcelableArrayList(KEY_CONFLICT_DATA);
            move = p1[0].getBoolean("move");
            copy = new Copy();
            copy.execute(id, files, currentDir, move, copyData);
            return id;
        }

        @Override
        public void onPostExecute(Integer b) {
            copy.publishResults("", 0, b, 0, 0, move);
            Intent intent = new Intent(ACTION_OP_REFRESH);
            intent.putExtra(KEY_RESULT, copy.copy_successful);
            intent.putExtra(KEY_OPERATION, move ? CUT : COPY);
            intent.putStringArrayListExtra(KEY_FILES, filesToMediaIndex);
            sendBroadcast(intent);
            if (mProgressListener != null) {
                mProgressListener.onUpdate(intent);
            }
            hash.put(b, false);
            boolean stop = true;
            for (int i = 0; i < hash.size(); i++) {
                int key = hash.keyAt(i);
                // get the object by the key.
                if (hash.get(key)) stop = false;
            }
            if (!stop)
                stopSelf(b);
            else stopSelf();

        }

        class Copy {

            long totalBytes = 0L, copiedBytes = 0L;
            boolean calculatingTotalSize = false;
            final ArrayList<FileInfo> failedFOps;
            boolean copy_successful;
            int count = 0;

            Copy() {
                copy_successful = true;
                failedFOps = new ArrayList<>();
            }

            void getTotalBytes(final ArrayList<FileInfo> files) {
                calculatingTotalSize = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long totalBytes = 0L;
                        try {
                            for (int i = 0; i < files.size(); i++) {
                                FileInfo f1 = (files.get(i));
                                if (f1.isDirectory()) {
                                    totalBytes = totalBytes + FileUtils.getFolderSize(new File(f1.getFilePath()));
                                } else {
                                    totalBytes = totalBytes + new File(f1.getFilePath()).length();
                                }
                            }
                        } catch (Exception ignored) {
                            //empty
                        }
                        Copy.this.totalBytes = totalBytes;
                        calculatingTotalSize = false;
                    }
                }).run();
            }

            void execute(int id, final ArrayList<FileInfo> files, final String currentDir, final boolean move,
                         ArrayList<CopyData> copyData) {


                if (OperationUtils.checkFolder(currentDir, mContext) == OperationUtils.WriteMode.INTERNAL) {
                    getTotalBytes(files);
                    for (int i = 0; i < files.size(); i++) {
                        FileInfo sourceFile = files.get(i);
                        Log.e("Copy", "basefile\t" + sourceFile.getFilePath());
                        try {

                            if (hash.get(id)) {
                                if (!new File(files.get(i).getFilePath()).canRead()) {
                                    copyRoot(files.get(i).getFilePath(), files.get(i).getFileName(), currentDir);
                                    continue;
                                }
                                FileInfo destFile = new FileInfo(sourceFile.getCategory(),sourceFile.getFileName(),
                                        sourceFile.getFilePath(),sourceFile.getDate(), sourceFile.getSize(), sourceFile.isDirectory(),
                                        sourceFile.getExtension(), sourceFile.getPermissions());
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
                                String path = currentDir + "/" + fileName;
                                if (action == FileUtils.ACTION_KEEP) {
                                    String fileNameWithoutExt = fileName.substring(0, fileName.
                                            lastIndexOf("."));
                                    path = currentDir + "/" + fileNameWithoutExt + "(2)" + "." + files.get(i)
                                            .getExtension();
                                }
                                destFile.setFilePath(path);
                                Logger.log("CopyService", "Execute-Dest file path=" + destFile.getFilePath());

                                copyFiles(sourceFile, destFile, id, move);
                            } else {
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("Copy", "Got exception checkout");

                            failedFOps.add(files.get(i));
                            for (int j = i + 1; j < files.size(); j++) failedFOps.add(files.get(j));
                            break;
                        }
                    }

                } else {
                    for (int i = 0; i < files.size(); i++) {
                        String path = files.get(i).getFilePath();
                        String name = files.get(i).getFileName();
                        copyRoot(path, name, currentDir);
                        FileInfo newFileInfo = files.get(i);
                        newFileInfo.setFilePath(currentDir + "/" + (newFileInfo.getFileName()));
                        if (checkFiles(files.get(i), newFileInfo)) {
                            failedFOps.add(files.get(i));
                        }
                    }

                }

                if (move) {
                    ArrayList<FileInfo> toDelete = new ArrayList<>();
                    for (FileInfo fileInfo : files) {
                        if (!failedFOps.contains(fileInfo))
                            toDelete.add(fileInfo);
                    }
                    Logger.log("Copy", "todel" + toDelete.size());

                    DeleteTask deleteTask = new DeleteTask(mContext, rootmode, toDelete);
                    deleteTask.setmShowToast();
                    deleteTask.execute();
                }
            }

            void copyRoot(String path, String name, String destinationPath) {
                String targetPath = destinationPath + File.separator + name;
                try {
                    // TODO: 04-01-2017 This causes the phone to brick. Fix it ASAP.
                    RootUtils.mountRW(destinationPath);
//                    RootUtils.mountOwnerRW(destinationPath);
//                    if (!move)

                    RootUtils.copy(path, targetPath);
                    RootUtils.mountRO(destinationPath);
//                    else if (move) RootUtils.move(path, targetPath);
                } catch (RootNotPermittedException e) {
//                    failedFOps.add(sourceFile);
                    e.printStackTrace();
                }
//                if (result) {
                FileUtils.scanFile(mContext, destinationPath + "/" + name);
//                }
            }

            private void copyFiles(final FileInfo sourceFile, final FileInfo targetFile, final int id, final boolean
                    move)
                    throws IOException {
                Log.i("Copy", sourceFile.getFilePath());
                if (sourceFile.isDirectory()) {
                    if (!hash.get(id)) return;
                    File destinationDir = new File(targetFile.getFilePath());
                    if (!destinationDir.exists()) {
/*                        int mode = new FileOpsHelper().checkFolder(destinationDir.getParent(), mContext);
                        //TODO Find way to show SAF dialog since in service its not possible.
*//*                        if (mode == 2) {

                        }*/
                            FileUtils.mkdir(destinationDir, mContext);
                    }
                    if (!destinationDir.exists()) {
                        Log.e("Copy", "cant make dir");
                        failedFOps.add(sourceFile);
                        copy_successful = false;
                        return;
                    }
//                    targetFile.setFileDate(sourceFile.lastModified());
                    if (!hash.get(id)) return;
                    ArrayList<FileInfo> filePaths = RootHelper.getFilesList(sourceFile.getFilePath(), false,
                            true, false);
                    for (FileInfo file : filePaths) {

                        FileInfo destFile = new FileInfo(sourceFile.getCategory(),sourceFile.getFileName(), sourceFile.getFilePath(),
                                sourceFile.getDate(), sourceFile.getSize(), sourceFile.isDirectory(),
                                sourceFile.getExtension(), sourceFile.getPermissions());
                        destFile.setFilePath(targetFile.getFilePath() + "/" + file.getFileName());
                        copyFiles(file, destFile, id, move);
                    }
                    if (filePaths.size() == 0) {
                        Intent intent = new Intent(COPY_PROGRESS);
                        intent.putExtra("PROGRESS", 100);
                        intent.putExtra("DONE", 0L);
                        intent.putExtra("TOTAL", totalBytes);
                        intent.putExtra("COUNT", 1);
                        if (mProgressListener != null) {
                            mProgressListener.onUpdate(intent);
                            if (totalBytes == 0) mProgressListener = null;
                        }
                    }
                } else {
                    if (!hash.get(id)) return;
                    long size = new File(sourceFile.getFilePath()).length();

                    Logger.log("Copy", "target file=" + targetFile.getFilePath());
                    BufferedOutputStream out = null;
                    try {
                        File target = new File(targetFile.getFilePath());
                        OutputStream outputStream = FileUtils.getOutputStream(target, mContext);
                        if (outputStream != null)
                            out = new BufferedOutputStream(outputStream);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    BufferedInputStream in = new BufferedInputStream(
                            new FileInputStream(sourceFile.getFilePath()));
                    if (out == null) {
                        Log.e("Copy", "streams null");
                        failedFOps.add(sourceFile);
                        copy_successful = false;
                        return;
                    }
                    if (!hash.get(id)) return;
                    copy(in, out, size, id, sourceFile.getFileName(), move, targetFile.getFilePath());
                }
            }


            long time = System.nanoTime() / 500000000;
            AsyncTask asyncTask;

            void calculateProgress(final String name, final int id, final boolean move) {
                if (asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
                    asyncTask.cancel(true);
                asyncTask = new AsyncTask<Void, Void, Void>() {
                    int p1;

                    @Override
                    protected Void doInBackground(Void... voids) {
                        p1 = (int) ((copiedBytes / (float) totalBytes) * 100);
                        Logger.log("CopyService", "Copied=" + copiedBytes + " Totalbytes=" + totalBytes);
                        if (calculatingTotalSize) p1 = 0;
                        return null;
                    }

                    @Override
                    public void onPostExecute(Void v) {
                        publishResults(name, p1, id, totalBytes, copiedBytes, move);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }


            void copy(BufferedInputStream in, BufferedOutputStream out, long size, int id, String name,
                      boolean move, String targetPath) throws IOException {
                long fileBytes = 0L;
                final int buffer = 2048; //2 KB
                byte[] data = new byte[2048];
                int length;
                //copy the file content in bytes
                while ((length = in.read(data, 0, buffer)) != -1) {
                    boolean b = hash.get(id);
                    if (b) {
                        out.write(data, 0, length);
                        copiedBytes += length;
                        fileBytes += length;
                        long time1 = System.nanoTime() / 500000000;
                        if (((int) time1) > ((int) (time))) {
                            calculateProgress(name, id, move);
                            time = System.nanoTime() / 500000000;
                        }


                    } else {
                        break;
                    }
                }


                if (fileBytes == size) {
                    count++;
                    if (FileUtils.isMediaScanningRequired(FileUtils.getMimeType(new File(targetPath)))) {
                        filesToMediaIndex.add(targetPath);
                    }
                    Logger.log("CopyService", "Completed " + name + " COUNT=" + count);

                    Intent intent = new Intent(COPY_PROGRESS);
                    intent.putExtra("PROGRESS", 100);
                    intent.putExtra("DONE", copiedBytes);
                    intent.putExtra("TOTAL", totalBytes);
                    intent.putExtra("COUNT", count);
                    int p1 = (int) ((copiedBytes / (float) totalBytes) * 100);
                    intent.putExtra("TOTAL_PROGRESS", p1);

                    if (mProgressListener != null) {
                        mProgressListener.onUpdate(intent);
                        if (copiedBytes == totalBytes) mProgressListener = null;
                    }

                }
                in.close();
                out.close();
            }

            private void publishResults(String fileName, int p1, int id, long total, long done,
                                        boolean move) {
                if (hash.get(id)) {
                    //notification
                    Logger.log("CopyService", "Total bytes=" + totalBytes + "Copied=" + copiedBytes + "Progress = " +
                            p1);
                    mBuilder.setProgress(100, p1, false);
                    mBuilder.setOngoing(true);
                    int title = R.string.copying;
                    if (move) title = R.string.moving;
                    mBuilder.setContentTitle(getString(title));
                    mBuilder.setContentText(new File(fileName).getName() + " " + FileUtils.formatSize(mContext, done)
                            + "/" + FileUtils
                            .formatSize(mContext, total));
                    int id1 = NOTIFICATION_ID + id;
                    mNotifyManager.notify(id1, mBuilder.build());
                    if (p1 == 100 || total == 0 || totalBytes == copiedBytes) {
                        mBuilder.setContentTitle("Copy completed");
                        if (move)
                            mBuilder.setContentTitle("Move Completed");
                        mBuilder.setContentText("");
                        mBuilder.setProgress(0, 0, false);
                        mBuilder.setOngoing(false);
                        mBuilder.setAutoCancel(true);
                        mNotifyManager.notify(id1, mBuilder.build());
                        publishCompletedResult(id1);
                    }

                    Intent intent = new Intent(COPY_PROGRESS);
                    intent.putExtra("DONE", copiedBytes);
                    intent.putExtra("TOTAL", totalBytes);
                    intent.putExtra("TOTAL_PROGRESS", p1);

                    if (mProgressListener != null)
                        mProgressListener.onUpdate(intent);

                    if (copiedBytes == totalBytes) {
                        mProgressListener = null;
                    }

                } else publishCompletedResult(NOTIFICATION_ID + id);
            }

            private void publishCompletedResult(int id1) {
                try {
                    mNotifyManager.cancel(id1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    //check if copy is successful
    private boolean checkFiles(FileInfo oldFileInfo, FileInfo newFileInfo) {
        if (oldFileInfo.isDirectory()) {
            if (RootHelper.fileExists(newFileInfo.getFilePath())) return false;
            ArrayList<FileInfo> baseFiles = RootHelper.getFilesList(oldFileInfo.getFilePath(), true, true, false);
            if (baseFiles.size() > 0) {
                boolean b = true;
                for (FileInfo baseFile : baseFiles) {
                    baseFile.setFilePath(newFileInfo.getFilePath() + "/" + (baseFile.getFileName()));
                    if (!checkFiles(baseFile, baseFile))
                        b = false;
                }
                return b;
            }
            return RootHelper.fileExists(newFileInfo.getFilePath());
        } else {
            String parent = new File(oldFileInfo.getFilePath()).getParent();
            ArrayList<FileInfo> baseFiles = RootHelper.getFilesList(parent, true, true, false);
            int i = -1;
            int index = -1;
            for (FileInfo b : baseFiles) {
                i++;
                if (b.getFilePath().equals(oldFileInfo.getFilePath())) {
                    index = i;
                    break;
                }
            }
            ArrayList<FileInfo> baseFiles1 = RootHelper.getFilesList(parent, true, true, false);
            int i1 = -1;
            int index1 = -1;
            for (FileInfo b : baseFiles1) {
                i1++;
                if (b.getFilePath().equals(oldFileInfo.getFilePath())) {
                    index1 = i1;
                    break;
                }
            }
            return !(index == -1 || index1 == -1) && baseFiles.get(index).getSize() == (baseFiles1.get(index1)
                    .getSize());
        }
    }

    private final IBinder mBinder = new LocalBinder();


    public class LocalBinder extends Binder {
        public CopyService getService() {
            // Return this instance of LocalService so clients can call public methods
            return CopyService.this;
        }
    }

    private Progress mProgressListener;


    public void registerProgressListener(Progress progress) {
        mProgressListener = progress;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;//registerCallback.asBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
    }
}
