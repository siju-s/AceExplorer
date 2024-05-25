package com.siju.acexplorer.home.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.databinding.LibraryItemBinding
import com.siju.acexplorer.home.types.HomeLibraryInfo
import com.siju.acexplorer.main.model.groups.CategoryHelper

class HomeLibAdapter internal constructor(private val clickListener: (HomeLibraryInfo) -> Unit) :
        ListAdapter<HomeLibraryInfo, HomeLibAdapter.ViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LibraryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = getItem(position)
        viewHolder.bind(item, clickListener)
    }

    class ViewHolder(private val binding: LibraryItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HomeLibraryInfo, clickListener: (HomeLibraryInfo) -> Unit) {
            itemView.tag = item.category
            binding.imageLibrary.setImageResource(item.resourceId)
            binding.textLibrary.text = CategoryHelper.getCategoryName(itemView.context, item.category)
            binding.textCount.text = item.count.toString()
            itemView.setOnClickListener { clickListener(item) }
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<HomeLibraryInfo>() {
        override fun areItemsTheSame(oldItem: HomeLibraryInfo, newItem: HomeLibraryInfo) = oldItem.category == newItem.category

        override fun areContentsTheSame(oldItem: HomeLibraryInfo, newItem: HomeLibraryInfo): Boolean = oldItem == newItem

    }


}