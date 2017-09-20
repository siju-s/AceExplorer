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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.format.Formatter;
import android.util.Log;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import com.siju.acexplorer.view.AceActivity;
import com.siju.acexplorer.R;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.storage.modules.zip.ZipUtils;

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
import static com.siju.acexplorer.model.helper.FileOperations.mkdir;

public class ExtractService extends Service {
    private Context context;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    @Override
    public void onCreate() {
        context = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String file = intent.getStringExtra(KEY_FILEPATH);
        String newFile = intent.getStringExtra(KEY_FILEPATH2);

        Bundle bundle = new Bundle();
        bundle.putInt("id", startId);
        bundle.putString(KEY_FILEPATH, file);
        bundle.putString(KEY_FILEPATH2, newFile);

        Intent notificationIntent = new Intent(this, AceActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setContentTitle(getResources().getString(R.string.extracting))
                .setContentText(new File(file).getName())
                .setSmallIcon(R.drawable.ic_doc_compressed);
        new Doback().execute(bundle);
        return START_STICKY;
    }

    private void publishResults(String fileName, int p1, int id, long total, long done) {
        int NOTIFICATION_ID = 1000;
        mBuilder.setContentTitle(getResources().getString(R.string.extracting));
        mBuilder.setProgress(100, p1, false);
        mBuilder.setOngoing(true);
        mBuilder.setContentText(new File(fileName).getName() + " " + Formatter.formatFileSize
                (context, done) + "/" + Formatter.formatFileSize(context, total));
        int id1 = NOTIFICATION_ID + id;
        mNotifyManager.notify(id1, mBuilder.build());
        if (p1 == 100) {
            mBuilder.setContentTitle("Extract completed");
            mBuilder.setContentText(new File(fileName).getName() + " " + Formatter.formatFileSize(context, total));
            mBuilder.setProgress(0, 0, false);
            mBuilder.setOngoing(false);
            mNotifyManager.notify(id1, mBuilder.build());
            publishCompletedResult(id1);
        }

        Logger.log(ExtractService.this.getClass().getSimpleName(), "Progress=" + p1 + " done=" + done + " total="
                + total);
        Intent intent = new Intent(EXTRACT_PROGRESS);
        intent.putExtra(KEY_PROGRESS, p1);
        intent.putExtra(KEY_COMPLETED, done);
        intent.putExtra(KEY_TOTAL, total);
        intent.putExtra(KEY_FILENAME, fileName);
        if (mProgressListener != null) {
            mProgressListener.onUpdate(intent);
            if (p1 == 100) {
                mProgressListener = null;
            }
        }
    }

    private void publishCompletedResult(int id1) {
        try {
            mNotifyManager.cancel(id1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class Doback extends AsyncTask<Bundle, Void, Integer> {
        long copiedbytes = 0, totalbytes = 0;

        private void createDir(File dir) {
            mkdir(dir);
        }

        AsyncTask asyncTask;

        void calculateProgress(final String name, final int id) {
            calculateProgress(name, id, copiedbytes, totalbytes);
        }

        void calculateProgress(final String name, final int id, final long
                copiedbytes, final long totalbytes) {
            if (asyncTask != null && asyncTask.getStatus() == Status.RUNNING)
                asyncTask.cancel(true);
            asyncTask = new AsyncTask<Void, Void, Void>() {
                int p1;

                @Override
                protected Void doInBackground(Void... voids) {
                    if (isCancelled()) return null;
                    p1 = (int) ((copiedbytes / (float) totalbytes) * 100);
                    if (isCancelled()) return null;
                    return null;
                }

                @Override
                public void onPostExecute(Void v) {
                    publishResults(name, p1, id, totalbytes, copiedbytes);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }

        long time = System.nanoTime() / 500000000;

        void stop(int b) {
            stopSelf(b);
        }

        private void unzipEntry(int id, ZipFile zipfile, ZipEntry entry, String outputDir)
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
                        calculateProgress(zipfile.getName(), id);
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

        private void unzipRAREntry(int id, String a, Archive zipfile, FileHeader entry, String outputDir)
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
                        calculateProgress(a, id);
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

        private void unzipTAREntry(int id, TarArchiveInputStream zipfile, TarArchiveEntry entry, String outputDir,
                                   String string)
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
                        calculateProgress(string, id);
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

        void extract(int id, File archive, String destinationPath) {
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
                    unzipEntry(id, zipfile, entry, destinationPath);
                }
                Intent intent = new Intent(ACTION_RELOAD_LIST);
                intent.putExtra(KEY_OPERATION, EXTRACT);
                sendBroadcast(intent);
                calculateProgress(archive.getName(), id, copiedbytes, totalbytes);
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "Error while extracting file " + archive, e);
                Intent intent = new Intent(ACTION_OP_FAILED);
                intent.putExtra(KEY_OPERATION, EXTRACT);
                sendBroadcast(intent);
                mProgressListener = null;
                publishResults(archive.getName(), 100, id, totalbytes, copiedbytes);
            }

        }

        void extractTar(int id, File archive, String destinationPath) {
            try {
                ArrayList<TarArchiveEntry> archiveEntries = new ArrayList<>();
                TarArchiveInputStream inputStream;
                if (archive.getName().endsWith(".tar"))
                    inputStream = new TarArchiveInputStream(new BufferedInputStream(new FileInputStream(archive)));
                else
                    inputStream = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(archive)));
                publishResults(archive.getName(), 0, id, totalbytes, copiedbytes);
                TarArchiveEntry tarArchiveEntry = inputStream.getNextTarEntry();
                while (tarArchiveEntry != null) {

                    archiveEntries.add(tarArchiveEntry);
                    tarArchiveEntry = inputStream.getNextTarEntry();

                }
                for (TarArchiveEntry entry : archiveEntries) {
                    totalbytes = totalbytes + entry.getSize();
                }
                for (TarArchiveEntry entry : archiveEntries) {
                    unzipTAREntry(id, inputStream, entry, destinationPath, archive.getName());

                }

                inputStream.close();

                Intent intent = new Intent(ACTION_RELOAD_LIST);
                intent.putExtra(KEY_OPERATION, EXTRACT);
                sendBroadcast(intent);
                publishResults(archive.getName(), 100, id, totalbytes, copiedbytes);

            } catch (Exception e) {
                Log.e("TAG", "Error while extracting file " + archive, e);
                Intent intent = new Intent(ACTION_RELOAD_LIST);
                intent.putExtra(KEY_OPERATION, EXTRACT);
                sendBroadcast(intent);
                publishResults(archive.getName(), 100, id, totalbytes, copiedbytes);

            }

        }

        void extractRar(int id, File archive, String destinationPath) {
            try {
                ArrayList<FileHeader> arrayList = new ArrayList<>();
                Archive zipfile = new Archive(archive);
                FileHeader fh = zipfile.nextFileHeader();
                publishResults(archive.getName(), 0, id, totalbytes, copiedbytes);
                while (fh != null) {


                    arrayList.add(fh);
                    fh = zipfile.nextFileHeader();

                }
                for (FileHeader header : arrayList) {
                    totalbytes = totalbytes + header.getFullUnpackSize();
                }
                for (FileHeader header : arrayList) {

                    unzipRAREntry(id, archive.getName(), zipfile, header, destinationPath);

                }
                Intent intent = new Intent(ACTION_RELOAD_LIST);
                intent.putExtra(KEY_OPERATION, EXTRACT);
                sendBroadcast(intent);
                calculateProgress(archive.getName(), id, copiedbytes, totalbytes);

            } catch (Exception e) {
                Log.e("TAG", "Error while extracting file " + archive, e);
                Intent intent = new Intent(ACTION_RELOAD_LIST);
                intent.putExtra(KEY_OPERATION, EXTRACT);
                sendBroadcast(intent);
                calculateProgress(archive.getName(), id, copiedbytes, totalbytes);

            }
        }

        protected Integer doInBackground(Bundle... p1) {
            String zipFilePath = p1[0].getString(KEY_FILEPATH);
            String newFile = p1[0].getString(KEY_FILEPATH2);

            if (zipFilePath != null) {
                File zipFile = new File(zipFilePath);
                if (ZipUtils.isZipViewable(zipFilePath))
                    extract(p1[0].getInt("id"), zipFile, newFile);
                else if (zipFilePath.toLowerCase().endsWith(".rar"))
                    extractRar(p1[0].getInt("id"), zipFile, newFile);
                else if (zipFilePath.toLowerCase().endsWith(".tar") || zipFile.getName().toLowerCase().endsWith
                        (".tar.gz"))
                    extractTar(p1[0].getInt("id"), zipFile, newFile);
            }

            Logger.log("ExtractService", "ZIp file=" + zipFilePath + "new file=" + newFile);

            return p1[0].getInt("id");
        }

        @Override
        public void onPostExecute(Integer b) {
            stop(b);
        }


    }

    private final IBinder mBinder = new LocalBinder();
    private Progress mProgressListener;


    public class LocalBinder extends Binder {

        public ExtractService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ExtractService.this;
        }

    }

    public void registerProgressListener(Progress progress) {
        mProgressListener = progress;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }
}


