package com.siju.acexplorer.storage.view

import android.content.Context
import android.graphics.Color
import android.text.format.Formatter
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
import com.siju.acexplorer.main.model.groups.CategoryHelper.isAnyCameraCategory
import com.siju.acexplorer.main.model.groups.CategoryHelper.isAnyLargeFilesCategory
import com.siju.acexplorer.main.model.groups.CategoryHelper.isDateInMs
import com.siju.acexplorer.main.model.groups.CategoryHelper.isGenericImagesCategory
import com.siju.acexplorer.main.model.groups.CategoryHelper.isGenericMusic
import com.siju.acexplorer.main.model.groups.CategoryHelper.isGenericVideosCategory
import com.siju.acexplorer.main.model.groups.CategoryHelper.isMusicCategory
import com.siju.acexplorer.main.model.groups.CategoryHelper.isRecentCategory
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.storage.model.ViewMode
import com.siju.acexplorer.utils.ThumbnailUtils.displayThumb

const val INVALID_POS = -1
private const val TAG = "FileListAdapter"
class FileListAdapter internal constructor(var viewMode: ViewMode, private val clickListener: (Pair<FileInfo, Int>) -> Unit,
                                           private val longClickListener: (FileInfo, Int, View) -> Unit) :
        ListAdapter<FileInfo, FileListAdapter.ViewHolder>(FileInfoDiffCallback()), BaseListAdapter {

    private var draggedPosition = -1
    private var multiSelectionHelper: MultiSelectionHelper? = null
    private var mainCategory: Category? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, viewMode)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
