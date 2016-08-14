package com.siju.filemanager.filesystem;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.siju.filemanager.R;
import com.siju.filemanager.common.Logger;
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
    private SparseBooleanArray mDraggedItemsIds;

    private ArrayList<FileInfo> mSelectedFileList;
    OnItemClickListener mItemClickListener;
    OnItemLongClickListener mOnItemLongClickListener;
    OnItemTouchListener mOnItemTouchListener;

    private int mCategory;
    private Uri mAudioUri = Uri.parse("content://media/external/audio/albumart");
    private Uri mImageUri = Uri.parse("content://media/external/images/albumart");
    private int mViewMode;
    private ArrayList<FileInfo> fileInfoArrayListCopy = new ArrayList<>();
    private Fragment mFragment;
    private int draggedPos = -1;



     FileListAdapter(Fragment fragment, Context mContext, ArrayList<FileInfo>
            fileInfoArrayList, int
                                   category, int viewMode) {
        this.mFragment = fragment;
        this.mContext = mContext;
        this.fileInfoArrayList = fileInfoArrayList;
        mSelectedItemsIds = new SparseBooleanArray();
        mDraggedItemsIds = new SparseBooleanArray();
        mSelectedFileList = new ArrayList<>();
        mCategory = category;
        this.mViewMode = viewMode;
    }

    public FileListAdapter(Context mContext, ArrayList<FileInfo>
            fileInfoArrayList, int
                                   category, int viewMode) {

        this.mContext = mContext;
        this.fileInfoArrayList = fileInfoArrayList;
        mSelectedItemsIds = new SparseBooleanArray();
        mDraggedItemsIds = new SparseBooleanArray();
        mSelectedFileList = new ArrayList<>();
        mCategory = category;
        this.mViewMode = viewMode;
    }

    public void updateAdapter(ArrayList<FileInfo> fileInfos) {
        this.fileInfoArrayList = fileInfos;
        fileInfoArrayListCopy.addAll(fileInfos);
//        Log.d("SIJU","updateAdapter"+fileInfoArrayList.size());
        Logger.log(this.getClass().getSimpleName(),"adapter size="+fileInfos.size());
        notifyDataSetChanged();
    }


    public void clearList() {
        if (!fileInfoArrayList.isEmpty()) {
            fileInfoArrayList.clear();
            fileInfoArrayListCopy.clear();
        }
    }

    public void setCategory(int category) {
        mCategory = category;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    public interface OnItemTouchListener {
        boolean onItemTouch(View view, int position, MotionEvent event);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void setOnItemLongClickListener(final OnItemLongClickListener mItemClickListener) {
        this.mOnItemLongClickListener = mItemClickListener;
    }

    public void setOnItemTouchListener(final OnItemTouchListener mItemTouchListener) {
        this.mOnItemTouchListener = mItemTouchListener;
    }

    @Override
    public FileListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (mViewMode == FileConstants.KEY_LISTVIEW) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_list_item,
                    parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_grid_item,
                    parent, false);
        }
        FileListViewHolder tvh = new FileListViewHolder(view);
        return tvh;
    }

    @Override
    public void onBindViewHolder(FileListViewHolder fileListViewHolder, int position) {
        //change background color if list item is selected
//        Log.d("TAG","OnBindviewholder Pos="+position);

        int color = ContextCompat.getColor(mContext, R.color.actionModeItemSelected);

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
            return fileInfoArrayList.size();
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
            case 5:
            case 7:
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
                    int type = fileInfoArrayList.get(position).getType();
                    fileListViewHolder.imageIcon.setImageDrawable(null);
                    // If Image or Video file, load thumbnail
                    if (type == FileConstants.CATEGORY.IMAGE.getValue() ||
                            type == FileConstants.CATEGORY.VIDEO.getValue()) {

                        Uri imageUri = Uri.fromFile(new File(filePath));
                        Glide.with(mContext).load(imageUri).centerCrop()
                                .crossFade(2)
                                .into(fileListViewHolder.imageIcon);

                    } else if (type == FileConstants.CATEGORY.AUDIO.getValue()) {
                        displayAudioAlbumArt(fileListViewHolder, fileInfoArrayList.get(position)
                                .getFilePath());
                    } else {

                        String extension = fileInfoArrayList.get(position).getExtension();
                        if (extension != null) {
                            changeFileIcon(fileListViewHolder,extension.toLowerCase(),filePath);
                        }
                        else {
                            fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_doc_white);
                        }

                    }

                }
                if (fileName.startsWith(".")) {
                    fileListViewHolder.imageIcon.setColorFilter(Color.argb(200, 255, 255, 255));
                }
                else {
                    fileListViewHolder.imageIcon.clearColorFilter();

                }
                break;
            case 1:
                Uri uri = ContentUris.withAppendedId(mAudioUri, fileInfoArrayList.get(position)
                        .getBucketId());
                Glide.with(mContext).load(uri).centerCrop()
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
                extension = extension.toLowerCase();
                changeFileIcon(fileListViewHolder, extension,null);
                break;

        }

    }

    private void changeFileIcon(FileListViewHolder fileListViewHolder, String extension,String
            path) {
        switch (extension) {
            case FileConstants.APK_EXTENSION:
                Drawable apkIcon = FileUtils.getAppIcon(mContext,path);
                fileListViewHolder.imageIcon.setImageDrawable(apkIcon);
                break;
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
                break;
            default:
                fileListViewHolder.imageIcon.setImageResource(R.drawable.ic_doc_white);
                break;
        }
    }

    private void displayAudioAlbumArt(FileListViewHolder fileListViewHolder, String path) {
//        Uri uri = ContentUris.withAppendedId(mAudioUri, fileInfoArrayList.get(position)
// .getBucketId());
        Uri audioUri = Uri.fromFile(new File(path));

        MediaMetadataRetriever myRetriever = new MediaMetadataRetriever();
        myRetriever.setDataSource(mContext, audioUri);

        byte[] artwork;


        artwork = myRetriever.getEmbeddedPicture();
        Glide.with(mContext).load(artwork).centerCrop()
                .placeholder(R.drawable.ic_music)
                .crossFade(2)
                .into(fileListViewHolder.imageIcon);




       /* if (artwork != null) {
            Bitmap bMap = BitmapFactory.decodeByteArray(artwork, 0, artwork.length);
            fileListViewHolder.imageIcon.setImageBitmap(bMap);
        } else {
            fileListViewHolder.imageIcon.setImageBitmap(null);*/
//        }



       /* String projection[] = { MediaStore.Audio.Media.ALBUM_ID };
        String selection = MediaStore.Audio.Media.DATA + " = ? ";
        String selectionArgs[] = new String[]{path};

        Cursor cursor = mContext.getContentResolver().query(mAudioUri, projection,
                selection,
                selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                long albumId = cursor.getLong(albumIdIndex);
                Uri uri = ContentUris.withAppendedId(mAudioUri, albumId);

                Glide.with(mContext).loadFromMediaStore(uri)
                        .centerCrop()
                        .placeholder(R.drawable.ic_music)
                        .crossFade(2)
                        .into(fileListViewHolder.imageIcon);

            } while (cursor.moveToNext());
            cursor.close();
        }*/
    }

    public void toggleSelection(int position, boolean isLongPress) {
        if (isLongPress) {
            selectView(position, true);
        } else {
            selectView(position, !mSelectedItemsIds.get(position));

        }
    }


    public void toggleDragSelection(int position) {
        selectDragView(position, !mDraggedItemsIds.get(position));
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

    public void removeDragSelection() {
        mDraggedItemsIds = new SparseBooleanArray();
//        mSelectedFileList = new ArrayList<>();
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

    public void selectDragView(int position, boolean value) {
        if (value) {
            mDraggedItemsIds.put(position, true);
//            mSelectedFileList.add(fileInfoArrayList.get(position));
        } else {
            mDraggedItemsIds.delete(position);

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
        TextView textFileName;
        TextView textFileModifiedDate;
        TextView textNoOfFileOrSize;
        ImageView imageIcon;
        ImageView imageThumbIcon;


        FileListViewHolder(View itemView) {
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
/*            FileListFragment.myDragEventListener dragEventListener = ((FileListFragment)
                    mFragment).new myDragEventListener();

            itemView.setOnDragListener(dragEventListener);*/
//            itemView.setOnTouchListener(this);

        }

        @Override
        public void onClick(View v) {
            Logger.log("TAG",""+ mItemClickListener);

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

  /*      @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (mOnItemTouchListener != null) {
                mOnItemTouchListener.onItemTouch(view, getAdapterPosition(),motionEvent);
            }
            return true;
        }*/
    }


}
