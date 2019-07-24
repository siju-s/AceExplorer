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
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.R
import com.siju.acexplorer.home.model.CategoryEdit
import com.siju.acexplorer.main.model.groups.CategoryHelper.getCategory
import com.siju.acexplorer.main.model.groups.CategoryHelper.getCategoryName
import com.siju.acexplorer.storage.view.custom.helper.ItemTouchHelperAdapter
import java.util.*

class CategoryEditAdapter(private val dragStartListener: OnStartDragListener) :
        ListAdapter<CategoryEdit, CategoryEditAdapter.ViewHolder>(CategoryDiffCallback()),
        ItemTouchHelperAdapter
{
    private lateinit var data : List<CategoryEdit>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), dragStartListener)
    }

    override fun onItemDismiss(position: Int) {
        //do nothing
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(data, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun getCheckedCategories() : ArrayList<Int> {
        val count = itemCount
        val selectedCategories = arrayListOf<Int>()
        for (index in 0 until count) {
            val item = data[index]
            if (item.checked) {
                val category = item.categoryId
                selectedCategories.add(category)
            }
        }
        return selectedCategories
    }

    fun submitData(data : List<CategoryEdit>) {
        this.data = data
        submitList(data)
    }

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sortImage: ImageView = itemView.findViewById(R.id.imageSort)
        private val categoryText: TextView = itemView.findViewById(R.id.textLibrary)
        private val selectedState: CheckBox = itemView.findViewById(R.id.checkbox)

        fun bind(categoryEdit: CategoryEdit, dragStartListener: OnStartDragListener) {
            val category = getCategory(categoryEdit.categoryId)
            categoryText.text = getCategoryName(itemView.context, category)
            selectedState.isChecked = categoryEdit.checked
            initListeners(categoryEdit, dragStartListener)
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun initListeners(categoryEdit: CategoryEdit,
                                  dragStartListener: OnStartDragListener) {
            selectedState.setOnCheckedChangeListener { _, isChecked ->
                categoryEdit.checked = isChecked
            }
            categoryText.setOnClickListener {
                val isChecked = categoryEdit.checked
                categoryEdit.checked = !isChecked
                selectedState.isChecked = !isChecked
            }
            sortImage.setOnTouchListener { view, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    dragStartListener.onStartDrag(this)
                }
                if (event.actionMasked == MotionEvent.ACTION_UP) {
                    view.performClick()
                }
                false
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
}

class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryEdit>() {
    override fun areItemsTheSame(oldItem: CategoryEdit,
                                 newItem: CategoryEdit) = oldItem.categoryId == newItem.categoryId

    override fun areContentsTheSame(oldItem: CategoryEdit,
                                    newItem: CategoryEdit): Boolean = oldItem == newItem

}
