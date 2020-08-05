package com.siju.acexplorer.home.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.databinding.StorageItemBinding
import com.siju.acexplorer.main.model.StorageItem


class HomeStorageAdapter(private val clickListener: (StorageItem) -> Unit) : ListAdapter<StorageItem,
        HomeStorageAdapter.ViewHolder>(StorageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = StorageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    class ViewHolder constructor(private val binding: StorageItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StorageItem, clickListener: (StorageItem) -> Unit) {
            binding.item = item
            itemView.tag = item.path
            itemView.setOnClickListener { clickListener(item) }
            binding.executePendingBindings()
        }
    }

    class StorageDiffCallback :
            DiffUtil.ItemCallback<StorageItem>() {
        override fun areItemsTheSame(oldItem: StorageItem, newItem: StorageItem) = oldItem.path == newItem.path

        override fun areContentsTheSame(oldItem: StorageItem, newItem: StorageItem) = oldItem == newItem
    }


}