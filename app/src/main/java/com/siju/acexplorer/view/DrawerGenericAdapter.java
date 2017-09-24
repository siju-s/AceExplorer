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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.siju.acexplorer.model.Generic;
import com.siju.acexplorer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Siju on 28 August,2017
 */
public class DrawerGenericAdapter extends BaseAdapter {

    private Context context;
    private List<Generic> generics = new ArrayList<>();

    DrawerGenericAdapter(Context context) {
        this.context = context;
        addGenericList();
    }


    private void addGenericList() {
        generics.add(new Generic(R.drawable.ic_unlock_full, context.getString(R.string.unlock_full_version)));
        generics.add(new Generic(R.drawable.ic_rate_white, context.getString(R.string.rate_us)));
        generics.add(new Generic(R.drawable.ic_settings_white, context.getString(R.string.action_settings)));
    }

    void setPremium() {
        generics.remove(0);
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return generics.size();
    }

    @Override
    public Object getItem(int position) {
        return generics.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.drawer_generic_item, parent, false);
            holder = new ViewHolder();
            holder.text = view.findViewById(R.id.textDrawerItem);
            holder.image = view.findViewById(R.id.imageDrawerItem);
            view.setTag(holder);
        }
        holder = (ViewHolder) view.getTag();
        holder.text.setText(generics.get(position).getText());
        holder.image.setImageResource(generics.get(position).getResourceId());

        return view;
    }

    private static class ViewHolder {

        private TextView text;
        private ImageView image;

    }
}