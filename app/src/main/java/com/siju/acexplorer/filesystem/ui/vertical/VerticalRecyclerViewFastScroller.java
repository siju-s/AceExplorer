package com.siju.acexplorer.filesystem.ui.vertical;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.siju.acexplorer.R;
import com.siju.acexplorer.filesystem.ui.calculation.VerticalScrollBoundsProvider;
import com.siju.acexplorer.filesystem.ui.calculation.position.VerticalScreenPositionCalculator;
import com.siju.acexplorer.filesystem.ui.calculation.progress.TouchableScrollProgressCalculator;
import com.siju.acexplorer.filesystem.ui.calculation.progress.VerticalLinearLayoutManagerScrollProgressCalculator;
import com.siju.acexplorer.filesystem.ui.calculation.progress.VerticalScrollProgressCalculator;

//import android.support.v4.view.

/**
 * Widget used to fast-scroll a vertical {@link RecyclerView}.
 * Currently assumes the use of a {@link LinearLayoutManager}
 */
public class VerticalRecyclerViewFastScroller extends com.siju.acexplorer.filesystem.ui.AbsRecyclerViewFastScroller {

    @Nullable
    private VerticalScrollProgressCalculator mScrollProgressCalculator;
    @Nullable
    private VerticalScreenPositionCalculator mScreenPositionCalculator;

    public VerticalRecyclerViewFastScroller(Context context) {
        this(context, null);
    }

    public VerticalRecyclerViewFastScroller(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalRecyclerViewFastScroller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.vertical_recycler_fast_scroller_layout;
    }

    @Override
    @Nullable
    protected TouchableScrollProgressCalculator getScrollProgressCalculator() {
        return mScrollProgressCalculator;
    }

    @Override
    public void moveHandleToPosition(float scrollProgress) {
        if (mScreenPositionCalculator == null) {
            return;
        }
//        Logger.log("TAG","Handle pos="+mScreenPositionCalculator.getYPositionFromScrollProgress(scrollProgress));
        mHandle.setY(mScreenPositionCalculator.getYPositionFromScrollProgress(scrollProgress));
    }

    protected void onCreateScrollProgressCalculator() {

        float minScrollY = mBar.getY();
        float maxScrollY = mBar.getY() +  mBar.getHeight() -  ((float) (mHandle.getHeight()));
//        Logger.log("TAG","Min scroll="+minScrollY+ " Max scroll="+maxScrollY);

        VerticalScrollBoundsProvider boundsProvider =
                new VerticalScrollBoundsProvider(minScrollY, maxScrollY);
        mScrollProgressCalculator = new VerticalLinearLayoutManagerScrollProgressCalculator(boundsProvider);


        mScreenPositionCalculator = new VerticalScreenPositionCalculator(boundsProvider);
    }


/*    public void newOnCreateScrollProgressCalculator() {

        float minScrollY = mBar.getY();
        float maxScrollY = mBar.getY() + mBar.getHeight() - 162 - ((float) (mHandle.getHeight()));
        Logger.log("TAG","Min scroll="+minScrollY+ " Max scroll="+maxScrollY);

        VerticalScrollBoundsProvider boundsProvider =
                new VerticalScrollBoundsProvider(minScrollY, maxScrollY);
        mScrollProgressCalculator = new VerticalLinearLayoutManagerScrollProgressCalculator(boundsProvider);
        mScreenPositionCalculator = new VerticalScreenPositionCalculator(boundsProvider);
    }*/




}
