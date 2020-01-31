package com.siju.acexplorer.search.view

import android.content.Context
import android.text.format.Formatter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.main.model.groups.CategoryHelper.getCategoryName
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.search.model.SearchDataFetcher
import com.siju.acexplorer.search.model.SearchHeaderType
import com.siju.acexplorer.storage.view.INVALID_POS
import com.siju.acexplorer.utils.ThumbnailUtils

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class SearchAdapter(private val clickListener: (Pair<FileInfo, Int>) -> Unit) : ListAdapter<SearchDataFetcher.SearchDataItem, RecyclerView.ViewHolder>(SearchDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ItemViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.e("SearchAdapter", "onBindViewHolder $position, ${holder is HeaderViewHolder}")
        when (holder) {
            is HeaderViewHolder -> {
                val item = getItem(position) as SearchDataFetcher.SearchDataItem.Header
                holder.bind(item.headerType, item.count)
            }
            is ItemViewHolder -> {
                val item = getItem(position) as SearchDataFetcher.SearchDataItem.Item
                holder.bind(item.fileInfo, clickListener)
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SearchDataFetcher.SearchDataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is SearchDataFetcher.SearchDataItem.Item -> ITEM_VIEW_TYPE_ITEM
        }
    }

    fun addHeaderAndSubmitList(searchData: ArrayList<SearchDataFetcher.SearchDataItem>) {
        submitList(searchData)
    }


    class ItemViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textFileName: TextView = itemView.findViewById(R.id.textFolderName)
        private val textNoOfFileOrSize: TextView = itemView.findViewById(R.id.textSecondLine)
        private val imageIcon: ImageView = itemView.findViewById(R.id.imageIcon)
        private var dateText: TextView = itemView.findViewById(R.id.textDate)
        private val imageThumbIcon: ImageView = itemView.findViewById(R.id.imageThumbIcon)

        fun bind(item: FileInfo?, clickListener: (Pair<FileInfo, Int>) -> Unit) {
            item?.let {
                bindViewByCategory(itemView.context, it)
                itemView.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        clickListener(Pair(item, position))
                    }
                }
            }
        }

        private fun bindViewByCategory(context: Context, fileInfo: FileInfo) {
            val category = fileInfo.category
            when {
                CategoryHelper.isGenericMusic(category) -> bindGenericMusic(context, fileInfo)
                CategoryHelper.isMusicCategory(category) -> bindMusicCategory(context, fileInfo)
                CategoryHelper.isGenericImagesCategory(category) -> bindGenericImagesVidsCategory(context,
                        fileInfo)
                CategoryHelper.isGenericVideosCategory(category) -> bindGenericImagesVidsCategory(context,
                        fileInfo)
                else -> {
                    bindFilesCategory(fileInfo, category, context)
                }
            }
        }

        private fun bindFilesCategory(fileInfo: FileInfo,
                                      category: Category?,
                                      context: Context) {
            val fileName = fileInfo.fileName
            category?.let { bindDate(it, fileInfo) }

            val isDirectory = fileInfo.isDirectory
            val fileNumOrSize: String
            fileNumOrSize = if (isDirectory) {
                getDirectoryFileCount(context, fileInfo)
            } else {
                Formatter.formatFileSize(context, fileInfo.size)
            }
            textFileName.text = fileName
            textNoOfFileOrSize.text = fileNumOrSize
            ThumbnailUtils.displayThumb(context, fileInfo, category, imageIcon,
                    imageThumbIcon)
        }

        private fun bindDate(category: Category,
                             fileInfo: FileInfo) {
            val dateMs = if (CategoryHelper.isDateInMs(category)) {
                fileInfo.date
            } else {
                fileInfo.date * 1000
            }
            dateText.text = FileUtils.convertDate(dateMs)
        }

        private fun getDirectoryFileCount(context: Context,
                                          fileInfo: FileInfo): String {
            return if (fileInfo.isRootMode) {
                context.getString(R.string.directory)
            } else {
                return when (val childFileListSize = fileInfo.size.toInt()) {
                    0 -> context.resources.getString(R.string.empty)
                    -1 -> ""
                    else -> context.resources.getQuantityString(R.plurals
                            .number_of_files,
                            childFileListSize,
                            childFileListSize)
                }
            }
        }

        private fun bindGenericMusic(context: Context, fileInfo: FileInfo) {
            val count = fileInfo.count
            val files = context.resources.getQuantityString(R.plurals.number_of_files,
                    count, count)
            textFileName.text = getCategoryName(context, fileInfo.subcategory)
            textNoOfFileOrSize.text = files
            imageIcon.setImageResource(R.drawable.ic_folder)
        }

        private fun bindMusicCategory(context: Context, fileInfo: FileInfo) {
            textFileName.text = fileInfo.title
            val num = fileInfo.numTracks.toInt()
            if (num != INVALID_POS) {
                val files = context.resources.getQuantityString(R.plurals.number_of_files,
                        num, num)
                textNoOfFileOrSize.text = files
            }
            ThumbnailUtils.displayThumb(context, fileInfo, fileInfo.category, imageIcon,
                    imageThumbIcon)
        }

        private fun bindGenericImagesVidsCategory(context: Context, fileInfo: FileInfo) {

            textFileName.text = fileInfo.fileName
            val num = fileInfo.numTracks.toInt()
            if (num != INVALID_POS) {
                val files = context.resources.getQuantityString(R.plurals.number_of_files,
                        num, num)
                textNoOfFileOrSize.text = files
            }
            ThumbnailUtils.displayThumb(context, fileInfo, fileInfo.category, imageIcon,
                    imageThumbIcon)
        }


        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.search_item,
                        parent, false)
                return ItemViewHolder(view)
            }
        }
    }

    class HeaderViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var headerName: TextView = itemView.findViewById(R.id.headerNameText)
        private var textCount: TextView = itemView.findViewById(R.id.textCount)

        fun bind(headerType: Int, count : Int) {
            headerName.text = SearchHeaderType.getHeaderName(itemView.context, headerType)
            textCount.text = itemView.context.resources.getQuantityString(R.plurals.number_of_files, count, count)
        }

        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.search_header,
                        parent, false)
                return HeaderViewHolder(view)
            }
        }
    }

}
class SearchDiffCallback : DiffUtil.ItemCallback<SearchDataFetcher.SearchDataItem>() {
    override fun areItemsTheSame(oldItem: SearchDataFetcher.SearchDataItem, newItem: SearchDataFetcher.SearchDataItem) = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: SearchDataFetcher.SearchDataItem, newItem: SearchDataFetcher.SearchDataItem): Boolean = oldItem.id == newItem.id

}


