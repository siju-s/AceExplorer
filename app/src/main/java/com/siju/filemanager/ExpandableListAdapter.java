package com.siju.filemanager;

/**
 * Created by Siju on 11-06-2016.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.siju.filemanager.model.SectionGroup;
import com.siju.filemanager.model.SectionItems;

import java.util.ArrayList;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    //    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
//    private HashMap<String, List<String>> _listDataChild;
    private ArrayList<SectionGroup> groups;

//    public ExpandableListAdapter(Context context, List<String> listDataHeader,
//                                 HashMap<String, List<String>> listChildData) {
//        this._context = context;
//        this._listDataHeader = listDataHeader;
//        this._listDataChild = listChildData;
//    }

    public ExpandableListAdapter(Context context, ArrayList<SectionGroup> groups) {
        this.mContext = context;
        this.groups = groups;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList<SectionItems> chList = groups.get(groupPosition).getmChildItems();
        return chList.get(childPosition);
//        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
//                .get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

//        final String childFirstLineText = (String) getChild(groupPosition, childPosition);

        SectionItems child = (SectionItems) getChild(groupPosition, childPosition);

        View view = convertView;
        ChildViewHolder childViewHolder;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.drawer_item, null);
            childViewHolder = new ChildViewHolder();
            childViewHolder.image = (ImageView) view.findViewById(R.id.image_drawer_item);
            childViewHolder.textFirstLine = (TextView) view.findViewById(R.id.textFirstLine);
            childViewHolder.textSecondLine = (TextView) view.findViewById(R.id.textSecondLine);
            view.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) view.getTag();
        }

        childViewHolder.image.setBackgroundResource(child.getmImage());
        childViewHolder.textFirstLine.setText(child.getmFirstLine());
        if (groupPosition == 0) {
            childViewHolder.textSecondLine.setText(child.getmSecondLine());
        }
//        else {
//            RelativeLayout.LayoutParams layoutParams =
//                    (RelativeLayout.LayoutParams) childViewHolder.textSecondLine.getLayoutParams();
//            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
//            childViewHolder.textFirstLine.setLayoutParams(layoutParams);
//        }
        return view;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
//        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
//                .size();
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
            holder.imageArrow.setImageResource(R.drawable.ic_expand_less_black);
        } else {
            holder.imageArrow.setImageResource(R.drawable.ic_expand_more_black);
        }


        holder.textHeader.setTypeface(null, Typeface.BOLD);
        holder.textHeader.setText(group.getmHeader());

        return view;
    }


    static class GroupViewHolder {
        TextView textHeader;
        ImageView imageArrow;
    }

    static class ChildViewHolder {
        ImageView image;
        TextView textFirstLine;
        TextView textSecondLine;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}