package com.siju.acexplorer.home.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.R
import com.siju.acexplorer.databinding.StorageItemBinding
import com.siju.acexplorer.main.model.StorageItem
import com.siju.acexplorer.main.model.StorageUtils
import java.util.Locale


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

    class ViewHolder(private val binding: StorageItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StorageItem, clickListener: (StorageItem) -> Unit) {
            itemView.tag = item.path
            val context = binding.root.context
            binding.progressBarSD.progress = item.progress
            binding.textProgress.text = String.format(Locale.getDefault(), context.getString(R.string.storage_progress_percent), item.progress)
            binding.textStorageName.text = if (item.storageType == StorageUtils.StorageType.EXTERNAL) {
                item.name
            }
            else {
                StorageUtils.StorageType.getStorageText(context, item.storageType)
            }
            binding.textStorageSpace.text = item.secondLine
            itemView.setOnClickListener { clickListener(item) }
        }
    }

    class StorageDiffCallback :
            DiffUtil.ItemCallback<StorageItem>() {
        override fun areItemsTheSame(oldItem: StorageItem, newItem: StorageItem) = oldItem.path == newItem.path

        override fun areContentsTheSame(oldItem: StorageItem, newItem: StorageItem) = oldItem == newItem
    }


}