//        Log.e(TAG, "onBindViewHolder : $position")
        val item = getItem(position)
        viewHolder.bind(item, itemCount, viewMode, mainCategory, multiSelectionHelper?.isSelected(position), position, draggedPosition,
                clickListener, longClickListener)
    }

    override fun setMultiSelectionHelper(multiSelectionHelper: MultiSelectionHelper) {
        this.multiSelectionHelper = multiSelectionHelper
    }

    override fun getMultiSelectionHelper() = multiSelectionHelper

    override fun setDraggedPosition(pos: Int) {
        draggedPosition = pos
        notifyDataSetChanged()
    }

    override fun clearDragPosition() {
        draggedPosition = -1
    }

    override fun setMainCategory(category: Category) {
        this.mainCategory = category
    }

    override fun refresh() {
        notifyDataSetChanged()
    }

    class ViewHolder private constructor(itemView: View,
                                         private val viewMode: ViewMode) : RecyclerView.ViewHolder(itemView) {

        private val textFileName: TextView = itemView.findViewById(R.id.textFolderName)
        private val textNoOfFileOrSize: TextView = itemView.findViewById(R.id.textSecondLine)
        private val imageIcon: ImageView = itemView.findViewById(R.id.imageIcon)
        private val imageThumbIcon: ImageView = itemView.findViewById(R.id.imageThumbIcon)
        private var dateText: TextView? = null

        init {
            if (viewMode == ViewMode.LIST) {
                dateText = itemView.findViewById(R.id.textDate)
            }
        }

        fun bind(item: FileInfo, count: Int, viewMode: ViewMode, mainCategory: Category?, selected: Boolean?, pos: Int, draggedPos: Int,
                 clickListener: (Pair<FileInfo, Int>) -> Unit,
                 longClickListener: (FileInfo, Int, View) -> Unit) {
//            Log.e("FileListAdapter", "bind:${item.fileName}, mainCategory:$mainCategory")
            onSelection(selected, pos, draggedPos)
            bindViewByCategory(itemView.context, item, viewMode, mainCategory)
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position < count && position != RecyclerView.NO_POSITION) {
                    clickListener(Pair(item, position))
                }
            }

            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position < count && position != RecyclerView.NO_POSITION) {
                    longClickListener(item, position, it)
                }
                true
            }
        }

        private fun onSelection(selected: Boolean?, position: Int, draggedPos: Int) {
            val color = ContextCompat.getColor(itemView.context,
                    R.color.dark_actionModeItemSelected)
            when {
                selected == true       -> itemView.setBackgroundColor(color)
                position == draggedPos -> itemView.setBackgroundColor(color)
                else                   -> itemView.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        private fun bindViewByCategory(context: Context,
                                       fileInfo: FileInfo,
                                       viewMode: ViewMode,
                                       mainCategory: Category?) {
            val category = fileInfo.category
            Log.d(TAG, "bindViewByCategory:$category")
            when {
                category == Category.PICKER       -> {
                    bindPickerView(fileInfo)
                }
                isGenericMusic(category)          -> bindGenericMusic(context, fileInfo)
                isMusicCategory(category)         -> bindMusicCategory(context, fileInfo)
                isGenericImagesCategory(category) -> bindGenericImagesVidsCategory(context,
                                                                                   fileInfo)
                isGenericVideosCategory(category) -> bindGenericImagesVidsCategory(context,
                                                                                   fileInfo)
                isAppManager(category)            -> bindAppManagerCategory(context, fileInfo, viewMode)
                isRecentCategory(category)        -> bindGenericRecent(context, fileInfo)
                isAnyLargeFilesCategory(category) -> bindLargeFilesGeneric(context, fileInfo)
                isAnyCameraCategory(category)     -> bindCameraGeneric(context, fileInfo)
                else                              -> {
                    bindFilesCategory(fileInfo, category, mainCategory, context)
                }
            }
        }

        private fun isAppManager(category: Category?) = category == Category.APP_MANAGER

        private fun bindFilesCategory(fileInfo: FileInfo,
                                      category: Category?,
                                      mainCategory: Category?,
                                      context: Context) {

            val fileName = fileInfo.fileName
            if (mainCategory == null) {
                category?.let { bindDate(it, fileInfo) }
            }
            else {
                bindDate(mainCategory, fileInfo)
            }
            val isDirectory = fileInfo.isDirectory
            val fileNumOrSize: String
            fileNumOrSize = if (isDirectory) {
                getDirectoryFileCount(context, fileInfo)
            }
            else {
                Formatter.formatFileSize(context, fileInfo.size)
            }
            textFileName.text = fileName
            textNoOfFileOrSize.text = fileNumOrSize
            toggleGalleryViewVisibility(category)
            displayThumb(context, fileInfo, category, getThumbIcon(category), imageThumbIcon)
        }

        private fun getThumbIcon(category: Category?) : ImageView {
            return if (viewMode == ViewMode.GALLERY && (category != Category.FILES)) {
                itemView.findViewById(R.id.imageThumb) as ImageView
            } else {
                imageIcon
            }
        }

        private fun bindDate(category: Category,
                             fileInfo: FileInfo) {
            dateText?.let {
                val dateMs = if (isDateInMs(category)) {
                    fileInfo.date
                }
                else {
                    fileInfo.date * 1000
                }
                it.text = FileUtils.convertDate(dateMs)
            }
        }

        private fun getDirectoryFileCount(context: Context,
                                          fileInfo: FileInfo): String {
            return if (fileInfo.isRootMode) {
                context.getString(R.string.directory)
            }
            else {
                return when (val childFileListSize = fileInfo.size.toInt()) {
                    0    -> context.resources.getString(R.string.empty)
                    -1   -> ""
                    else -> context.resources.getQuantityString(R.plurals
                                                                        .number_of_files,
                                                                childFileListSize,
                                                                childFileListSize)
                }
            }
        }

        private fun bindPickerView(fileInfo: FileInfo) {
            imageIcon.setImageResource(fileInfo.icon)
            textFileName.text = fileInfo.fileName
            textNoOfFileOrSize.text = fileInfo.filePath
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
            displayThumb(context, fileInfo, fileInfo.category, imageIcon, imageThumbIcon)
        }

        private fun bindGenericImagesVidsCategory(context: Context, fileInfo: FileInfo) {
            val category = fileInfo.category
            toggleGalleryViewVisibility(category)
            textFileName.text = fileInfo.fileName
            val num = fileInfo.numTracks.toInt()
            if (num != INVALID_POS) {
                val files = context.resources.getQuantityString(R.plurals.number_of_files,
                        num, num)
                textNoOfFileOrSize.text = files
            }
            displayThumb(context, fileInfo, category, getThumbIcon(category),
                    imageThumbIcon)
        }

        private fun toggleGalleryViewVisibility(category: Category?) {
            Log.e(TAG, "toggleGalleryViewVisibility:$category")
            if (viewMode == ViewMode.GALLERY) {
                val imageGalleryThumb: ImageView = itemView.findViewById(R.id.imageThumb)
                if (category == Category.FILES) {
                    imageGalleryThumb.visibility = View.GONE
                    imageIcon.visibility = View.VISIBLE
                    textFileName.visibility = View.VISIBLE
                }
                else if (category == Category.GENERIC_IMAGES || category == Category.GENERIC_VIDEOS ||
                        CategoryHelper.isGalleryMusicCategory(category) || category == Category.DOCS) {
                    imageIcon.visibility = View.GONE
                    imageGalleryThumb.visibility = View.VISIBLE
                    textFileName.visibility = View.VISIBLE
                }
                else {
                    imageIcon.visibility = View.GONE
                    textFileName.visibility = View.GONE
                    imageGalleryThumb.visibility = View.VISIBLE
                }
            }
        }

        private fun bindGenericRecent(context: Context, fileInfo: FileInfo) {
            val count = fileInfo.count
            val files = context.resources.getQuantityString(R.plurals.number_of_files,
                    count, count)
            textFileName.text = getCategoryName(context, fileInfo.subcategory)
            textNoOfFileOrSize.text = files
            imageIcon.setImageResource(R.drawable.ic_folder)
        }

        private fun bindLargeFilesGeneric(context: Context, fileInfo: FileInfo) {
            val count = fileInfo.count
            val files = context.resources.getQuantityString(R.plurals.number_of_files,
                    count, count)
            textFileName.text = getCategoryName(context, fileInfo.subcategory)
            textNoOfFileOrSize.text = files
            imageIcon.setImageResource(R.drawable.ic_folder)
        }

        private fun bindCameraGeneric(context: Context, fileInfo: FileInfo) {
            val count = fileInfo.count
            val files = context.resources.getQuantityString(R.plurals.number_of_files,
                    count, count)
            textFileName.text = getCategoryName(context, fileInfo.subcategory)
            textNoOfFileOrSize.text = files
            imageIcon.setImageResource(R.drawable.ic_folder)
        }

        private fun bindAppManagerCategory(context: Context, fileInfo: FileInfo, viewMode: ViewMode) {

            textFileName.text = fileInfo.fileName
            val size = fileInfo.size
            val fileSize = Formatter.formatFileSize(context, size)
            textNoOfFileOrSize.text = fileSize
            val fileDate = FileUtils.convertDate(fileInfo.date)
            if (viewMode == ViewMode.LIST) {
                dateText?.text = fileDate
            }
            displayThumb(context, fileInfo, fileInfo.category, imageIcon,
                    imageThumbIcon)
        }
        companion object {
            fun from(parent: ViewGroup,
                     viewMode: ViewMode): ViewHolder {

                val layoutId = when (viewMode) {
                    ViewMode.LIST -> R.layout.file_list_item
                    ViewMode.GRID -> R.layout.file_grid_item
                    ViewMode.GALLERY -> R.layout.file_gallery_item
                }
                val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
                return ViewHolder(view, viewMode)
            }
        }
    }

    class FileInfoDiffCallback : DiffUtil.ItemCallback<FileInfo>() {
        override fun areItemsTheSame(oldItem: FileInfo,
                                     newItem: FileInfo): Boolean {
            if (oldItem.category == newItem.category) {
//                Log.e(TAG, "category:${oldItem.category}, path:${oldItem.filePath}")
                val result = when (oldItem.category) {
                    Category.GENERIC_IMAGES, Category.GENERIC_VIDEOS               -> oldItem.bucketId == newItem.bucketId
                    else                                                           -> {
                        oldItem.filePath == newItem.filePath
                    }
                }
                return result
            }
            return oldItem.category == newItem.category
        }

        override fun areContentsTheSame(oldItem: FileInfo,
                                        newItem: FileInfo): Boolean {
            return oldItem == newItem
        }

    }


}