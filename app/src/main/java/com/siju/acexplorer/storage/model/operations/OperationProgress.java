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

package com.siju.acexplorer.storage.model.operations;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.R;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.storage.model.CopyData;
import com.siju.acexplorer.storage.model.task.CopyService;
import com.siju.acexplorer.storage.model.task.CreateZipService;
import com.siju.acexplorer.storage.model.task.ExtractService;
import com.siju.acexplorer.storage.model.task.Progress;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_OP_FAILED;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILEPATH2;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_OPERATION;
import static com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_RESULT;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.COPY_PROGRESS;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.EXTRACT_PROGRESS;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.KEY_COMPLETED;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.KEY_COUNT;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.KEY_PROGRESS;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.KEY_TOTAL;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.KEY_TOTAL_PROGRESS;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.MOVE_PROGRESS;
import static com.siju.acexplorer.storage.model.operations.ProgressUtils.ZIP_PROGRESS;


public class OperationProgress implements Progress {

    private static final String TAG = "OperationProgress";
    private ProgressBar progressBarPaste;
    private AlertDialog progressDialog;
    private int copiedFilesSize;
    private TextView textFileName;
    private TextView textFileFromPath;
    private TextView textFileCount;
    private TextView textProgress;
    private ArrayList<FileInfo> copiedFileInfo;
    private Context mContext;
    private Intent mServiceIntent;


    public void showPasteProgress(final Context context, String destinationDir, List<FileInfo> files,
                                  List<CopyData> copyData, boolean isMove) {

        registerReceiver(context);
        String title;
        if (isMove) {
            title = context.getString(R.string.move);
        } else {
            title = context.getString(R.string.action_copy);
        }
        String texts[] = new String[]{title, context.getString(R.string.background),
                context.getString(R.string.dialog_cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_progress_paste, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        progressDialog = builder.create();

        progressDialog.setCancelable(false);
        textFileName = progressDialog.findViewById(R.id.textFileName);
        textFileFromPath = progressDialog.findViewById(R.id.textFileFromPath);
        TextView textFileToPath = progressDialog.findViewById(R.id.textFileToPath);
        textFileCount = progressDialog.findViewById(R.id.textFilesLeft);
        textProgress = progressDialog.findViewById(R.id.textProgressPercent);
        progressBarPaste = progressDialog.findViewById(R.id.progressBarPaste);

        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        positiveButton.setText(texts[1]);
        negativeButton.setText(texts[2]);

        copiedFileInfo.clear();
        copiedFileInfo.addAll(files);
        copiedFilesSize = copiedFileInfo.size();
        Logger.log("FileUtils", "Totalfiles=" + copiedFilesSize);


        textFileFromPath.setText(copiedFileInfo.get(0).getFilePath());
        textFileName.setText(copiedFileInfo.get(0).getFileName());
        textFileToPath.setText(destinationDir);
        textFileCount.setText(String.format(Locale.getDefault(), "%s%d", context.getString(R.string.count_placeholder),
                copiedFilesSize));
        textProgress.setText("0%");

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.dismiss();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopCopyService();
                progressDialog.dismiss();
            }
        });

