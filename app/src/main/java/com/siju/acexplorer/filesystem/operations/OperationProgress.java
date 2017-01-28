package com.siju.acexplorer.filesystem.operations;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.task.CopyService;
import com.siju.acexplorer.filesystem.task.CreateZipTask;
import com.siju.acexplorer.filesystem.task.ExtractService;
import com.siju.acexplorer.filesystem.task.Progress;
import com.siju.acexplorer.utils.Dialogs;

import java.util.ArrayList;
import java.util.Locale;

import static com.siju.acexplorer.filesystem.operations.OperationUtils.KEY_OPERATION;


public class OperationProgress implements Progress {

    private static final String TAG = "OperationProgress";
    public static final String COPY_PROGRESS = "copy_progress_update";
    public static final String ZIP_PROGRESS = "zip_progress_update";
    public static final String EXTRACT_PROGRESS = "extract_progress_update";

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
        progressDialog = new Dialogs().showCustomDialog(context,
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
        progressDialog = new Dialogs().showCustomDialog(context,
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
        progressDialog = new Dialogs().showCustomDialog(context,
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
                Operations operation = (Operations) intent.getSerializableExtra(KEY_OPERATION);
                switch (operation) {
                    case EXTRACT:
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




    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CopyService.LocalBinder binder = (CopyService.LocalBinder) service;
            CopyService mService = binder.getService();
            mService.registerProgressListener(OperationProgress.this);

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
            mService.registerProgressListener(OperationProgress.this);
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
            mService.registerProgressListener(OperationProgress.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

}
