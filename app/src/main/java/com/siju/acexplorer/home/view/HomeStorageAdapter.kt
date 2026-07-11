package com.siju.acexplorer.home.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.R
import com.siju.acexplorer.databinding.StorageItemBinding
import com.siju.acexplorer.databinding.StorageSectionHeaderBinding
import com.siju.acexplorer.main.model.StorageItem
import com.siju.acexplorer.main.model.StorageUtils
import java.util.Locale

private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_STORAGE = 1

/** A row in the storage list - either a section title or a storage entry. */
sealed class StorageRow {
    data class Header(val title: String) : StorageRow()
    data class Item(val storageItem: StorageItem) : StorageRow()
}

class HomeStorageAdapter(private val clickListener: (StorageItem) -> Unit) :
        ListAdapter<StorageRow, RecyclerView.ViewHolder>(StorageDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is StorageRow.Header -> VIEW_TYPE_HEADER
            is StorageRow.Item -> VIEW_TYPE_STORAGE
        }
    }

    fun isHeader(position: Int) = getItem(position) is StorageRow.Header

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_HEADER) {
            HeaderViewHolder(StorageSectionHeaderBinding.inflate(inflater, parent, false))
        }
        else {
            StorageViewHolder(StorageItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = getItem(position)) {
            is StorageRow.Header -> (holder as HeaderViewHolder).bind(row)
            is StorageRow.Item -> (holder as StorageViewHolder).bind(row.storageItem, clickListener)
        }
    }

    class HeaderViewHolder(private val binding: StorageSectionHeaderBinding) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(header: StorageRow.Header) {
            binding.textSectionTitle.text = header.title
        }
    }

    class StorageViewHolder(private val binding: StorageItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StorageItem, clickListener: (StorageItem) -> Unit) {
            itemView.tag = item.path
            val context = binding.root.context
            binding.progressBarSD.progress = item.progress
            binding.textProgress.text = String.format(Locale.getDefault(),
                    context.getString(R.string.storage_progress_percent), item.progress)
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

    class StorageDiffCallback : DiffUtil.ItemCallback<StorageRow>() {
        override fun areItemsTheSame(oldItem: StorageRow, newItem: StorageRow): Boolean {
            return when {
                oldItem is StorageRow.Header && newItem is StorageRow.Header ->
                    oldItem.title == newItem.title
                oldItem is StorageRow.Item && newItem is StorageRow.Item ->
                    oldItem.storageItem.path == newItem.storageItem.path
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: StorageRow, newItem: StorageRow) = oldItem == newItem
    }
}
