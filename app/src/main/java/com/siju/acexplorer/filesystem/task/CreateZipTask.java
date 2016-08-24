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
import android.text.format.Formatter;

import com.siju.acexplorer.BaseActivity;
import com.siju.acexplorer.R;
import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.model.ZipProgressModel;
import com.siju.acexplorer.filesystem.utils.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CreateZipTask extends Service {

    // Binder given to clients
    HashMap<Integer, Boolean> hash = new HashMap<Integer, Boolean>();
    public HashMap<Integer, ZipProgressModel> hash1 = new HashMap<>();
    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;
    String zpath;
    Context c;
    private final int NOTIFICATION_ID = 1000;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        c = getApplicationContext();
        registerReceiver(receiver1, new IntentFilter("zipcancel"));
    }

    boolean foreground = true;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle b = new Bundle();
//        zpath = PreferenceManager.getDefaultSharedPreferences(this).getString("zippath", "");
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String name = intent.getStringExtra("name");
        /*if ((zpath != null && zpath.length() != 0)) {
            if (zpath.endsWith("/")) name = zpath + new File(name).getName();
            else name = zpath + "/" + new File(name).getName();
        }*/
        File c = new File(name);
        if (!c.exists()) {
            try {
                c.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ZipProgressModel progressModel = new ZipProgressModel();
        progressModel.setName(name);
        progressModel.setTotal(0);
        progressModel.setDone(0);
        progressModel.setId(startId);
        progressModel.setP1(0);
        progressModel.setCompleted(false);
        hash1.put(startId, progressModel);
        mBuilder = new NotificationCompat.Builder(this);
        Intent notificationIntent = new Intent(this, BaseActivity.class);
        notificationIntent.putExtra("openprocesses", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setContentTitle(getResources().getString(R.string.zip_progress_title))
                .setSmallIcon(R.drawable.ic_library_compressed);

        startForeground(NOTIFICATION_ID + startId, mBuilder.build());

        ArrayList<FileInfo> zipFiles = intent.getParcelableArrayListExtra("files");
        b.putInt("id", startId);
        b.putParcelableArrayList("files", zipFiles);
        b.putString("name", name);
        hash.put(startId, true);
        new Doback().execute(b);
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public CreateZipTask getService() {
            // Return this instance of LocalService so clients can call public methods
            return CreateZipTask.this;
        }
    }

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    ProgressListener progressListener;

    public interface ProgressListener {
        void onUpdate(ZipProgressModel zipProgressModel);

        void refresh();
    }

    public class Doback extends AsyncTask<Bundle, Void, Integer> {
        ArrayList<String> files;

        public Doback() {
        }

        long totalBytes = 0L;
        String name;

        protected Integer doInBackground(Bundle... p1) {
            int id = p1[0].getInt("id");
            ArrayList<FileInfo> files = p1[0].getParcelableArrayList("files");
            name = p1[0].getString("name");
            new zip().execute(id, toFileArray(files), name);
            // TODO: Implement this method
            return id;
        }

        @Override
        public void onPostExecute(Integer b) {
            publishResults(b, name, 100, true, 0, totalBytes);
            hash.put(b, false);
            boolean stop = true;
            for (int a : hash.keySet()) {
                if (hash.get(a)) stop = false;
            }
            if (stop)
                stopSelf(b);

            // Broadcast result to FileListFragment
            Intent intent = new Intent("reload_list");
            intent.putExtra(FileConstants.KEY_PATH,name);
            sendBroadcast(intent);
        }

    }

    public ArrayList<File> toFileArray(ArrayList<FileInfo> a) {
        ArrayList<File> b = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            b.add(new File(a.get(i).getFilePath()));
        }
        return b;
    }

    private void publishResults(int id, String fileName, int i, boolean b, long done, long total) {
        if (hash.get(id)) {
            mBuilder.setProgress(100, i, false);
            mBuilder.setOngoing(true);
            int title = R.string.zip_progress_title;
            mBuilder.setContentTitle(getResources().getString(title));
            mBuilder.setContentText(new File(fileName).getName() + " " + Formatter.formatFileSize
                    (c, done) + "/" + Formatter.formatFileSize(c, total));
            int id1 = NOTIFICATION_ID + id;
            mNotifyManager.notify(id1, mBuilder.build());
            if (i == 100 || total == 0) {
                mBuilder.setContentTitle("Zip completed");
                mBuilder.setContentText("");
                mBuilder.setProgress(0, 0, false);
                mBuilder.setOngoing(false);
                mNotifyManager.notify(id1, mBuilder.build());
                publishCompletedResult(id1);
                b = true;
            }
            ZipProgressModel progressModel = new ZipProgressModel();
            progressModel.setName(fileName);
            progressModel.setTotal(total);
            progressModel.setDone(done);
            progressModel.setId(id);
            progressModel.setP1(i);
            progressModel.setCompleted(b);
            hash1.put(id, progressModel);
            if (progressListener != null) {
                progressListener.onUpdate(progressModel);
                if (b) progressListener.refresh();
            }
        } else {
            publishCompletedResult(NOTIFICATION_ID + id);
        }
    }

    public void publishCompletedResult(int id1) {
        try {
            mNotifyManager.cancel(id1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */

    private BroadcastReceiver receiver1 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            hash.put(intent.getIntExtra("id", 1), false);
        }
    };

    class zip {
        public zip() {
        }

        int count, lastpercent = 0;
        long size, totalBytes = 0;
        String fileName;

        public void execute(int id, ArrayList<File> a, String fileOut) {
            for (File f1 : a) {
                if (f1.isDirectory()) {
                    totalBytes = totalBytes + FileUtils.getFolderSize(f1);
                } else {
                    totalBytes = totalBytes + f1.length();
                }
            }
            OutputStream out = null;
            count = a.size();
            fileName = fileOut;
            File zipDirectory = new File(fileOut);

            try {
                out = FileUtils.getOutputStream(zipDirectory, c, totalBytes);
                zos = new ZipOutputStream(new BufferedOutputStream(out));
            } catch (Exception e) {
            }
            for (File file : a) {
                try {
                    compressFile(id, file, "");
                } catch (Exception e) {
                }
            }
            try {
                zos.flush();
                zos.close();

            } catch (Exception e) {
            }
        }

        ZipOutputStream zos;
        private int isCompressed = 0;
        AsyncTask asyncTask;

        void calculateProgress(final String name, final int id, final boolean completed, final long
                copiedbytes, final long totalbytes) {
            if (asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
                asyncTask.cancel(true);
            asyncTask = new AsyncTask<Void, Void, Void>() {
                int p1, p2;

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
                    publishResults(id, name, p1, completed, copiedbytes, totalbytes);
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
                    if (hash.get(id)) {
                        zos.write(buf, 0, len);
                        size += len;
                        int p = (int) ((size / (float) totalBytes) * 100);
                        if (p != lastpercent || lastpercent == 0) {
                            calculateProgress(fileName, id, false, size, totalBytes);
                        }
                        lastpercent = p;
                    }
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

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return mBinder;
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(receiver1);
    }
}