        progressDialog.show();

    }


    public void showZipProgressDialog(final Context context, final Intent intent) {
/*        mContext = context;
        mServiceIntent = intent;
        context.bindService(mServiceIntent, mZipServiceConnection, Context.BIND_AUTO_CREATE);
        context.startService(mServiceIntent);*/
        String title = context.getString(R.string.zip_progress_title);
        String texts[] = new String[]{title, context.getString(R.string.background), "",
                context.getString(R.string.dialog_cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_progress_paste, null);
        builder.setView(dialogView);
        progressDialog = builder.create();
        progressDialog.setCancelable(false);
        textFileName = progressDialog.findViewById(R.id.textFileName);
        textFileFromPath = progressDialog.findViewById(R.id.textFileFromPath);
        TextView textFromPlaceHolder = progressDialog.findViewById(R.id.textFileFromPlaceHolder);
        (progressDialog.findViewById(R.id.textFileToPlaceHolder)).setVisibility(View.GONE);

        textFromPlaceHolder.setVisibility(View.GONE);
        textFileCount = progressDialog.findViewById(R.id.textFilesLeft);
        textProgress = progressDialog.findViewById(R.id.textProgressPercent);
        progressBarPaste = progressDialog.findViewById(R.id.progressBarPaste);

        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        positiveButton.setText(texts[1]);
        negativeButton.setText(texts[2]);

        copiedFileInfo = intent.getParcelableArrayListExtra(KEY_FILES);
        String fileName = intent.getStringExtra(KEY_FILEPATH);
        copiedFilesSize = copiedFileInfo.size();
        Logger.log("FileUtils", "Totalfiles=" + copiedFilesSize);
        textFileName.setText(fileName);
        textProgress.setText("0%");

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.dismiss();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
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
 /*       mContext = context;
        mServiceIntent = intent;
        context.bindService(mServiceIntent, mExtractServiceConnection, Context.BIND_AUTO_CREATE);
        context.startService(mServiceIntent);*/
        registerReceiver(context);
        String title = context.getString(R.string.extracting);
        String texts[] = new String[]{title, context.getString(R.string.background), "",
                context.getString(R.string.dialog_cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_progress_paste, null);
        builder.setView(dialogView);
        progressDialog.setCancelable(false);
        textFileName = progressDialog.findViewById(R.id.textFileName);
        textFileFromPath = progressDialog.findViewById(R.id.textFileFromPath);
        TextView textFileToPath = progressDialog.findViewById(R.id.textFileToPath);
        TextView textFromPlaceHolder = progressDialog.findViewById(R.id.textFileFromPlaceHolder);
        textFromPlaceHolder.setVisibility(View.GONE);
        textFileCount = progressDialog.findViewById(R.id.textFilesLeft);
        textProgress = progressDialog.findViewById(R.id.textProgressPercent);
        progressBarPaste = progressDialog.findViewById(R.id.progressBarPaste);

        Button positiveButton = dialogView.findViewById(R.id.buttonPositive);
        Button negativeButton = dialogView.findViewById(R.id.buttonNegative);

        positiveButton.setText(texts[1]);
        negativeButton.setText(texts[2]);

        textFileToPath.setText(intent.getStringExtra(KEY_FILEPATH));
        String fileName = intent.getStringExtra(KEY_FILEPATH2);

        textFileName.setText(fileName);
        textProgress.setText("0%");

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.dismiss();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopExtractService();
                progressDialog.dismiss();
            }
        });

        progressDialog.show();
    }


    private void stopZipService() {
        Context context = AceApplication.getAppContext();
        Intent intent = new Intent(context, CreateZipService.class);
        context.stopService(intent);
        unregisterReceiver(context);
    }

    private void stopExtractService() {
        Context context = AceApplication.getAppContext();
        Intent intent = new Intent(context, ExtractService.class);
        context.stopService(intent);
        unregisterReceiver(context);

    }

    private BroadcastReceiver operationFailureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_OP_FAILED)) {
                Operations operation = (Operations) intent.getSerializableExtra(KEY_OPERATION);
                switch (operation) {
                    case EXTRACT:
//                        Logger.log(TAG, "Failure broacast=" + isExtractServiceAlive);
//                        if (isExtractServiceAlive) {
                            unregisterReceiver(mContext);
                            progressDialog.dismiss();
//                            isExtractServiceAlive = false;
//                        }
                        break;
                }
            }
        }
    };

    private void stopCopyService() {
        Context context = AceApplication.getAppContext();
        Intent intent = new Intent(context, CopyService.class);
        context.stopService(intent);
        unregisterReceiver(context);
    }

    private BroadcastReceiver operationProgressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MOVE_PROGRESS)) {
                handleMessage(intent);
            }
        }
    };

    private void unregisterReceiver(Context context) {
        context.unregisterReceiver(operationProgressReceiver);
        context.unregisterReceiver(operationFailureReceiver);
    }


    private void registerReceiver(Context context) {
        IntentFilter filter = new IntentFilter(ACTION_OP_FAILED);
        context.registerReceiver(operationFailureReceiver, filter);
        IntentFilter filter1 = new IntentFilter(MOVE_PROGRESS);
        context.registerReceiver(operationProgressReceiver, filter1);
    }

    @Override
    public void onUpdate(Intent intent) {
 /*       Message msg = handler.obtainMessage();
        msg.obj = intent;
        handler.sendMessage(msg);*/

    }


    // Define the Handler that receives messages from the thread and update the progress
//    private final Handler handler = new Handler(Looper.getMainLooper()) {
    public void handleMessage(Intent intent) {
        int progress = intent.getIntExtra(KEY_PROGRESS, 0);
        long copiedBytes = intent.getLongExtra(KEY_COMPLETED, 0);
        long totalBytes = intent.getLongExtra(KEY_TOTAL, 0);

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

                if (progress == 100 || copiedBytes == totalBytes) {
                    stopExtractService();
                    progressDialog.dismiss();
                }
                break;
            case COPY_PROGRESS:
                boolean isSuccess = intent.getBooleanExtra(KEY_RESULT, true);

                if (!isSuccess) {
                    stopCopyService();
                    progressDialog.dismiss();
                    return;
                }
                int totalProgress = intent.getIntExtra(KEY_TOTAL_PROGRESS, 0);
                Logger.log("FileUtils", "KEY_PROGRESS=" + progress + " KEY_TOTAL KEY_PROGRESS=" + totalProgress);
                Logger.log("FileUtils", "Copied bytes=" + copiedBytes + " KEY_TOTAL bytes=" + totalBytes);
                progressBarPaste.setProgress(totalProgress);
                textProgress.setText(String.format(Locale.getDefault(), "%d%s", totalProgress, mContext.getString
                        (R.string
                                .percent_placeholder)));
                if (progress == 100 || totalBytes == copiedBytes) {
                    int count = intent.getIntExtra(KEY_COUNT, 1);
                    Logger.log("FileUtils", "KEY_COUNT=" + count);
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
            case MOVE_PROGRESS:
                int totalProgressPaste = intent.getIntExtra(KEY_TOTAL_PROGRESS, 0);
                Logger.log("FileUtils", "KEY_PROGRESS=" + progress + " KEY_TOTAL KEY_PROGRESS=" + totalProgressPaste);
                Logger.log("FileUtils", "Copied bytes=" + copiedBytes + " KEY_TOTAL bytes=" + totalBytes);
                progressBarPaste.setProgress(totalProgressPaste);
                textProgress.setText(String.format(Locale.getDefault(), "%d%s", totalProgressPaste, mContext.getString
                        (R.string.percent_placeholder)));
                int count = (int) copiedBytes;
                textFileFromPath.setText(copiedFileInfo.get(count).getFilePath());
                textFileName.setText(copiedFileInfo.get(count).getFileName());
                textFileCount.setText(count + "/" + copiedFilesSize);
                if (progress == 100) {
                    stopCopyService();
                    progressDialog.dismiss();
                }
                break;
        }
    }

//    };



}