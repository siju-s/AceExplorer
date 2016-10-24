package com.siju.acexplorer.filesystem;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.siju.acexplorer.R;
import com.siju.acexplorer.model.SectionItems;

import java.util.ArrayList;


class HomeStoragesAdapter extends RecyclerView.Adapter<HomeStoragesAdapter
        .StoragesViewHolder> {

    private ArrayList<SectionItems> homeStoragesList;
    private OnItemClickListener mItemClickListener;


    HomeStoragesAdapter(Context context, ArrayList<SectionItems>
            storagesInfos) {
        this.homeStoragesList = storagesInfos;
    }

    interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }


    @Override
    public StoragesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.storage_item,
                parent, false);

        return new StoragesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StoragesViewHolder libraryViewHolder, int position) {
        //change background color if list item is selected
//        Log.d("TAG","OnBindviewholder Pos="+position);
//        setViewByCategory(libraryViewHolder, position);
        libraryViewHolder.imageStorage.setImageResource(homeStoragesList.get(position).getIcon());
        libraryViewHolder.textStorage.setText(homeStoragesList.get(position).getFirstLine());
        libraryViewHolder.textSpace.setText(homeStoragesList.get(position).getSecondLine());
        libraryViewHolder.progressBarSpace.setProgress(homeStoragesList.get(position).getProgress());
    }


    @Override
    public int getItemCount() {
        return homeStoragesList == null ? 0 : homeStoragesList.size();
    }


    class StoragesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageStorage;
        TextView textSpace;
        TextView textStorage;
        ProgressBar progressBarSpace;

        StoragesViewHolder(View itemView) {
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
    }


}
