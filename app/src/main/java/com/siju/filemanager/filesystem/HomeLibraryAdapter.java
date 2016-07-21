package com.siju.filemanager.filesystem;

import android.content.Context;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.siju.filemanager.R;
import com.siju.filemanager.filesystem.model.FileInfo;
import com.siju.filemanager.filesystem.model.HomeLibraryInfo;

import java.util.ArrayList;

/**
 * Created by Siju on 13-06-2016.
 */

public class HomeLibraryAdapter extends RecyclerView.Adapter<HomeLibraryAdapter
        .LibraryViewHolder> {

    private Context mContext;
    private ArrayList<HomeLibraryInfo> homeLibraryList;
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

    public HomeLibraryAdapter(Context mContext, ArrayList<HomeLibraryInfo>
            homeLibraryInfos) {

        this.mContext = mContext;
        this.homeLibraryList = homeLibraryInfos;

    }

    public void updateAdapter(ArrayList<HomeLibraryInfo> homeLibraryInfos) {
        this.homeLibraryList = homeLibraryInfos;
  //        Log.d("SIJU","updateAdapter"+homeLibraryList.size());

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


    @Override
    public LibraryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.library_item,
                parent, false);

        LibraryViewHolder tvh = new LibraryViewHolder(view);
        return tvh;
    }

    @Override
    public void onBindViewHolder(LibraryViewHolder libraryViewHolder, int position) {
        //change background color if list item is selected
//        Log.d("TAG","OnBindviewholder Pos="+position);
//        setViewByCategory(libraryViewHolder, position);
        libraryViewHolder.imageLibrary.setImageResource(homeLibraryList.get(position).getResourceId());
        libraryViewHolder.textLibraryName.setText(homeLibraryList.get(position).getCategoryName());

    }


    @Override
    public int getItemCount() {
        if (homeLibraryList == null) {
            return 0;
        } else {
            return homeLibraryList.size();
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




    class LibraryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageLibrary;
        TextView textCount;
        TextView textLibraryName;

        public LibraryViewHolder(View itemView) {
            super(itemView);
            textLibraryName = (TextView) itemView
                    .findViewById(R.id.text);
            imageLibrary = (ImageView) itemView.findViewById(R.id.imageLibrary);
            textLibraryName = (TextView) itemView.findViewById(R.id.textLibrary);
            textCount = (TextView) itemView.findViewById(R.id.textCount);
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
