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
import android.support.v4.app.NotificationCompat;
import android.text.format.Formatter;

import com.siju.acexplorer.BaseActivity;
import com.siju.acexplorer.R;
import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.utils.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.siju.acexplorer.filesystem.utils.FileUtils.ZIP_PROGRESS;

public class CreateZipTask extends Service {

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private Context context;
    private final int NOTIFICATION_ID = 1000;

    @Override
    public void onCreate() {
        context = getApplicationContext();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle b = new Bundle();
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String name = intent.getStringExtra("name");
        File zipName = new File(name);
        if (!zipName.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                zipName.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mBuilder = new NotificationCompat.Builder(this);
        Intent notificationIntent = new Intent(this, BaseActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setContentTitle(getResources().getString(R.string.zip_progress_title))
                .setSmallIcon(R.drawable.ic_archive_white);

        startForeground(NOTIFICATION_ID + startId, mBuilder.build());

        ArrayList<FileInfo> zipFiles = intent.getParcelableArrayListExtra("files");
        b.putInt("id", startId);
        b.putParcelableArrayList("files", zipFiles);
        b.putString("name", name);
        new Doback().execute(b);

        return START_STICKY;
    }


    private class Doback extends AsyncTask<Bundle, Void, Integer> {
        final long totalBytes = 0L;
        String name;

        protected Integer doInBackground(Bundle... p1) {
            int id = p1[0].getInt("id");
            ArrayList<FileInfo> files = p1[0].getParcelableArrayList("files");
            name = p1[0].getString("name");
            new zip().execute(id, toFileArray(files), name);
            return id;
        }

        @Override
        public void onPostExecute(Integer b) {
            publishResults(b, name, 100, 0, totalBytes);

            // Broadcast result to FileListFragment
            Intent intent = new Intent(FileConstants.RELOAD_LIST);
            intent.putExtra(FileConstants.KEY_PATH, name);
            sendBroadcast(intent);
            stopSelf(b);

        }

    }

    private ArrayList<File> toFileArray(ArrayList<FileInfo> a) {
        ArrayList<File> b = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            b.add(new File(a.get(i).getFilePath()));
        }
        return b;
    }

    private void publishResults(int id, String fileName, int i, long done, long total) {


        mBuilder.setProgress(100, i, false);
        mBuilder.setOngoing(true);
        int title = R.string.zip_progress_title;
        mBuilder.setContentTitle(getResources().getString(title));
        mBuilder.setContentText(new File(fileName).getName() + " " + Formatter.formatFileSize
                (context, done) + "/" + Formatter.formatFileSize(context, total));
        int id1 = NOTIFICATION_ID + id;
        mNotifyManager.notify(id1, mBuilder.build());
        if (i == 100 || total == 0) {
            mBuilder.setContentTitle("Zip completed");
            mBuilder.setContentText("");
            mBuilder.setProgress(0, 0, false);
            mBuilder.setOngoing(false);
            mNotifyManager.notify(id1, mBuilder.build());
            publishCompletedResult(id1);
        }

        Intent intent = new Intent(ZIP_PROGRESS);
        if (i == 100 || total == 0) {
            intent.putExtra("PROGRESS", 100);
        } else {
            intent.putExtra("PROGRESS", i);
        }
        intent.putExtra("DONE", done);
        intent.putExtra("TOTAL", total);
        intent.putExtra("name", fileName);
        if (mProgressListener != null) {
            mProgressListener.onUpdate(intent);
            if (total == done) mProgressListener = null;
        }
    }

    private void publishCompletedResult(int id1) {
        try {
            mNotifyManager.cancel(id1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    class zip {
        public zip() {
        }

        int lastpercent;
        long size, totalBytes = 0;
        String fileName;

        void execute(int id, ArrayList<File> a, String fileOut) {
            for (File f1 : a) {
                if (f1.isDirectory()) {
                    totalBytes = totalBytes + FileUtils.getFolderSize(f1);
                } else {
                    totalBytes = totalBytes + f1.length();
                }
            }
            OutputStream out;
            fileName = fileOut;
            File zipDirectory = new File(fileOut);

            try {
                out = FileUtils.getOutputStream(zipDirectory, context);
                zos = new ZipOutputStream(new BufferedOutputStream(out));
            } catch (Exception ignored) {
            }
            for (File file : a) {
                try {
                    compressFile(id, file, "");
                } catch (Exception ignored) {
                }
            }
            try {
                zos.flush();
                zos.close();

            } catch (Exception ignored) {
            }
        }

        ZipOutputStream zos;
        // --Commented out by Inspection (06-11-2016 11:23 PM):private int isCompressed = 0;
        AsyncTask asyncTask;

        void calculateProgress(final String name, final int id, final long
                copiedbytes, final long totalbytes) {
            if (asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
                asyncTask.cancel(true);
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
                    publishResults(id, name, p1, copiedbytes, totalbytes);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }

        private void compressFile(int id, File file, String path) throws IOException, NullPointerException {

            if (!file.isDirectory()) {
                byte[] buf = new byte[2048];
                int len;
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
                zos.putNextEntry(new ZipEntry(path + "/" + file.getName()));
                while ((len = in.read(buf)) > 0) {
                    zos.write(buf, 0, len);
                    size += len;
                    int p = (int) ((size / (float) totalBytes) * 100);
                    if (p != lastpercent || lastpercent == 0) {
                        calculateProgress(fileName, id, size, totalBytes);
                    }
                    lastpercent = p;
                }
                in.close();
                return;
            }
            if (file.list() == null) {
                return;
            }
            for (String fileName : file.list()) {

                File f = new File(file.getAbsolutePath() + File.separator
                        + fileName);
                compressFile(id, f, path + File.separator + file.getName());

            }
        }
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public CreateZipTask getService() {
            // Return this instance of LocalService so clients can call public methods
            return CreateZipTask.this;
        }
    }

    private Progress mProgressListener;


    public void registerProgressListener(Progress progress) {
        mProgressListener = progress;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

}

