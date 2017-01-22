package com.siju.acexplorer.filesystem.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.siju.acexplorer.AceActivity;
import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.filesystem.BaseFileList;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.task.CopyService;
import com.siju.acexplorer.filesystem.task.CreateZipTask;
import com.siju.acexplorer.filesystem.task.ExtractService;
import com.siju.acexplorer.filesystem.task.Progress;
import com.siju.acexplorer.utils.DialogUtils;
import com.siju.acexplorer.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static android.webkit.MimeTypeMap.getSingleton;


public class FileUtils implements Progress {

    private static final String TAG = "FileUtils";
    public static final int ACTION_NONE = 0;
    public static final int ACTION_KEEP = 3;
    private static final HashMap<String, String> MIME_TYPES = new HashMap<>();


    static {


		/*
         * ================= MIME TYPES ====================
		 */
        MIME_TYPES.put("asm", "text/x-asm");
        MIME_TYPES.put("def", "text/plain");
        MIME_TYPES.put("in", "text/plain");
        MIME_TYPES.put("rc", "text/plain");
        MIME_TYPES.put("list", "text/plain");
        MIME_TYPES.put("log", "text/plain");
        MIME_TYPES.put("pl", "text/plain");
        MIME_TYPES.put("prop", "text/plain");
        MIME_TYPES.put("properties", "text/plain");
        MIME_TYPES.put("rc", "text/plain");

        MIME_TYPES.put("epub", "application/epub+zip");
        MIME_TYPES.put("ibooks", "application/x-ibooks+zip");

        MIME_TYPES.put("ifb", "text/calendar");
        MIME_TYPES.put("eml", "message/rfc822");
        MIME_TYPES.put("msg", "application/vnd.ms-outlook");

        MIME_TYPES.put("ace", "application/x-ace-compressed");
        MIME_TYPES.put("bz", "application/x-bzip");
        MIME_TYPES.put("bz2", "application/x-bzip2");
        MIME_TYPES.put("cab", "application/vnd.ms-cab-compressed");
        MIME_TYPES.put("gz", "application/x-gzip");
        MIME_TYPES.put("lrf", "application/octet-stream");
        MIME_TYPES.put("jar", "application/java-archive");
        MIME_TYPES.put("xz", "application/x-xz");
        MIME_TYPES.put("Z", "application/x-compress");

        MIME_TYPES.put("bat", "application/x-msdownload");
        MIME_TYPES.put("ksh", "text/plain");
        MIME_TYPES.put("sh", "application/x-sh");

        MIME_TYPES.put("db", "application/octet-stream");
        MIME_TYPES.put("db3", "application/octet-stream");

        MIME_TYPES.put("otf", "application/x-font-otf");
        MIME_TYPES.put("ttf", "application/x-font-ttf");
        MIME_TYPES.put("psf", "application/x-font-linux-psf");

        MIME_TYPES.put("cgm", "image/cgm");
        MIME_TYPES.put("btif", "image/prs.btif");
        MIME_TYPES.put("dwg", "image/vnd.dwg");
        MIME_TYPES.put("dxf", "image/vnd.dxf");
        MIME_TYPES.put("fbs", "image/vnd.fastbidsheet");
        MIME_TYPES.put("fpx", "image/vnd.fpx");
        MIME_TYPES.put("fst", "image/vnd.fst");
        MIME_TYPES.put("mdi", "image/vnd.ms-mdi");
        MIME_TYPES.put("npx", "image/vnd.net-fpx");
        MIME_TYPES.put("xif", "image/vnd.xiff");
        MIME_TYPES.put("pct", "image/x-pict");
        MIME_TYPES.put("pic", "image/x-pict");

        MIME_TYPES.put("adp", "audio/adpcm");
        MIME_TYPES.put("au", "audio/basic");
        MIME_TYPES.put("snd", "audio/basic");
        MIME_TYPES.put("m2a", "audio/mpeg");
        MIME_TYPES.put("m3a", "audio/mpeg");
        MIME_TYPES.put("oga", "audio/ogg");
        MIME_TYPES.put("spx", "audio/ogg");
        MIME_TYPES.put("aac", "audio/x-aac");
        MIME_TYPES.put("mka", "audio/x-matroska");

        MIME_TYPES.put("jpgv", "video/jpeg");
        MIME_TYPES.put("jpgm", "video/jpm");
        MIME_TYPES.put("jpm", "video/jpm");
        MIME_TYPES.put("mj2", "video/mj2");
        MIME_TYPES.put("mjp2", "video/mj2");
        MIME_TYPES.put("mpa", "video/mpeg");
        MIME_TYPES.put("ogv", "video/ogg");
        MIME_TYPES.put("flv", "video/x-flv");
        MIME_TYPES.put("mkv", "video/x-matroska");

    }

    private ProgressBar progressBarPaste;
    private MaterialDialog progressDialog;
    private int copiedFilesSize;
    private TextView textFileName;
    private TextView textFileFromPath;
    private TextView textFileCount;
    private TextView textProgress;
    private ArrayList<FileInfo> copiedFileInfo;
    private Context mContext;
    private Intent mServiceIntent;

