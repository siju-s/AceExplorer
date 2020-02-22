package com.siju.acexplorer.storage.view

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.main.model.groups.CategoryHelper.getCategoryName
import com.siju.acexplorer.storage.model.RecentTimeData
import com.siju.acexplorer.storage.model.ViewMode
import com.siju.acexplorer.theme.Theme
import com.siju.acexplorer.ui.peekandpop.PeekPopView
import com.siju.acexplorer.utils.ThumbnailUtils


class RecentAdapter(var viewMode: ViewMode, private val clickListener: (Pair<FileInfo, Int>) -> Unit,
                    private val longClickListener: (FileInfo, Int, View) -> Unit,
                    private val imageClickListener: (Int, Boolean) -> Unit,
                    private val peekPopView: PeekPopView?) : ListAdapter<RecentTimeData.RecentDataItem,
        RecyclerView.ViewHolder>(RecentDiffCallback()), BaseListAdapter {

    init {
        peekPopView?.initPeekPopListener()
    }

    private var multiSelectionHelper: MultiSelectionHelper? = null

    override fun setMultiSelectionHelper(multiSelectionHelper: MultiSelectionHelper) {
        Log.d("REcentAdapter", "setMultiSelectionHelper:$multiSelectionHelper, instance:$this")
        this.multiSelectionHelper = multiSelectionHelper
    }

    override fun getMultiSelectionHelper(): MultiSelectionHelper? {
        return multiSelectionHelper
    }

    override fun setDraggedPosition(pos: Int) {
    }

    override fun clearDragPosition() {
    }

    override fun setMainCategory(category: Category) {
    }

    override fun refresh() {
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ItemViewHolder.from(parent, viewMode)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val item = getItem(position) as RecentTimeData.RecentDataItem.Header
                holder.bind(item.headerType, item.count, multiSelectionHelper?.hasSelectedItems(),
                        multiSelectionHelper?.isCompleteRecentHeaderSelected(item.headerType, item.count),
                        position,
                        imageClickListener)
            }
            is ItemViewHolder -> {
                val item = getItem(position) as RecentTimeData.RecentDataItem.Item
                holder.bind(item.fileInfo, itemCount, multiSelectionHelper?.isSelected(position), position, -1,
                        clickListener, longClickListener, peekPopView)
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is RecentTimeData.RecentDataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is RecentTimeData.RecentDataItem.Item -> ITEM_VIEW_TYPE_ITEM
        }
    }

    class ItemViewHolder private constructor(itemView: View, private val viewMode: ViewMode) : RecyclerView.ViewHolder(itemView) {

        private val textFileName: TextView = itemView.findViewById(R.id.textFolderName)
        private val imageIcon: ImageView = itemView.findViewById(R.id.imageIcon)
        private val secondLineText: TextView = itemView.findViewById(R.id.textSecondLine)
        private val imageThumbIcon: ImageView = itemView.findViewById(R.id.imageThumbIcon)
        private val imageSelection: ImageView = itemView.findViewById(R.id.imageSelection)
        private val imageVideoThumb : ImageView = itemView.findViewById(R.id.imageVideoThumb)

        fun bind(item: FileInfo?, count: Int, selected: Boolean?, pos: Int, draggedPos: Int,
                 clickListener: (Pair<FileInfo, Int>) -> Unit, longClickListener: (FileInfo, Int, View) -> Unit, peekPopView: PeekPopView?) {
            item?.let {
                onSelection(selected, pos, draggedPos)
                bindViewByCategory(itemView.context, it, peekPopView, pos)

                itemView.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        clickListener(Pair(item, position))
                    }
                }
                itemView.setOnLongClickListener { view ->
                    val position = adapterPosition
                    if (position < count && position != RecyclerView.NO_POSITION) {
                        longClickListener(item, position, view)
                    }
                    true
                }
            }
        }

        private fun bindViewByCategory(context: Context, fileInfo: FileInfo, peekPopView: PeekPopView?, pos: Int) {
            val category = fileInfo.category
            when {
                CategoryHelper.isGenericMusic(category) -> bindGenericMusic(context, fileInfo)
                CategoryHelper.isMusicCategory(category) -> bindMusicCategory(context, fileInfo)
                CategoryHelper.isGenericImagesCategory(category) -> bindGenericImagesVidsCategory(context,
                        fileInfo)
                CategoryHelper.isGenericVideosCategory(category) -> bindGenericImagesVidsCategory(context,
                        fileInfo)
                else -> {
                    bindFilesCategory(fileInfo, category, context, peekPopView, pos)
                }
            }
        }

        private fun bindFilesCategory(fileInfo: FileInfo,
                                      category: Category?,
                                      context: Context,
                                      peekPopView: PeekPopView?,
                                      pos: Int) {
            val fileName = fileInfo.fileName
            textFileName.text = fileName
            toggleGalleryViewVisibility(category)
            if (viewMode != ViewMode.GALLERY) {
                category?.let { addPeekPop(peekPopView, imageIcon, pos, it) }
            }
            setVideoThumbVisibility(category)
            ThumbnailUtils.displayThumb(context, fileInfo, category, getThumbIcon(category), imageThumbIcon)
        }

        private fun addPeekPop(peekPopView: PeekPopView?, icon: ImageView, pos : Int, category: Category) {
            peekPopView?.addClickView(icon, pos, category)
        }

        private fun setVideoThumbVisibility(category: Category?) {
            if (CategoryHelper.isAnyVideoCategory(category)) {
                imageVideoThumb.visibility = View.VISIBLE
            } else {
                imageVideoThumb.visibility = View.GONE
            }
        }


        private fun getThumbIcon(category: Category?): ImageView {
            return if (viewMode == ViewMode.GALLERY && (category != Category.FILES)) {
                itemView.findViewById(R.id.imageThumb)
            } else {
                imageIcon
            }
        }

        private fun onSelection(selected: Boolean?, position: Int, draggedPos: Int) {
            val context = itemView.context
            val color = if (Theme.isDarkColoredTheme(context.resources, Theme.getTheme(context))) {
                ContextCompat.getColor(itemView.context,
                        R.color.dark_actionModeItemSelected)
            }
            else   {
                ContextCompat.getColor(itemView.context,
                        R.color.actionModeItemSelected)
            }
            when {
                selected == true       -> {
                    itemView.setBackgroundColor(color)
                    imageSelection.visibility = View.VISIBLE
                    imageSelection.isSelected = selected
                }
                position == draggedPos -> itemView.setBackgroundColor(color)
                else                   -> {
                    itemView.setBackgroundColor(Color.TRANSPARENT)
                    imageSelection.visibility = View.GONE
                }
            }
        }

        private fun toggleGalleryViewVisibility(category: Category?) {
//            Log.d(TAG, "toggleGalleryViewVisibility:$category")
            if (viewMode == ViewMode.GALLERY) {
                val imageGalleryThumb: ImageView = itemView.findViewById(R.id.imageThumb)
                if (category == Category.FILES) {
                    imageGalleryThumb.visibility = View.GONE
                    imageIcon.visibility = View.VISIBLE
                    textFileName.visibility = View.VISIBLE
                } else if (category == Category.GENERIC_IMAGES || category == Category.GENERIC_VIDEOS ||
                        CategoryHelper.isGalleryMusicCategory(category) || category == Category.DOCS) {
                    imageIcon.visibility = View.GONE
                    imageGalleryThumb.visibility = View.VISIBLE
                    textFileName.visibility = View.VISIBLE
                } else {
                    imageIcon.visibility = View.GONE
                    textFileName.visibility = View.GONE
                    imageGalleryThumb.visibility = View.VISIBLE
                }
            }
        }

        private fun bindGenericMusic(context: Context, fileInfo: FileInfo) {
            val count = fileInfo.count
            val files = context.resources.getQuantityString(R.plurals.number_of_files,
                    count, count)
            textFileName.text = getCategoryName(context, fileInfo.subcategory)
            secondLineText.text = files
            imageIcon.setImageResource(R.drawable.ic_folder)
        }

        private fun bindMusicCategory(context: Context, fileInfo: FileInfo) {
            textFileName.text = fileInfo.title
            val num = fileInfo.numTracks.toInt()
            if (num != INVALID_POS) {
                val files = context.resources.getQuantityString(R.plurals.number_of_files,
                        num, num)
                secondLineText.text = files
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
                secondLineText.text = files
            }
            ThumbnailUtils.displayThumb(context, fileInfo, fileInfo.category, imageIcon,
                    imageThumbIcon)
        }


        companion object {
            fun from(parent: ViewGroup,
                     viewMode: ViewMode): ItemViewHolder {

                val layoutId = when (viewMode) {
                    ViewMode.LIST -> R.layout.file_list_item
                    ViewMode.GRID -> R.layout.file_grid_item
                    ViewMode.GALLERY -> R.layout.file_gallery_item
                }
                val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
                return ItemViewHolder(view, viewMode)
            }
        }
    }

    class HeaderViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var dateText: TextView = itemView.findViewById(R.id.dateText)
        private var countText: TextView = itemView.findViewById(R.id.countText)
        private var imageSelection : ImageView = itemView.findViewById(R.id.imageSelection)

        fun bind(headerType: RecentTimeData.HeaderType, count: Int, hasSelectedItems: Boolean?,
                 headerItemsChecked: Boolean?, position: Int, imageClickListener: (Int, Boolean) -> Unit) {
            Log.d("RecentAdapter", "bindHeader:type:$headerType, itemChecked:$headerItemsChecked, count:$count")
            dateText.text = RecentTimeData.getHeaderName(itemView.context, headerType)
            countText.text = itemView.context.resources.getQuantityString(R.plurals.number_of_files, count, count)

            if (hasSelectedItems == true) {
                countText.visibility = View.GONE
                imageSelection.visibility = View.VISIBLE
            }
            else {
                imageSelection.visibility = View.GONE
                countText.visibility = View.VISIBLE
            }

            imageSelection.isSelected = headerItemsChecked == true

            imageSelection.setOnClickListener {
                val tag = imageSelection.tag
                if (tag == null) {
                    imageSelection.tag = true
                }
                else {
                    tag as Boolean
                    imageSelection.tag = !tag
                }
                imageSelection.isSelected = imageSelection.tag == true

                imageClickListener(position, imageSelection.tag as Boolean)
            }
        }

        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.recent_header,
                        parent, false)
                return HeaderViewHolder(view)
            }
        }
    }

    companion object {
        const val ITEM_VIEW_TYPE_HEADER = 0
        const val ITEM_VIEW_TYPE_ITEM = 1
    }

}

class RecentDiffCallback : DiffUtil.ItemCallback<RecentTimeData.RecentDataItem>() {
    override fun areItemsTheSame(oldItem: RecentTimeData.RecentDataItem, newItem: RecentTimeData.RecentDataItem) = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: RecentTimeData.RecentDataItem, newItem: RecentTimeData.RecentDataItem): Boolean = oldItem.id == newItem.id

}


