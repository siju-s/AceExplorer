package com.siju.acexplorer.filesystem;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.siju.acexplorer.R;
import com.siju.acexplorer.filesystem.model.HomeLibraryInfo;
import com.siju.acexplorer.filesystem.utils.ThemeUtils;

import java.util.ArrayList;


class HomeLibraryAdapter extends RecyclerView.Adapter<HomeLibraryAdapter
        .LibraryViewHolder> {

    private Context mContext;
    private ArrayList<HomeLibraryInfo> homeLibraryList;
    private OnItemClickListener mItemClickListener;
    private boolean mIsThemeDark;

    HomeLibraryAdapter(Context mContext, ArrayList<HomeLibraryInfo>
            homeLibraryInfos) {

        this.mContext = mContext;
        this.homeLibraryList = homeLibraryInfos;
        mIsThemeDark = ThemeUtils.isDarkTheme(mContext);

    }

    void updateAdapter(ArrayList<HomeLibraryInfo> homeLibraryInfos) {
        homeLibraryList = new ArrayList<>();
        this.homeLibraryList = homeLibraryInfos;
        notifyDataSetChanged();
    }

    interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }


    @Override
    public LibraryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.library_item,
                parent, false);

        return new LibraryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LibraryViewHolder libraryViewHolder, int position) {
        libraryViewHolder.imageLibrary.setImageResource(homeLibraryList.get(position).getResourceId());
        libraryViewHolder.textLibraryName.setText(homeLibraryList.get(position).getCategoryName());
        if (homeLibraryList.get(position).getCategoryId() == FileConstants.CATEGORY.ADD.getValue()) {
            libraryViewHolder.textCount.setVisibility(View.GONE);
        } else {
            libraryViewHolder.textCount.setVisibility(View.VISIBLE);
        }
        libraryViewHolder.textCount.setText(roundOffCount(homeLibraryList.get(position).getCount()));
        changeColor(libraryViewHolder.imageLibrary, homeLibraryList.get(position).getCategoryId());
    }


    @Override
    public int getItemCount() {
        if (homeLibraryList == null) {
            return 0;
        } else {
            return homeLibraryList.size();
        }
    }

    private String roundOffCount(int count) {
       String roundedCount;
        if (count > 99999) {
           roundedCount = 99999 + "+";
       }
        else {
            roundedCount = "" +count;
        }
        return roundedCount;
    }


    private void changeColor(View itemView, int category) {
        if (mIsThemeDark) {
            switch (category) {
                    case 1:
                        ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.audio_bg_dark));
                        break;
                    case 2:
                        ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.video_bg_dark));
                        break;
                    case 3:
                        ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.image_bg_dark));
                        break;
                    case 4:
                        ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.docs_bg_dark));
                        break;
                    case 5:
                        ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.downloads_bg_dark));
                        break;
                    case 6:
                        ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.add_bg_dark));
                        break;
                    case 7:
                        ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.compressed_bg_dark));
                        break;
                    case 8:
                        ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.fav_bg_dark));
                        break;
                    case 9:
                        ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.pdf_bg_dark));
                        break;
                    case 10:
                        ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.apps_bg_dark));
                        break;
                    case 11:
                        ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.large_files_bg_dark));
                        break;

                    default:
                        ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.colorPrimary));

                }
        }
        else {
            switch (category) {
                case 1:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.audio_bg));
                    break;
                case 2:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.video_bg));
                    break;
                case 3:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.image_bg));
                    break;
                case 4:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.docs_bg));
                    break;
                case 5:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.downloads_bg));
                    break;
                case 6:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.add_bg));
                    break;
                case 7:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.compressed_bg));
                    break;
                case 8:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.fav_bg));
                    break;
                case 9:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.pdf_bg));
                    break;
                case 10:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.apps_bg));
                    break;
                case 11:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.large_files_bg));
                    break;

                default:
                    ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor(mContext, R.color.colorPrimary));


            }
        }
    }



    class LibraryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageLibrary;
        TextView textCount;
        TextView textLibraryName;

        LibraryViewHolder(View itemView) {
            super(itemView);
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

    }


}