    @SuppressWarnings("ConstantConditions")
    public void showCopyProgressDialog(final Context context, final Intent intent) {
        mContext = context;
        mServiceIntent = intent;
        context.bindService(mServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        context.startService(mServiceIntent);
        String title = context.getString(R.string.action_copy);
        String texts[] = new String[]{title, context.getString(R.string.button_paste_progress), "",
                context.getString(R.string.dialog_cancel)};
        progressDialog = new DialogUtils().showCustomDialog(context,
                R.layout.dialog_progress_paste, texts);
        progressDialog.setCancelable(false);
        View view = progressDialog.getCustomView();
        textFileName = (TextView) view.findViewById(R.id.textFileName);
        textFileFromPath = (TextView) view.findViewById(R.id.textFileFromPath);
        TextView textFileToPath = (TextView) view.findViewById(R.id.textFileToPath);
        textFileCount = (TextView) view.findViewById(R.id.textFilesLeft);
        textProgress = (TextView) view.findViewById(R.id.textProgressPercent);

        progressBarPaste = (ProgressBar) view.findViewById(R.id.progressBarPaste);
        copiedFileInfo = intent.getParcelableArrayListExtra("TOTAL_LIST");
        copiedFilesSize = copiedFileInfo.size();
        Logger.log("FileUtils", "Totalfiles=" + copiedFilesSize);


        textFileFromPath.setText(copiedFileInfo.get(0).getFilePath());
        textFileName.setText(copiedFileInfo.get(0).getFileName());
        textFileToPath.setText(intent.getStringExtra("COPY_DIRECTORY"));
        textFileCount.setText(String.format(Locale.getDefault(), "%s%d", context.getString(R.string.count_placeholder),
                copiedFilesSize));
        textProgress.setText("0%");

        progressDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.dismiss();
            }
        });

        progressDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopCopyService();
                progressDialog.dismiss();
            }
        });

        progressDialog.show();
    }

    @SuppressWarnings("ConstantConditions")
    public void showZipProgressDialog(final Context context, final Intent intent) {
        mContext = context;
        mServiceIntent = intent;
        context.bindService(mServiceIntent, mZipServiceConnection, Context.BIND_AUTO_CREATE);
        context.startService(mServiceIntent);
        String title = context.getString(R.string.zip_progress_title);
        String texts[] = new String[]{title, context.getString(R.string.button_paste_progress), "",
                context.getString(R.string.dialog_cancel)};
        progressDialog = new DialogUtils().showCustomDialog(context,
                R.layout.dialog_progress_paste, texts);
        progressDialog.setCancelable(false);
        View view = progressDialog.getCustomView();
        textFileName = (TextView) view.findViewById(R.id.textFileName);
        textFileFromPath = (TextView) view.findViewById(R.id.textFileFromPath);
        TextView textFromPlaceHolder = (TextView) view.findViewById(R.id.textFileFromPlaceHolder);
        (view.findViewById(R.id.textFileToPlaceHolder)).setVisibility(View.GONE);

        textFromPlaceHolder.setVisibility(View.GONE);
        textFileCount = (TextView) view.findViewById(R.id.textFilesLeft);
        textProgress = (TextView) view.findViewById(R.id.textProgressPercent);

        progressBarPaste = (ProgressBar) view.findViewById(R.id.progressBarPaste);
        copiedFileInfo = intent.getParcelableArrayListExtra("files");
        String fileName = intent.getStringExtra("name");
        copiedFilesSize = copiedFileInfo.size();
        Logger.log("FileUtils", "Totalfiles=" + copiedFilesSize);
        textFileName.setText(fileName);
        textProgress.setText("0%");

        progressDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.dismiss();
            }
        });

        progressDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopZipService();
                progressDialog.dismiss();
            }
        });

        progressDialog.show();
    }

    @SuppressWarnings("ConstantConditions")
    public void showExtractProgressDialog(final Context context, final Intent intent) {
        mContext = context;
        mServiceIntent = intent;
        context.bindService(mServiceIntent, mExtractServiceConnection, Context.BIND_AUTO_CREATE);
        context.startService(mServiceIntent);
        registerReceiver(context);
        isExtractServiceAlive = true;
        String title = context.getString(R.string.extracting);
        String texts[] = new String[]{title, context.getString(R.string.button_paste_progress), "",
                context.getString(R.string.dialog_cancel)};
        progressDialog = new DialogUtils().showCustomDialog(context,
                R.layout.dialog_progress_paste, texts);
        progressDialog.setCancelable(false);
        View view = progressDialog.getCustomView();
        textFileName = (TextView) view.findViewById(R.id.textFileName);
        textFileFromPath = (TextView) view.findViewById(R.id.textFileFromPath);
        TextView textFileToPath = (TextView) view.findViewById(R.id.textFileToPath);
        TextView textFromPlaceHolder = (TextView) view.findViewById(R.id.textFileFromPlaceHolder);
        textFromPlaceHolder.setVisibility(View.GONE);
        textFileCount = (TextView) view.findViewById(R.id.textFilesLeft);
        textProgress = (TextView) view.findViewById(R.id.textProgressPercent);

        progressBarPaste = (ProgressBar) view.findViewById(R.id.progressBarPaste);
        textFileToPath.setText(intent.getStringExtra("new_path"));
        String fileName = intent.getStringExtra("zip");

        textFileName.setText(fileName);
        textProgress.setText("0%");

        progressDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.dismiss();
            }
        });

        progressDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopExtractService();
                progressDialog.dismiss();
            }
        });

        progressDialog.show();
    }

    private void stopCopyService() {
        mContext.unbindService(mServiceConnection);
        mContext.stopService(mServiceIntent);
    }

    private void stopZipService() {
        mContext.unbindService(mZipServiceConnection);
        mContext.stopService(mServiceIntent);
    }

    private void stopExtractService() {
        mContext.unbindService(mExtractServiceConnection);
        mContext.stopService(mServiceIntent);
        if (isExtractServiceAlive) {
            unRegisterReceiver(mContext);
        }
        isExtractServiceAlive = false;

    }

    private BroadcastReceiver operationFailureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(FileConstants.OPERATION_FAILED)) {
                int operation = intent.getIntExtra(FileConstants.OPERATION, -1);
                switch (operation) {
                    case FileConstants.EXTRACT:
                        Logger.log(TAG, "Failure broacast=" + isExtractServiceAlive);
                        if (isExtractServiceAlive) {
                            unRegisterReceiver(mContext);
                            progressDialog.dismiss();
                            isExtractServiceAlive = false;
                        }
                        break;
                }
            }
        }
    };

    private void registerReceiver(Context context) {
        IntentFilter filter = new IntentFilter(FileConstants.OPERATION_FAILED);
        context.registerReceiver(operationFailureReceiver, filter);
    }

    private void unRegisterReceiver(Context context) {
        context.unregisterReceiver(operationFailureReceiver);
    }

    @Override
    public void onUpdate(Intent intent) {
        Message msg = handler.obtainMessage();
        msg.obj = intent;
        handler.sendMessage(msg);

    }


    // Define the Handler that receives messages from the thread and update the progress
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            Intent intent = (Intent) msg.obj;
            int progress = intent.getIntExtra("PROGRESS", 0);
            long copiedBytes = intent.getLongExtra("DONE", 0);
            long totalBytes = intent.getLongExtra("TOTAL", 0);

            switch (intent.getAction()) {
                case ZIP_PROGRESS:
                    progressBarPaste.setProgress(progress);
                    textFileCount.setText(Formatter.formatFileSize
                            (mContext, copiedBytes) + "/" + Formatter.formatFileSize(mContext, totalBytes));
                    textProgress.setText(String.format(Locale.getDefault(), "%d%s", progress, mContext.getString
                            (R.string.percent_placeholder)));

                    if (progress == 100 || totalBytes == copiedBytes) {
                        stopZipService();
                        progressDialog.dismiss();
                    }
                    break;

                case EXTRACT_PROGRESS:
                    Logger.log(TAG, "Progress=" + progress + "Operation=" + EXTRACT_PROGRESS);
                    progressBarPaste.setProgress(progress);
                    textFileCount.setText(Formatter.formatFileSize
                            (mContext, copiedBytes) + "/" + Formatter.formatFileSize(mContext, totalBytes));
                    textProgress.setText(String.format(Locale.getDefault(), "%d%s", progress, mContext.getString
                            (R.string.percent_placeholder)));

                    if (progress == 100) {
                        stopExtractService();
                        progressDialog.dismiss();
                    }
                    break;
                case COPY_PROGRESS:
                    boolean isSuccess = intent.getBooleanExtra(FileConstants.IS_OPERATION_SUCCESS, true);

                    if (!isSuccess) {
                        stopCopyService();
                        progressDialog.dismiss();
                        return;
                    }
                    int totalProgress = intent.getIntExtra("TOTAL_PROGRESS", 0);
                    Logger.log("FileUtils", "PROGRESS=" + progress + " TOTAL PROGRESS=" + totalProgress);
                    Logger.log("FileUtils", "Copied bytes=" + copiedBytes + " TOTAL bytes=" + totalBytes);
                    progressBarPaste.setProgress(totalProgress);
                    textProgress.setText(String.format(Locale.getDefault(), "%d%s", totalProgress, mContext.getString
                            (R.string
                                    .percent_placeholder)));
                    if (progress == 100 || totalBytes == copiedBytes) {
                        int count = intent.getIntExtra("COUNT", 1);
                        Logger.log("FileUtils", "COUNT=" + count);
                        if (count == copiedFilesSize || copiedBytes == totalBytes) {
                            stopCopyService();
                            progressDialog.dismiss();
                        } else {
                            int newCount = count + 1;
                            textFileFromPath.setText(copiedFileInfo.get(count).getFilePath());
                            textFileName.setText(copiedFileInfo.get(count).getFileName());
                            textFileCount.setText(newCount + "/" + copiedFilesSize);
                        }
                    }
                    break;
            }
        }

    };

    public static final String COPY_PROGRESS = "copy_progress_update";
    public static final String ZIP_PROGRESS = "zip_progress_update";
    public static final String EXTRACT_PROGRESS = "extract_progress_update";


    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CopyService.LocalBinder binder = (CopyService.LocalBinder) service;
            CopyService mService = binder.getService();
            mService.registerProgressListener(FileUtils.this);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    private final ServiceConnection mZipServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CreateZipTask.LocalBinder binder = (CreateZipTask.LocalBinder) service;
            CreateZipTask mService = binder.getService();
            mService.registerProgressListener(FileUtils.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    private boolean isExtractServiceAlive;
    private final ServiceConnection mExtractServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ExtractService.LocalBinder binder = (ExtractService.LocalBinder) service;
            ExtractService mService = binder.getService();
            mService.registerProgressListener(FileUtils.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    public static boolean isFileMusic(String path) {
        return path.toLowerCase().endsWith(".mp3") || path.toLowerCase().endsWith(".amr") || path.
                toLowerCase().endsWith(".wav") || path.
                toLowerCase().endsWith(".m4a");
    }


    /**
     * To be used when RAR as viewable not needed
     *
     * @param filePath
     * @return
     */
    public static boolean isZipViewable(String filePath) {
        return filePath.toLowerCase().endsWith(".zip") ||
                filePath.toLowerCase().endsWith(".jar") ||
                filePath.toLowerCase().endsWith(".apk");
    }


    public static File getRootDirectory() {
        return Environment.getRootDirectory();
    }

    public static File getInternalStorage() {
        return Environment.getExternalStorageDirectory();
    }

    public static String getAbsolutePath(File file) {
        if (file == null)
            return null;
        return file.getAbsolutePath();
    }

    public static File getDownloadsDirectory() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    private final static Pattern DIR_SEPARATOR = Pattern.compile("/");

    public static List<String> getStorageDirectories(Context context) {
        // Final set of paths
        final ArrayList<String> paths = new ArrayList<>();
        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
            // Device has physical external storage; use plain paths.
            if (TextUtils.isEmpty(rawExternalStorage)) {
                // EXTERNAL_STORAGE undefined; falling back to default.
                paths.add("/storage/sdcard0");
            } else {
                paths.add(rawExternalStorage);
            }
        } else {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                rawUserId = "";
            } else {
                final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                final String[] folders = DIR_SEPARATOR.split(path);
                final String lastFolder = folders[folders.length - 1];
                boolean isDigit = false;
                try {
                    //noinspection ResultOfMethodCallIgnored
                    Integer.valueOf(lastFolder);
                    isDigit = true;
                } catch (NumberFormatException ignored) {
                }
                rawUserId = isDigit ? lastFolder : "";
            }
            // /storage/emulated/0[1,2,...]
            if (TextUtils.isEmpty(rawUserId)) {
                paths.add(rawEmulatedStorageTarget);
            } else {
                paths.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(paths, rawSecondaryStorages);
        }
        if (Utils.isAtleastMarsh())
            paths.clear();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String pathList[] = getExtSdCardPaths(context);
            for (String path : pathList) {
                if (!paths.contains(path))
                    paths.add(path);
            }
        }

        File usb = getUsbDrive();
        if (usb != null && !paths.contains(usb.getPath())) paths.add(usb.getPath());
        return paths;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static String[] getExtSdCardPaths(Context context) {
        List<String> paths = new ArrayList<>();
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w("FileUtils", "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    if (!paths.contains(path)) {
                        paths.add(path);
                    }
                }
            }
        }

        File file = new File("/storage/sdcard1");
        if (file.exists() && file.canExecute() && !paths.contains(file.getAbsolutePath())) {
            paths.add("/storage/sdcard1");
        }
        return paths.toArray(new String[0]);
    }


    @SuppressLint("SdCardPath")
    private static File getUsbDrive() {
        File parent;
        parent = new File("/storage");

        try {
            for (File f : parent.listFiles()) {
                if (f.exists() && f.getName().toLowerCase().contains("usb") && f.canExecute()) {
                    return f;
                }
            }
        } catch (Exception ignored) {
        }
        parent = new File("/mnt/sdcard/usbStorage");
        if (parent.exists() && parent.canExecute())
            return (parent);
        parent = new File("/mnt/sdcard/usb_storage");
        if (parent.exists() && parent.canExecute())
            return parent;

        return null;
    }


    public static String convertDate(long dateInMs) {
        SimpleDateFormat df2 = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        return df2.format(dateInMs);
    }

    public static boolean isDateNotInMs(int category) {
        return category == FileConstants.CATEGORY.FILES.getValue() ||
                category == FileConstants.CATEGORY.DOWNLOADS.getValue() ||
                category == FileConstants.CATEGORY.FAVORITES.getValue() ||
                category == FileConstants.CATEGORY.ZIP_VIEWER.getValue();
    }


    /**
     * View the file in external apps based on Mime Type
     *
     * @param fragment
     * @param path
     * @param extension
     */
    public static void viewFile(Fragment fragment, String path, String extension) {

        Context context = fragment.getContext();
        Uri uri = createContentUri(fragment.getContext(), path);

        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (extension == null) {
            openWith(uri, context);
            return;
        }
        String ext = extension.toLowerCase();

        if (ext.equals("apk")) {
            showApkOptionsDialog(fragment, path, ext);
        } else {
            String mimeType = getSingleton().getMimeTypeFromExtension(ext);
            Logger.log(TAG, " uri==" + uri + "MIME=" + mimeType);
            intent.setDataAndType(uri, mimeType);
            if (mimeType != null) {
                grantUriPermission(context, intent, uri);
            } else {
                openWith(uri, context);
            }
        }

    }

    public static Uri createContentUri(Context context, String path) {

        if (Utils.isAtleastNougat()) {
            String authority = context.getPackageName() + ".fileprovider";
            return FileProvider.getUriForFile(context, authority, new File(path));
        } else {
            return Uri.fromFile(new File(path));
        }
    }

    private static void grantUriPermission(Context context, Intent intent, Uri uri) {
        PackageManager packageManager = context.getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            if (Utils.isAtleastLollipop()) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                List<ResolveInfo> resInfoList = packageManager.queryIntentActivities(intent, PackageManager
                        .MATCH_DEFAULT_ONLY);

                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }
            context.startActivity(intent);
        } else {
            showMessage(context, context.getString(R.string.msg_error_not_supported));
        }
    }


    private static void openWith(final Uri uri, final Context context) {

        String texts[] = new String[]{context.getString(R.string.open_as), null, null, null};
        ArrayList<String> items = new ArrayList<>();
        items.add(context.getString(R.string.text));
        items.add(context.getString(R.string.image));
        items.add(context.getString(R.string.audio));
        items.add(context.getString(R.string.other));
        items.add(context.getString(R.string.text));

        final MaterialDialog materialDialog = new DialogUtils().showListDialog(context, texts, items);

        materialDialog.getBuilder().itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                switch (position) {
                    case 0:
                        intent.setDataAndType(uri, "text/*");
                        break;
                    case 1:
                        intent.setDataAndType(uri, "image/*");
                        break;
                    case 2:
                        intent.setDataAndType(uri, "video/*");
                        break;
                    case 3:
                        intent.setDataAndType(uri, "audio/*");
                        break;
                    case 4:
                        intent.setDataAndType(uri, "*/*");
                        break;
                }
                grantUriPermission(context, intent, uri);
                materialDialog.dismiss();
            }
        });
        materialDialog.show();
    }

    /**
     * Validates file name at the time of creation
     * special reserved characters shall not be allowed in the file names
     *
     * @param name the file which needs to be validated
     * @return boolean if the file name is valid or invalid
     */
    public static boolean isFileNameInvalid(String name) {

       /* StringBuilder builder = new StringBuilder(file.getPath());
        String newName = builder.substring(builder.lastIndexOf("/") + 1, builder.length());*/
        String newName = name.trim();
        return newName.contains("/") || newName.length() == 0;
    }


    public static void showMessage(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void shareFiles(Context context, ArrayList<FileInfo> fileInfo, int category) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        if (checkIfFileCategory(category)) {
            intent.setType("*/*");
        } else {
            String extension = fileInfo.get(0).getExtension();
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            intent.setType(mimeType);
        }

        ArrayList<Uri> files = new ArrayList<>();

        for (FileInfo info : fileInfo) {
            Uri uri = createContentUri(context, info.getFilePath());
            System.out.println("shareuri==" + uri);
            files.add(uri);
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        context.startActivity(intent);
    }


    public static ArrayList<FileInfo> sortFiles(ArrayList<FileInfo> files, int sortMode) {

        switch (sortMode) {
            case 0:
                Collections.sort(files, FileUtils.comparatorByName);
                break;
            case 1:
                Collections.sort(files, FileUtils.comparatorByNameDesc);
                break;
            case 2:
                Collections.sort(files, FileUtils.comparatorByType);
                break;
            case 3:
                Collections.sort(files, FileUtils.comparatorByTypeDesc);
                break;
            case 4:
                Collections.sort(files, FileUtils.comparatorBySize);
                break;
            case 5:
                Collections.sort(files, FileUtils.comparatorBySizeDesc);
                break;
            case 6:
                Collections.sort(files, FileUtils.comparatorByDate);
                break;
            case 7:
                Collections.sort(files, FileUtils.comparatorByDateDesc);
                break;

        }
        return files;
    }


    private static final Comparator<? super FileInfo> comparatorByName = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            if ((file1.isDirectory()) && (!file2.isDirectory()))
                return -1;
            if ((!file1.isDirectory()) && (file2.isDirectory()))
                return 1;
            // here both are folders or both are files : sort alpha
            return file1.getFileName().toLowerCase()
                    .compareTo(file2.getFileName().toLowerCase());
        }

    };

    private static final Comparator<? super FileInfo> comparatorByNameDesc = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            if ((file1.isDirectory()) && (!file2.isDirectory()))
                return -1;
            if ((!file1.isDirectory()) && (file2.isDirectory()))
                return 1;
            // here both are folders or both are files : sort alpha
            return file2.getFileName().toLowerCase()
                    .compareTo(file1.getFileName().toLowerCase());
        }

    };

    private static final Comparator<? super FileInfo> comparatorBySize = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            if ((file1.isDirectory()) && (!file2.isDirectory())) {
                return -1;
            }
            if ((!file1.isDirectory()) && (file2.isDirectory())) {
                return 1;
            }


            Long first = getSize(new File(file1.getFilePath()));
            Long second = getSize(new File(file2.getFilePath()));
            return first.compareTo(second);
        }
    };

    private static final Comparator<? super FileInfo> comparatorBySizeDesc = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            if ((file1.isDirectory()) && (!file2.isDirectory())) {
                return -1;
            }
            if ((!file1.isDirectory()) && (file2.isDirectory())) {
                return 1;
            }

            Long first = getSize(new File(file1.getFilePath()));
            Long second = getSize(new File(file2.getFilePath()));
            return second.compareTo(first);
        }
    };

    private static final Comparator<? super FileInfo> comparatorByType = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            String arg0 = file1.getFileName();
            String arg1 = file2.getFileName();

            final int s1Dot = arg0.lastIndexOf('.');
            final int s2Dot = arg1.lastIndexOf('.');

            if ((s1Dot == -1) == (s2Dot == -1)) { // both or neither

                arg0 = arg0.substring(s1Dot + 1);
                arg1 = arg1.substring(s2Dot + 1);
                return (arg0.toLowerCase()).compareTo((arg1.toLowerCase()));
            } else if (s1Dot == -1) { // only s2 has an extension, so s1 goes
                // first
                return -1;
            } else { // only s1 has an extension, so s1 goes second
                return 1;
            }
        }

    };

    private static final Comparator<? super FileInfo> comparatorByTypeDesc = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            String arg0 = file2.getFileName();
            String arg1 = file1.getFileName();

            final int s1Dot = arg0.lastIndexOf('.');
            final int s2Dot = arg1.lastIndexOf('.');

            if ((s1Dot == -1) == (s2Dot == -1)) { // both or neither

                arg0 = arg0.substring(s1Dot + 1);
                arg1 = arg1.substring(s2Dot + 1);
                return (arg0.toLowerCase()).compareTo((arg1.toLowerCase()));
            } else if (s1Dot == -1) { // only s2 has an extension, so s1 goes
                // first
                return -1;
            } else { // only s1 has an extension, so s1 goes second
                return 1;
            }
        }

    };


    private static final Comparator<? super FileInfo> comparatorByDate = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            Long date1 = new File(file1.getFilePath()).lastModified();
            Long date2 = new File(file2.getFilePath()).lastModified();
            return date1.compareTo(date2);
        }
    };

    private static final Comparator<? super FileInfo> comparatorByDateDesc = new Comparator<FileInfo>() {

        public int compare(FileInfo file1, FileInfo file2) {

            Long date1 = new File(file1.getFilePath()).lastModified();
            Long date2 = new File(file2.getFilePath()).lastModified();
            return date2.compareTo(date1);
        }
    };

    public static long getFolderSize(File directory) {
        long length = 0;
        try {
            for (File file : directory.listFiles()) {

                if (file.isFile())
                    length += file.length();
                else
                    length += getFolderSize(file);
            }
        } catch (Exception ignored) {
        }
        return length;
    }

    public static boolean checkIfFileCategory(int category) {
        return category == FileConstants.CATEGORY.FILES.getValue() ||
                category == FileConstants.CATEGORY.COMPRESSED.getValue() ||
                category == FileConstants.CATEGORY.DOWNLOADS.getValue() ||
                category == FileConstants.CATEGORY.FAVORITES.getValue() ||
                category == FileConstants.CATEGORY.LARGE_FILES.getValue();
    }

    public static Uri getUriForCategory(int category) {
        switch (category) {
            case 0:
            case 4:
            case 5:
            case 7:
            case 9:
            case 10:
            case 11:
                return MediaStore.Files.getContentUri("external");
            case 1:
                return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            case 2:
                return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            case 3:
                return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        return MediaStore.Files.getContentUri("external");

    }



    public static boolean isMediaScanningRequired(String mimeType) {
        Logger.log(TAG, "Mime type=" + mimeType);
        return mimeType != null && (mimeType.startsWith("audio") ||
                mimeType.startsWith("video") || mimeType.startsWith("image"));
    }

    public static void removeMedia(Context context, File file, int category) {
        Logger.log(TAG, "Type=" + category);
        ContentResolver resolver = context.getContentResolver();
        String path = file.getAbsolutePath();
        switch (category) {
            case 1:
                Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                resolver.delete(musicUri, MediaStore.Audio.Media.DATA + "=?", new String[]{path});
                break;

            case 2:
                Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                resolver.delete(videoUri, MediaStore.Video.Media.DATA + "=?", new String[]{path});
                break;

            case 3:
                Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                resolver.delete(imageUri, MediaStore.Images.Media.DATA + "=?", new String[]{path});
                break;

            case 4:
                Uri filesUri = MediaStore.Files.getContentUri("external");
                resolver.delete(filesUri, MediaStore.Files.FileColumns.DATA + "=?", new String[]{path});
                break;
            default:
                break;
        }
    }


    private static long getSize(File file) {

        long size = 0;
        if (file.isFile()) {
            size = file.length();
        } else if (file.isDirectory()) {
            File[] list = file.listFiles();
            if (list != null) {
                size = list.length;
            }
        }
        return size;
    }

    public static OutputStream getOutputStream(@NonNull final File target, Context context) {

        OutputStream outStream = null;
        try {
            // First try the normal way
            if (isWritable(target)) {
                // standard way
                outStream = new FileOutputStream(target);
            } else {
                if (Utils.isAtleastLollipop()) {
                    // Storage Access Framework
                    DocumentFile targetDocument = getDocumentFile(target, false, context);
                    if (targetDocument != null)
                        outStream =
                                context.getContentResolver().openOutputStream(targetDocument.getUri());
                } else if (Utils.isKitkat()) {
                    // Workaround for Kitkat ext SD card
                    return getOutputStream(context, target.getPath());
                }


            }
        } catch (Exception ignored) {
        }
        return outStream;
    }


    /**
     * Check is a file is writable. Detects write issues on external SD card.
     *
     * @param file The file
     * @return true if the file is writable.
     */
    public static boolean isWritable(final File file) {
        if (file == null)
            return false;
        boolean isExisting = file.exists();

        try {
            FileOutputStream output = new FileOutputStream(file, true);
            try {
                output.close();
            } catch (IOException e) {
                // do nothing.
            }
        } catch (FileNotFoundException e) {
            return false;
        }
        boolean result = file.canWrite();

        // Ensure that file is not created during this process.
        if (!isExisting) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }

        return result;
    }


    /**
     * Get a DocumentFile corresponding to the given file (for writing on ExtSdCard on Android 5). If the file is not
     * existing, it is created.
     *
     * @param file        The file.
     * @param isDirectory flag indicating if the file should be a directory.
     * @return The DocumentFile
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static DocumentFile getDocumentFile(final File file, final boolean isDirectory, Context context) {
        String baseFolder = getExtSdCardFolder(file, context);
        boolean originalDirectory = false;
        if (baseFolder == null) {
            return null;
        }

        String relativePath = null;
        try {
            String fullPath = file.getCanonicalPath();
            if (!baseFolder.equals(fullPath))
                relativePath = fullPath.substring(baseFolder.length() + 1);
            else originalDirectory = true;
        } catch (IOException e) {
            return null;
        } catch (Exception f) {
            originalDirectory = true;
        }
        String as = PreferenceManager.getDefaultSharedPreferences(context).getString(FileConstants.SAF_URI, null);

        Uri treeUri = null;
        if (as != null) treeUri = Uri.parse(as);
        if (treeUri == null) {
            return null;
        }

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);
        if (originalDirectory) return document;
        String[] parts = relativePath.split("/");
        for (int i = 0; i < parts.length; i++) {
            DocumentFile nextDocument = document.findFile(parts[i]);

            if (nextDocument == null) {
                if ((i < parts.length - 1) || isDirectory) {
                    nextDocument = document.createDirectory(parts[i]);
                } else {
                    nextDocument = document.createFile("image", parts[i]);
                }
            }
            document = nextDocument;
        }

        return document;
    }

    private static OutputStream getOutputStream(Context context, String str) {
        OutputStream outputStream = null;
        Uri fileUri = getUriFromFile(str, context);
        if (fileUri != null) {
            try {
                outputStream = context.getContentResolver().openOutputStream(fileUri);
            } catch (Throwable ignored) {
            }
        }
        return outputStream;
    }

    private static Uri getUriFromFile(final String path, Context context) {
        ContentResolver resolver = context.getContentResolver();

        Cursor filecursor = resolver.query(MediaStore.Files.getContentUri("external"),
                new String[]{BaseColumns._ID}, MediaStore.MediaColumns.DATA + " = ?",
                new String[]{path}, MediaStore.MediaColumns.DATE_ADDED + " desc");

        if (filecursor != null) {
            filecursor.moveToFirst();
            if (filecursor.isAfterLast()) {
                filecursor.close();
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DATA, path);
                return resolver.insert(MediaStore.Files.getContentUri("external"), values);
            } else {
                int imageId = filecursor.getInt(filecursor.getColumnIndex(BaseColumns._ID));
                Uri uri = MediaStore.Files.getContentUri("external").buildUpon().appendPath(
                        Integer.toString(imageId)).build();
                filecursor.close();
                return uri;
            }
        }
        return null;

    }


    /**
     * Determine the main folder of the external SD card containing the given file.
     *
     * @param file the file.
     * @return The main folder of the external SD card containing this file, if the file is on an SD card. Otherwise,
     * null is returned.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static String getExtSdCardFolder(final File file, Context context) {
        String[] extSdPaths = getExtSdCardPaths(context);
        try {
            for (String extSdPath : extSdPaths) {
                if (file.getCanonicalPath().startsWith(extSdPath)) {
                    return extSdPath;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    static boolean mkfile(final File file, Context context) {
        if (file == null)
            return false;
        if (file.exists()) {
            // nothing to create.
            return !file.isDirectory();
        }

        // Try the normal way
        try {
            if (file.createNewFile()) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Even after exception, In Kitkat file might have got created so return true
        if (file.exists()) return true;
        boolean result;
        // Try with Storage Access Framework.
        if (Utils.isAtleastLollipop() && isOnExtSdCard(file, context)) {
            DocumentFile document = getDocumentFile(file.getParentFile(), true, context);
            // getDocumentFile implicitly creates the directory.
            try {
                Logger.log(TAG, "mkfile--doc=" + document);
                result = document != null && document.createFile(getMimeType(file), file.getName()) != null;
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        if (Utils.isKitkat()) {
            try {
                return MediaStoreHack.mkfile(context, file);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }


    static void renameRoot(File sourceFile, String newFileName) throws RootNotPermittedException {
        String destinationPath = sourceFile.getParent() + File.separator + newFileName;
        RootUtils.mountRW(sourceFile.getPath());
        RootUtils.move(sourceFile.getPath(), destinationPath);
        RootUtils.mountRO(sourceFile.getPath());

/*        if (!("rw".equals(res = RootTools.getMountedAs(sourceFile.getParent()))))
            remount = true;
        if (remount)
            RootTools.remount(sourceFile.getParent(), "rw");
        RootHelper.runAndWait("mv \"" + sourceFile.getPath() + "\" \"" + destinationPath + "\"", true);
        if (remount) {
            if (res == null || res.length() == 0) res = "ro";
            RootTools.remount(sourceFile.getParent(), res);
        }*/
    }

    /**
     * Rename a folder. In case of extSdCard in Kitkat, the old folder stays in place, but files are moved.
     *
     * @param source The source folder.
     * @param target The target folder.
     * @return true if the renaming was successful.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    static boolean renameFolder(@NonNull final File source, @NonNull final File target, Context context) {
        // First try the normal rename.
        if (rename(source, target.getName())) {
            return true;
        }
        if (target.exists()) {
            return false;
        }

        // Try the Storage Access Framework if it is just a rename within the same parent folder.
        if (Utils.isAtleastLollipop()
                && source.getParent().equals(target.getParent()) && isOnExtSdCard(source, context)) {

            DocumentFile document = getDocumentFile(source, true, context);

            Log.d(TAG, " Document uri in rename=" + document);
            if (document != null && document.renameTo(target.getName())) {
                return true;
            }
        }

        if (source.isFile()) {
            Logger.log(TAG, "Rename--root=");


            if (!mkfile(target, context)) {
                return false;
            }

        } else {
            // Try the manual way, moving files individually.
            if (!mkdir(target, context)) {
                return false;
            }
        }

        File[] sourceFiles = source.listFiles();

        if (sourceFiles == null || sourceFiles.length == 0) {
            return true;
        }

        for (File sourceFile : sourceFiles) {
            String fileName = sourceFile.getName();
            File targetFile = new File(target, fileName);
            if (!copyFile(sourceFile, targetFile, context)) {
                // stop on first error
                return false;
            }
        }
        // Only after successfully copying all files, delete files on source folder.
        for (File sourceFile : sourceFiles) {
            if (!deleteFile(sourceFile, context)) {
                // stop on first error
                return false;
            }
        }
        return true;
    }


    /**
     * Copy a file. The target file may even be on external SD card for Kitkat.
     *
     * @param source The source file
     * @param target The target file
     * @return true if the copying was successful.
     */
    @SuppressWarnings("null")
    private static boolean copyFile(final File source, final File target, Context context) {
        FileInputStream inStream = null;
        OutputStream outStream = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inStream = new FileInputStream(source);

            // First try the normal way
            if (isWritable(target)) {
                // standard way
                outStream = new FileOutputStream(target);
                inChannel = inStream.getChannel();
                outChannel = ((FileOutputStream) outStream).getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } else {
                if (Utils.isAtleastLollipop()) {
                    // Storage Access Framework
                    DocumentFile targetDocument = getDocumentFile(target, false, context);
                    if (targetDocument != null)
                        outStream =
                                context.getContentResolver().openOutputStream(targetDocument.getUri());
                } else if (Utils.isKitkat()) {
                    // Workaround for Kitkat ext SD card
                    Uri uri = MediaStoreHack.getUriFromFile(target.getAbsolutePath(), context);
                    if (uri != null)
                        outStream = context.getContentResolver().openOutputStream(uri);
                } else {
                    return false;
                }

                if (outStream != null) {
                    // Both for SAF and for Kitkat, write to output stream.
                    byte[] buffer = new byte[16384]; // MAGIC_NUMBER
                    int bytesRead;
                    while ((bytesRead = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);
                    }
                }

            }
        } catch (Exception e) {
            Log.e(TAG,
                    "Error when copying file from " + source.getAbsolutePath() + " to " + target.getAbsolutePath(), e);
            return false;
        } finally {
            try {
                if (inStream != null) inStream.close();
                if (outStream != null) outStream.close();
                if (inChannel != null) inChannel.close();
                if (outChannel != null) outChannel.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return true;
    }

    private static boolean rename(File f, String name) {
        String newname = f.getParent() + "/" + name;
        if (f.getParentFile().canWrite()) {
            Logger.log(TAG, "Rename--canWrite=" + true);
            return f.renameTo(new File(newname));
        }
        return false;
    }

    public static String getMimeType(File file) {
        if (file.isDirectory()) {
            return null;
        }

        String type = "*/*";
        final String extension = getExtension(file.getName());

        if (extension != null && !extension.isEmpty()) {
            final String extensionLowerCase = extension.toLowerCase(Locale
                    .getDefault());
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extensionLowerCase);
            if (type == null) {
                type = MIME_TYPES.get(extensionLowerCase);
            }
        }
        if (type == null) type = "*/*";
        return type;
    }


    public int checkFolder(final String f, Context context) {
        if (f == null) return 0;
        File folder = new File(f);
        if (Utils.isAtleastLollipop() && isOnExtSdCard(folder, context)) {
            if (!folder.exists() || !folder.isDirectory()) {
                return FileConstants.WRITE_MODES.ROOT.getValue();
            }

            // On Android 5 and above, trigger storage access framework.
            if (isFileNonWritable(folder, context)) {
                return FileConstants.WRITE_MODES.EXTERNAL.getValue();
            }
            return FileConstants.WRITE_MODES.INTERNAL.getValue();
        } else if (Utils.isKitkat() && isOnExtSdCard(folder, context)) {
            // Assume that Kitkat workaround works
            return FileConstants.WRITE_MODES.INTERNAL.getValue();
        } else if (isWritable(new File(folder, "DummyFile"))) {
            return FileConstants.WRITE_MODES.INTERNAL.getValue();
        } else {
            return FileConstants.WRITE_MODES.ROOT.getValue();
        }
    }


    /**
     * Create a folder. The folder may even be on external SD card for Kitkat.
     *
     * @param file The folder to be created.
     * @return True if creation was successful.
     */
    public static boolean mkdir(final File file, Context context) {
        if (file == null)
            return false;
        if (file.exists()) {
            // nothing to create.
            return file.isDirectory();
        }

        // Try the normal way
        if (file.mkdirs()) {
            return true;
        }

        // Try with Storage Access Framework.
        if (Utils.isAtleastLollipop() && isOnExtSdCard(file, context)) {
            DocumentFile document = getDocumentFile(file, true, context);
            // getDocumentFile implicitly creates the directory.
            return document != null && document.exists();
        }

        // Try the Kitkat workaround.
        if (Utils.isKitkat()) {
            try {
                return MediaStoreHack.mkdir(context, file);
            } catch (IOException e) {
                return false;
            }
        }

        return false;
    }

    public static ArrayList<Boolean[]> parse(String permLine) {
        ArrayList<Boolean[]> arrayList = new ArrayList<>();
        Boolean[] read = new Boolean[]{false, false, false};
        Boolean[] write = new Boolean[]{false, false, false};
        Boolean[] execute = new Boolean[]{false, false, false};
        if (permLine.charAt(1) == 'r') {
            read[0] = true;
        }
        if (permLine.charAt(2) == 'w') {
            write[0] = true;
        }
        if (permLine.charAt(3) == 'x') {
            execute[0] = true;
        }
        if (permLine.charAt(4) == 'r') {
            read[1] = true;
        }
        if (permLine.charAt(5) == 'w') {
            write[1] = true;
        }
        if (permLine.charAt(6) == 'x') {
            execute[1] = true;
        }
        if (permLine.charAt(7) == 'r') {
            read[2] = true;
        }
        if (permLine.charAt(8) == 'w') {
            write[2] = true;
        }
        if (permLine.charAt(9) == 'x') {
            execute[2] = true;
        }
        arrayList.add(read);
        arrayList.add(write);
        arrayList.add(execute);
        return arrayList;
    }

    /**
     * @param fileInfo Paths to delete
     */
    public void showDeleteDialog(final AceActivity activity, final ArrayList<FileInfo> fileInfo, final boolean
            isRooted) {
        String title = activity.getString(R.string.dialog_delete_title);
        String texts[] = new String[]{title, activity.getString(R.string.msg_ok), "", activity.getString(R.string
                .dialog_cancel)};

        ArrayList<String> items = new ArrayList<>();
        for (int i = 0; i < fileInfo.size(); i++) {
            String path = fileInfo.get(i).getFilePath();
            items.add(path);
            if (i == 9 && fileInfo.size() > 10) {
                int rem = fileInfo.size() - 10;
                items.add("+" + rem + " " + activity.getString(R.string.more));
                break;
            }
        }
        final MaterialDialog materialDialog = new DialogUtils().showListDialog(activity, texts, items);


        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.mFileOpsHelper.deleteFiles(fileInfo, isRooted);
                materialDialog.dismiss();
            }
        });

        materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });

        materialDialog.show();
    }

    @SuppressWarnings("ConstantConditions")
    public void createFileDialog(final AceActivity activity, final boolean isRootMode, final String path) {

        String title = activity.getString(R.string.new_file);
        String texts[] = new String[]{activity.getString(R.string.enter_name), "", title, activity.getString(R.string
                .create),
                "",
                activity.getString(R.string.dialog_cancel)};
        final MaterialDialog materialDialog = new DialogUtils().showEditDialog(activity, texts);

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = materialDialog.getInputEditText().getText().toString();
                if (FileUtils.isFileNameInvalid(fileName)) {
                    materialDialog.getInputEditText().setError(activity.getResources().getString(R.string
                            .msg_error_valid_name));
                    return;
                }

                fileName = fileName.trim();
                fileName = fileName + ".txt";
                fileName = path + File.separator + fileName;
                activity.mFileOpsHelper.mkFile(isRootMode, new File(fileName));
                materialDialog.dismiss();
            }
        });

        materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });

        materialDialog.show();
    }

    @SuppressWarnings("ConstantConditions")
    public void createDirDialog(final AceActivity activity, final boolean isRootMode, final String path) {

        String title = activity.getString(R.string.new_folder);
        String texts[] = new String[]{activity.getString(R.string.enter_name), "", title, activity.getString(R.string
                .create), "", activity.getString(R.string.dialog_cancel)};
        final MaterialDialog materialDialog = new DialogUtils().showEditDialog(activity, texts);

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = materialDialog.getInputEditText().getText().toString();
                if (FileUtils.isFileNameInvalid(fileName)) {
                    materialDialog.getInputEditText().setError(activity.getResources().getString(R.string
                            .msg_error_valid_name));
                    return;
                }

                fileName = fileName.trim();
                fileName = path + File.separator + fileName;
                activity.mFileOpsHelper.mkDir(isRootMode, path, fileName);
                materialDialog.dismiss();
            }
        });

        materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });

        materialDialog.show();
    }

    private static void showApkOptionsDialog(final Fragment fragment, final String path, final String extension) {
        final Context context = fragment.getContext();

        String texts[] = new String[]{context.getString(R.string.package_installer), context.getString(R.string
                .install), context.getString(R.string.dialog_cancel), context.getString(R.string.view),
                context.getString(R.string.package_installer_content)};

        final MaterialDialog materialDialog = new DialogUtils().showDialog(context, texts);


        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = createContentUri(fragment.getContext(), path);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_INSTALL_PACKAGE);

                String mimeType = getSingleton().getMimeTypeFromExtension(extension);
                intent.setData(uri);

                if (mimeType != null) {
                    grantUriPermission(context, intent, uri);
                } else {
                    openWith(uri, context);
                }
                materialDialog.dismiss();

            }
        });

        materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             /*   if (fragment instanceof FileListDualFragment)
                    ((FileListDualFragment) fragment).openCompressedFile(path);
                else*/
                ((BaseFileList) fragment).openCompressedFile(path);

                materialDialog.dismiss();
            }
        });

        materialDialog.show();

    }

    @SuppressWarnings("ConstantConditions")
    public void showCompressDialog(final AceActivity activity, final String currentDir, final ArrayList<FileInfo>
            paths) {

        final String ext = ".zip";
        String fileName = paths.get(0).getFileName();
        String filePath = paths.get(0).getFilePath();
        String zipName = fileName;
        if (!(new File(filePath).isDirectory())) {
            zipName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        String title = activity.getString(R.string.create);
        String texts[] = new String[]{"", zipName, title, title, "",
                activity.getString(R.string.dialog_cancel)};
        final MaterialDialog materialDialog = new DialogUtils().showEditDialog(activity, texts);

        materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = materialDialog.getInputEditText().getText().toString();
                if (FileUtils.isFileNameInvalid(fileName)) {
                    materialDialog.getInputEditText().setError(activity.getResources().getString(R.string
                            .msg_error_valid_name));
                    return;
                }
                String newFilePath = currentDir + "/" + fileName + ext;

                if (FileUtils.isFileExisting(currentDir, fileName + ext)) {
                    materialDialog.getInputEditText().setError(activity.getResources().getString(R.string
                            .dialog_title_paste_conflict));
                    return;
                }
                activity.mFileOpsHelper.compressFile(new File(newFilePath), paths);
                materialDialog.dismiss();
            }
        });

        materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDialog.dismiss();
            }
        });

        materialDialog.show();
    }


    /**
     * Check for a directory if it is possible to create files within this directory, either via normal writing or via
     * Storage Access Framework.
     *
     * @param folder The directory
     * @return true if it is possible to write in this directory.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isFileNonWritable(final File folder, Context c) {
        // Verify that this is a directory.
        if (folder == null)
            return false;
        if (!folder.exists() || !folder.isDirectory()) {
            return false;
        }

        // Find a non-existing file in this directory.
        int i = 0;
        File file;
        do {
            String fileName = "AugendiagnoseDummyFile" + (++i);
            file = new File(folder, fileName);
        }
        while (file.exists());

        // First check regular writability
        if (isWritable(file)) {
            return false;
        }

        // Next check SAF writability.
        DocumentFile document = getDocumentFile(file, false, c);

        if (document == null) {
            return true;
        }

        // This should have created the file - otherwise something is wrong with access URL.
        boolean result = document.canWrite() && file.exists();

        // Ensure that the dummy file is not remaining.
        deleteFile(file, c);
        return !result;
    }

    /**
     * Delete a file. May be even on external SD card.
     *
     * @param file the file to be deleted.
     * @return True if successfully deleted.
     */
    public static boolean deleteFile(@NonNull final File file, Context context) {
        // First try the normal deletion.
        boolean fileDelete = deleteFilesInFolder(file, context);
        if (file.delete() || fileDelete)
            return true;

        // Try with Storage Access Framework.
        if (Utils.isAtleastLollipop() && isOnExtSdCard(file, context)) {

            DocumentFile document = getDocumentFile(file, false, context);
            return document != null && document.delete();
        }

        // Try the Kitkat workaround.
        if (Utils.isKitkat()) {
            ContentResolver resolver = context.getContentResolver();

            try {
                Uri uri = getUriFromFile(file.getAbsolutePath(), context);
                if (uri != null) resolver.delete(uri, null, null);
                return !file.exists();
            } catch (Exception e) {
                Logger.log(TAG, "Error when deleting file " + file.getAbsolutePath());
                return false;
            }
        }

        return !file.exists();
    }

    public static void scanFile(Context context, String path) {

        Intent intent =
                new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(createContentUri(context, path));
        context.sendBroadcast(intent);


/*        MediaScannerConnection.scanFile(context,
                new String[]{path}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i(TAG, "Scanned " + path + ":");
                        Log.i(TAG, "-> uri=" + uri);
                    }
                });*/
    }


    public static boolean isPackageIntentUnavailable(Context context, Intent intent) {
        return context.getPackageManager().resolveActivity(intent, 0) == null;
    }

    public static boolean isFileCompressed(String filePath) {
        return filePath.toLowerCase().endsWith("zip") ||
                filePath.toLowerCase().endsWith("apk") ||
                filePath.toLowerCase().endsWith("jar") ||
                filePath.toLowerCase().endsWith("tar") ||
                filePath.toLowerCase().endsWith("tar.gz") ||
                filePath.toLowerCase().endsWith("rar");
    }

    /**
     * Delete all files in a folder.
     *
     * @param folder the folder
     * @return true if successful.
     */
    private static boolean deleteFilesInFolder(final File folder, Context context) {
        boolean totalSuccess = true;
        if (folder == null)
            return false;
        if (folder.isDirectory()) {
            if (folder.listFiles() != null) {
                for (File child : folder.listFiles()) {
                    deleteFilesInFolder(child, context);
                }
            }

            if (!folder.delete())
                totalSuccess = false;
        } else {

            if (!folder.delete())
                totalSuccess = false;
        }
        return totalSuccess;
    }


    /**
     * Determine if a file is on external sd card. (Kitkat or higher.)
     *
     * @param file The file.
     * @return true if on external sd card.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isOnExtSdCard(final File file, Context c) {
        return getExtSdCardFolder(file, c) != null;
    }

    public static boolean isFileExisting(String currentDir, String fileName) {
        File file = new File(currentDir);
        String[] list = file.list();
        for (String aList : list) {
            if (fileName.equals(aList)) return true;
        }
        return false;
    }

    public static Drawable getAppIcon(Context context, String url) {


        Drawable icon;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(url, PackageManager
                    .GET_ACTIVITIES);
            if (packageInfo == null)
                return ContextCompat.getDrawable(context, R.drawable.ic_apk_green);
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            appInfo.sourceDir = url;
            appInfo.publicSourceDir = url;

            icon = appInfo.loadIcon(context.getPackageManager());
            return icon;
        } catch (Exception e) {
            e.printStackTrace();
            return ContextCompat.getDrawable(context, R.drawable.ic_apk_green);

        }
    }

    public static Drawable getAppIconForFolder(Context context, String packageName) {

        try {
            return context.getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * Gets the extension of a file name, like ".png" or ".jpg".
     *
     * @param uri
     * @return Extension including the dot("."); "" if there is no extension;
     * null if uri was null.
     */
    private static String getExtension(String uri) {
        if (uri == null) {
            return null;
        }

        int dot = uri.lastIndexOf(".");
        if (dot >= 0) {
            return uri.substring(dot + 1);
        } else {
            // No extension.
            return "";
        }
    }

    public static String formatSize(Context context, long sizeInBytes) {
        return Formatter.formatFileSize(context, sizeInBytes);
    }

    public static String getFastHash(String filepath) {
        MessageDigest md;
        String hash;
        File file = new File(filepath);
        try {
            try {
                md = MessageDigest.getInstance("MD5");

            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("cannot initialize MD5 hash function", e);
            }
            FileInputStream fin = new FileInputStream(file);
            if (file.length() > 1048576L) {
                byte data[] = new byte[(int) file.length() / 100];
                //noinspection ResultOfMethodCallIgnored
                fin.read(data);
                fin.close();
                hash = new BigInteger(1, md.digest(data)).toString(16);
            } else if (file.length() > 1024L) {
                byte data[] = new byte[(int) file.length() / 10];
                //noinspection ResultOfMethodCallIgnored
                fin.read(data);
                fin.close();
                hash = new BigInteger(1, md.digest(data)).toString(16);
            } else {
                byte data[] = new byte[(int) file.length()];
                //noinspection ResultOfMethodCallIgnored
                fin.read(data);
                fin.close();
                hash = new BigInteger(1, md.digest(data)).toString(16);
            }
        } catch (IOException e) {
            throw new RuntimeException("cannot read file " + file.getAbsolutePath(), e);

        }


        return hash;
    }


}
