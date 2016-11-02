/*
 * Copyright (C) 2014 Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>
 *
 * This file is part of Amaze File Manager.
 *
 * Amaze File Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.siju.acexplorer.filesystem.task;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.SparseBooleanArray;

import com.siju.acexplorer.BaseActivity;
import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.filesystem.model.BaseFile;
import com.siju.acexplorer.filesystem.model.CopyData;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.model.ZipProgressModel;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.helper.RootHelper;
import com.stericson.RootTools.RootTools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static com.siju.acexplorer.filesystem.utils.FileUtils.COPY_PROGRESS;

public class CopyService extends Service {
    private SparseBooleanArray hash = new SparseBooleanArray();
    private HashMap<Integer, ZipProgressModel> hash1 = new HashMap<>();
    private boolean rootmode;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private Context mContext;
    private final int NOTIFICATION_ID = 1000;
    private boolean foreground = true;
    private ArrayList<String> filesToMediaIndex = new ArrayList<>();


    @Override
    public void onCreate() {
        mContext = getApplicationContext();
/*        SharedPreferences Sp = PreferenceManager.getDefaultSharedPreferences(this);
        rootmode = Sp.getBoolean("rootmode", false);*/
        registerReceiver(receiver3, new IntentFilter("copycancel"));
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle b = new Bundle();
        ArrayList<FileInfo> files = intent.getParcelableArrayListExtra("FILE_PATHS");
        ArrayList<CopyData> copyData = intent.getParcelableArrayListExtra("ACTION");

        String currentDir = intent.getStringExtra("COPY_DIRECTORY");
        int mode = intent.getIntExtra("MODE", 0);
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        b.putInt("id", startId);
        Intent notificationIntent = new Intent(this, BaseActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationIntent.putExtra("openprocesses", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setContentTitle(getResources().getString(R.string.copying)).setSmallIcon(R.drawable.ic_copy_white);
        if (foreground) {
            startForeground(NOTIFICATION_ID + startId, mBuilder.build());
            foreground = false;
        }
        b.putBoolean("move", intent.getBooleanExtra("move", false));
        b.putString("current_dir", currentDir);
        b.putInt("MODE", mode);
        b.putParcelableArrayList("files", files);
        b.putParcelableArrayList("action", copyData);
        hash.put(startId, true);
        ZipProgressModel progressModel = new ZipProgressModel();
        progressModel.setName(files.get(0).getFileName());
        progressModel.setTotal(0);
        progressModel.setDone(0);
        progressModel.setId(startId);
        progressModel.setP1(0);
        progressModel.setP2(0);
        progressModel.setCompleted(false);
        progressModel.setMove(intent.getBooleanExtra("move", false));
        hash1.put(startId, progressModel);
        //going async
        new DoInBackground().execute(b);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    private ProgressListener progressListener;


    public void onDestroy() {
        this.unregisterReceiver(receiver3);
    }

    public class DoInBackground extends AsyncTask<Bundle, Void, Integer> {
        ArrayList<FileInfo> files;
        ArrayList<CopyData> copyData;

        boolean move;
        Copy copy;


        protected Integer doInBackground(Bundle... p1) {
            String currentDir = p1[0].getString("current_dir");
            int id = p1[0].getInt("id");
            files = p1[0].getParcelableArrayList("files");
            copyData = p1[0].getParcelableArrayList("action");

            move = p1[0].getBoolean("move");
            copy = new Copy();
            copy.execute(id, files, currentDir, move, copyData);
            return id;
        }

        @Override
        public void onPostExecute(Integer b) {
            copy.publishResults("", 0, 0, b, 0, 0, true, move);
//            generateNotification(copy.failedFOps, move);
            Intent intent = new Intent("refresh");
            intent.putExtra(FileConstants.IS_OPERATION_SUCCESS, copy.copy_successful);
            intent.putExtra(FileConstants.OPERATION, move ? FileConstants.MOVE : FileConstants.COPY);
            intent.putStringArrayListExtra(FileConstants.KEY_PATH,filesToMediaIndex);
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
            ArrayList<FileInfo> failedFOps;
            ArrayList<BaseFile> toDelete;
            boolean copy_successful;
            int count = 0;

            Copy() {
                copy_successful = true;
                failedFOps = new ArrayList<>();
                toDelete = new ArrayList<>();
            }

            long getTotalBytes(final ArrayList<FileInfo> files) {
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
                        } catch (Exception e) {
                        }
                        Copy.this.totalBytes = totalBytes;
                        calculatingTotalSize = false;
                    }
                }).run();

                return totalBytes;
            }

            void execute(int id, final ArrayList<FileInfo> files, final String currentDir, final boolean move,
                         ArrayList<CopyData> copyData) {
                Logger.log("TAG", "execute" + files.size() + " mode==" + new FileUtils().checkFolder(currentDir, mContext));

                if (new FileUtils().checkFolder(currentDir, mContext) == 1) {
                    getTotalBytes(files);
                    for (int i = 0; i < files.size(); i++) {
                        FileInfo sourceFile = files.get(i);
                        Log.e("Copy", "basefile\t" + sourceFile.getFilePath());
                        try {

                            if (hash.get(id)) {
                                if (!new File(files.get(i).getFilePath()).canRead() && rootmode) {
                                    copyRoot(files.get(i).getFilePath(), files.get(i).getFileName(), currentDir, move);
                                    continue;
                                }
                                FileInfo destFile = new FileInfo(sourceFile.getFileName(), sourceFile.getFilePath(),
                                        sourceFile.getDate(), sourceFile.getSize(), sourceFile.isDirectory(),
                                        sourceFile.getExtension(), sourceFile.getType(), sourceFile.getPermissions());
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

                } else if (rootmode) {
                    for (int i = 0; i < files.size(); i++) {
                        String path = files.get(i).getFilePath();
                        String name = files.get(i).getFileName();
                        copyRoot(path, name, currentDir, move);
                        FileInfo newFileInfo = files.get(i);
                        newFileInfo.setFilePath(currentDir + "/" + (newFileInfo.getFileName()));
                        if (checkFiles(files.get(i), newFileInfo)) {
                            failedFOps.add(files.get(i));
                        }
                    }

                } else {
                    for (FileInfo f : files)
                        failedFOps.add(f);
                }

                if (move) {
                    ArrayList<FileInfo> toDelete = new ArrayList<>();
                    for (FileInfo fileInfo : files) {
                        if (!failedFOps.contains(fileInfo))
                            toDelete.add(fileInfo);
                    }
                    Logger.log("Copy", "todel" + toDelete.size());

                    new DeleteTask(mContext, rootmode, toDelete,false).execute();
                }
            }

            boolean copyRoot(String path, String name, String FILE2, boolean move) {
                boolean b = RootTools.copyFile(RootHelper.getCommandLineString(path), RootHelper.getCommandLineString(FILE2) + "/" + name, true, true);
                if (!b && path.contains("/0/"))
                    b = RootTools.copyFile(RootHelper.getCommandLineString(path.replace("/0/", "/legacy/")), RootHelper.getCommandLineString(FILE2) + "/" + name, true, true);
                FileUtils.scanFile(mContext, FILE2 + "/" + name);
                return b;
            }

            private void copyFiles(final FileInfo sourceFile, final FileInfo targetFile, final int id, final boolean move)
                    throws IOException {
                Log.i("Copy", sourceFile.getFilePath());
                if (sourceFile.isDirectory()) {
                    if (!hash.get(id)) return;
                    File destinationDir = new File(targetFile.getFilePath());
                    if (!destinationDir.exists()) {
                        int mode = new FileUtils().checkFolder(destinationDir.getParent(), mContext);
                        //TODO Find way to show SAF dialog since in service its not possible.
/*                        if (mode == 2) {

                        }*/
                        if (mode == 1 || mode == 0)
                            FileUtils.mkdir(destinationDir, mContext);
//                        destinationDir.mkdir();
                    }
                    if (!destinationDir.exists()) {
                        Log.e("Copy", "cant make dir");
                        failedFOps.add(sourceFile);
                        copy_successful = false;
                        return;
                    }
//                    targetFile.setFileDate(sourceFile.lastModified());
                    if (!hash.get(id)) return;
                    ArrayList<FileInfo> filePaths = RootHelper.getFilesList(mContext, sourceFile.getFilePath(), false,
                            true, false);
                    for (FileInfo file : filePaths) {

                        FileInfo destFile = new FileInfo(sourceFile.getFileName(), sourceFile.getFilePath(),
                                sourceFile.getDate(), sourceFile.getSize(), sourceFile.isDirectory(),
                                sourceFile.getExtension(), sourceFile.getType(), sourceFile.getPermissions());
                        destFile.setFilePath(targetFile.getFilePath() + "/" + file.getFileName());
                        copyFiles(file, destFile, id, move);
                    }
                    if (filePaths.size() == 0) {
                        Intent intent = new Intent(COPY_PROGRESS);
                        intent.putExtra("PROGRESS", 100);
                        intent.putExtra("DONE", 0);
                        intent.putExtra("TOTAL", totalBytes);
                        intent.putExtra("COUNT", 1);
                        if (mProgressListener != null)
                            mProgressListener.onUpdate(intent);
                    }
                } else {
                    if (!hash.get(id)) return;
                    long size = new File(sourceFile.getFilePath()).length();

                    Logger.log("Copy", "target file=" + targetFile.getFilePath());
                    BufferedOutputStream out = null;
                    try {
                        File target = new File(targetFile.getFilePath());
                        long size1 = target.length();
                        OutputStream outputStream = FileUtils.getOutputStream(target, mContext, size1);
                        if (outputStream != null)
                            out = new BufferedOutputStream(outputStream);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    BufferedInputStream in = new BufferedInputStream(
                            new FileInputStream(sourceFile.getFilePath()));
                    if (in == null || out == null) {
                        Log.e("Copy", "streams null");
                        failedFOps.add(sourceFile);
                        copy_successful = false;
                        return;
                    }
                    if (!hash.get(id)) return;
                    copy(in, out, size, id, sourceFile.getFileName(), move,targetFile.getFilePath());
                }
            }


            long time = System.nanoTime() / 500000000;
            AsyncTask asyncTask;

            void calculateProgress(final String name, final long fileBytes, final int id, final long
                    size, final boolean move) {
                if (asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
                    asyncTask.cancel(true);
                asyncTask = new AsyncTask<Void, Void, Void>() {
                    int p1, p2;

                    @Override
                    protected Void doInBackground(Void... voids) {
                        p1 = (int) ((copiedBytes / (float) totalBytes) * 100);
                        Logger.log("CopyService", "Copied=" + copiedBytes + " Totalbytes=" + totalBytes);
                        p2 = (int) ((fileBytes / (float) size) * 100);
                        if (calculatingTotalSize) p1 = 0;
                        return null;
                    }

                    @Override
                    public void onPostExecute(Void v) {
                        publishResults(name, p1, p2, id, totalBytes, copiedBytes, false, move);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }


            void copy(BufferedInputStream in, BufferedOutputStream out, long size, int id, String name,
                      boolean move,String targetPath) throws IOException {
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
                            calculateProgress(name, fileBytes, id, size, move);
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

            private void publishResults(String fileName, int p1, int p2, int id, long total, long done, boolean b, boolean move) {
                if (hash.get(id)) {
                    //notification
                    Logger.log("CopyService", "Total bytes=" + totalBytes + "Copied=" + copiedBytes+"Progress = "+p1);
                    mBuilder.setProgress(100, p1, false);
                    mBuilder.setOngoing(true);
                    int title = R.string.copying;
                    if (move) title = R.string.moving;
                    mBuilder.setContentTitle(getString(title));
                    mBuilder.setContentText(new File(fileName).getName() + " " + FileUtils.formatSize(mContext, done) + "/" + FileUtils
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
                        publishCompletedResult(id, id1);
                    }
                    //for processviewer
                    ZipProgressModel progressModel = new ZipProgressModel();
                    progressModel.setName(fileName);
                    progressModel.setTotal(total);
                    progressModel.setDone(done);
                    progressModel.setId(id);
                    progressModel.setP1(p1);
                    progressModel.setP2(p2);
                    progressModel.setMove(move);
                    progressModel.setCompleted(b);
                    hash1.put(id, progressModel);

                    Intent intent = new Intent(COPY_PROGRESS);
                    intent.putExtra("DONE", copiedBytes);
                    intent.putExtra("TOTAL", totalBytes);
                    intent.putExtra("TOTAL_PROGRESS", p1);

                    if (mProgressListener != null)
                        mProgressListener.onUpdate(intent);

                } else publishCompletedResult(id, NOTIFICATION_ID + id);
            }

            private void publishCompletedResult(int id, int id1) {
                try {
                    mNotifyManager.cancel(id1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    //check if copy is successful
    private boolean checkFiles(FileInfo hFile1, FileInfo hFile2) {
        if (RootHelper.isDirectory(hFile1.getFilePath(), rootmode, 5)) {
            if (RootHelper.fileExists(mContext, hFile2.getFilePath())) return false;
            ArrayList<FileInfo> baseFiles = RootHelper.getFilesList(mContext, hFile1.getFilePath(), true, true, false);
            if (baseFiles.size() > 0) {
                boolean b = true;
                for (FileInfo baseFile : baseFiles) {
                    FileInfo newFileInfo = baseFile;
                    newFileInfo.setFilePath(hFile2.getFilePath() + "/" + (baseFile.getFileName()));
                    if (!checkFiles(baseFile, newFileInfo))
                        b = false;
                }
                return b;
            }
            return RootHelper.fileExists(mContext, hFile2.getFilePath());
        } else {
            String parent = new File(hFile1.getFilePath()).getParent();
            ArrayList<FileInfo> baseFiles = RootHelper.getFilesList(mContext, parent, true, true, false);
            int i = -1;
            int index = -1;
            for (FileInfo b : baseFiles) {
                i++;
                if (b.getFilePath().equals(hFile1.getFilePath())) {
                    index = i;
                    break;
                }
            }
            ArrayList<FileInfo> baseFiles1 = RootHelper.getFilesList(mContext, parent, true, true, false);
            int i1 = -1;
            int index1 = -1;
            for (FileInfo b : baseFiles1) {
                i1++;
                if (b.getFilePath().equals(hFile1.getFilePath())) {
                    index1 = i1;
                    break;
                }
            }
            return baseFiles.get(index).getNoOfFilesOrSize().equalsIgnoreCase(baseFiles1.get(index1).getNoOfFilesOrSize());
        }
    }

    private final IBinder mBinder = new LocalBinder();


    public class LocalBinder extends Binder {
        public CopyService getService() {
            // Return this instance of LocalService so clients can call public methods
            return CopyService.this;
        }
    }

    public interface ProgressListener {
        void onUpdate(ZipProgressModel zipProgressModel);

        void refresh();
    }

    private Progress mProgressListener;

    public interface Progress {
        void onUpdate(Intent intent);

    }


    public void registerProgressListener(Progress progress) {
        mProgressListener = progress;
    }

    private BroadcastReceiver receiver3 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //cancel operation
            hash.put(intent.getIntExtra("id", 1), false);
        }
    };
/*    //bind with processviewer
    RegisterCallback registerCallback = new RegisterCallback.Stub() {
        @Override
        public void registerCallBack(ProgressListener p) throws RemoteException {
            progressListener = p;
        }

        @Override
        public List<DataPackage> getCurrent() throws RemoteException {
            List<DataPackage> dataPackages = new ArrayList<>();
            for (int i : hash1.keySet()) {
                dataPackages.add(hash1.get(i));
            }
            return dataPackages;
        }
    };*/

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return mBinder;//registerCallback.asBinder();
    }
}
