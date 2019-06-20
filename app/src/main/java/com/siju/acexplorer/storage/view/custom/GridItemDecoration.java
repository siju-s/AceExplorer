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
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.siju.acexplorer.R;
import com.siju.acexplorer.theme.Theme;

public class GridItemDecoration extends RecyclerView.ItemDecoration {

    private final Drawable mHorizontalDivider;
    private final Drawable mVerticalDivider;
    private final int mNumColumns;

    public GridItemDecoration(Context context, Theme theme, int numColumns) {
        switch (theme) {
            case LIGHT:
                mHorizontalDivider = ContextCompat.getDrawable(context, R.drawable.divider_line);
                mVerticalDivider = ContextCompat.getDrawable(context, R.drawable.divider_line);
                break;
            case DARK:
                mHorizontalDivider = ContextCompat.getDrawable(context, R.drawable.divider_line_dark);
                mVerticalDivider = ContextCompat.getDrawable(context, R.drawable.divider_line_dark);
                break;
            default:
                mHorizontalDivider = ContextCompat.getDrawable(context, R.drawable.divider_line_dark);
                mVerticalDivider = ContextCompat.getDrawable(context, R.drawable.divider_line_dark);
        }
        mNumColumns = numColumns;
    }

    /**
     * Draws horizontal and/or vertical dividers onto the parent RecyclerView.
     *
     * @param canvas The {@link Canvas} onto which dividers will be drawn
     * @param parent The RecyclerView onto which dividers are being added
     * @param state  The current RecyclerView.State of the RecyclerView
     */
    @Override
    public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        drawHorizontalDividers(canvas, parent);
        drawVerticalDividers(canvas, parent);

    }

    /**
     * Determines the size and location of offsets between items in the parent
     * RecyclerView.
     *
     * @param outRect The {@link Rect} of offsets to be added around the child view
     * @param view    The child view to be decorated with an offset
     * @param parent  The RecyclerView onto which dividers are being added
     * @param state   The current RecyclerView.State of the RecyclerView
     */
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        boolean childIsInLeftmostColumn = (parent.getChildAdapterPosition(view) % mNumColumns) == 0;
        if (!childIsInLeftmostColumn) {
            outRect.left = mHorizontalDivider.getIntrinsicWidth();
        }

        boolean childIsInFirstRow = (parent.getChildAdapterPosition(view)) < mNumColumns;
        if (!childIsInFirstRow) {
            outRect.top = mVerticalDivider.getIntrinsicHeight();
        }
    }

    /**
     * Adds horizontal dividers to a RecyclerView with a GridLayoutManager or its
     * subclass.
     *
     * @param canvas The {@link Canvas} onto which dividers will be drawn
     * @param parent The RecyclerView onto which dividers are being added
     */
    private void drawHorizontalDividers(Canvas canvas, RecyclerView parent) {
        int childCount = parent.getChildCount();
        int rowCount = childCount / mNumColumns;
        int lastRowChildCount = childCount % mNumColumns;
        int newChildCount;
        if (childCount < mNumColumns) {
            newChildCount = childCount;
        } else {
            newChildCount = mNumColumns;
        }

        for (int i = 1; i < newChildCount; i++) {
            int lastRowChildIndex;
            if (i < lastRowChildCount) {
                lastRowChildIndex = i + (rowCount * mNumColumns);
            } else {
                lastRowChildIndex = i + ((rowCount - 1) * mNumColumns);
            }


            View firstRowChild = parent.getChildAt(i);
            View lastRowChild = parent.getChildAt(lastRowChildIndex);

       /*     Logger.log("TAG", "firstRowChild=" + firstRowChild + "item=" + i);
            Logger.log("TAG", "lastRowChild=" + lastRowChild + "item=" + lastRowChildIndex);*/

            int dividerTop = firstRowChild.getTop();
            int dividerRight = firstRowChild.getLeft();
            int dividerLeft = dividerRight - mHorizontalDivider.getIntrinsicWidth();
            int dividerBottom = lastRowChild.getBottom();

            mHorizontalDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
            mHorizontalDivider.draw(canvas);
        }
    }

    /**
     * Adds vertical dividers to a RecyclerView with a GridLayoutManager or its
     * subclass.
     *
     * @param canvas The {@link Canvas} onto which dividers will be drawn
     * @param parent The RecyclerView onto which dividers are being added
     */
    private void drawVerticalDividers(Canvas canvas, RecyclerView parent) {
        int childCount = parent.getChildCount();
        int rowCount = childCount / mNumColumns;
        int rightmostChildIndex;
        for (int i = 1; i < rowCount; i++) {
 /*           if (i == rowCount) {
                rightmostChildIndex = parent.getChildCount() - 1;
            } else {*/
            rightmostChildIndex = (i * mNumColumns) + mNumColumns - 1;
//            }


            View leftmostChild = parent.getChildAt(i * mNumColumns);
            View rightmostChild = parent.getChildAt(rightmostChildIndex);

        /*    Logger.log("TAG","Leftmostchild="+leftmostChild+ "item="+i*mNumColumns);
            Logger.log("TAG","rightmostChild="+rightmostChild+ "item="+rightmostChildIndex);*/


            int dividerLeft = leftmostChild.getLeft();
            int dividerBottom = leftmostChild.getTop();
            int dividerTop = dividerBottom - mVerticalDivider.getIntrinsicHeight();
            int dividerRight = rightmostChild.getRight();

            mVerticalDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
            mVerticalDivider.draw(canvas);
        }
    }
}
