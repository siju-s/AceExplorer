package com.siju.acexplorer.filesystem;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.siju.acexplorer.R;
import com.siju.acexplorer.filesystem.helper.*;
import com.siju.acexplorer.filesystem.model.LibrarySortModel;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Siju on 13-06-2016.
 */

public class LibrarySortAdapter extends RecyclerView.Adapter<LibrarySortAdapter.LibrarySortViewHolder>
        implements com.siju.acexplorer.filesystem.helper.ItemTouchHelperAdapter {

    private Context mContext;
    OnItemClickListener mItemClickListener;
    private int mCategory;
    private final OnStartDragListener mDragStartListener;
    private ArrayList<LibrarySortModel> totalLibraries = new ArrayList<>();


    public LibrarySortAdapter(Context context, OnStartDragListener dragStartListener,
                              ArrayList<LibrarySortModel> models) {

        this.mContext = mContext;
        mDragStartListener = dragStartListener;
        totalLibraries = models;
        Log.d("TAG","Total libs="+totalLibraries.size());
    }

/*    public void updateAdapter(ArrayList<FileInfo> fileInfos) {
        this.fileInfoArrayList = fileInfos;

//        Log.d("SIJU","updateAdapter"+fileInfoArrayList.size());

        notifyDataSetChanged();
    }*/

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }


    public interface OnItemTouchListener {
        boolean onItemTouch(View view, int position, MotionEvent event);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }


    @Override
    public LibrarySortViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.library_sort_item,
                parent, false);
        LibrarySortViewHolder tvh = new LibrarySortViewHolder(view);
        return tvh;
    }

    @Override
    public void onBindViewHolder(final LibrarySortViewHolder librarySortViewHolder, final int position) {
        //change background color if list item is selected
        Log.d("TAG","OnBindviewholder Pos="+position);
        final LibrarySortModel model = totalLibraries.get(position);

        librarySortViewHolder.textLibrary.setText(model.getLibraryName());

        librarySortViewHolder.imageSort.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(librarySortViewHolder);
                }
                return false;
            }
        });

        //if true, your checkbox will be selected, else unselected
        librarySortViewHolder.checkBox.setChecked(model.isChecked());

        librarySortViewHolder.textLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean isChecked = model.isChecked();
                Log.d("TAG","Text clicked=="+isChecked);
                model.setChecked(!isChecked);
                librarySortViewHolder.checkBox.setChecked(!isChecked);
            }
        });

        librarySortViewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                Log.d("TAG","Checkbox clicked=="+isChecked);
                model.setChecked(isChecked);
            }
        });


    }


    @Override
    public int getItemCount() {

        if (totalLibraries == null) {
            return 0;
        } else {
            return totalLibraries.size();
        }
    }

    @Override
    public void onItemDismiss(int position) {
        totalLibraries.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(totalLibraries, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }


/*    public void toggleSelection(int position, boolean isLongPress) {
        if (isLongPress) {
            selectView(position, true);
        } else {
            selectView(position, !mSelectedItemsIds.get(position));

        }
    }*/


/*    public void toggleDragSelection(int position) {
        selectDragView(position, !mDraggedItemsIds.get(position));
    }*/

/*
    public void toggleSelectAll(int position, boolean selectAll) {
        if (selectAll)
            mSelectedItemsIds.put(position, selectAll);
        else
            mSelectedItemsIds = new SparseBooleanArray();
    }


    public void selectView(int position, boolean value) {
        if (value) {
            mSelectedItemsIds.put(position, true);
//            mSelectedFileList.add(fileInfoArrayList.get(position));
        } else {
            mSelectedItemsIds.delete(position);

        }

        notifyDataSetChanged();
    }*/
/*
    public void selectDragView(int position, boolean value) {
        if (value) {
            mDraggedItemsIds.put(position, true);
//            mSelectedFileList.add(fileInfoArrayList.get(position));
        } else {
            mDraggedItemsIds.delete(position);

        }

        notifyDataSetChanged();
    }*/

   /* public int getSelectedCount() {
        return mSelectedItemsIds.size();// mSelectedCount;
    }

    public SparseBooleanArray getSelectedItemPositions() {
        return mSelectedItemsIds;
    }


    public void setModel(List<FileInfo> models) {
        fileInfoArrayList = new ArrayList<>(models);
    }*/

    public LibrarySortModel removeItem(int position) {
        final LibrarySortModel model = totalLibraries.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, LibrarySortModel model) {
        totalLibraries.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final LibrarySortModel model = totalLibraries.remove(fromPosition);
        totalLibraries.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

/*    public void animateTo(List<LibrarySortModel> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<LibrarySortModel> newModels) {
        for (int i = totalLibraries.size() - 1; i >= 0; i--) {
            final LibrarySortModel model = totalLibraries.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<LibrarySortModel> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final LibrarySortModel model = newModels.get(i);
            if (!totalLibraries.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<LibrarySortModel> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final LibrarySortModel model = newModels.get(toPosition);
            final int fromPosition = totalLibraries.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }*/


    class LibrarySortViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            ItemTouchHelperViewHolder {
        ImageView imageSort;
        TextView textLibrary;
        CheckBox checkBox;


        public LibrarySortViewHolder(View itemView) {
            super(itemView);
            textLibrary = (TextView) itemView
                    .findViewById(R.id.textLibrary);
            imageSort = (ImageView) itemView.findViewById(R.id.imageSort);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });


/*            FileListFragment.myDragEventListener dragEventListener = ((FileListFragment)
                    mFragment).new myDragEventListener();

            itemView.setOnDragListener(dragEventListener);*/
//            itemView.setOnTouchListener(this);

        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }

        @Override
        public void onItemSelected() {

        }

        @Override
        public void onItemClear() {

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
