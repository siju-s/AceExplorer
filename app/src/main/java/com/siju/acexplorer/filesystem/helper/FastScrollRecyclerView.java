/*
 * Copyright (c) 2016 Tim Malseed
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.siju.acexplorer.filesystem.helper;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;


public class FastScrollRecyclerView extends RecyclerView implements RecyclerView.OnItemTouchListener {

    private final FastScroller mScrollbar;
    private int mPaddingBottom;

    /**
     * The current scroll state of the recycler view.  We use this in onUpdateScrollbar()
     * and scrollToPositionAtProgress() to determine the scroll position of the recycler view so
     * that we can calculate what the scroll bar looks like, and where to jump to from the fast
     * scroller.
     */
    public static class ScrollPositionState {
        // The index of the first visible row
        int rowIndex;
        // The offset of the first visible row
        int rowTopOffset;
        // The height of a given row (they are currently all the same height)
        int rowHeight;
    }

    private final ScrollPositionState mScrollPosState = new ScrollPositionState();

    private int mDownX;
    private int mDownY;
    private int mLastY;

    private OnFastScrollStateChangeListener mStateChangeListener;

    public FastScrollRecyclerView(Context context) {
        this(context, null);
    }

    public FastScrollRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FastScrollRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScrollbar = new FastScroller(context, this, attrs);
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            mPaddingBottom = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        Log.d("TAG","Padding bottom="+mPaddingBottom);
    }

    public int getScrollBarWidth() {
        return mScrollbar.getWidth();
    }

    public int getScrollBarThumbHeight() {
        return mScrollbar.getThumbHeight();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        addOnItemTouchListener(this);
    }

    /**
     * We intercept the touch handling only to support fast scrolling when initiated from the
     * scroll bar.  Otherwise, we fall back to the default RecyclerView touch handling.
     */
    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent ev) {
        return handleTouchEvent(ev);
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent ev) {
        handleTouchEvent(ev);
    }

    /**
     * Handles the touch event and determines whether to show the fast scroller (or updates it if
     * it is already showing).
     */
    private boolean handleTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Keep track of the down positions
                mDownX = x;
                mDownY = mLastY = y;
                mScrollbar.handleTouchEvent(ev, mDownX, mDownY, mLastY, mStateChangeListener);
                break;
            case MotionEvent.ACTION_MOVE:
                mLastY = y;
                mScrollbar.handleTouchEvent(ev, mDownX, mDownY, mLastY, mStateChangeListener);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mScrollbar.handleTouchEvent(ev, mDownX, mDownY, mLastY, mStateChangeListener);
                break;
        }
        return mScrollbar.isDragging();
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    /**
     * Returns the available scroll height:
     * AvailableScrollHeight = Total height of the all items - last page height
     * <p>
     * This assumes that all rows are the same height.
     *
     */
    private int getAvailableScrollHeight(int rowCount, int rowHeight) {
        int visibleHeight = getHeight();
        int scrollHeight = getPaddingTop() + rowCount * rowHeight + getPaddingBottom();
        return scrollHeight - visibleHeight;
    }

    /**
     * Returns the available scroll bar height:
     * AvailableScrollBarHeight = Total height of the visible view - thumb height
     */
    private int getAvailableScrollBarHeight() {
        int visibleHeight = getHeight() ;
/*        int padding = 0 ;
        if (mLastY > mDownY && !mScrollbar.isDragging())  {
            padding = mPaddingBottom;
        }*/
        return visibleHeight - mScrollbar.getThumbHeight();
    }

    @Override
    public void draw(Canvas c) {
        super.draw(c);
        onUpdateScrollbar();
        mScrollbar.draw(c);
    }

    /**
     * Updates the scrollbar thumb offset to match the visible scroll of the recycler view.  It does
     * this by mapping the available scroll area of the recycler view to the available space for the
     * scroll bar.
     *  @param scrollPosState the current scroll position
     * @param rowCount       the number of rows, used to calculate the total scroll height (assumes that
     *                       all rows are the same height)
     */
    private void synchronizeScrollBarThumbOffsetToViewScroll(ScrollPositionState scrollPosState, int rowCount) {
        int availableScrollHeight = getAvailableScrollHeight(rowCount, scrollPosState.rowHeight);
        int availableScrollBarHeight = getAvailableScrollBarHeight();

        // Only show the scrollbar if there is height to be scrolled
        if (availableScrollHeight <= 0) {
            mScrollbar.setThumbPosition(-1, -1);
            return;
        }

        // Calculate the current scroll position, the scrollY of the recycler view accounts for the
        // view padding, while the scrollBarY is drawn right up to the background padding (ignoring
        // padding)
        int scrollY = getPaddingTop() + (scrollPosState.rowIndex * scrollPosState.rowHeight) - scrollPosState.rowTopOffset;
        int scrollBarY = (int) (((float) scrollY / availableScrollHeight) * availableScrollBarHeight);
//        Log.d("TAG","scrollY="+scrollY+" scrollbarY="+scrollBarY);


        // Calculate the position and size of the scroll bar
        int scrollBarX;
        if (Utils.isRtl(getResources())) {
            scrollBarX = 0;
        } else {
            scrollBarX = getWidth() - mScrollbar.getWidth();
        }
        mScrollbar.setThumbPosition(scrollBarX, scrollBarY);
    }

    /**
     * Maps the touch (from 0..1) to the adapter position that should be visible.
     */
    public String scrollToPositionAtProgress(float touchFraction) {
        int itemCount = getAdapter().getItemCount();
        if (itemCount == 0) {
            return "";
        }
        int spanCount = 1;
        int rowCount = itemCount;
        if (getLayoutManager() instanceof GridLayoutManager) {
            spanCount = ((GridLayoutManager) getLayoutManager()).getSpanCount();
            rowCount = (int) Math.ceil((double) rowCount / spanCount);
        }

        // Stop the scroller if it is scrolling
        stopScroll();

        getCurScrollState(mScrollPosState);

        float itemPos = itemCount * touchFraction;

        int availableScrollHeight = getAvailableScrollHeight(rowCount, mScrollPosState.rowHeight);

        //The exact position of our desired item
        int exactItemPos = (int) (availableScrollHeight * touchFraction);

        //Scroll to the desired item. The offset used here is kind of hard to explain.
        //If the position we wish to scroll to is, say, position 10.5, we scroll to position 10,
        //and then offset by 0.5 * rowHeight. This is how we achieve smooth scrolling.
        LinearLayoutManager layoutManager = ((LinearLayoutManager) getLayoutManager());
        layoutManager.scrollToPositionWithOffset(spanCount * exactItemPos / mScrollPosState.rowHeight,
                -(exactItemPos % mScrollPosState.rowHeight));

        if (!(getAdapter() instanceof SectionedAdapter)) {
            return "";
        }

        int posInt = (int) ((touchFraction == 1) ? itemPos - 1 : itemPos);

        SectionedAdapter sectionedAdapter = (SectionedAdapter) getAdapter();
        return sectionedAdapter.getSectionName(posInt);
    }

    /**
     * Updates the bounds for the scrollbar.
     */
    private void onUpdateScrollbar() {

        if (getAdapter() == null) {
            return;
        }

        int rowCount = getAdapter().getItemCount();
        if (getLayoutManager() instanceof GridLayoutManager) {
            int spanCount = ((GridLayoutManager) getLayoutManager()).getSpanCount();
            rowCount = (int) Math.ceil((double) rowCount / spanCount);
        }
        // Skip early if, there are no items.
        if (rowCount == 0) {
            mScrollbar.setThumbPosition(-1, -1);
            return;
        }

        // Skip early if, there no child laid out in the container.
        getCurScrollState(mScrollPosState);
        if (mScrollPosState.rowIndex < 0) {
            mScrollbar.setThumbPosition(-1, -1);
            return;
        }

        synchronizeScrollBarThumbOffsetToViewScroll(mScrollPosState, rowCount);
    }

    /**
     * Returns the current scroll state of the apps rows.
     */
    private void getCurScrollState(ScrollPositionState stateOut) {
        stateOut.rowIndex = -1;
        stateOut.rowTopOffset = -1;
        stateOut.rowHeight = -1;

        int itemCount = getAdapter().getItemCount();

        // Return early if there are no items, or no children.
        if (itemCount == 0 || getChildCount() == 0) {
            return;
        }

        View child = getChildAt(0);

        stateOut.rowIndex = getChildAdapterPosition(child);
        if (getLayoutManager() instanceof GridLayoutManager) {
            stateOut.rowIndex = stateOut.rowIndex / ((GridLayoutManager) getLayoutManager()).getSpanCount();
        }
        stateOut.rowTopOffset = getLayoutManager().getDecoratedTop(child);
        stateOut.rowHeight = child.getHeight();
    }

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setThumbColor(@ColorInt int color) {
//        mScrollbar.setThumbColor(color);
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setTrackColor(@ColorInt int color) {
//        mScrollbar.setTrackColor(color);
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setPopupBgColor(@ColorInt int color) {
//        mScrollbar.setPopupBgColor(color);
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setPopupTextColor(@ColorInt int color) {
//        mScrollbar.setPopupTextColor(color);
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setPopupTextSize(int textSize) {
//        mScrollbar.setPopupTextSize(textSize);
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setPopUpTypeface(Typeface typeface) {
//        mScrollbar.setPopupTypeface(typeface);
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setAutoHideDelay(int hideDelay) {
//        mScrollbar.setAutoHideDelay(hideDelay);
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setAutoHideEnabled(boolean autoHideEnabled) {
//        mScrollbar.setAutoHideEnabled(autoHideEnabled);
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

// --Commented out by Inspection START (06-11-2016 11:23 PM):
//    public void setStateChangeListener(OnFastScrollStateChangeListener stateChangeListener) {
//        mStateChangeListener = stateChangeListener;
//    }
// --Commented out by Inspection STOP (06-11-2016 11:23 PM)

    public interface SectionedAdapter {
        @NonNull
        String getSectionName(int position);
    }
}