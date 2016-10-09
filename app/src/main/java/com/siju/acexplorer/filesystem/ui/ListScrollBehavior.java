package com.siju.acexplorer.filesystem.ui;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by SJ on 10-10-2016.
 */

public class ListScrollBehavior extends AppBarLayout.ScrollingViewBehavior {

    private View layout;
    private AppBarLayout appBarLayout;


    public ListScrollBehavior() {
        super();
    }

    public ListScrollBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {

        if (appBarLayout == null) {
            appBarLayout = (AppBarLayout) dependency;
        }

        final boolean result = super.onDependentViewChanged(parent, child, dependency);
        final int bottomPadding = calculateBottomPadding(appBarLayout);
        final boolean paddingChanged = bottomPadding != child.getPaddingBottom();
        if (paddingChanged) {
            child.setPadding(
                    child.getPaddingLeft(),
                    child.getPaddingTop(),
                    child.getPaddingRight(),
                    bottomPadding);
            child.requestLayout();
        }
        return paddingChanged || result;
    }


    // Calculate the padding needed to keep the bottom of the view pager's content at the same location on the screen.
    private int calculateBottomPadding(AppBarLayout dependency) {
        final int totalScrollRange = dependency.getTotalScrollRange();
        return totalScrollRange + dependency.getTop();
    }

/*    public ListScrollBehavior() {
    }

    public ListScrollBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        boolean result = super.onDependentViewChanged(parent, child, dependency);
        if (layout != null) {
            layout.setPadding(layout.getPaddingLeft(), layout.getPaddingTop(), layout
                    .getPaddingRight(), layout.getTop());
        }
        return result;
    }

    public void setLayout(View layout) {
        this.layout = layout;
    }*/
}
