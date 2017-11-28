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

package com.siju.acexplorer.view;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.siju.acexplorer.R;
import com.siju.acexplorer.model.SectionGroup;
import com.siju.acexplorer.model.SectionItems;
import com.siju.acexplorer.model.StorageUtils;
import com.siju.acexplorer.model.groups.Category;
import com.siju.acexplorer.model.groups.DrawerGroup;
import com.siju.acexplorer.theme.ThemeUtils;

import java.io.File;
import java.util.ArrayList;

import static com.siju.acexplorer.model.StorageUtils.StorageType.EXTERNAL;
import static com.siju.acexplorer.model.StorageUtils.getStorageSpaceText;
import static com.siju.acexplorer.view.NavigationDrawer.DRAWER_HEADER_FAV_POS;
import static com.siju.acexplorer.view.NavigationDrawer.DRAWER_HEADER_STORAGE_POS;

class ExpandableListAdapter extends BaseExpandableListAdapter {

    private final Context                 context;
    private final ArrayList<SectionGroup> groups;
    private final boolean                 isDarkTheme;


    ExpandableListAdapter(Context context, ArrayList<SectionGroup> groups) {
        this.context = context;
        this.groups = groups;
        isDarkTheme = ThemeUtils.isDarkTheme(context);
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
            view = LayoutInflater.from(context).inflate(R.layout.drawer_item, parent, false);
            childViewHolder = new ChildViewHolder();
            childViewHolder.image = view.findViewById(R.id.imageDrawerItem);
            childViewHolder.textFirstLine = view.findViewById(R.id.textFirstLine);
            childViewHolder.textSecondLine = view.findViewById(R.id.textSecondLine);
            childViewHolder.progressBar = view.findViewById(R.id.progressBarSD);
            view.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) view.getTag();
        }

        Category category = child.getCategory();
        StorageUtils.StorageType storageType = child.getStorageType();
        String text;
        if (storageType != null && !storageType.equals(EXTERNAL)) {
            text = StorageUtils.StorageType.getStorageText(context, storageType);
        } else if (category != null) {
            if (category.equals(Category.FAVORITES)) {
                text = new File(child.getSecondLine()).getName();
            } else {
                text = Category.getCategoryName(context, category);
            }
        } else {
            text = child.getFirstLine();
        }
        childViewHolder.image.setBackgroundResource(child.getIcon());
        childViewHolder.image.setContentDescription(text);
        childViewHolder.textFirstLine.setText(text);

        if (groupPosition == DRAWER_HEADER_STORAGE_POS) {
            childViewHolder.progressBar.setVisibility(View.VISIBLE);
            childViewHolder.progressBar.setProgress(child.getProgress());
            childViewHolder.textSecondLine.setText(getStorageSpaceText(context, child.getSecondLine()));

        } else if (groupPosition == DRAWER_HEADER_FAV_POS) {
            childViewHolder.progressBar.setVisibility(View.GONE);
            childViewHolder.textSecondLine.setText(child.getSecondLine());

        } else {
            childViewHolder.progressBar.setVisibility(View.GONE);
            childViewHolder.textFirstLine.setPadding(0, context.getResources().
                    getDimensionPixelSize(R.dimen.padding_5), 0, 0);
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
            view = LayoutInflater.from(context).inflate(R.layout.drawer_item_header, parent, false);
            holder = new GroupViewHolder();
            holder.textHeader = view.findViewById(R.id.textDrawerHeaders);
            holder.imageArrow = view.findViewById(R.id.imageExpand);
            view.setTag(holder);
        } else {
            holder = (GroupViewHolder) view.getTag();
        }

        if (isExpanded) {
            if (isDarkTheme) {
                holder.imageArrow.setImageResource(R.drawable.ic_expand_less_white);
            } else {
                holder.imageArrow.setImageResource(R.drawable.ic_expand_less_black);
            }
        } else {
            if (isDarkTheme) {
                holder.imageArrow.setImageResource(R.drawable.ic_expand_more_white);
            } else {
                holder.imageArrow.setImageResource(R.drawable.ic_expand_more_black);
            }
        }
        holder.textHeader.setText(DrawerGroup.getDrawerGroupName(context, group.getGroups()));

        return view;
    }


    private static class GroupViewHolder {
        TextView  textHeader;
        ImageView imageArrow;
    }

    private static class ChildViewHolder {
        ImageView   image;
        TextView    textFirstLine;
        TextView    textSecondLine;
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