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

class SmbNearbyServerAdapter(
    private val onServerClicked: (SmbNearbyServer) -> Unit
) : ListAdapter<SmbNearbyServer, SmbNearbyServerAdapter.SmbNearbyServerViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmbNearbyServerViewHolder {
        val binding = ItemSmbServerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SmbNearbyServerViewHolder(binding, onServerClicked)
    }

    override fun onBindViewHolder(holder: SmbNearbyServerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SmbNearbyServerViewHolder(
        private val binding: ItemSmbServerBinding,
        private val onServerClicked: (SmbNearbyServer) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(server: SmbNearbyServer) = with(binding) {
            host.text = server.host
            share.setText(R.string.smb_nearby_server)
            root.setOnClickListener { onServerClicked(server) }
        }
    }

    private companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<SmbNearbyServer>() {
            override fun areItemsTheSame(oldItem: SmbNearbyServer, newItem: SmbNearbyServer): Boolean =
                oldItem.host == newItem.host

            override fun areContentsTheSame(oldItem: SmbNearbyServer, newItem: SmbNearbyServer): Boolean =
                oldItem == newItem
        }
    }
}
