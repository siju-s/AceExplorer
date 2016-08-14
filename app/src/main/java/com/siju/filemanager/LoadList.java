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

package com.siju.filemanager;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.siju.filemanager.filesystem.FileConstants;
import com.siju.filemanager.filesystem.FileListFragment;
import com.siju.filemanager.filesystem.model.FileInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class LoadList extends AsyncTask<Void, Void, ArrayList<FileInfo>> {

    private String path;
    boolean back;
//    Main ma;
    Context c;
    int openmode = 0;//0 for normal 1 for smb 2 for custom 3 for drive
    private ArrayList<FileInfo> fileInfoList;
    private String mPath;
    private Context mContext;
    private boolean showHidden;
    private int mCategory;
    private List<String> mZipRootFiles;
    private String mZipPath;
    private boolean mIsDualPaneInFocus;
    private FileListFragment mFragment;
    private boolean mInParentZip;

/*    public LoadList(boolean back, Context c, Main ma, int openmode) {
        this.back = back;
        this.ma = ma;
        this.openmode = openmode;
        this.c=c;
    }*/

    public LoadList(Context context, FileListFragment fragment,String path, int category) {
//        super(context);
        mPath = path;
        mContext = context;
        mCategory = category;
        mFragment = fragment;
        showHidden = PreferenceManager.getDefaultSharedPreferences(context).getBoolean
                (FileConstants.PREFS_HIDDEN, false);
    }

    @Override
    protected void onPreExecute() {
//        if (ma!=null && ma.mSwipeRefreshLayout!=null)
//            ma.mSwipeRefreshLayout.setRefreshing(true);
    }

/*
    @Override
    public void onProgressUpdate(String... message) {
//        if(c!=null)
//        Toast.makeText(c, message[0], Toast.LENGTH_SHORT).show();
    }
*/

    boolean grid;

    @Override
    // Actual download method, run in the task thread
    protected ArrayList<FileInfo> doInBackground(Void... params) {
        // params comes from the execute() call: params[0] is the url.
        fileInfoList = new ArrayList<>();
        fetchDataByCategory();
        return fileInfoList;


    }

    private void fetchDataByCategory() {
        switch (mCategory) {
            case 0:
            case 5:
                fetchFiles();
                break;
/*            case 1:
                fetchMusic();
                break;
            case 2:
                fetchVideos();
                break;
            case 3:
                fetchImages();
                break;
            case 10:
                Logger.log("SIJU", "apk category");
                fetchApk();
                break;
            case 4:
            case 7:
            case 9:
            case 11:
                fetchByCategory(mCategory);
                break;
            case 12:
                getZipContents(mZipPath, mPath);
                break;
            case 8:
                fetchFavorites();
                break;*/


        }
    }

    private ArrayList<FileInfo> fetchFiles() {
        File file = new File(mPath);
        String fileExtension = mPath.substring(mPath.lastIndexOf(".") + 1);
        boolean isRootAccessGranted = false;
        boolean isRoot = false;
        if (file.canRead()) {
            Log.d("TAG", "yeah");
        } else {
            Log.d("TAG", " NOOOO");

        }

        if (!file.canRead()) {
            fileInfoList = com.siju.filemanager.helper.RootHelper.getFilesList(mContext,mPath,
                    true,
                    showHidden);
        }


        return fileInfoList;

    }

    @Override
    protected void onPostExecute(ArrayList<FileInfo> infos) {
        Log.d("LoadList","Post ex="+infos.size());
//        mFragment.fileInfoList = infos;
        mFragment.update(infos);
    }

    /*  ArrayList<BaseFile> listaudio() {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media.DATA
        };

        Cursor cursor = c.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);

        ArrayList<BaseFile> songs = new ArrayList<>();
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                String path = cursor.getString(cursor.getColumnIndex
                        (MediaStore.Files.FileColumns.DATA));
                BaseFile strings = RootHelper.generateBaseFile(new File(path), ma.SHOW_HIDDEN);
                if (strings != null) songs.add(strings);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return songs;
    }

    ArrayList<BaseFile> listImages() {
        ArrayList<BaseFile> songs = new ArrayList<>();
        final String[] projection = {MediaStore.Images.Media.DATA};
        final Cursor cursor = c.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null);
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                String path = cursor.getString(cursor.getColumnIndex
                        (MediaStore.Files.FileColumns.DATA));
                BaseFile strings = RootHelper.generateBaseFile(new File(path), ma.SHOW_HIDDEN);
                if (strings != null) songs.add(strings);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return songs;
    }

    ArrayList<BaseFile> listVideos() {
        ArrayList<BaseFile> songs = new ArrayList<>();
        final String[] projection = {MediaStore.Images.Media.DATA};
        final Cursor cursor = c.getContentResolver().query(MediaStore.Video.Media
                        .EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null);
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                String path = cursor.getString(cursor.getColumnIndex
                        (MediaStore.Files.FileColumns.DATA));
                BaseFile strings = RootHelper.generateBaseFile(new File(path), ma.SHOW_HIDDEN);
                if (strings != null) songs.add(strings);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return songs;
    }

    ArrayList<BaseFile> listRecentFiles() {
        ArrayList<BaseFile> songs = new ArrayList<BaseFile>();
        final String[] projection = {MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.DATE_MODIFIED};
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) - 2);
        Date d = c.getTime();
        Cursor cursor = this.c.getContentResolver().query(MediaStore.Files
                        .getContentUri("external"), projection,
                null,
                null, null);
        if (cursor == null) return songs;
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                String path = cursor.getString(cursor.getColumnIndex
                        (MediaStore.Files.FileColumns.DATA));
                File f = new File(path);
                if (d.compareTo(new Date(f.lastModified())) != 1 && !f.isDirectory()) {
                    BaseFile strings = RootHelper.generateBaseFile(new File(path), ma.SHOW_HIDDEN);
                    if (strings != null) songs.add(strings);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        Collections.sort(songs, new Comparator<BaseFile>() {
            @Override
            public int compare(BaseFile lhs, BaseFile rhs) {
                return -1 * Long.valueOf(lhs.getDate()).compareTo(Long.valueOf(rhs.getDate()));

            }
        });
        if (songs.size() > 20)
            for (int i = songs.size() - 1; i > 20; i--) {
                songs.remove(i);
            }
        return songs;
    }

    ArrayList<BaseFile> listApks() {
        ArrayList<BaseFile> songs = new ArrayList<BaseFile>();
        final String[] projection = {MediaStore.Files.FileColumns.DATA};

        Cursor cursor = c.getContentResolver().query(MediaStore.Files
                        .getContentUri("external"), projection,
                null,
                null, null);
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                String path = cursor.getString(cursor.getColumnIndex
                        (MediaStore.Files.FileColumns.DATA));
                if (path != null && path.endsWith(".apk")) {
                    BaseFile strings = RootHelper.generateBaseFile(new File(path), ma.SHOW_HIDDEN);
                    if (strings != null) songs.add(strings);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return songs;
    }

    ArrayList<BaseFile> listRecent() {
        final HistoryManager history = new HistoryManager(c, "Table2");
        final ArrayList<String> paths = history.readTable(DataUtils.HISTORY);
        history.end();
        ArrayList<BaseFile> songs = new ArrayList<>();
        for (String f : paths) {
            if (!f.equals("/")) {
                BaseFile a = RootHelper.generateBaseFile(new File(f), ma.SHOW_HIDDEN);
                a.generateMode(ma.getActivity());
                if (a != null && !a.isSmb() && !(a).isDirectory() && a.exists())
                    songs.add(a);
            }
        }
        return songs;
    }

    ArrayList<BaseFile> listDocs() {
        ArrayList<BaseFile> songs = new ArrayList<>();
        final String[] projection = {MediaStore.Files.FileColumns.DATA};
        Cursor cursor = c.getContentResolver().query(MediaStore.Files
                        .getContentUri("external"), projection,
                null,
                null, null);
        String[] types = new String[]{".pdf", ".xml", ".html", ".asm", ".text/x-asm", ".def", ".in", ".rc",
                ".list", ".log", ".pl", ".prop", ".properties", ".rc",
                ".doc", ".docx", ".msg", ".odt", ".pages", ".rtf", ".txt", ".wpd", ".wps"};
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                String path = cursor.getString(cursor.getColumnIndex
                        (MediaStore.Files.FileColumns.DATA));
                if (path != null && contains(types, path)) {
                    BaseFile strings = RootHelper.generateBaseFile(new File(path), ma.SHOW_HIDDEN);
                    if (strings != null) songs.add(strings);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return songs;
    }

    boolean contains(String[] types, String path) {
        for (String string : types) {
            if (path.endsWith(string)) return true;
        }
        return false;
    }*/
}
