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
import android.util.Log;
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
import com.siju.acexplorer.analytics.Analytics;
import com.siju.acexplorer.common.types.FileInfo;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.main.model.groups.Category;
import com.siju.acexplorer.main.model.groups.CategoryHelper;
import com.siju.acexplorer.main.model.helper.FileUtils;
import com.siju.acexplorer.storage.model.ViewMode;
import com.siju.acexplorer.theme.ThemeUtils;
import com.siju.acexplorer.ui.autoplay.AutoPlayContainer;
import com.siju.acexplorer.ui.autoplay.AutoPlayView;
import com.siju.acexplorer.ui.peekandpop.PeekAndPop;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static android.view.View.GONE;
import static com.siju.acexplorer.main.model.groups.Category.APP_MANAGER;
import static com.siju.acexplorer.main.model.groups.Category.AUDIO;
import static com.siju.acexplorer.main.model.groups.Category.FOLDER_VIDEOS;
import static com.siju.acexplorer.main.model.groups.Category.PICKER;
import static com.siju.acexplorer.main.model.groups.Category.VIDEO;
import static com.siju.acexplorer.main.model.groups.CategoryHelper.isGenericImagesCategory;
import static com.siju.acexplorer.main.model.groups.CategoryHelper.isGenericMusic;
import static com.siju.acexplorer.main.model.groups.CategoryHelper.isGenericVideosCategory;
import static com.siju.acexplorer.main.model.groups.CategoryHelper.isMusicCategory;
import static com.siju.acexplorer.main.model.groups.CategoryHelper.isPeekPopCategory;
import static com.siju.acexplorer.main.model.groups.CategoryHelper.isRecentCategory;
import static com.siju.acexplorer.utils.ThumbnailUtils.displayThumb;


