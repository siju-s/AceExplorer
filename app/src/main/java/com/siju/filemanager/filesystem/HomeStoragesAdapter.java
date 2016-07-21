package com.siju.filemanager.filesystem;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.siju.filemanager.R;
import com.siju.filemanager.filesystem.model.FileInfo;
import com.siju.filemanager.filesystem.model.HomeLibraryInfo;
import com.siju.filemanager.filesystem.model.HomeStoragesInfo;

import java.util.ArrayList;

/**
 * Created by Siju on 13-06-2016.
 */

public class HomeStoragesAdapter extends RecyclerView.Adapter<HomeStoragesAdapter
        .StoragesViewHolder> {

    private Context mContext;
    private ArrayList<HomeStoragesInfo> homeStoragesList;
    private SparseBooleanArray mSelectedItemsIds;
    private SparseBooleanArray mDraggedItemsIds;

    private ArrayList<FileInfo> mSelectedFileList;
    OnItemClickListener mItemClickListener;


/*    public HomeLibraryAdapter(Fragment fragment, Context mContext, ArrayList<FileInfo>
            homeLibraryList, int
                                      category, int viewMode) {
        this.mFragment = fragment;
        this.mContext = mContext;
        this.homeLibraryList = homeLibraryList;
        mSelectedItemsIds = new SparseBooleanArray();
        mDraggedItemsIds = new SparseBooleanArray();
        mSelectedFileList = new ArrayList<>();
        mCategory = category;
        this.mViewMode = viewMode;
    }*/

    public HomeStoragesAdapter(Context mContext, ArrayList<HomeStoragesInfo>
            storagesInfos) {

        this.mContext = mContext;
        this.homeStoragesList = storagesInfos;

    }

    public void updateAdapter(ArrayList<HomeStoragesInfo> homeStoragesList) {
        this.homeStoragesList = homeStoragesList;
  //        Log.d("SIJU","updateAdapter"+homeLibraryList.size());

        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }


    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }


    @Override
    public StoragesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.storage_item,
                parent, false);

        StoragesViewHolder tvh = new StoragesViewHolder(view);
        return tvh;
    }

    @Override
    public void onBindViewHolder(StoragesViewHolder libraryViewHolder, int position) {
        //change background color if list item is selected
//        Log.d("TAG","OnBindviewholder Pos="+position);
//        setViewByCategory(libraryViewHolder, position);
        libraryViewHolder.imageStorage.setImageResource(homeStoragesList.get(position).getResourceId());
        libraryViewHolder.textStorage.setText(homeStoragesList.get(position).getStorageName());
        libraryViewHolder.textSpace.setText(homeStoragesList.get(position).getSpace());
        libraryViewHolder.progressBarSpace.setProgress(homeStoragesList.get(position).getProgress());


    }


    @Override
    public int getItemCount() {
        if (homeStoragesList == null) {
            return 0;
        } else {
            return homeStoragesList.size();
        }
    }

    /*private void setViewByCategory(LibraryViewHolder libraryViewHolder, int position) {

        switch (position) {
            case 0:
                libraryViewHolder.imageLibrary.setImageResource(homeLibraryList.get(position).getResourceId());
                libraryViewHolder.textLibraryName.setText(mLabels[0]);
                break;
            case 1:
                libraryViewHolder.imageLibrary.setImageResource(mResourceIds[1]);
                libraryViewHolder.textLibraryName.setText(mLabels[1]);
                break;
            case 2:
                libraryViewHolder.imageLibrary.setImageResource(mResourceIds[2]);
                libraryViewHolder.textLibraryName.setText(mLabels[2]);
                break;
            case 3:
                libraryViewHolder.imageLibrary.setImageResource(mResourceIds[3]);
                libraryViewHolder.textLibraryName.setText(mLabels[3]);
                break;
            case 4:
                libraryViewHolder.imageLibrary.setImageResource(mResourceIds[4]);
                libraryViewHolder.textLibraryName.setText(mLabels[4]);
                break;
       *//*     case 5:
                libraryViewHolder.imageLibrary.setImageResource(R.drawable.ic_library_images);
                libraryViewHolder.textLibraryName.setText(mContext.getString(R.string
                        .nav_menu_image));
                break;*//*

        }

    }*/




    class StoragesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageStorage;
        TextView textSpace;
        TextView textStorage;
        ProgressBar progressBarSpace;

        public StoragesViewHolder(View itemView) {
            super(itemView);
            progressBarSpace = (ProgressBar) itemView
                    .findViewById(R.id.progressBarSD);
            imageStorage = (ImageView) itemView.findViewById(R.id.imageStorage);
            textStorage = (TextView) itemView.findViewById(R.id.textStorage);
            textSpace = (TextView) itemView.findViewById(R.id.textSpace);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getAdapterPosition());
            }
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
