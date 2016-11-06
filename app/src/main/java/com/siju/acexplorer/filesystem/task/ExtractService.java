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
import android.text.format.Formatter;
import android.util.Log;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import com.siju.acexplorer.BaseActivity;
import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.filesystem.model.ZipProgressModel;
import com.siju.acexplorer.filesystem.utils.FileUtils;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ExtractService extends Service {
    // --Commented out by Inspection (06-11-2016 11:23 PM):public final String EXTRACT_CONDITION = "EXTRACT_CONDITION";


    private Context c;
    // Binder given to clients
    private final HashMap<Integer, Boolean> hash = new HashMap<>();
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private ArrayList<String> entries = new ArrayList<>();
    private boolean eentries;

    @Override
    public void onCreate() {
        c = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle b = new Bundle();
        b.putInt("id", startId);
        String epath = PreferenceManager.getDefaultSharedPreferences(this).getString("extractpath", "");
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String file = intent.getStringExtra("zip");
        String newFile = intent.getStringExtra("new_path");
        eentries = intent.getBooleanExtra("entries1", false);
        if (eentries) {
            entries = intent.getStringArrayListExtra("entries");
        }
        b.putString("file", file);
        b.putString("new_path", newFile);

        ZipProgressModel progressModel = new ZipProgressModel();
        progressModel.setName(file);
        progressModel.setTotal(0);
        progressModel.setDone(0);
        progressModel.setId(startId);
        progressModel.setP1(0);
        progressModel.setCompleted(false);
        Intent notificationIntent = new Intent(this, BaseActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.putExtra("openprocesses", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        mBuilder = new NotificationCompat.Builder(c);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setContentTitle(getResources().getString(R.string.extracting))
                .setContentText(new File(file).getName())
                .setSmallIcon(R.drawable.ic_doc_compressed);
        hash.put(startId, true);
        new Doback().execute(b);
        return START_STICKY;
    }

    private final IBinder mBinder = new LocalBinder();

    private class LocalBinder extends Binder {

        public ExtractService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ExtractService.this;
        }

    }


    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }


    private ProgressListener progressListener;

    public interface ProgressListener {
        void onUpdate(ZipProgressModel zipProgressModel);

        void refresh();
    }


    private void publishResults(String fileName, int p1, int id, long total, long done, boolean b) {
        int NOTIFICATION_ID = 1000;
        if (hash.get(id)) {
            mBuilder.setContentTitle(getResources().getString(R.string.extracting));
            mBuilder.setProgress(100, p1, false);
            mBuilder.setOngoing(true);
            mBuilder.setContentText(new File(fileName).getName() + " " + Formatter.formatFileSize
                    (c, done) + "/" + Formatter.formatFileSize(c, total));
            int id1 = NOTIFICATION_ID + id;
            mNotifyManager.notify(id1, mBuilder.build());
            if (p1 == 100) {
                mBuilder.setContentTitle("Extract completed");
                mBuilder.setContentText(new File(fileName).getName() + " " + Formatter.formatFileSize(c, total));
                mBuilder.setProgress(0, 0, false);
                mBuilder.setOngoing(false);
                mNotifyManager.notify(id1, mBuilder.build());
                publishCompletedResult(id1);
            }
            ZipProgressModel progressModel = new ZipProgressModel();
            progressModel.setName(fileName);
            progressModel.setTotal(total);
            progressModel.setDone(done);
            progressModel.setId(id);
            progressModel.setP1(p1);
            progressModel.setCompleted(b);
            if (progressListener != null) {
                progressListener.onUpdate(progressModel);
                if (b) progressListener.refresh();
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

    public class Doback extends AsyncTask<Bundle, Void, Integer> {
        long copiedbytes = 0, totalbytes = 0;
        int lastpercent = 0;

        private void createDir(File dir) {
            FileUtils.mkdir(dir, c);
        }

        AsyncTask asyncTask;

        void calculateProgress(final String name, final int id, final boolean completed) {
            calculateProgress(name, id, completed, copiedbytes, totalbytes);
        }

        void calculateProgress(final String name, final int id, final boolean completed, final long
                copiedbytes, final long totalbytes) {
            if (asyncTask != null && asyncTask.getStatus() == Status.RUNNING) asyncTask.cancel(true);
            asyncTask = new AsyncTask<Void, Void, Void>() {
                int p1; // --Commented out by Inspection (06-11-2016 11:23 PM):p2;

                @Override
                protected Void doInBackground(Void... voids) {
                    if (isCancelled()) return null;
                    p1 = (int) ((copiedbytes / (float) totalbytes) * 100);
                    lastpercent = (int) copiedbytes;
                    if (isCancelled()) return null;
                    return null;
                }

                @Override
                public void onPostExecute(Void v) {
                    publishResults(name, p1, id, totalbytes, copiedbytes, completed);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }

        long time = System.nanoTime() / 500000000;

        void stop(int b) {
            hash.put(b, false);
            boolean stop = true;
            for (int a : hash.keySet()) {
                if (hash.get(a)) stop = false;
            }
            if (stop)
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
                    FileUtils.getOutputStream(outputFile, c));
            Logger.log("ExtractService", "zipfile=" + zipfile + " zipentry=" + entry + " stream=" + inputStream);

            try {
                int len;
                byte buf[] = new byte[20480];
                while ((len = inputStream.read(buf)) > 0) {
                    //System.out.println(id + " " + hash.get(id));
                    if (hash.get(id)) {
                        outputStream.write(buf, 0, len);
                        copiedbytes = copiedbytes + len;

                        long time1 = System.nanoTime() / 500000000;
                        if (((int) time1) > ((int) (time))) {
                            calculateProgress(zipfile.getName(), id, false);
                            time = System.nanoTime() / 500000000;
                        }
                    } else {

                        calculateProgress(zipfile.getName(), id, true, copiedbytes, totalbytes);
                        cancel(true);
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
                    FileUtils.getOutputStream(outputFile, c));
            try {
                int len;
                byte buf[] = new byte[20480];
                while ((len = inputStream.read(buf)) > 0) {
                    //System.out.println(id + " " + hash.get(id));
                    if (hash.get(id)) {
                        outputStream.write(buf, 0, len);
                        copiedbytes = copiedbytes + len;
                        long time1 = System.nanoTime() / 500000000;
                        if (((int) time1) > ((int) (time))) {
                            calculateProgress(a, id, false);
                            time = System.nanoTime() / 500000000;
                        }
                    } else {
                        calculateProgress(a, id, true);
                        cancel(true);
                        stop(id);
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
                    //System.out.println(id + " " + hash.get(id));
                    if (hash.get(id)) {
                        outputStream.write(buf, 0, len);
                        copiedbytes = copiedbytes + len;
                        long time1 = System.nanoTime() / 500000000;
                        if (((int) time1) > ((int) (time))) {
                            calculateProgress(string, id, false);
                            time = System.nanoTime() / 500000000;
                        }
                    } else {
                        calculateProgress(string, id, true);
                        cancel(true);
                        stop(id);
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

        public void extract(int id, File archive, String destinationPath, ArrayList<String> x) {
            int i = 0;
            ArrayList<ZipEntry> entry1 = new ArrayList<>();
            try {
                ZipFile zipfile = new ZipFile(archive);
                calculateProgress(archive.getName(), id, false, copiedbytes, totalbytes);
                for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                    if (hash.get(id)) {
                        ZipEntry entry = (ZipEntry) e.nextElement();
                        for (String y : x) {
                            if (y.endsWith("/")) {
                                if (entry.getName().contains(y)) entry1.add(entry);
                            } else {
                                if (entry.getName().equals(y) || ("/" + entry.getName()).equals(y)) {
                                    entry1.add(entry);
                                }
                            }
                        }
                        i++;
                    } else {
                        cancel(true);
                        stop(id);

                    }
                }
                for (ZipEntry entry : entry1) {
                    totalbytes = totalbytes + entry.getSize();
                }
                for (ZipEntry entry : entry1) {
                    unzipEntry(id, zipfile, entry, destinationPath);
                }
                Intent intent = new Intent(FileConstants.RELOAD_LIST);
                sendBroadcast(intent);
                calculateProgress(archive.getName(), id, true, copiedbytes, totalbytes);
            } catch (Exception e) {
                Log.e("TAG", "Error while extracting file " + archive, e);
                Intent intent = new Intent(FileConstants.RELOAD_LIST);
                sendBroadcast(intent);
                calculateProgress(archive.getName(), id, true, copiedbytes, totalbytes);
            }

        }

        void extract(int id, File archive, String destinationPath) {
            try {
                ArrayList<ZipEntry> arrayList = new ArrayList<>();
                ZipFile zipfile = new ZipFile(archive);
                calculateProgress(archive.getName(), id, false, copiedbytes, totalbytes);
                for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                    if (hash.get(id)) {
                        ZipEntry entry = (ZipEntry) e.nextElement();
                        arrayList.add(entry);
                    } else {
                        stop(id);

                    }
                }
                for (ZipEntry entry : arrayList) {
                    totalbytes = totalbytes + entry.getSize();
                }
                for (ZipEntry entry : arrayList) {
                    if (hash.get(id)) {
                        unzipEntry(id, zipfile, entry, destinationPath);

                    } else {
                        stop(id);

                    }
                }
                Intent intent = new Intent(FileConstants.RELOAD_LIST);
                sendBroadcast(intent);
                calculateProgress(archive.getName(), id, true, copiedbytes, totalbytes);
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "Error while extracting file " + archive, e);
                publishResults(archive.getName(), 100, id, totalbytes, copiedbytes, true);
            }

        }

        void extractTar(int id, File archive, String destinationPath) {
            try {
                ArrayList<TarArchiveEntry> archiveEntries = new ArrayList<>();
                TarArchiveInputStream inputStream;
                if (archive.getName().endsWith(".tar"))
                    inputStream = new TarArchiveInputStream(new BufferedInputStream(new FileInputStream(archive)));
                else inputStream = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(archive)));
                publishResults(archive.getName(), 0, id, totalbytes, copiedbytes, false);
                TarArchiveEntry tarArchiveEntry = inputStream.getNextTarEntry();
                while (tarArchiveEntry != null) {
                    if (hash.get(id)) {

                        archiveEntries.add(tarArchiveEntry);
                        tarArchiveEntry = inputStream.getNextTarEntry();
                    } else {
                        stop(id);

                    }
                }
                for (TarArchiveEntry entry : archiveEntries) {
                    totalbytes = totalbytes + entry.getSize();
                }
                for (TarArchiveEntry entry : archiveEntries) {
                    if (hash.get(id)) {
                        unzipTAREntry(id, inputStream, entry, destinationPath, archive.getName());
                    } else {
                        stop(id);

                    }
                }

                inputStream.close();

                Intent intent = new Intent(FileConstants.RELOAD_LIST);
                sendBroadcast(intent);
                publishResults(archive.getName(), 100, id, totalbytes, copiedbytes, true);

            } catch (Exception e) {
                Log.e("TAG", "Error while extracting file " + archive, e);
                Intent intent = new Intent(FileConstants.RELOAD_LIST);
                sendBroadcast(intent);
                publishResults(archive.getName(), 100, id, totalbytes, copiedbytes, true);

            }

        }

        void extractRar(int id, File archive, String destinationPath) {
            try {
                ArrayList<FileHeader> arrayList = new ArrayList<>();
                Archive zipfile = new Archive(archive);
                FileHeader fh = zipfile.nextFileHeader();
                publishResults(archive.getName(), 0, id, totalbytes, copiedbytes, false);
                while (fh != null) {
                    if (hash.get(id)) {

                        arrayList.add(fh);
                        fh = zipfile.nextFileHeader();
                    } else {
                        stop(id);

                    }
                }
                for (FileHeader header : arrayList) {
                    totalbytes = totalbytes + header.getFullUnpackSize();
                }
                for (FileHeader header : arrayList) {
                    if (hash.get(id)) {
                        unzipRAREntry(id, archive.getName(), zipfile, header, destinationPath);
                    } else {
                        stop(id);

                    }
                }
                Intent intent = new Intent(FileConstants.RELOAD_LIST);
                sendBroadcast(intent);
                calculateProgress(archive.getName(), id, true, copiedbytes, totalbytes);

            } catch (Exception e) {
                Log.e("TAG", "Error while extracting file " + archive, e);
                Intent intent = new Intent(FileConstants.RELOAD_LIST);
                sendBroadcast(intent);
                calculateProgress(archive.getName(), id, true, copiedbytes, totalbytes);

            }
        }

        protected Integer doInBackground(Bundle... p1) {
            String file = p1[0].getString("file");
            String newFile = p1[0].getString("new_path");


            File f = new File(file);

            Log.d("TAG", "ZIp file=" + file + "new file=" + newFile);
            /*if (epath.length() == 0) {
                path = f.getParent() + "/" + f.getName().substring(0, f.getName().lastIndexOf("."));
            } else {
                if (epath.endsWith("/")) {
                    path = epath + f.getName().substring(0, f.getName().lastIndexOf("."));
                } else {
                    path = epath + "/" + f.getName().substring(0, f.getName().lastIndexOf("."));
                }
            }*/
            if (eentries) {
                extract(p1[0].getInt("id"), f, newFile, entries);
            } else if (f.getName().toLowerCase().endsWith(".zip") || f.getName().toLowerCase().endsWith(".jar") || f
                    .getName().toLowerCase().endsWith(".apk"))
                extract(p1[0].getInt("id"), f, newFile);
            else if (f.getName().toLowerCase().endsWith(".rar"))
                extractRar(p1[0].getInt("id"), f, newFile);
            else if (f.getName().toLowerCase().endsWith(".tar") || f.getName().toLowerCase().endsWith(".tar.gz"))
                extractTar(p1[0].getInt("id"), f, newFile);
            Log.d("TAG", "TAR");
            return p1[0].getInt("id");
        }

        @Override
        public void onPostExecute(Integer b) {
            stop(b);
        }


    }


    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }
}


