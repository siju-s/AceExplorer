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

package com.siju.acexplorer.home;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.siju.acexplorer.R;
import com.siju.acexplorer.home.model.LibrarySortModel;
import com.siju.acexplorer.storage.view.custom.helper.ItemTouchHelperAdapter;
import com.siju.acexplorer.storage.view.custom.helper.ItemTouchHelperViewHolder;

import java.util.ArrayList;
import java.util.Collections;


class LibrarySortAdapter extends RecyclerView.Adapter<LibrarySortAdapter.LibrarySortViewHolder>
        implements ItemTouchHelperAdapter {

    private final OnStartDragListener mDragStartListener;
    private ArrayList<LibrarySortModel> totalLibraries = new ArrayList<>();


    LibrarySortAdapter(OnStartDragListener dragStartListener,
                       ArrayList<LibrarySortModel> models) {

        mDragStartListener = dragStartListener;
        totalLibraries = models;
    }

    @Override
    public LibrarySortViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.library_sort_item,
                parent, false);
        return new LibrarySortViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final LibrarySortViewHolder librarySortViewHolder, final int position) {
        //change background color if list item is selected
        final LibrarySortModel model = totalLibraries.get(position);

        librarySortViewHolder.textLibrary.setText(model.getLibraryName());

        librarySortViewHolder.imageSort.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(librarySortViewHolder);
                }
                return false;
            }
        });

        //if true, your checkbox will be selected, else unselected
        librarySortViewHolder.checkBox.setChecked(model.isChecked());

        librarySortViewHolder.textLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean isChecked = model.isChecked();
                model.setChecked(!isChecked);
                librarySortViewHolder.checkBox.setChecked(!isChecked);
            }
        });

        librarySortViewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                model.setChecked(isChecked);
            }
        });


    }


    @Override
    public int getItemCount() {

        if (totalLibraries == null) {
            return 0;
        } else {
            return totalLibraries.size();
        }
    }

    @Override
    public void onItemDismiss(int position) {
        totalLibraries.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(totalLibraries, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    class LibrarySortViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder {
        final ImageView imageSort;
        final TextView textLibrary;
        final CheckBox checkBox;


        LibrarySortViewHolder(View itemView) {
            super(itemView);
            textLibrary = (TextView) itemView
                    .findViewById(R.id.textLibrary);
            imageSort = (ImageView) itemView.findViewById(R.id.imageSort);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

        }

    }


}
