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

package com.siju.acexplorer;


import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.siju.acexplorer.filesystem.theme.ThemeUtils;
import com.siju.acexplorer.model.SectionGroup;
import com.siju.acexplorer.model.SectionItems;

import java.util.ArrayList;

class ExpandableListAdapter extends BaseExpandableListAdapter {

    private final Context mContext;
    private final ArrayList<SectionGroup> groups;
    private final boolean mIsDarkTheme;


    ExpandableListAdapter(Context context, ArrayList<SectionGroup> groups) {
        this.mContext = context;
        this.groups = groups;
        mIsDarkTheme = ThemeUtils.isDarkTheme(context);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList<SectionItems> chList = groups.get(groupPosition).getmChildItems();
        return chList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        SectionItems child = (SectionItems) getChild(groupPosition, childPosition);

        View view = convertView;
        ChildViewHolder childViewHolder;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.drawer_item, parent, false);
            childViewHolder = new ChildViewHolder();
            childViewHolder.image = (ImageView) view.findViewById(R.id.imageDrawerItem);
            childViewHolder.textFirstLine = (TextView) view.findViewById(R.id.textFirstLine);
            childViewHolder.textSecondLine = (TextView) view.findViewById(R.id.textSecondLine);
            childViewHolder.progressBar = (ProgressBar) view.findViewById(R.id.progressBarSD);
            view.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) view.getTag();
        }

        childViewHolder.image.setBackgroundResource(child.getIcon());
        childViewHolder.textFirstLine.setText(child.getFirstLine());

        if (groupPosition == 0 || groupPosition == 1) {
            if (groupPosition == 0) {
                childViewHolder.progressBar.setVisibility(View.VISIBLE);
                childViewHolder.progressBar.setProgress(child.getProgress());
            } else {
                childViewHolder.progressBar.setVisibility(View.GONE);
            }
            childViewHolder.textSecondLine.setText(child.getSecondLine());
        } else {
            childViewHolder.progressBar.setVisibility(View.GONE);
            childViewHolder.textFirstLine.setPadding(0,mContext.getResources().getDimensionPixelSize(R.dimen
                    .padding_5),0,0);
            childViewHolder.textSecondLine.setText("");
        }
        return view;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<SectionItems> chList = groups.get(groupPosition).getmChildItems();
        return chList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        SectionGroup group = (SectionGroup) getGroup(groupPosition);

        View view = convertView;
        GroupViewHolder holder;
        if (view == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.drawer_item_header, parent, false);
            holder = new GroupViewHolder();
            holder.textHeader = (TextView) view.findViewById(R.id.textDrawerHeaders);
            holder.imageArrow = (ImageView) view.findViewById(R.id.imageExpand);
            view.setTag(holder);
        } else {
            holder = (GroupViewHolder) view.getTag();
        }

        if (isExpanded) {
            if (mIsDarkTheme) {
                holder.imageArrow.setImageResource(R.drawable.ic_expand_less_white);
            } else {
                holder.imageArrow.setImageResource(R.drawable.ic_expand_less_black);
            }
        } else {
            if (mIsDarkTheme) {
                holder.imageArrow.setImageResource(R.drawable.ic_expand_more_white);
            } else {
                holder.imageArrow.setImageResource(R.drawable.ic_expand_more_black);
            }
        }
        holder.textHeader.setTypeface(null, Typeface.BOLD);
        holder.textHeader.setText(group.getmHeader());

        return view;
    }


    private static class GroupViewHolder {
        TextView textHeader;
        ImageView imageArrow;
    }

    private static class ChildViewHolder {
        ImageView image;
        TextView textFirstLine;
        TextView textSecondLine;
        ProgressBar progressBar;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    /**
     * To make Child clickable
     *
     * @param groupPosition
     * @param childPosition
     * @return True to make child clickable
     */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}