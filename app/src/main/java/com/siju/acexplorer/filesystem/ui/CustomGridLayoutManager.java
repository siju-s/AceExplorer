package com.siju.acexplorer.filesystem.ui;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

/**
 * Created by SIJU on 15-07-2016.
 */

public class CustomGridLayoutManager extends GridLayoutManager {
    private final String TAG = this.getClass().getSimpleName();
    private static final float MILLISECONDS_PER_INCH = 500f;
    private Context mContext;
    private int minItemWidth;
    private int mSpanCount;

/*    public CustomGridLayoutManager(Context context,int spanCount) {
        super(context,spanCount);
        mContext = context;
    }*/

    public CustomGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
        mSpanCount = spanCount;
//        this.minItemWidth = minItemWidth;
    }


    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
                                       int position) {
        LinearSmoothScroller smoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
            //This controls the direction in which smoothScroll looks for your view
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return CustomGridLayoutManager.this
                        .computeScrollVectorForPosition(targetPosition);
            }

            //This returns the milliseconds it takes to scroll one pixel.
            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }
        };
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler,
                                 RecyclerView.State state) {
//        updateSpanCount();
//        Logger.log("SIJU","LM width="+getWidth()/mSpanCount);

        super.onLayoutChildren(recycler, state);
    }

    private void updateSpanCount() {
        int spanCount = getWidth() / minItemWidth;
        if (spanCount < 1) {
            spanCount = 1;
        }
//        Logger.log(TAG,"Min width="+minItemWidth+"width="+getWidth()+"Spancount="+spanCount);
        this.setSpanCount(spanCount);
    }


/*    private class TopSnappedSmoothScroller extends LinearSmoothScroller {
        public TopSnappedSmoothScroller(Context context) {
            super(context);

        }

        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
            return CustomLayoutManager.this
                    .computeScrollVectorForPosition(targetPosition);
        }

        @Override
        protected int getVerticalSnapPreference() {
            return SNAP_TO_START;
        }
    }*/
}