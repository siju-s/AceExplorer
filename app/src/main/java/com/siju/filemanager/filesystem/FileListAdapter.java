package com.siju.filemanager.filesystem;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.siju.filemanager.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Siju on 13-06-2016.
 */

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.FileListViewHolder> {

    private Context mContext;
    private ArrayList<FileInfo> fileInfoArrayList;
    private SparseBooleanArray mSelectedItemsIds;
    private ArrayList<FileInfo> mSelectedFileList;
    OnItemClickListener mItemClickListener;
    OnItemLongClickListener mOnItemLongClickListener;

    public FileListAdapter(Context mContext, ArrayList<FileInfo> fileInfoArrayList) {
        this.mContext = mContext;
        this.fileInfoArrayList = fileInfoArrayList;
        mSelectedItemsIds = new SparseBooleanArray();
        mSelectedFileList = new ArrayList<>();
    }

    public void updateAdapter(ArrayList<FileInfo> fileInfos) {
        this.fileInfoArrayList = fileInfos;
//        Log.d("SIJU","updateAdapter"+fileInfoArrayList.size());
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
         void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
         void onItemLongClick(View view, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
    public void setOnItemLongClickListener(final OnItemLongClickListener mItemClickListener) {
        this.mOnItemLongClickListener = mItemClickListener;
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
    public FileListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_list_item, parent, false);
        FileListViewHolder tvh = new FileListViewHolder(v);
        return tvh;
    }

    @Override
    public void onBindViewHolder(FileListViewHolder fileListViewHolder, int position) {
        //change background color if list item is selected
        int color = ContextCompat.getColor(mContext, R.color.actionModeItemSelected);
        fileListViewHolder.itemView.setBackgroundColor(mSelectedItemsIds.get(position) ? color :
                Color.TRANSPARENT);
        String fileName = fileInfoArrayList.get(position).getFileName();
        String fileDate = fileInfoArrayList.get(position).getFileDate();
        boolean isDirectory = fileInfoArrayList.get(position).isDirectory();
        String fileNoOrSize = fileInfoArrayList.get(position).getNoOfFilesOrSize();
        String filePath = fileInfoArrayList.get(position).getFilePath();

        fileListViewHolder.textFileName.setText(fileName);
        fileListViewHolder.textFileModifiedDate.setText(fileDate);

        if (isDirectory) {
            fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_folder_white);
            Drawable apkIcon = FileUtils.getAppIconForFolder(mContext, fileName);
            if (apkIcon != null) {
                fileListViewHolder.imageThumbIcon.setImageDrawable(apkIcon);
            }


        } else {
            if (fileInfoArrayList.get(position).getExtension().equals(FileConstants.APK_EXTENSION)) {
                Drawable apkIcon = FileUtils.getAppIcon(mContext, filePath);
//                Drawable apkIcon = getApkIcon(filePath);
                fileListViewHolder.imageIcon.setImageDrawable(apkIcon);
            } else {
                fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_doc_white);
            }
            if ((fileInfoArrayList.get(position).getType() == 1) || (fileInfoArrayList.get(position).getType() == 2)) {

                Uri imageUri = Uri.fromFile(new File(filePath));
                Glide.with(mContext).load(imageUri).centerCrop()
                        .crossFade(2)
                        .into(fileListViewHolder.imageIcon);

            }


        }
        fileListViewHolder.textNoOfFileOrSize.setText(fileNoOrSize);
    }


    @Override
    public int getItemCount() {
        if (fileInfoArrayList == null) {
            return 0;
        } else {
            return fileInfoArrayList.size();
        }
    }


    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        mSelectedFileList = new ArrayList<>();
        notifyDataSetChanged();

    }

    public void selectView(int position, boolean value) {
        if (value) {
            mSelectedItemsIds.put(position, true);
//            mSelectedFileList.add(fileInfoArrayList.get(position));
        } else {
            mSelectedItemsIds.delete(position);

        }

        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return mSelectedItemsIds.size();// mSelectedCount;
    }

    public SparseBooleanArray getSelectedItemPositions() {
        return mSelectedItemsIds;
    }


    class FileListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
                                                                    View.OnLongClickListener{
        ImageView imageIcon;
        ImageView imageThumbIcon;
        TextView textFileName;
        TextView textFileModifiedDate;
        TextView textNoOfFileOrSize;

        public FileListViewHolder(View itemView) {
            super(itemView);
            textFileName = (TextView) itemView
                    .findViewById(R.id.textFolderName);
            imageIcon = (ImageView) itemView.findViewById(R.id.imageIcon);
            imageThumbIcon = (ImageView) itemView.findViewById(R.id.imageThumbIcon);
            textNoOfFileOrSize = (TextView) itemView.findViewById(R.id.textSecondLine);
            textFileModifiedDate = (TextView) itemView.findViewById(R.id.textDate);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mOnItemLongClickListener != null) {
                mOnItemLongClickListener.onItemLongClick(v, getAdapterPosition());
            }
            return true;
        }
    }


}
