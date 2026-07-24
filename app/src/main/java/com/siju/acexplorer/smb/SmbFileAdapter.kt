/*
 * Copyright (C) 2026 Ace Explorer owned by Siju Sakaria
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

package com.siju.acexplorer.smb

import android.text.format.DateFormat
import android.text.format.Formatter
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.siju.acexplorer.R
import com.siju.acexplorer.databinding.ItemSmbFileBinding
import java.util.Date

class SmbFileAdapter(
    private val onItemClicked: (SmbEntry) -> Unit
) : ListAdapter<SmbEntry, SmbFileAdapter.SmbFileViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmbFileViewHolder {
        val binding = ItemSmbFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SmbFileViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(holder: SmbFileViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SmbFileViewHolder(
        private val binding: ItemSmbFileBinding,
        private val onItemClicked: (SmbEntry) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: SmbEntry) = with(binding) {
            name.text = entry.name
            detail.text = if (entry.isDirectory) {
                root.context.getString(R.string.smb_folder)
            } else {
                Formatter.formatFileSize(root.context, entry.size)
            }
            modified.visibility = if (entry.modifiedAt == 0L) View.GONE else View.VISIBLE
            modified.text = DateFormat.getMediumDateFormat(root.context).format(Date(entry.modifiedAt))
            Glide.with(icon).clear(icon)
            when {
                entry.isDirectory -> icon.setImageResource(R.drawable.ic_folder)
                entry.thumbnailPath != null -> Glide.with(icon)
                    .load(entry.thumbnailPath)
                    .centerCrop()
                    .placeholder(R.drawable.ic_image_default)
                    .into(icon)
                entry.isVideo() -> icon.setImageResource(R.drawable.ic_movie)
                else -> icon.setImageResource(R.drawable.ic_doc)
            }
            root.setOnClickListener { onItemClicked(entry) }
        }

        private fun SmbEntry.isVideo(): Boolean = name.substringAfterLast('.', "")
            .lowercase() in VIDEO_EXTENSIONS

        private companion object {
            val VIDEO_EXTENSIONS = setOf("3gp", "avi", "mkv", "mov", "mp4", "mpeg", "webm")
        }
    }

    private companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<SmbEntry>() {
            override fun areItemsTheSame(oldItem: SmbEntry, newItem: SmbEntry): Boolean =
                oldItem.path == newItem.path

            override fun areContentsTheSame(oldItem: SmbEntry, newItem: SmbEntry): Boolean =
                oldItem == newItem
        }
    }
}
