package com.siju.filemanager.filesystem;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.siju.filemanager.R;

import java.io.File;
import java.util.ArrayList;

import static com.siju.filemanager.R.id.imageIcon;

/**
 * Created by Siju on 13-06-2016.
 */

public class FileListAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<FileInfo> fileInfoArrayList;
//    OnItemClickListener mItemClickListener;

    public FileListAdapter(Context mContext, ArrayList<FileInfo> fileInfoArrayList) {
        this.mContext = mContext;
        this.fileInfoArrayList = fileInfoArrayList;
    }

    public void updateAdapter(ArrayList<FileInfo> fileInfos) {
        this.fileInfoArrayList = fileInfos;
//        Log.d("SIJU","updateAdapter"+fileInfoArrayList.size());
        notifyDataSetChanged();
    }
//
//    @Override
//    public FileListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_list_item, parent, false);
//        FileListViewHolder viewHolder = new FileListViewHolder(v);
//        return viewHolder;
//    }
//
//    @Override
//    public void onBindViewHolder(FileListViewHolder holder, int position) {
//
//        Log.d("SIJU", "onBindViewHolder" + fileInfoArrayList.size());
//
//        String fileName = fileInfoArrayList.get(position).getFileName();
//        String fileDate = fileInfoArrayList.get(position).getFileDate();
//        boolean isDirectory = fileInfoArrayList.get(position).isDirectory();
//        String fileNoOrSize = fileInfoArrayList.get(position).getNoOfFilesOrSize();
//
//        holder.textFileName.setText(fileName);
//        holder.textFileModifiedDate.setText(fileDate);
//
//        if (isDirectory) {
//            holder.imageIcon.setImageResource(R.drawable.ic_folder_black);
//
//        } else {
//            holder.imageIcon.setImageResource(R.drawable.ic_file_black);
//
//        }
//        holder.textNoOfFileOrSize.setText(fileNoOrSize);
//    }

    @Override
    public int getCount() {

        if (fileInfoArrayList == null) {
            return 0;
        } else {

            return fileInfoArrayList.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return fileInfoArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        FileListViewHolder fileListViewHolder;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.file_list_item, null);
            fileListViewHolder = new FileListViewHolder();
            fileListViewHolder.textFileName = (TextView) view
                    .findViewById(R.id.textFolderName);
            fileListViewHolder.textFileModifiedDate = (TextView) view.findViewById(R.id.textDate);
            fileListViewHolder.imageIcon = (ImageView) view.findViewById(imageIcon);
            fileListViewHolder.textNoOfFileOrSize = (TextView) view.findViewById(R.id.textSecondLine);
            view.setTag(fileListViewHolder);
        } else {
            fileListViewHolder = (FileListViewHolder) view.getTag();
        }
        String fileName = fileInfoArrayList.get(position).getFileName();
        String fileDate = fileInfoArrayList.get(position).getFileDate();
        boolean isDirectory = fileInfoArrayList.get(position).isDirectory();
        String fileNoOrSize = fileInfoArrayList.get(position).getNoOfFilesOrSize();
        String filePath = fileInfoArrayList.get(position).getFilePath();

        fileListViewHolder.textFileName.setText(fileName);
        fileListViewHolder.textFileModifiedDate.setText(fileDate);

        if (isDirectory) {
            fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_folder3);

        } else {
            if (fileInfoArrayList.get(position).getExtension().equals(FileConstants.APK_EXTENSION)) {
                Drawable apkIcon = getApkIcon(filePath);
                fileListViewHolder.imageIcon.setImageDrawable(apkIcon);
            } else {
                fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_file_black);
            }
            if ((fileInfoArrayList.get(position).getType() == 1) || (fileInfoArrayList.get(position).getType() == 2)) {

                Uri imageUri = Uri.fromFile(new File(filePath));
                Glide.with(mContext).load(imageUri).centerCrop()
                        .crossFade(2)
                        .into(fileListViewHolder.imageIcon);

            }


        }
        fileListViewHolder.textNoOfFileOrSize.setText(fileNoOrSize);
        return view;
    }

    private Drawable getApkIcon(String path) {
        PackageManager pm = mContext.getPackageManager();
        PackageInfo packageInfo = pm.getPackageArchiveInfo(path, 0);

        // the secret are these two lines....
        packageInfo.applicationInfo.sourceDir = path;
        packageInfo.applicationInfo.publicSourceDir = path;
        //

        Drawable apkIcon = packageInfo.applicationInfo.loadIcon(pm);
        return apkIcon;
    }


    static class FileListViewHolder {
        ImageView imageIcon;
        TextView textFileName;
        TextView textFileModifiedDate;
        TextView textNoOfFileOrSize;

    }


}
