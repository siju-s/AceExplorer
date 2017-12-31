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

package com.siju.acexplorer.storage.view;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.siju.acexplorer.R;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.FileInfo;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.storage.model.ViewMode;
import com.siju.acexplorer.theme.ThemeUtils;

import java.util.ArrayList;

import static com.siju.acexplorer.model.groups.Category.PICKER;
import static com.siju.acexplorer.utils.ThumbnailUtils.displayThumb;


public class FileListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM   = 1;
    private static final int TYPE_FOOTER = 2;
    private static final String TAG = "FileListAdapter";
    private Context context;

    private       ArrayList<FileInfo> fileList = new ArrayList<>();
    private       ArrayList<FileInfo> filteredList = new ArrayList<>();
    private final SparseBooleanArray  mAnimatedPos          = new SparseBooleanArray();
    private SparseBooleanArray      mSelectedItemsIds;
    private OnItemClickListener     mItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private Category                category;

    private final int mViewMode;
    private int draggedPos = -1;
    private final int mAnimation;
    private int offset = 0;
    boolean mStopAnimation;
    private boolean mIsAnimNeeded = true;
    private final boolean mIsThemeDark;


    public FileListAdapter(Context context, ArrayList<FileInfo>
            fileList, Category category, int viewMode) {

        this.context = context;
        this.fileList = fileList;
        if (fileList != null) {
            filteredList.addAll(fileList);
        }
        mSelectedItemsIds = new SparseBooleanArray();
        this.category = category;
        this.mViewMode = viewMode;
        mAnimation = R.anim.fade_in_top;
        mIsThemeDark = ThemeUtils.isDarkTheme(context);
    }

    public void updateAdapter(ArrayList<FileInfo> fileInfos) {
        clear();
        if (fileInfos != null) {
            this.fileList = fileInfos;
            clearList();
            filteredList.addAll(fileList);
            offset = 0;
            mStopAnimation = !mIsAnimNeeded;
            notifyDataSetChanged();
            for (int i = 0; i < fileInfos.size(); i++) {
                mAnimatedPos.put(i, false);
            }
        }
    }

    void setList(ArrayList<FileInfo> fileList) {
        this.fileList = fileList;
    }

    private void clear() {
        fileList = new ArrayList<>();
    }


    public void setStopAnimation(boolean flag) {
        mIsAnimNeeded = !flag;
    }


    public void clearList() {
        if (filteredList != null && !filteredList.isEmpty()) {
            filteredList.clear();
        }
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }


    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    void setOnItemLongClickListener(final OnItemLongClickListener mItemClickListener) {
        this.mOnItemLongClickListener = mItemClickListener;
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder instanceof FileListViewHolder) {
            FileListViewHolder fileListViewHolder = (FileListViewHolder) holder;
            fileListViewHolder.container.clearAnimation();
        }
    }

    @Override
    public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
        if (holder instanceof FileListViewHolder) {
            FileListViewHolder fileListViewHolder = (FileListViewHolder) holder;
            fileListViewHolder.container.clearAnimation();
        }
        return super.onFailedToRecycleView(holder);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_FOOTER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_footer,
                                                                      parent, false);
            return new FooterViewHolder(v);
        }
        if (mViewMode == ViewMode.LIST) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_list_item,
                                                                    parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_grid_item,
                                                                    parent, false);
        }

        return new FileListViewHolder(view);


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (context == null) {
            return;
        }
        if (holder instanceof FileListViewHolder) {
            FileListViewHolder fileListViewHolder = (FileListViewHolder) holder;

            if (!mStopAnimation && !mAnimatedPos.get(position)) {
                animate(fileListViewHolder);
                mAnimatedPos.put(position, true);
            }

            int color;
            if (mIsThemeDark) {
                color = ContextCompat.getColor(context, R.color.dark_actionModeItemSelected);
            } else {
                color = ContextCompat.getColor(context, R.color.actionModeItemSelected);
            }

            if (mSelectedItemsIds.get(position)) {
                fileListViewHolder.itemView.setBackgroundColor(color);
            } else {
                if (position == draggedPos) {
                    fileListViewHolder.itemView.setBackgroundColor(color);
                } else {
                    fileListViewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
                }
            }

            setViewByCategory(fileListViewHolder, position);
        }

    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

    private void animate(FileListViewHolder fileListViewHolder) {
        fileListViewHolder.container.clearAnimation();
        Animation localAnimation = AnimationUtils.loadAnimation(context, mAnimation);
        localAnimation.setStartOffset(this.offset);
        fileListViewHolder.container.startAnimation(localAnimation);
        this.offset += 30;
    }

    @Override
    public int getItemCount() {
        if (fileList == null) {
            return 0;
        } else {
            return fileList.size();
        }
    }

    void setDraggedPos(int pos) {
        draggedPos = pos;
        notifyDataSetChanged();
    }

    void clearDragPos() {
        draggedPos = -1;
    }

    private void setViewByCategory(FileListViewHolder fileListViewHolder, int position) {
        FileInfo fileInfo = fileList.get(position);

        if (fileInfo.getCategory().equals(PICKER)) {
            fileListViewHolder.imageIcon.setImageResource(fileInfo.getIcon());
            fileListViewHolder.textFileName.setText(fileInfo.getFileName());
        } else {
            String fileName = fileInfo.getFileName();
            String fileDate;
            if (Category.checkIfFileCategory(category)) {
                fileDate = FileUtils.convertDate(fileInfo.getDate());
            } else {
                fileDate = FileUtils.convertDate(fileInfo.getDate() * 1000);
            }
            boolean isDirectory = fileInfo.isDirectory();
            String fileNoOrSize;
            if (isDirectory) {
                if (fileInfo.isRootMode()) {
                    fileNoOrSize = context.getString(R.string.directory);
                } else {
                    int childFileListSize = (int) fileInfo.getSize();
                    if (childFileListSize == 0) {
                        fileNoOrSize = context.getResources().getString(R.string.empty);
                    } else if (childFileListSize == -1) {
                        fileNoOrSize = "";
                    } else {
                        fileNoOrSize = context.getResources().getQuantityString(R.plurals
                                                                                        .number_of_files,
                                                                                childFileListSize, childFileListSize);
                    }
                }
            } else {
                long size = fileInfo.getSize();
                fileNoOrSize = Formatter.formatFileSize(context, size);
            }


            fileListViewHolder.textFileName.setText(fileName);
            if (mViewMode == ViewMode.LIST) {
                fileListViewHolder.textFileModifiedDate.setText(fileDate);
            }
            fileListViewHolder.textNoOfFileOrSize.setText(fileNoOrSize);

            displayThumb(context, fileInfo, fileInfo.getCategory(), fileListViewHolder.imageIcon,
                         fileListViewHolder.imageThumbIcon);
        }

    }


    void toggleSelection(int position, boolean isLongPress) {
        if (isLongPress) {
            selectView(position, true);
        } else {
            selectView(position, !mSelectedItemsIds.get(position));

        }
    }


    void toggleSelectAll(int position, boolean selectAll) {
        if (selectAll) {
            mSelectedItemsIds.put(position, true);
        } else {
            mSelectedItemsIds = new SparseBooleanArray();
        }
    }

    void clearSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
    }

    void removeSelection() {
        clearSelection();
        notifyDataSetChanged();
    }


    private void selectView(int position, boolean value) {
        if (value) {
            mSelectedItemsIds.put(position, true);
        } else {
            mSelectedItemsIds.delete(position);

        }

        notifyDataSetChanged();
    }

    int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    SparseBooleanArray getSelectedItemPositions() {
        return mSelectedItemsIds;
    }

    void filter(String text) {
        if (text.isEmpty()) {
            if (fileList != null) {
                fileList.clear();
                fileList.addAll(filteredList);
            }
        } else {
            ArrayList<FileInfo> result = new ArrayList<>();
            text = text.toLowerCase();
            for (FileInfo item : filteredList) {
                if (item.getFileName().toLowerCase().contains(text)) {
                    result.add(item);
                }
            }
            if (fileList != null) {
                fileList.clear();
                fileList.addAll(result);
            }
        }
        if (searchCallback != null) {
            searchCallback.updateList(fileList);
        }

        notifyDataSetChanged();
    }


    private class FileListViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener,
                       View.OnLongClickListener
    {
        final TextView textFileName;
        TextView textFileModifiedDate;
        final TextView       textNoOfFileOrSize;
        final ImageView      imageIcon;
        final ImageView      imageThumbIcon;
        final RelativeLayout container;


        FileListViewHolder(View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container_list);
            textFileName = itemView
                    .findViewById(R.id.textFolderName);
            imageIcon = itemView.findViewById(R.id.imageIcon);
            imageThumbIcon = itemView.findViewById(R.id.imageThumbIcon);
            textNoOfFileOrSize = itemView.findViewById(R.id.textSecondLine);
            if (mViewMode == ViewMode.LIST) {
                textFileModifiedDate = itemView.findViewById(R.id.textDate);
            }
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }


        @Override
        public void onClick(View v) {
            Logger.log("TAG", "" + mItemClickListener);

            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(getAdapterPosition());
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

    private SearchCallback searchCallback;

    void setSearchCallback(SearchCallback searchCallback) {
        this.searchCallback = searchCallback;
    }

    interface SearchCallback {
        void updateList(ArrayList<FileInfo> fileList);
    }

    private static class FooterViewHolder extends RecyclerView.ViewHolder {

        FooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    void onDetach() {
        context = null;
    }


}
