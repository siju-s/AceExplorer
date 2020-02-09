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

package com.siju.acexplorer.home.edit.view

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.R
import com.siju.acexplorer.home.edit.model.CategoryEditModelImpl
import com.siju.acexplorer.home.edit.model.CategoryEditType
import com.siju.acexplorer.home.model.CategoryEdit
import com.siju.acexplorer.storage.view.custom.helper.ItemTouchHelperAdapter
import java.util.*


class CategoryEditAdapter(private val selectedStateListener: (CategoryEditModelImpl.DataItem.Content, Int) -> Unit) :
        ListAdapter<CategoryEditModelImpl.DataItem, RecyclerView.ViewHolder>(CategoryDiffCallback()),
        ItemTouchHelperAdapter {

    private var data = arrayListOf<CategoryEditModelImpl.DataItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            EDIT_ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            EDIT_ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val item = getItem(position) as CategoryEditModelImpl.DataItem.Header
                holder.bind(item.headerType)
            }
            is ViewHolder -> {
                val item = getItem(position) as CategoryEditModelImpl.DataItem.Content
                Log.d("Adapter", "onBindViewHolder : pos:$position, headedrType:${item.categoryEdit.headerType}, checked: ${item.categoryEdit.checked}")
                holder.bind(item, selectedStateListener)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CategoryEditModelImpl.DataItem.Header -> EDIT_ITEM_VIEW_TYPE_HEADER
            is CategoryEditModelImpl.DataItem.Content -> EDIT_ITEM_VIEW_TYPE_ITEM
        }
    }

    override fun onItemDismiss(position: Int) {
        //do nothing
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(data, fromPosition, toPosition)
        Log.d("Adapter", "itemMove : from:$fromPosition, to:$toPosition")
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onMoved(fromPos: Int, toPos: Int) {
        val checkedCount = getCheckedItemCount()

        if (checkedCount in fromPos until toPos) {
            onMovedFromSavedToOther(fromPos, toPos)
        }
        else  if (checkedCount in toPos until fromPos) {
            onMovedFromOtherToSaved(fromPos, toPos)
        }
        Log.d("Adapter", "onMoved : from:$fromPos, to:$toPos")

    }

    private fun onMovedFromSavedToOther(fromPos: Int, toPos: Int) {
        val fromItem = data[fromPos]
        val toItem = data[toPos]

        fromItem as CategoryEditModelImpl.DataItem.Content
        fromItem.categoryEdit.checked = true
        fromItem.categoryEdit.headerType = CategoryEditType.SAVED
        data[fromPos] = fromItem

        toItem as CategoryEditModelImpl.DataItem.Content
        toItem.categoryEdit.checked = false
        toItem.categoryEdit.headerType = CategoryEditType.OTHER
        data[toPos] = toItem

        submitList(data)
        notifyItemRangeChanged(fromPos, toPos - fromPos + 1)
    }

    private fun onMovedFromOtherToSaved(fromPos: Int, toPos: Int) {
        val fromItem = data[fromPos]
        val toItem = data[toPos]

        fromItem as CategoryEditModelImpl.DataItem.Content
        fromItem.categoryEdit.checked = false
        fromItem.categoryEdit.headerType = CategoryEditType.OTHER
        data[fromPos] = fromItem

        toItem as CategoryEditModelImpl.DataItem.Content
        toItem.categoryEdit.checked = true
        toItem.categoryEdit.headerType = CategoryEditType.SAVED
        data[toPos] = toItem

        submitList(data)
        notifyItemRangeChanged(toPos, fromPos - toPos + 1)
    }

    fun getCheckedCategories(): ArrayList<Int> {
        val count = itemCount
        val selectedCategories = arrayListOf<Int>()
        for (index in 0 until count) {
            val item = getItem(index)
            if (item is CategoryEditModelImpl.DataItem.Content && item.categoryEdit.checked) {
                val category = item.categoryEdit.categoryId
                selectedCategories.add(category)
            }
        }
        return selectedCategories
    }

    fun getCheckedItemCount() : Int {
        val count = itemCount
        var checked = 0
        for (index in 0 until count) {
            val item = getItem(index)
            if (item is CategoryEditModelImpl.DataItem.Content && item.categoryEdit.checked) {
              checked++
            }
        }
        return checked
    }

    fun submitData(data: ArrayList<CategoryEditModelImpl.DataItem>) {
        this.data = data
        submitList(data)
    }

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val categoryText: TextView = itemView.findViewById(R.id.textLibrary)
        private val imageLibrary : ImageView = itemView.findViewById(R.id.imageLibrary)
        private val selectedState: ImageButton = itemView.findViewById(R.id.checkedState)

        fun bind(item: CategoryEditModelImpl.DataItem.Content, selectedStateListener: (CategoryEditModelImpl.DataItem.Content, Int) -> Unit) {
            val categoryEdit = item.categoryEdit
            categoryText.text = CategoryEdit.getCategoryName(itemView.context, categoryEdit.categoryId, categoryEdit.path)
            imageLibrary.setImageResource(CategoryEdit.getResourceIdForCategory(categoryEdit.categoryId,
                    categoryEdit.path))
            handleSelectedState(categoryEdit)
            initListeners(item, selectedStateListener)
        }

        private fun handleSelectedState(categoryEdit: CategoryEdit) {
            if (categoryEdit.checked) {
                selectedState.setImageResource(R.drawable.ic_remove_category)
            } else {
                selectedState.setImageResource(R.drawable.ic_add_category)
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun initListeners(categoryEdit: CategoryEditModelImpl.DataItem.Content,
                                  selectedStateListener: (CategoryEditModelImpl.DataItem.Content, Int) -> Unit) {
            selectedState.setOnClickListener {
//                val isChecked = categoryEdit.checked
//                categoryEdit.checked = !isChecked
//                handleSelectedState(categoryEdit)
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    selectedStateListener(categoryEdit, position)
                }
            }
        }


        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.category_edit_item, parent, false)
                return ViewHolder(view)
            }
        }
    }

    class HeaderViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var headerName: TextView = itemView.findViewById(R.id.headerNameText)

        fun bind(headerType: CategoryEditType) {
            headerName.text = CategoryEditType.getHeaderName(itemView.context, headerType)
        }

        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.category_edit_header,
                        parent, false)
                return HeaderViewHolder(view)
            }
        }
    }

    companion object {
        const val EDIT_ITEM_VIEW_TYPE_HEADER = 0
        const val EDIT_ITEM_VIEW_TYPE_ITEM = 1
    }
}

class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryEditModelImpl.DataItem>() {
    override fun areItemsTheSame(oldItem: CategoryEditModelImpl.DataItem,
                                 newItem: CategoryEditModelImpl.DataItem) = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: CategoryEditModelImpl.DataItem,
                                    newItem: CategoryEditModelImpl.DataItem): Boolean = oldItem.id == newItem.id

}
