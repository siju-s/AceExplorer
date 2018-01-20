package com.siju.acexplorer.home.view;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.siju.acexplorer.R;
import com.siju.acexplorer.home.types.HomeLibraryInfo;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.storage.view.custom.helper.ItemTouchHelperAdapter;
import com.siju.acexplorer.theme.Theme;

import java.util.Collections;
import java.util.List;

import static com.siju.acexplorer.model.groups.Category.ADD;
import static com.siju.acexplorer.model.groups.CategoryHelper.getCategoryName;


public class HomeLibAdapter extends RecyclerView.Adapter<HomeLibAdapter.HomeLibHolder>
        implements ItemTouchHelperAdapter
{
    @SuppressWarnings("FieldCanBeLocal")
    private static final int MAX_LIMIT_ROUND_COUNT = 99999;
    private Context                 context;
    private OnItemClickListener     mItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private List<HomeLibraryInfo>   homeLibraryInfoArrayList;
    private Theme                   theme;
    private SparseBooleanArray      mSelectedItemsIds;


    HomeLibAdapter(Context context, List<HomeLibraryInfo>
            homeLibraryInfoList, Theme theme) {

        this.context = context;
        this.homeLibraryInfoArrayList = homeLibraryInfoList;
        this.theme = theme;
        mSelectedItemsIds = new SparseBooleanArray();

    }

    void updateAdapter(List<HomeLibraryInfo>
                               homeLibraryInfoList) {
        this.homeLibraryInfoArrayList = homeLibraryInfoList;
        notifyDataSetChanged();
    }

    void updateCount(int index, int count) {
        homeLibraryInfoArrayList.get(index).setCount(count);
        notifyItemChanged(index);
    }

    void updateFavCount(int index, int count) {
        int count1 = homeLibraryInfoArrayList.get(index).getCount();
        updateCount(index, count1 + count);
    }

    void removeFav(int index, int count) {
        int count1 = homeLibraryInfoArrayList.get(index).getCount();
        updateCount(index, count1 - count);
    }

    @Override
    public void onItemDismiss(int position) {
        // We don't have swipe to delete feature
//        homeLibraryInfoArrayList.remove(position);
//        notifyItemRemoved(position);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        int addPos = homeLibraryInfoArrayList.size() - 1;
        if (fromPosition == addPos || toPosition == addPos) {
            return;
        }
        Collections.swap(homeLibraryInfoArrayList, fromPosition, toPosition);
        mSelectedItemsIds.delete(fromPosition);
        mSelectedItemsIds.put(toPosition, true);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    List<HomeLibraryInfo> getList() {
        return homeLibraryInfoArrayList;
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }


    void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    void setOnItemLongClickListener(final OnItemLongClickListener mItemClickListener) {
        this.mOnItemLongClickListener = mItemClickListener;
    }


    @Override
    public HomeLibHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.library_item,
                                                                     parent, false);

        return new HomeLibHolder(view);


    }

    @Override
    public void onBindViewHolder(HomeLibHolder homeLibHolder, int position) {

        homeLibHolder.imageLibrary.setImageResource(homeLibraryInfoArrayList.get(position)
                                                            .getResourceId());
        String name = getCategoryName(context, homeLibraryInfoArrayList.get(position)
                .getCategory());

        homeLibHolder.textLibraryName.setText(name);
        if (homeLibraryInfoArrayList.get(position).getCategory().equals(ADD)) {
            homeLibHolder.textCount.setVisibility(View.GONE);
        } else {
            homeLibHolder.textCount.setVisibility(View.VISIBLE);
        }

        if (mSelectedItemsIds.get(position)) {
            homeLibHolder.imageDone.setVisibility(View.VISIBLE);
            if (theme == Theme.LIGHT) {
                homeLibHolder.imageDone.setImageResource(R.drawable.ic_done_black);
            } else {
                homeLibHolder.imageDone.setImageResource(R.drawable.ic_done_white);
            }
        } else {
            homeLibHolder.imageDone.setVisibility(View.GONE);
        }
        homeLibHolder.textCount.setText(roundOffCount(homeLibraryInfoArrayList.get(position)
                                                              .getCount()));
        homeLibHolder.itemView.setTag(homeLibraryInfoArrayList.get(position).getCategory());
        changeColor(homeLibHolder.imageLibrary, homeLibraryInfoArrayList.get(position)
                .getCategory());
    }


    @Override
    public int getItemCount() {
        return homeLibraryInfoArrayList.size();
    }



    private void changeColor(View itemView, Category category) {
        switch (category) {
            case AUDIO:
                ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                        (context, R.color
                                .audio_bg_dark));
                break;
            case VIDEO:
                ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                        (context, R.color
                                .video_bg_dark));
                break;
            case IMAGE:
                ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                        (context, R.color
                                .image_bg_dark));
                break;
            case DOCS:
                ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                        (context, R.color
                                .docs_bg_dark));
                break;
            case DOWNLOADS:
                ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                        (context, R.color
                                .downloads_bg_dark));
                break;
            case ADD:
                ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                        (context, R.color
                                .add_bg_dark));
                break;
            case COMPRESSED:
                ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                        (context, R.color
                                .compressed_bg_dark));
                break;
            case FAVORITES:
                ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                        (context, R.color
                                .fav_bg_dark));
                break;
            case PDF:
                ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                        (context, R.color
                                .pdf_bg_dark));
                break;
            case APPS:
                ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                        (context, R.color
                                .apps_bg_dark));
                break;
            case LARGE_FILES:
                ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                        (context, R.color
                                .large_files_bg_dark));
                break;
            case GIF:
                ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                        (context, R.color
                                .gif_bg_dark));
                break;
            case RECENT:
                ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                        (context, R.color
                                .recents_bg_dark));
                break;

            default:
                ((GradientDrawable) itemView.getBackground()).setColor(ContextCompat.getColor
                        (context, R.color
                                .colorPrimary));

        }
    }


    private String roundOffCount(int count) {
        String roundedCount;
        if (count > MAX_LIMIT_ROUND_COUNT) {
            roundedCount = MAX_LIMIT_ROUND_COUNT + "+";
        } else {
            roundedCount = "" + count;
        }
        return roundedCount;
    }

    void toggleSelection(int position, boolean isLongPress) {
        if (isLongPress) {
            selectView(position, true);
        } else {
            selectView(position, !mSelectedItemsIds.get(position));
        }
    }

    void clearSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
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


    class HomeLibHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener,
                       View.OnLongClickListener
    {
        ImageView imageLibrary;
        TextView  textLibraryName;
        TextView  textCount;
        ImageView imageDone;


        HomeLibHolder(View itemView) {
            super(itemView);
            imageLibrary = itemView.findViewById(R.id.imageLibrary);
            textLibraryName = itemView.findViewById(R.id.textLibrary);
            textCount = itemView.findViewById(R.id.textCount);
            imageDone = itemView.findViewById(R.id.imageDone);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }


        @Override
        public void onClick(View v) {
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
}