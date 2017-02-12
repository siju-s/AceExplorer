package com.siju.acexplorer.filesystem;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
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

import com.bumptech.glide.Glide;
import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.groups.Category;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.modes.ViewMode;
import com.siju.acexplorer.filesystem.theme.ThemeUtils;
import com.siju.acexplorer.filesystem.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;

import static com.siju.acexplorer.filesystem.app.AppUtils.getAppIcon;
import static com.siju.acexplorer.filesystem.app.AppUtils.getAppIconForFolder;
import static com.siju.acexplorer.filesystem.groups.Category.AUDIO;
import static com.siju.acexplorer.filesystem.groups.Category.IMAGE;
import static com.siju.acexplorer.filesystem.groups.Category.PICKER;
import static com.siju.acexplorer.filesystem.groups.Category.VIDEO;


public class FileListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<FileInfo> fileInfoArrayList = new ArrayList<>();
    private SparseBooleanArray mSelectedItemsIds;
    private final SparseBooleanArray mAnimatedPos = new SparseBooleanArray();
    boolean mStopAnimation;
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private Category category;
    private final Uri mAudioUri = Uri.parse("content://media/external/audio/albumart");
    private final int mViewMode;
    private final ArrayList<FileInfo> fileInfoArrayListCopy = new ArrayList<>();
    private int draggedPos = -1;
    private final int mAnimation;
    private int offset = 0;
    private boolean mIsAnimNeeded = true;
    private final boolean mIsThemeDark;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;


    public FileListAdapter(Context mContext, ArrayList<FileInfo>
            fileInfoArrayList, Category category, int viewMode) {

        this.mContext = mContext;
        this.fileInfoArrayList = fileInfoArrayList;
        mSelectedItemsIds = new SparseBooleanArray();
        this.category = category;
        this.mViewMode = viewMode;
        mAnimation = R.anim.fade_in_top;
        mIsThemeDark = ThemeUtils.isDarkTheme(mContext);
    }

    public void updateAdapter(ArrayList<FileInfo> fileInfos) {
        clear();
        if (fileInfos != null) {
            this.fileInfoArrayList = fileInfos;
            fileInfoArrayListCopy.addAll(fileInfos);
            offset = 0;
            mStopAnimation = !mIsAnimNeeded;
            notifyDataSetChanged();
            for (int i = 0; i < fileInfos.size(); i++) {
                mAnimatedPos.put(i, false);
            }
        }
        Logger.log("SIJU", "updateAdapter--animated=" + mStopAnimation);
    }

    void updateSearchResult(FileInfo fileInfo) {
        Logger.log("Adapter", "Count=" + getItemCount());
        fileInfoArrayList.add(fileInfo);
        notifyDataSetChanged();
//        notifyItemChanged(getItemCount());
    }

    void clear() {
        fileInfoArrayList = new ArrayList<>();
    }


    public void setStopAnimation(boolean flag) {
        mIsAnimNeeded = !flag;
    }


    public void clearList() {
        if (fileInfoArrayList != null && !fileInfoArrayList.isEmpty()) {
            fileInfoArrayListCopy.clear();
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_footer, parent, false);
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

        if (mContext == null) return;

        if (holder instanceof FileListViewHolder) {
            FileListViewHolder fileListViewHolder = (FileListViewHolder) holder;

            if (!mStopAnimation && !mAnimatedPos.get(position)) {
                animate(fileListViewHolder);
                mAnimatedPos.put(position, true);
            }

            int color;
//        Log.d("TAG","OnBindviewholder mIsThemeDark="+mIsThemeDark);
            if (mIsThemeDark) {
                color = ContextCompat.getColor(mContext, R.color.dark_actionModeItemSelected);
            } else {
                color = ContextCompat.getColor(mContext, R.color.actionModeItemSelected);
            }

            fileListViewHolder.itemView.setBackgroundColor(mSelectedItemsIds.get(position) ? color :
                    Color.TRANSPARENT);

            if (!mSelectedItemsIds.get(position)) {
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
        if (isPositionFooter(position)) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    private boolean isPositionFooter(int position) {
        return position == fileInfoArrayList.size();
    }


    private void animate(FileListViewHolder fileListViewHolder) {
        fileListViewHolder.container.clearAnimation();
        Animation localAnimation = AnimationUtils.loadAnimation(mContext, mAnimation);
        localAnimation.setStartOffset(this.offset);
        fileListViewHolder.container.startAnimation(localAnimation);
        this.offset += 30;
    }

    @Override
    public int getItemCount() {
        if (fileInfoArrayList == null) {
            return 0;
        } else {
            return fileInfoArrayList.size() + 1;
        }
    }

    void setDraggedPos(int pos) {
        draggedPos = pos;
        notifyDataSetChanged();
    }

    void clearDragPos() {
        draggedPos = -1;
        notifyDataSetChanged();
    }

    private void setViewByCategory(FileListViewHolder fileListViewHolder, int position) {

        if (fileInfoArrayList.get(position).getType() == PICKER.getValue()) {
            FileInfo fileInfo = fileInfoArrayList.get(position);
            fileListViewHolder.imageIcon.setImageResource(fileInfo.getIcon());
            fileListViewHolder.textFileName.setText(fileInfo.getFileName());
        } else {

            String fileName = fileInfoArrayList.get(position).getFileName();
            String fileDate;
            if (Category.checkIfFileCategory(category)) {
                fileDate = FileUtils.convertDate(fileInfoArrayList.get(position).getDate());
            } else {
                fileDate = FileUtils.convertDate(fileInfoArrayList.get(position).getDate() * 1000);
            }
            boolean isDirectory = fileInfoArrayList.get(position).isDirectory();
            String fileNoOrSize;
            if (isDirectory) {
                if (fileInfoArrayList.get(position).isRootMode()) {
                    fileNoOrSize = mContext.getString(R.string.directory);
                } else {
                    int childFileListSize = (int) fileInfoArrayList.get(position).getSize();
                    if (childFileListSize == 0) {
                        fileNoOrSize = mContext.getResources().getString(R.string.empty);
                    } else if (childFileListSize == -1) {
                        fileNoOrSize = "";
                    } else {
                        fileNoOrSize = mContext.getResources().getQuantityString(R.plurals.number_of_files,
                                childFileListSize, childFileListSize);
                    }
                }
            } else {
                long size = fileInfoArrayList.get(position).getSize();
                fileNoOrSize = Formatter.formatFileSize(mContext, size);
            }


            fileListViewHolder.textFileName.setText(fileName);
            if (mViewMode == ViewMode.LIST) {
                fileListViewHolder.textFileModifiedDate.setText(fileDate);
            }
            fileListViewHolder.textNoOfFileOrSize.setText(fileNoOrSize);

            displayThumb(fileListViewHolder, fileInfoArrayList.get(position).getCategory(), position);
        }

    }

    private void displayThumb(FileListViewHolder fileListViewHolder, Category category, int position) {

        String filePath = fileInfoArrayList.get(position).getFilePath();
        String fileName = fileInfoArrayList.get(position).getFileName();
        boolean isDirectory = fileInfoArrayList.get(position).isDirectory();

        switch (category) {

            case FILES:
            case DOWNLOADS:
            case COMPRESSED:
            case FAVORITES:
            case PDF:
            case APPS:
            case LARGE_FILES:
            case ZIP_VIEWER:
                if (isDirectory) {
                    fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_folder_white);
                    Drawable apkIcon = getAppIconForFolder(mContext, fileName);
                    if (apkIcon != null) {
                        fileListViewHolder.imageThumbIcon.setVisibility(View.VISIBLE);
                        fileListViewHolder.imageThumbIcon.setImageDrawable(apkIcon);
                    } else {
                        fileListViewHolder.imageThumbIcon.setVisibility(View.GONE);
                        fileListViewHolder.imageThumbIcon.setImageDrawable(null);
                    }

                } else {
                    fileListViewHolder.imageThumbIcon.setVisibility(View.GONE);
                    fileListViewHolder.imageThumbIcon.setImageDrawable(null);

                    int type = fileInfoArrayList.get(position).getType();
                    fileListViewHolder.imageIcon.setImageDrawable(null);
//                        Logger.log("TAG", "Adpater path=" + filePath + "position=" + position);
//                        if (updateItems) {
                    // If Image or Video file, load thumbnail
                    if (type == IMAGE.getValue()) {
                        displayImageThumb(fileListViewHolder, filePath);
                    } else if (type == VIDEO.getValue()) {
                        displayVideoThumb(fileListViewHolder, filePath);
                    } else if (type == AUDIO.getValue()) {
                        displayAudioAlbumArt(fileListViewHolder, fileInfoArrayList.get(position)
                                .getFilePath());
                    } else {
                        String extension = fileInfoArrayList.get(position).getExtension();
                        if (extension != null) {
                            changeFileIcon(fileListViewHolder, extension.toLowerCase(), filePath);
                        } else {
                            fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_doc_white);
                        }
                    }


                }
                if (fileName.startsWith(".")) {
                    fileListViewHolder.imageIcon.setColorFilter(Color.argb(200, 255, 255, 255));
                } else {
                    fileListViewHolder.imageIcon.clearColorFilter();
                }
                break;
            case AUDIO:
                Uri uri = ContentUris.withAppendedId(mAudioUri, fileInfoArrayList.get(position)
                        .getBucketId());
                Glide.with(mContext).load(uri).centerCrop()
                        .placeholder(R.drawable.ic_music_default)
                        .crossFade(2)
                        .into(fileListViewHolder.imageIcon);
                break;

            case VIDEO:
                displayVideoThumb(fileListViewHolder, filePath);
                break;

            case IMAGE: // For images group
                displayImageThumb(fileListViewHolder, filePath);
                break;
            case DOCS: // For docs group
                String extension = fileInfoArrayList.get(position).getExtension();
                extension = extension.toLowerCase();
                changeFileIcon(fileListViewHolder, extension, null);
                break;

        }
    }


    private void displayVideoThumb(FileListViewHolder fileListViewHolder, String path) {
        // For videos group
        Uri videoUri = Uri.fromFile(new File(path));
        Glide.with(mContext).load(videoUri).centerCrop()
                .placeholder(R.drawable.ic_movie)
                .crossFade(2)
                .into(fileListViewHolder.imageIcon);
    }

    private void displayImageThumb(FileListViewHolder fileListViewHolder, String path) {
        Uri imageUri = Uri.fromFile(new File(path));
        Glide.with(mContext).load(imageUri).centerCrop()
                .crossFade(2)
                .placeholder(R.drawable.ic_image_default)
                .into(fileListViewHolder.imageIcon);
    }

    private void changeFileIcon(FileListViewHolder fileListViewHolder, String extension, String
            path) {
        switch (extension) {
            case FileConstants.APK_EXTENSION:
                Drawable apkIcon = getAppIcon(mContext, path);
                fileListViewHolder.imageIcon.setImageDrawable(apkIcon);
                break;
            case FileConstants.EXT_DOC:
            case FileConstants.EXT_DOCX:
                fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_doc);
                break;
            case FileConstants.EXT_XLS:
            case FileConstants.EXT_XLXS:
            case FileConstants.EXT_CSV:
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
                break;
            default:
                fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_doc_white);
                break;
        }
    }

    private void displayAudioAlbumArt(FileListViewHolder fileListViewHolder, String path) {
        fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_music_default);
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{MediaStore.Audio.Media.ALBUM_ID};
        String selection = MediaStore.Audio.Media.DATA + " = ?";
        String[] selectionArgs = new String[]{path};

        Cursor cursor = mContext.getContentResolver().query(uri, projection, selection, selectionArgs,
                null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                long albumId = cursor.getLong(albumIdIndex);

//                Logger.log("Adapter", "displayAudioAlbumArt=" + albumId);
                Uri newUri = ContentUris.withAppendedId(mAudioUri, albumId);
                Glide.with(mContext).load(newUri).centerCrop()
                        .placeholder(R.drawable.ic_music_default)
                        .into(fileListViewHolder.imageIcon);
            }
            cursor.close();
        } else {
            fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_music_default);
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
        if (selectAll)
            mSelectedItemsIds.put(position, true);
        else
            mSelectedItemsIds = new SparseBooleanArray();
    }

    void clearSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
    }

    void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
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
            if (fileInfoArrayList != null) {
                fileInfoArrayList.clear();
                fileInfoArrayList.addAll(fileInfoArrayListCopy);
            }
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


    private class FileListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {
        final TextView textFileName;
        TextView textFileModifiedDate;
        final TextView textNoOfFileOrSize;
        final ImageView imageIcon;
        final ImageView imageThumbIcon;
        final RelativeLayout container;


        FileListViewHolder(View itemView) {
            super(itemView);
            container = (RelativeLayout) itemView.findViewById(R.id.container_list);
            textFileName = (TextView) itemView
                    .findViewById(R.id.textFolderName);
            imageIcon = (ImageView) itemView.findViewById(R.id.imageIcon);
            imageThumbIcon = (ImageView) itemView.findViewById(R.id.imageThumbIcon);
            textNoOfFileOrSize = (TextView) itemView.findViewById(R.id.textSecondLine);
            if (mViewMode == ViewMode.LIST) {
                textFileModifiedDate = (TextView) itemView.findViewById(R.id.textDate);
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

    private class FooterViewHolder extends RecyclerView.ViewHolder {

        FooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    void onDetach() {
        mContext = null;
    }


}
