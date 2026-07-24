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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.R
import com.siju.acexplorer.databinding.ItemSmbServerBinding

class SmbServerAdapter(
    private val onServerClicked: (SmbSavedServer) -> Unit
) : ListAdapter<SmbSavedServer, SmbServerAdapter.SmbServerViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmbServerViewHolder {
        val binding = ItemSmbServerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SmbServerViewHolder(binding, onServerClicked)
    }

    override fun onBindViewHolder(holder: SmbServerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SmbServerViewHolder(
        private val binding: ItemSmbServerBinding,
        private val onServerClicked: (SmbSavedServer) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(server: SmbSavedServer) = with(binding) {
            host.text = server.host
            if (server.connectionType == SmbConnectionType.LAN) {
                share.setText(R.string.smb_lan_connection)
            } else {
                share.text = server.shareName
            }
            root.setOnClickListener { onServerClicked(server) }
        }
    }

    private companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<SmbSavedServer>() {
            override fun areItemsTheSame(oldItem: SmbSavedServer, newItem: SmbSavedServer): Boolean =
                oldItem.host.equals(newItem.host, ignoreCase = true) &&
                    oldItem.connectionType == newItem.connectionType &&
                    (oldItem.connectionType == SmbConnectionType.LAN || oldItem.shareName == newItem.shareName)

            override fun areContentsTheSame(oldItem: SmbSavedServer, newItem: SmbSavedServer): Boolean =
                oldItem == newItem
        }
    }
}
