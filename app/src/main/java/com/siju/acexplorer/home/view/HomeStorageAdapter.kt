package com.siju.acexplorer.home.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.R
import com.siju.acexplorer.main.model.StorageItem
import com.siju.acexplorer.main.model.StorageUtils
import java.util.*


class HomeStorageAdapter(private val clickListener: (StorageItem) -> Unit) : ListAdapter<StorageItem,
        HomeStorageAdapter.ViewHolder>(StorageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val storageProgressText: TextView = itemView.findViewById(R.id.textProgress)
        private val storageNameText: TextView = itemView.findViewById(R.id.textStorageName)
        private val storageSpaceText: TextView = itemView.findViewById(R.id.textStorageSpace)
        private val spaceProgress: ProgressBar = itemView.findViewById(R.id.progressBarSD)

        fun bind(item: StorageItem, clickListener: (StorageItem) -> Unit) {
            setStorageNameText(item)
            storageSpaceText.text = item.secondLine
            val progress = item.progress
            spaceProgress.progress = progress
            storageProgressText.text = String.format(Locale.getDefault(), itemView.context.
                    getString(R.string.storage_progress_percent), progress, progress)
            itemView.tag = item.path
            itemView.setOnClickListener { clickListener(item) }
        }


        private fun setStorageNameText(storageItem: StorageItem) {
            val storageType = storageItem.storageType

            if (storageType == StorageUtils.StorageType.EXTERNAL) {
                storageNameText.text = storageItem.name
            } else {
                storageNameText.text = StorageUtils.StorageType.getStorageText(itemView.context, storageType)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.storage_item,
                        parent, false)
                return ViewHolder(view)
            }
        }

    }

    class StorageDiffCallback :
            DiffUtil.ItemCallback<StorageItem>() {
        override fun areItemsTheSame(oldItem: StorageItem, newItem: StorageItem) = oldItem.path == newItem.path

        override fun areContentsTheSame(oldItem: StorageItem, newItem: StorageItem) = oldItem == newItem
    }


}