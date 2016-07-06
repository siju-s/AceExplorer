package com.siju.filemanager.filesystem;

import android.content.ContentUris;
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
import com.siju.filemanager.filesystem.model.FileInfo;
import com.siju.filemanager.filesystem.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    private int mCategory;
    private Uri mAudioUri = Uri.parse("content://media/external/audio/albumart");
    private Uri mImageUri = Uri.parse("content://media/external/images/albumart");
    private int mViewMode;
    private ArrayList<FileInfo> fileInfoArrayListCopy = new ArrayList<>();


    public FileListAdapter(Context mContext, ArrayList<FileInfo> fileInfoArrayList, int category, int viewMode) {
        this.mContext = mContext;
        this.fileInfoArrayList = fileInfoArrayList;
        mSelectedItemsIds = new SparseBooleanArray();
        mSelectedFileList = new ArrayList<>();
        mCategory = category;
        this.mViewMode = viewMode;
    }

    public void updateAdapter(ArrayList<FileInfo> fileInfos) {
        this.fileInfoArrayList = fileInfos;
        fileInfoArrayListCopy.addAll(fileInfos);
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

    @Override
    public FileListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (mViewMode == FileConstants.KEY_LISTVIEW) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_list_item, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_grid_item, parent, false);
        }
        FileListViewHolder tvh = new FileListViewHolder(view);
        return tvh;
    }

    @Override
    public void onBindViewHolder(FileListViewHolder fileListViewHolder, int position) {
        //change background color if list item is selected
        int color = ContextCompat.getColor(mContext, R.color.actionModeItemSelected);
        fileListViewHolder.itemView.setBackgroundColor(mSelectedItemsIds.get(position) ? color :
                Color.TRANSPARENT);
        setViewByCategory(fileListViewHolder, position);

    }


    @Override
    public int getItemCount() {
        if (fileInfoArrayList == null) {
            return 0;
        } else {
            return fileInfoArrayList.size();
        }
    }

    private void setViewByCategory(FileListViewHolder fileListViewHolder, int position) {

        String fileName = fileInfoArrayList.get(position).getFileName();
        String fileDate = fileInfoArrayList.get(position).getFileDate();
        boolean isDirectory = fileInfoArrayList.get(position).isDirectory();
        String fileNoOrSize = fileInfoArrayList.get(position).getNoOfFilesOrSize();
        String filePath = fileInfoArrayList.get(position).getFilePath();

        fileListViewHolder.textFileName.setText(fileName);
        if (mViewMode == FileConstants.KEY_LISTVIEW) {
            fileListViewHolder.textFileModifiedDate.setText(fileDate);
        }
        fileListViewHolder.textNoOfFileOrSize.setText(fileNoOrSize);

        switch (mCategory) {
            case 0: // For file group
                if (isDirectory) {
                    fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_folder_white);
                    Drawable apkIcon = FileUtils.getAppIconForFolder(mContext, fileName);
                    if (apkIcon != null) {
                        fileListViewHolder.imageThumbIcon.setImageDrawable(apkIcon);
                    } else {
                        fileListViewHolder.imageThumbIcon.setImageDrawable(null);
                    }

                } else {
                    String extension = fileInfoArrayList.get(position).getExtension();
                    if (extension.equals(FileConstants.APK_EXTENSION)) {
                        Drawable apkIcon = FileUtils.getAppIcon(mContext, filePath);
                        fileListViewHolder.imageIcon.setImageDrawable(apkIcon);
                    } else {
                        changeFileIcon(fileListViewHolder, extension);
                    }
                    int type = fileInfoArrayList.get(position).getType();
                    // If Image or Video file, load thumbnail
                    if (type == FileConstants.CATEGORY.IMAGE.getValue() ||
                            type == FileConstants.CATEGORY.VIDEO.getValue()) {

                        Uri imageUri = Uri.fromFile(new File(filePath));
                        Glide.with(mContext).load(imageUri).centerCrop()
                                .crossFade(2)
                                .into(fileListViewHolder.imageIcon);

                    }

                }
                break;
            case 1:
                Uri uri = ContentUris.withAppendedId(mAudioUri, fileInfoArrayList.get(position).getBucketId());
                Glide.with(mContext).loadFromMediaStore(uri).centerCrop()
                        .placeholder(R.drawable.ic_music)
                        .crossFade(2)
                        .into(fileListViewHolder.imageIcon);
                break;

            case 2:
                // For videos group
                Uri videoUri = Uri.fromFile(new File(filePath));
                Glide.with(mContext).load(videoUri).centerCrop()
                        .placeholder(R.drawable.ic_movie)
                        .crossFade(2)
                        .into(fileListViewHolder.imageIcon);
                break;

            case 3: // For images group
                Uri imageUri = Uri.fromFile(new File(filePath));
                Glide.with(mContext).load(imageUri).centerCrop()
                        .crossFade(2)
                        .into(fileListViewHolder.imageIcon);
                break;
            case 4: // For docs group
                String extension = fileInfoArrayList.get(position).getExtension();
                changeFileIcon(fileListViewHolder, extension);
                break;

        }

    }

    private void changeFileIcon(FileListViewHolder fileListViewHolder, String extension) {
        switch (extension) {
            case FileConstants.EXT_DOC:
            case FileConstants.EXT_DOCX:
                fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_doc);
                break;
            case FileConstants.EXT_XLS:
            case FileConstants.EXT_XLXS:
                fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_xls);
                break;
            case FileConstants.EXT_PPT:
            case FileConstants.EXT_PPTX:
                fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_ppt);
                break;
            case FileConstants.EXT_PDF:
                fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_pdf);
                break;
            case FileConstants.EXT_TEXT:
                fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_txt);
                break;
            case FileConstants.EXT_HTML:
                fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_html);
                break;
            case FileConstants.EXT_ZIP:
                fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_file_zip);
            default:
                fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_doc_white);
                break;
        }
    }


    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void toggleSelectAll(int position, boolean selectAll) {
        if (selectAll)
            mSelectedItemsIds.put(position, selectAll);
        else
            mSelectedItemsIds = new SparseBooleanArray();
    }

    public void clearSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
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


    public void setModel(List<FileInfo> models) {
        fileInfoArrayList = new ArrayList<>(models);
    }

    public FileInfo removeItem(int position) {
        final FileInfo model = fileInfoArrayList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, FileInfo model) {
        fileInfoArrayList.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final FileInfo model = fileInfoArrayList.remove(fromPosition);
        fileInfoArrayList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(List<FileInfo> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<FileInfo> newModels) {
        for (int i = fileInfoArrayList.size() - 1; i >= 0; i--) {
            final FileInfo model = fileInfoArrayList.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<FileInfo> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final FileInfo model = newModels.get(i);
            if (!fileInfoArrayList.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<FileInfo> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final FileInfo model = newModels.get(toPosition);
            final int fromPosition = fileInfoArrayList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public void filter(String text) {
        if (text.isEmpty()) {
            fileInfoArrayList.clear();
            fileInfoArrayList.addAll(fileInfoArrayListCopy);
        } else {
            ArrayList<FileInfo> result = new ArrayList<>();
            text = text.toLowerCase();
            for (FileInfo item : fileInfoArrayListCopy) {
                if (item.getFileName().toLowerCase().contains(text)) {
                    result.add(item);
                }
            }
            if (fileInfoArrayList != null) {
                fileInfoArrayList.clear();
                fileInfoArrayList.addAll(result);
            }
        }
        notifyDataSetChanged();
    }


    class FileListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {
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
            if (mViewMode == FileConstants.KEY_LISTVIEW) {
                textFileModifiedDate = (TextView) itemView.findViewById(R.id.textDate);
            }
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
