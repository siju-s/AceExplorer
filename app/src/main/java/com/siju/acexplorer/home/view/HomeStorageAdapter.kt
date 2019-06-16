package com.siju.acexplorer.home.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.R
import com.siju.acexplorer.main.model.StorageItem
import com.siju.acexplorer.main.model.StorageUtils


class HomeStorageAdapter(private val clickListener: (StorageItem) -> Unit) : ListAdapter<StorageItem,
        HomeStorageAdapter.ViewHolder>(StorageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, position, itemCount, clickListener)
    }

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val image: ImageView = itemView.findViewById(R.id.imageStorage)
        private val storageNameText: TextView = itemView.findViewById(R.id.textStorageName)
        private val storageSpaceText: TextView = itemView.findViewById(R.id.textStorageSpace)
        private val spaceProgress: ProgressBar = itemView.findViewById(R.id.progressBarSD)
        private val storageDivider: View = itemView.findViewById<View>(R.id.home_storages_divider)

        fun bind(item: StorageItem, position: Int, size: Int, clickListener: (StorageItem) -> Unit) {
            image.setImageResource(item.icon)
            setStorageNameText(item)
            storageSpaceText.text = StorageUtils.getStorageSpaceText(itemView.context, item.secondLine)
            spaceProgress.progress = item.progress
            setDividerVisibility(size, position)
            itemView.tag = item.path

            itemView.setOnClickListener { clickListener(item) }
        }

        private fun setDividerVisibility(size: Int, position: Int) {
            if (position >= size - 1) {
                storageDivider.visibility = View.GONE
            } else {
                storageDivider.visibility = View.VISIBLE
            }
        }

        private fun setStorageNameText(storageItem: StorageItem) {
            val storageType = storageItem.storageType

            if (storageType == StorageUtils.StorageType.EXTERNAL) {
                storageNameText.text = storageItem.firstLine
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