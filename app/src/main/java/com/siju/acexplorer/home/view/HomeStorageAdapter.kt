package com.siju.acexplorer.home.view

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.R
import com.siju.acexplorer.databinding.StorageItemBinding
import com.siju.acexplorer.main.model.StorageItem
import com.siju.acexplorer.main.model.StorageUtils
import java.util.*


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
            setStorageNameText(binding.textStorageName, item)
            binding.textStorageSpace.text = item.secondLine
            val progress = item.progress
            binding.progressBarSD.progress = progress
            binding.textProgress.text = String.format(Locale.getDefault(), itemView.context.getString(R.string.storage_progress_percent), progress, progress)
            itemView.tag = item.path
            itemView.setOnClickListener { clickListener(item) }
        }

        private fun setStorageNameText(storageNameText: TextView, storageItem: StorageItem) {
            val storageType = storageItem.storageType

            if (storageType == StorageUtils.StorageType.EXTERNAL) {
                storageNameText.text = storageItem.name
            } else {
                storageNameText.text = StorageUtils.StorageType.getStorageText(itemView.context, storageType)
            }
        }
    }

    class StorageDiffCallback :
            DiffUtil.ItemCallback<StorageItem>() {
        override fun areItemsTheSame(oldItem: StorageItem, newItem: StorageItem) = oldItem.path == newItem.path

        override fun areContentsTheSame(oldItem: StorageItem, newItem: StorageItem) = oldItem == newItem
    }


}