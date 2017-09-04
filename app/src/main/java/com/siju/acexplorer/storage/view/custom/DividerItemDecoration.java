/*
 * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.siju.acexplorer.storage.view.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.siju.acexplorer.R;
import com.siju.acexplorer.theme.Theme;

/**
 *
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

    private final Drawable mDivider;
    private int mOrientation;
    private final int leftMargin;

    public DividerItemDecoration(Context context, Theme theme) {

        switch (theme) {
            case LIGHT:
                mDivider = ContextCompat.getDrawable(context, R.drawable.divider_line);
                break;
            case DARK:
                mDivider = ContextCompat.getDrawable(context, R.drawable.divider_line_dark);
                break;
            default:
                mDivider = ContextCompat.getDrawable(context, R.drawable.divider_line_dark);

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