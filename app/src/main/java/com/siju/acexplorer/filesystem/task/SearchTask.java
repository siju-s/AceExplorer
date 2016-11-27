package com.siju.acexplorer.filesystem.task;

import android.os.AsyncTask;
import android.util.Log;

import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.helper.RootHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;


public class SearchTask {

    private SearchHelper searchHelper;
    private String input;
    public SearchAsync searchAsync;
    private String mPath;
    private boolean mShowHidden;

    public interface SearchHelper {
        void onPreExecute();

        void onPostExecute();

        void onProgressUpdate(FileInfo val);

        void onCancelled();
    }

    public SearchTask(SearchHelper context, String input, String path) {
        searchHelper =  context;
        this.input = input;
        mShowHidden = false;
        mPath = path;
        execute(input);

    }

    public void execute(String input) {
        this.input = input;
        if (searchAsync == null) {
            searchAsync = new SearchAsync();
        }
        else if (searchAsync.getStatus() == AsyncTask.Status.RUNNING) {
            searchAsync.cancel(true);
        }
        if (searchAsync.getStatus() != AsyncTask.Status.RUNNING) {
            searchAsync.execute(mPath);
        }
    }

    public class SearchAsync extends AsyncTask<String, FileInfo, Void> {


        @Override
        protected void onPreExecute() {

            /*
            * Note that we need to check if the callbacks are null in each
            * method in case they are invoked after the Activity's and
            * Fragment's onDestroy() method have been called.
             */

            searchHelper.onPreExecute();
        }

        // mCallbacks not checked for null because of possibility of
        // race conditions b/w worker thread main thread
        @Override
        protected Void doInBackground(String... params) {

            mPath = params[0];

            // compile the regular expression in the input
            Pattern pattern = Pattern.compile(bashRegexToJava(input));
            Log.d("SearchTask", "doInBackground:pattern= "+pattern);
            searchRegExFind(new File(mPath), pattern);
            return null;
        }

        @Override
        public void onPostExecute(Void c) {
            searchHelper.onPostExecute();
        }

        @Override
        protected void onCancelled() {
            searchHelper.onCancelled();
        }

        @Override
        public void onProgressUpdate(FileInfo... val) {
            Log.d("SearchTask", "onProgressUpdate: "+val[0].getFilePath());
            if (!isCancelled() && searchHelper != null) {
                searchHelper.onProgressUpdate(val[0]);
            }
        }

/*        *//**
         * Recursively search for occurrences of a given text in file names and publish the result
         *
         * @param file  the current path
         * @param query the searched text
         *//*
        private void search(FileInfo file, String query) {

            if (file.isDirectory()) {
                ArrayList<BaseFile> f = file.listFiles(mRootMode);
                // do you have permission to read this directory?
                if (!isCancelled())
                    for (BaseFile x : f) {
                        if (!isCancelled()) {
                            if (x.isDirectory()) {
                                if (x.getName().toLowerCase()
                                        .contains(query.toLowerCase())) {
                                    publishProgress(x);
                                }
                                if (!isCancelled()) search(x, query);

                            } else {
                                if (x.getName().toLowerCase()
                                        .contains(query.toLowerCase())) {
                                    publishProgress(x);
                                }
                            }
                        } else return;
                    }
                else return;
            } else {
                System.out
                        .println(file.getPath() + "Permission Denied");
            }
        }*/

        /**
         * Recursively find a java regex pattern {@link Pattern} in the file names and publish the result
         *
         * @param file    the current file
         * @param pattern the compiled java regex
         */
        private void searchRegExFind(File file, Pattern pattern) {

            if (file.isDirectory()) {
                String path = file.getAbsolutePath();
                ArrayList<FileInfo> f = RootHelper.getFilesList(path, true, mShowHidden, false);
                Log.d("SearchTask", "searchRegExFind:size= "+f.size());

                if (!isCancelled()) {
                    for (FileInfo x : f) {
                        if (!isCancelled()) {
                            if (x.isDirectory()) {
                                if (pattern.matcher(x.getFileName()).find()) {
                                    publishProgress(x);
                                }
                                if (!isCancelled()) {
                                    searchRegExFind(new File(x.getFilePath()), pattern);
                                }

                            } else {
                                if (pattern.matcher(x.getFileName()).find()) {
                                    publishProgress(x);
                                }
                            }
                        } else return;
                    }
                }

            } else {
                System.out
                        .println(file.getAbsolutePath() + "Permission Denied");
            }
        }

     /*   *//**
         * Recursively match a java regex pattern {@link Pattern} with the file names and publish the result
         *
         * @param file    the current file
         * @param pattern the compiled java regex
         *//*
        private void searchRegExMatch(HFile file, Pattern pattern) {

            if (file.isDirectory()) {
                ArrayList<BaseFile> f = file.listFiles(mRootMode);

                if (!isCancelled())
                    for (BaseFile x : f) {
                        if (!isCancelled()) {
                            if (x.isDirectory()) {
                                if (pattern.matcher(x.getName()).matches()) publishProgress(x);
                                if (!isCancelled()) searchRegExMatch(x, pattern);

                            } else {
                                if (pattern.matcher(x.getName()).matches()) {
                                    publishProgress(x);
                                }
                            }
                        } else return;
                    }
                else return;
            } else {
                System.out
                        .println(file.getPath() + "Permission Denied");
            }
        }*/

        /**
         * method converts bash style regular expression to java. See {@link Pattern}
         *
         * @param originalString
         * @return converted string
         */
        private String bashRegexToJava(String originalString) {
            StringBuilder stringBuilder = new StringBuilder();

            for (int i = 0; i < originalString.length(); i++) {
                switch (originalString.charAt(i) + "") {
                    case "*":
                        stringBuilder.append("\\w*");
                        break;
                    case "?":
                        stringBuilder.append("\\w");
                        break;
                    default:
                        stringBuilder.append(originalString.charAt(i));
                        break;
                }
            }

            Log.d(getClass().getSimpleName(), stringBuilder.toString());
            return stringBuilder.toString();
        }
    }
}