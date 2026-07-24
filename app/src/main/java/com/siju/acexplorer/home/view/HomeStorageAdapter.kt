package com.siju.acexplorer.home.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.R
import com.siju.acexplorer.databinding.ItemSmbServerBinding
import com.siju.acexplorer.databinding.NetworkLocationItemBinding
import com.siju.acexplorer.databinding.StorageItemBinding
import com.siju.acexplorer.databinding.StorageSectionHeaderBinding
import com.siju.acexplorer.main.model.StorageItem
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.smb.SmbConnectionType
import com.siju.acexplorer.smb.SmbSavedServer
import java.util.Locale

private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_STORAGE = 1
private const val VIEW_TYPE_NETWORK_SERVER = 2
private const val VIEW_TYPE_ADD_NETWORK_LOCATION = 3

/** A row in the storage list - either a section title or a storage entry. */
sealed class StorageRow {
    data class Header(val title: String) : StorageRow()
    data class Item(val storageItem: StorageItem) : StorageRow()
    data class NetworkServer(val server: SmbSavedServer) : StorageRow()
    object AddNetworkLocation : StorageRow()
}

class HomeStorageAdapter(
    private val clickListener: (StorageItem) -> Unit,
    private val networkLocationsClickListener: () -> Unit,
    private val networkServerClickListener: (SmbSavedServer) -> Unit
) :
        ListAdapter<StorageRow, RecyclerView.ViewHolder>(StorageDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is StorageRow.Header -> VIEW_TYPE_HEADER
            is StorageRow.Item -> VIEW_TYPE_STORAGE
            is StorageRow.NetworkServer -> VIEW_TYPE_NETWORK_SERVER
            StorageRow.AddNetworkLocation -> VIEW_TYPE_ADD_NETWORK_LOCATION
        }
    }

    fun isFullWidth(position: Int) = getItem(position) !is StorageRow.Item

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(StorageSectionHeaderBinding.inflate(inflater, parent, false))
            VIEW_TYPE_NETWORK_SERVER -> NetworkServerViewHolder(
                ItemSmbServerBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_ADD_NETWORK_LOCATION -> NetworkLocationsViewHolder(
                NetworkLocationItemBinding.inflate(inflater, parent, false)
            )
            else -> StorageViewHolder(StorageItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = getItem(position)) {
            is StorageRow.Header -> (holder as HeaderViewHolder).bind(row)
            is StorageRow.Item -> (holder as StorageViewHolder).bind(row.storageItem, clickListener)
            is StorageRow.NetworkServer -> (holder as NetworkServerViewHolder)
                .bind(row.server, networkServerClickListener)
            StorageRow.AddNetworkLocation -> (holder as NetworkLocationsViewHolder)
                .bind(networkLocationsClickListener)
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

    class NetworkLocationsViewHolder(private val binding: NetworkLocationItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(clickListener: () -> Unit) {
            binding.root.setOnClickListener { clickListener() }
        }
    }

    class NetworkServerViewHolder(private val binding: ItemSmbServerBinding) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(server: SmbSavedServer, clickListener: (SmbSavedServer) -> Unit) = with(binding) {
            val hostName = server.username.takeIf { it.isNotBlank() }?.let { "$it@${server.host}" }
                ?: server.host
            host.text = if (server.connectionType == SmbConnectionType.LAN) {
                hostName
            } else {
                listOf(hostName, server.shareName).filter { it.isNotBlank() }.joinToString("/")
            }
            if (server.connectionType == SmbConnectionType.LAN) {
                share.setText(R.string.smb_lan_connection)
            } else {
                share.setText(R.string.smb_server)
            }
            root.setOnClickListener { clickListener(server) }
        }
    }

    class StorageDiffCallback : DiffUtil.ItemCallback<StorageRow>() {
        override fun areItemsTheSame(oldItem: StorageRow, newItem: StorageRow): Boolean {
            return when {
                oldItem is StorageRow.Header && newItem is StorageRow.Header ->
                    oldItem.title == newItem.title
                oldItem is StorageRow.Item && newItem is StorageRow.Item ->
                    oldItem.storageItem.path == newItem.storageItem.path
                oldItem is StorageRow.NetworkServer && newItem is StorageRow.NetworkServer ->
                    oldItem.server.host.equals(newItem.server.host, ignoreCase = true) &&
                        oldItem.server.connectionType == newItem.server.connectionType &&
                        (oldItem.server.connectionType == SmbConnectionType.LAN ||
                            oldItem.server.shareName == newItem.server.shareName)
                oldItem is StorageRow.AddNetworkLocation && newItem is StorageRow.AddNetworkLocation -> true
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: StorageRow, newItem: StorageRow) = oldItem == newItem
    }
}
