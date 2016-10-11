package com.siju.acexplorer.filesystem.ui;


import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * Touch listener that will move a {@link AbsRecyclerViewFastScroller}'s handle to a specified offset along the scroll bar
 */
class FastScrollerTouchListener implements OnTouchListener {

    private final AbsRecyclerViewFastScroller mFastScroller;

    /**
     * @param fastScroller  for this listener to scroll
     */
    public FastScrollerTouchListener(AbsRecyclerViewFastScroller fastScroller) {
        mFastScroller = fastScroller;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
//        SectionIndicator sectionIndicator = mFastScroller.getSectionIndicator();
//        showOrHideIndicator(sectionIndicator, event);
        showOrHideIndicator(event);
        float scrollProgress = mFastScroller.getScrollProgress(event);
        mFastScroller.scrollTo(scrollProgress, true);
        mFastScroller.moveHandleToPosition(scrollProgress);
        return true;
    }

    private void showOrHideIndicator(MotionEvent event) {


        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mFastScroller.notifyScrollState(true);
                return;
            case MotionEvent.ACTION_UP:
                mFastScroller.notifyScrollState(false);
        }
    }

}
