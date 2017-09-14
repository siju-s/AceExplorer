///*
// * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.siju.acexplorer.dialog;
//
//
//import android.content.Context;
//import android.support.annotation.NonNull;
//import android.support.v7.app.AlertDialog;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//
//import com.siju.acexplorer.R;
//
//import java.text.NumberFormat;
//
//public class ProgressDialog extends AlertDialog {
//
//    private TextView dialogText;
//    private ProgressBar progressBar;
//    private Button cancelButton;
//    private TextView textCount;
//    private TextView textProgress;
//    private NumberFormat progressPercentFormat;
//    private String progressNumberFormat;
//    private OnCancelListener onCancelListener;
//
//
//    public ProgressDialog(@NonNull Context context) {
//        super(context);
//        init();
//    }
//
//    private void init() {
//        View view = LayoutInflater.from(getContext()).inflate(R.layout.progress_dialog, null);
//        setView(view);
//        dialogText = (TextView) view.findViewById(R.id.textDialogTitle);
//        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
//        cancelButton = (Button) view.findViewById(R.id.buttonCancel);
//        textCount = (TextView) view.findViewById(R.id.textCount);
//        textProgress = (TextView) view.findViewById(R.id.textProgress);
//        progressNumberFormat = "%1$d/%2$d";
//        progressPercentFormat = NumberFormat.getPercentInstance();
//        progressPercentFormat.setMaximumFractionDigits(0);
//    }
//
//
//    public void setTitle(String title) {
//        dialogText.setText(title);
//    }
//
//    public void setProgress(int progress) {
//        progressBar.setProgress(progress);
//        String progressPercent = progressPercentFormat.format((double) progress / 100);
//        textProgress.setText(progressPercent);
//    }
//
//    public void setProgressMax(int max) {
//        progressBar.setMax(max);
//    }
//
//    public void setProgressPercentFormat(NumberFormat numberFormat) {
//        progressPercentFormat = numberFormat;
//    }
//
//
//    public void updateProgressCount(int count, int total) {
//        String text = String.format(progressNumberFormat, count, total);
//        textCount.setText(text);
//    }
//
//
//    public void setIndeterminate(boolean indeterminate) {
//        progressBar.setIndeterminate(indeterminate);
//    }
//
//
//    public void setOnCancelListener(OnCancelListener cancelListener) {
//        this.onCancelListener = onCancelListener;
//        cancelButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onCancelListener.onCancelClicked();
//            }
//        });
//    }
//
//
//    public interface OnCancelListener {
//
//        void onCancelClicked();
//    }
//
//
//
//
//}