public class FileListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private static final int INVALID_POS = -1;
    private static final String TAG = "FileListAdapter";
    private final SparseBooleanArray mAnimatedPos = new SparseBooleanArray();
    private final int mAnimation;
    private final boolean isThemeDark;
    boolean mStopAnimation;
    private Context context;
    private ArrayList<FileInfo> fileList = new ArrayList<>();
    private ArrayList<FileInfo> filteredList = new ArrayList<>();
    private SparseBooleanArray mSelectedItemsIds;
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private Category category;
    private int mViewMode;
    private int draggedPos = -1;
    private int offset = 0;
    private boolean mIsAnimNeeded = true;
    private PeekAndPop peekAndPop;
    private int peekPos = INVALID_POS;
    private SearchCallback searchCallback;

    public FileListAdapter(Context context, ArrayList<FileInfo>
            fileList, Category category, int viewMode, PeekAndPop peekAndPop) {

        this.context = context;
        this.fileList = fileList;
        this.peekAndPop = peekAndPop;
        if (fileList != null) {
            filteredList.addAll(fileList);
        }
        mSelectedItemsIds = new SparseBooleanArray();
        this.category = category;
        this.mViewMode = viewMode;
        Logger.log(TAG, "FileListAdapter: " + viewMode);
        mAnimation = R.anim.fade_in_top;
        isThemeDark = ThemeUtils.isDarkTheme(context);
        setPeekPopListener();
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

    @Override
    public void onViewDetachedFromWindow(@NotNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder instanceof FileListViewHolder) {
            FileListViewHolder fileListViewHolder = (FileListViewHolder) holder;
            fileListViewHolder.container.clearAnimation();
        }
    }

    @Override
    public boolean onFailedToRecycleView(@NotNull RecyclerView.ViewHolder holder) {
        if (holder instanceof FileListViewHolder) {
            FileListViewHolder fileListViewHolder = (FileListViewHolder) holder;
            fileListViewHolder.container.clearAnimation();
        }
        return super.onFailedToRecycleView(holder);
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view;
//        if (viewType == TYPE_FOOTER) {
//            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_footer,
//                                                                      parent, false);
//            return new FooterViewHolder(v);
//        }
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
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {

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
            if (isThemeDark) {
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

    public void setViewMode(int viewMode) {
        Log.d(TAG, "setViewMode() called with: viewMode = [" + viewMode + "]");
        mViewMode = viewMode;
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    void setOnItemLongClickListener(final OnItemLongClickListener mItemClickListener) {
        this.mOnItemLongClickListener = mItemClickListener;
    }

    private void setPeekPopListener() {
        if (peekAndPop == null) {
            return;
        }

        peekAndPop.setOnGeneralActionListener(new PeekAndPop.OnGeneralActionListener() {
            @Override
            public void onPeek(View longClickView, int position) {
                loadPeekView(position, true);
            }

            @Override
            public void onPop(View longClickView, int position) {
                peekPos = INVALID_POS;
                stopAutoPlayVid();
            }
        });

        peekAndPop.setOnClickListener(new PeekAndPop.OnClickListener() {
            @Override
            public void onClick(View view, int position, boolean canShowPeek) {
                if (!canShowPeek) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(view, position);
                    }
                    return;
                }
                if (peekPos == INVALID_POS) {
                    return;
                }
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(view, peekPos);
                }
            }

            @Override
            public boolean canShowPeek() {
                return mItemClickListener.canShowPeek();
            }
        });
    }

    void loadPeekView(int position, boolean firstRun) {
        if (position >= fileList.size() || position == INVALID_POS) {
            return;
        }
        Category category = fileList.get(position).getCategory();

        this.peekPos = position;
        View view = peekAndPop.getPeekView();
        changePeekButtonsState(position, view);
        ImageView thumb = view.findViewById(R.id.imagePeekView);
        AutoPlayContainer autoPlayView = view.findViewById(R.id.autoPlayView);
        if (firstRun) {
            Analytics.getLogger().enterPeekMode();
            autoPlayView.init();
        }
        final AutoPlayView customVideoView = autoPlayView.getCustomVideoView();
        final ImageView volume = view.findViewById(R.id.imageVolume);
        final TextView fileNameText = view.findViewById(R.id.textFileName);
        FileInfo fileInfo = fileList.get(position);
        if (VIDEO.equals(category) || FOLDER_VIDEOS.equals(category) || AUDIO.equals(category)) {
            fileNameText.setVisibility(View.VISIBLE);
            fileNameText.setText(fileInfo.getFileName());
            thumb.setVisibility(GONE);
            autoPlayView.setVisibility(View.VISIBLE);
            volume.setVisibility(View.VISIBLE);
            volume.setImageResource(firstRun ? R.drawable.ic_volume_off : customVideoView.isMuted() ? R.drawable.ic_volume_off : R.drawable.ic_volume_on);
            customVideoView.setLooping(true);
            customVideoView.setSource(fileInfo.getFilePath());
            customVideoView.setListener(peekPopVideoCallback);
            customVideoView.setVideoMode(!AUDIO.equals(category));
            if (firstRun) {
                customVideoView.init();
            }
            else {
                customVideoView.stopPlayer();
                customVideoView.playNext();
            }


        } else {
            fileNameText.setVisibility(GONE);
            autoPlayView.setVisibility(GONE);
            volume.setVisibility(GONE);
            thumb.setVisibility(View.VISIBLE);
        }

        volume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customVideoView.isMuted()) {
                    customVideoView.unmutePlayer();
                    volume.setImageResource(R.drawable.ic_volume_on);
                    volume.setContentDescription(context.getString(R.string.unmute));
                } else {
                    customVideoView.mutePlayer();
                    volume.setImageResource(R.drawable.ic_volume_off);
                    volume.setContentDescription(context.getString(R.string.mute));
                }
            }
        });
        displayThumb(context, fileInfo, fileInfo.getCategory(), thumb, null);
    }

    private void disablePeekButton(View view) {
        view.setAlpha(0.3f);
        view.setEnabled(false);
    }

    private void enablePeekButton(View view) {
        view.setAlpha(1f);
        view.setEnabled(true);
    }

    private void changePeekButtonsState(int position, View view) {
        View prevButton = view.findViewById(R.id.buttonPrev);
        if (position == 0) {
            disablePeekButton(prevButton);
        } else {
            enablePeekButton(prevButton);
        }
        View nextButton = view.findViewById(R.id.buttonNext);
        if (position == fileList.size() - 1) {
            disablePeekButton(nextButton);
        } else {
            enablePeekButton(nextButton);
        }
    }

    private AutoPlayView.PeekPopVideoCallback peekPopVideoCallback = new AutoPlayView.PeekPopVideoCallback() {
        @Override
        public void onSurfaceDestroyed() {
            peekAndPop.resetViews();
        }
    };



    void stopAutoPlayVid() {
        View view = peekAndPop.getPeekView();
        peekAndPop.resetViews();
        AutoPlayContainer autoPlayView = view.findViewById(R.id.autoPlayView);
        autoPlayView.getCustomVideoView().clearAll();
        autoPlayView.cleanup();
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

        Category category = fileInfo.getCategory();
        if (category.equals(PICKER)) {
            fileListViewHolder.imageIcon.setImageResource(fileInfo.getIcon());
            fileListViewHolder.textFileName.setText(fileInfo.getFileName());
        } else if (isGenericMusic(category)) {
            setViewGenericMusic(fileListViewHolder, fileInfo);
        } else if (isMusicCategory(category)) {
            setViewMusicCategory(fileListViewHolder, fileInfo);
        } else if (isGenericImagesCategory(category) || isGenericVideosCategory(category)) {
            setViewGenericImagesVidsCategory(fileListViewHolder, fileInfo);
        } else if (category.equals(APP_MANAGER)) {
            setAppManagerCategory(fileListViewHolder, fileInfo);
        } else if (isRecentCategory(category)) {
            setViewGenericRecent(fileListViewHolder, fileInfo);
        } else {
            if (peekAndPop != null && isPeekPopCategory(category)) {
                peekAndPop.addClickView(fileListViewHolder.imageIcon, position);
            }
            String fileName = fileInfo.getFileName();
            long dateMs = CategoryHelper.isDateInMs(this.category) ? fileInfo.getDate() : fileInfo.getDate() * 1000;
            String fileDate = FileUtils.convertDate(dateMs);
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

            displayThumb(context, fileInfo, category, fileListViewHolder.imageIcon,
                    fileListViewHolder.imageThumbIcon);
        }

    }

    private void setViewGenericMusic(FileListViewHolder fileListViewHolder, FileInfo fileInfo) {
        int count = fileInfo.getCount();
        String files = context.getResources().getQuantityString(R.plurals.number_of_files,
                count, count);
        fileListViewHolder.textFileName.setText(CategoryHelper.getCategoryName(context, fileInfo.getSubcategory()));
        fileListViewHolder.textNoOfFileOrSize.setText(files);
        fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_folder);
    }

    private void setViewMusicCategory(FileListViewHolder fileListViewHolder, FileInfo fileInfo) {

        fileListViewHolder.textFileName.setText(fileInfo.getTitle());
        int num = (int) fileInfo.getNumTracks();
        if (num != INVALID_POS) {
            String files = context.getResources().getQuantityString(R.plurals.number_of_files,
                    num, num);
            fileListViewHolder.textNoOfFileOrSize.setText(files);
        }
        displayThumb(context, fileInfo, category, fileListViewHolder.imageIcon,
                fileListViewHolder.imageThumbIcon);
    }

    private void setViewGenericImagesVidsCategory(FileListViewHolder fileListViewHolder, FileInfo fileInfo) {

        fileListViewHolder.textFileName.setText(fileInfo.getFileName());
        int num = (int) fileInfo.getNumTracks();
        if (num != INVALID_POS) {
            String files = context.getResources().getQuantityString(R.plurals.number_of_files,
                    num, num);
            fileListViewHolder.textNoOfFileOrSize.setText(files);
        }
        displayThumb(context, fileInfo, category, fileListViewHolder.imageIcon,
                fileListViewHolder.imageThumbIcon);
    }

    private void setViewGenericRecent(FileListViewHolder fileListViewHolder, FileInfo fileInfo) {
        int count = fileInfo.getCount();
        String files = context.getResources().getQuantityString(R.plurals.number_of_files,
                count, count);
        fileListViewHolder.textFileName.setText(CategoryHelper.getCategoryName(context, fileInfo.getSubcategory()));
        fileListViewHolder.textNoOfFileOrSize.setText(files);
        fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_folder);
    }

    private void setAppManagerCategory(FileListViewHolder fileListViewHolder, FileInfo fileInfo) {

        fileListViewHolder.textFileName.setText(fileInfo.getFileName());
        long size = fileInfo.getSize();
        String fileSize = Formatter.formatFileSize(context, size);
        fileListViewHolder.textNoOfFileOrSize.setText(fileSize);
        String fileDate = FileUtils.convertDate(fileInfo.getDate());
        if (mViewMode == ViewMode.LIST) {
            fileListViewHolder.textFileModifiedDate.setText(fileDate);
        }
        displayThumb(context, fileInfo, category, fileListViewHolder.imageIcon,
                fileListViewHolder.imageThumbIcon);
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
//        notifyItemChanged(position);
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

    void setSearchCallback(SearchCallback searchCallback) {
        this.searchCallback = searchCallback;
    }

    void onDetach() {
        context = null;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        boolean canShowPeek();
    }

    interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    interface SearchCallback {
        void updateList(ArrayList<FileInfo> fileList);
    }

    private class FileListViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener,
            View.OnLongClickListener {
        final TextView textFileName;
        final TextView textNoOfFileOrSize;
        final ImageView imageIcon;
        final ImageView imageThumbIcon;
        final RelativeLayout container;
        TextView textFileModifiedDate;


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
            imageIcon.setOnLongClickListener(this);
            imageIcon.setOnClickListener(this);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }


        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            Logger.log(TAG, "" + mItemClickListener + " pos:" + pos);

            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, pos);
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
