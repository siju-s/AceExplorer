package com.siju.acexplorer.filesystem.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.siju.acexplorer.R;

/**
 *
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

    private final Drawable mDivider;
    private int mOrientation;
    private final int leftMargin;

    public DividerItemDecoration(Context context, boolean isDarkTheme) {
        if (isDarkTheme) {
            mDivider = ContextCompat.getDrawable(context, R.drawable.divider_line_dark);

        } else {
            mDivider = ContextCompat.getDrawable(context, R.drawable.divider_line);
        }

        setOrientation();
        leftMargin = context.getResources().getDimensionPixelSize(R.dimen.divider_margin_list);
    }

    public void setOrientation() {
        mOrientation = LinearLayoutManager.VERTICAL;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {

        if (mOrientation == VERTICAL_LIST) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    private void drawVertical(Canvas c, RecyclerView parent) {
        int left;
        if (mOrientation == VERTICAL_LIST) {
            left = parent.getPaddingLeft() + leftMargin;
        } else {
            left = parent.getPaddingLeft();
        }

        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childCount = parent.getChildCount();
//        Logger.log("TAG", "drawVertical"+childCount);

        for (int i = 0; i < childCount - 1; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    private void drawHorizontal(Canvas c, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();


        final int childCount = parent.getChildCount();

        for (int i = 0; i < childCount - 1; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int left = child.getRight() + params.rightMargin;
            final int right = left + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == VERTICAL_LIST) {
            outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
        } else {
            outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
        }
    }
}