package com.siju.acexplorer.filesystem;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.model.FileInfo;
import com.siju.acexplorer.filesystem.utils.FileUtils;
import com.siju.acexplorer.filesystem.utils.ThemeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class FileListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<FileInfo> fileInfoArrayList = new ArrayList<>();
    private SparseBooleanArray mSelectedItemsIds;
    private SparseBooleanArray mAnimatedPos = new SparseBooleanArray();
    boolean mStopAnimation;

    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private int mCategory;
    private Uri mAudioUri = Uri.parse("content://media/external/audio/albumart");
    private int mViewMode;
    private ArrayList<FileInfo> fileInfoArrayListCopy = new ArrayList<>();
    private Fragment mFragment;
    private int draggedPos = -1;
    private int mAnimation;
    private int offset = 0;
    private Animation localAnimation;
    private boolean mIsAnimNeeded = true;
    private boolean mIsThemeDark;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;


    FileListAdapter(Fragment fragment, Context mContext, ArrayList<FileInfo>
            fileInfoArrayList, int category, int viewMode) {
        this.mFragment = fragment;
        this.mContext = mContext;
        this.fileInfoArrayList = fileInfoArrayList;
        mSelectedItemsIds = new SparseBooleanArray();
        mCategory = category;
        this.mViewMode = viewMode;
        mAnimation = R.anim.fade_in_top;
        mIsThemeDark = ThemeUtils.isDarkTheme(mContext);
    }

    public FileListAdapter(Context mContext, ArrayList<FileInfo>
            fileInfoArrayList, int category, int viewMode) {

        this.mContext = mContext;
        this.fileInfoArrayList = fileInfoArrayList;
        mSelectedItemsIds = new SparseBooleanArray();
        mCategory = category;
        this.mViewMode = viewMode;
        mAnimation = R.anim.fade_in_top;
        mIsThemeDark = ThemeUtils.isDarkTheme(mContext);
    }

    public void updateAdapter(ArrayList<FileInfo> fileInfos) {
        this.fileInfoArrayList = fileInfos;
        fileInfoArrayListCopy.addAll(fileInfos);
        offset = 0;
        mStopAnimation = !mIsAnimNeeded;
        notifyDataSetChanged();
        for (int i = 0; i < fileInfos.size(); i++) {
            mAnimatedPos.put(i, false);
        }
        Logger.log("SIJU", "updateAdapter--animated=" + mStopAnimation);
    }

    public void setStopAnimation(boolean flag) {
        mIsAnimNeeded = !flag;
    }


    public void clearList() {
        if (fileInfoArrayList != null && !fileInfoArrayList.isEmpty()) {
//            fileInfoArrayList.clear();
            fileInfoArrayListCopy.clear();
        }
    }

    public void setCategory(int category) {
        mCategory = category;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
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


  /*  @Override
    public void onViewDetachedFromWindow(FileListViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.container.clearAnimation();
    }*/

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

/*    @Override
    public boolean onFailedToRecycleView(FileListViewHolder holder) {
        holder.container.clearAnimation();
        return super.onFailedToRecycleView(holder);
    }*/

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_FOOTER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_footer, parent, false);
            return new FooterViewHolder(v);
        }
        if (mViewMode == FileConstants.KEY_LISTVIEW) {
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
        return position == fileInfoArrayList.size() ;
    }


    private void animate(FileListViewHolder fileListViewHolder) {
        fileListViewHolder.container.clearAnimation();
        localAnimation = AnimationUtils.loadAnimation(mContext, mAnimation);
        localAnimation.setStartOffset(this.offset);
        fileListViewHolder.container.startAnimation(localAnimation);
        this.offset += 30;
    }

  /*  private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }
*/
   /* public  void setApplicationTheme(boolean themeLight) {
        if (themeLight) {
           textFileName.setTextColor(ContextCompat.getColor(mContext,R.color.text_dark));
            textFileModifiedDate.setTextColor(ContextCompat.getColor(mContext,R.color.text_dark));
            textFileName.setTextColor(ContextCompat.getColor(mContext,R.color.text_dark));


        } else {
            setToolBarTheme(ContextCompat.getColor(this, R.color.color_dark_bg),
                    ContextCompat.getColor(this, R.color.color_dark_status_bar));
            mMainLayout.setBackgroundColor(ContextCompat.getColor(this,R.color.color_dark_bg));

        }

    }*/


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

        if (fileInfoArrayList.get(position).getType() == FileConstants.CATEGORY.PICKER.getValue()) {
            FileInfo fileInfo = fileInfoArrayList.get(position);
            fileListViewHolder.imageIcon.setImageResource(fileInfo.getIcon());
            fileListViewHolder.textFileName.setText(fileInfo.getFileName());
        }
        else {

            String fileName = fileInfoArrayList.get(position).getFileName();
            String fileDate;
            if (FileUtils.isDateNotInMs(mCategory)) {
                fileDate = FileUtils.convertDate(fileInfoArrayList.get(position).getDate());
            } else {
                fileDate = FileUtils.convertDate(fileInfoArrayList.get(position).getDate() * 1000);
            }
            boolean isDirectory = fileInfoArrayList.get(position).isDirectory();
            String fileNoOrSize = "";
            if (isDirectory) {
                int childFileListSize = (int) fileInfoArrayList.get(position).getSize();
                if (childFileListSize == 0) {
                    fileNoOrSize = mContext.getResources().getString(R.string.empty);
                } else if (childFileListSize == -1) {
                    fileNoOrSize = "";
                } else {
                    fileNoOrSize = mContext.getResources().getQuantityString(R.plurals.number_of_files,
                            childFileListSize, childFileListSize);
                }
            } else {
                long size = fileInfoArrayList.get(position).getSize();
                fileNoOrSize = Formatter.formatFileSize(mContext, size);
            }

            String filePath = fileInfoArrayList.get(position).getFilePath();


            fileListViewHolder.textFileName.setText(fileName);
            if (mViewMode == FileConstants.KEY_LISTVIEW) {
                fileListViewHolder.textFileModifiedDate.setText(fileDate);
            }
            fileListViewHolder.textNoOfFileOrSize.setText(fileNoOrSize);

            switch (mCategory) {

                case 0: // For file group
                case 5:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                    if (isDirectory) {
                        fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_folder_white);
                        Drawable apkIcon = FileUtils.getAppIconForFolder(mContext, fileName);
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

                        // If Image or Video file, load thumbnail
                        if (type == FileConstants.CATEGORY.IMAGE.getValue()) {
                            displayImageThumb(fileListViewHolder, filePath);
                        } else if (type == FileConstants.CATEGORY.VIDEO.getValue()) {
                            displayVideoThumb(fileListViewHolder, filePath);
                        } else if (type == FileConstants.CATEGORY.AUDIO.getValue()) {
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
                case 1:
                    Uri uri = ContentUris.withAppendedId(mAudioUri, fileInfoArrayList.get(position)
                            .getBucketId());
                    Glide.with(mContext).load(uri).centerCrop()
                            .placeholder(R.drawable.ic_music_default)
                            .crossFade(2)
                            .into(fileListViewHolder.imageIcon);
                    break;

                case 2:
                    displayVideoThumb(fileListViewHolder, filePath);
                    break;

                case 3: // For images group
                    displayImageThumb(fileListViewHolder, filePath);
                    break;
                case 4: // For docs group
                    String extension = fileInfoArrayList.get(position).getExtension();
                    extension = extension.toLowerCase();
                    changeFileIcon(fileListViewHolder, extension, null);
                    break;

            }
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
                Drawable apkIcon = FileUtils.getAppIcon(mContext, path);
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
//        Uri uri = ContentUris.withAppendedId(mAudioUri, fileInfoArrayList.get(position)
// .getBucketId());
//        Uri audioUri = Uri.fromFile(new File(path));

        fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_music_default);
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{MediaStore.Audio.Media.ALBUM_ID};
        String selection = MediaStore.Audio.Media.DATA + " = ?";
        String[] selectionArgs = new String[]{path};

        //        String sortOrder = MediaStore.Audio.Media.DATE_MODIFIED + " DESC";
        Cursor cursor = mContext.getContentResolver().query(uri, projection, selection, selectionArgs,
                null);


        if (cursor != null && cursor.moveToFirst()) {


            int albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
            long albumId = cursor.getLong(albumIdIndex);

//            Logger.log("Adapter","displayAudioAlbumArt="+albumId);
            Uri newUri = ContentUris.withAppendedId(mAudioUri, albumId);
            Glide.with(mContext).loadFromMediaStore(newUri).centerCrop()
                    .placeholder(R.drawable.ic_music_default)
//                    .crossFade(2)
                    .into(fileListViewHolder.imageIcon);

            cursor.close();
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


    public void setModel(List<FileInfo> models) {
        fileInfoArrayList = new ArrayList<>(models);
    }

    private FileInfo removeItem(int position) {
        final FileInfo model = fileInfoArrayList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    private void addItem(int position, FileInfo model) {
        fileInfoArrayList.add(position, model);
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {
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


    class FileListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {
        TextView textFileName;
        TextView textFileModifiedDate;
        TextView textNoOfFileOrSize;
        ImageView imageIcon;
        ImageView imageThumbIcon;
        RelativeLayout container;


        FileListViewHolder(View itemView) {
            super(itemView);
            container = (RelativeLayout) itemView.findViewById(R.id.container_list);
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
/*            FileListFragment.myDragEventListener dragEventListener = ((FileListFragment)
                    mFragment).new myDragEventListener();

            itemView.setOnDragListener(dragEventListener);*/
//            itemView.setOnTouchListener(this);

        }


        @Override
        public void onClick(View v) {
            Logger.log("TAG", "" + mItemClickListener);

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

    class FooterViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayoutFooter;

        public FooterViewHolder(View itemView) {
            super(itemView);
            this.linearLayoutFooter = (LinearLayout) itemView.findViewById(R.id.linearLayoutFooter);
        }
    }


}
