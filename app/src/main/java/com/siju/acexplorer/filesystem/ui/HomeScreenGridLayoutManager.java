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

public class HomeScreenGridLayoutManager extends GridLayoutManager {
    private final String TAG = this.getClass().getSimpleName();
    private static final float MILLISECONDS_PER_INCH = 500f;


    public HomeScreenGridLayoutManager(Context context,int spanCount) {
        super(context,spanCount);
    }


    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
                                       int position) {
        LinearSmoothScroller smoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
            //This controls the direction in which smoothScroll looks for your view
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return HomeScreenGridLayoutManager.this
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
}
