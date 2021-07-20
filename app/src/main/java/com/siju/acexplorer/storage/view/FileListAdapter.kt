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
import com.siju.acexplorer.main.model.groups.CategoryHelper.shouldHideGalleryThumb
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.storage.model.ViewMode
import com.siju.acexplorer.ui.peekandpop.PeekPopView
import com.siju.acexplorer.utils.ThumbnailUtils.displayThumb
import java.util.*
import kotlin.collections.ArrayList


const val INVALID_POS = -1
private const val TAG = "FileListAdapter"
class FileListAdapter internal constructor(var viewMode: ViewMode, private val clickListener: (Pair<FileInfo, Int>) -> Unit,
                                           private val longClickListener: (FileInfo, Int, View) -> Unit,
                                           private val peekPopView: PeekPopView?) :
        ListAdapter<FileInfo, FileListAdapter.ViewHolder>(FileInfoDiffCallback()), BaseListAdapter {

    private var draggedPosition = -1
    private var multiSelectionHelper: MultiSelectionHelper? = null
    private var mainCategory: Category? = null
    private var filteredList: ArrayList<FileInfo> = ArrayList()
    private var fileList = arrayListOf<FileInfo>()

    init {
        peekPopView?.initPeekPopListener()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, viewMode)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val fileInfo = getItem(position)
        fileInfo?.let {
            viewHolder.bind(fileInfo, itemCount, viewMode, mainCategory, multiSelectionHelper?.isSelected(position), position, draggedPosition,
                    clickListener, longClickListener, peekPopView)
        }
    }

    fun onDataLoaded(data: java.util.ArrayList<FileInfo>) {
        this.fileList = data
        filteredList.clear()
        filteredList.addAll(fileList)
        submitList(data)
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

    override fun setMainCategory(category: Category?) {
        this.mainCategory = category
    }

    override fun refresh() {
        notifyDataSetChanged()
    }

    fun filter(text: String) {
        if (text.isEmpty()) {
            populateOriginalList(filteredList)
        } else {
            addSearchResults(text)
        }
    }

    private fun populateOriginalList(fileData: ArrayList<FileInfo>) {
        this.fileList.clear()
        fileList.addAll(fileData)
        submitList(fileList)
        notifyDataSetChanged()
    }

    private fun addSearchResults(query: String) {
        var text = query
        val result: ArrayList<FileInfo> = ArrayList()
        text = text.toLowerCase(Locale.getDefault())
        for (item in filteredList) {
            val fileName = item.fileName
            val packageName = item.filePath
            fileName?.let {
                if (fileName.toLowerCase(Locale.getDefault()).contains(text)) {
                    result.add(item)
                }
            }
            if (!result.contains(item)) {
                packageName?.let {
                    if (packageName.toLowerCase(Locale.getDefault()).contains(text)) {
                        result.add(item)
                    }
                }
            }
        }
        fileList.clear()
        fileList.addAll(result)
        submitList(fileList)
        notifyDataSetChanged()
    }

    class ViewHolder private constructor(itemView: View,
                                         private val viewMode: ViewMode) : RecyclerView.ViewHolder(itemView) {

        private val textFileName: TextView = itemView.findViewById(R.id.textFolderName)
        private val textNoOfFileOrSize: TextView = itemView.findViewById(R.id.textSecondLine)
        private val imageIcon: ImageView = itemView.findViewById(R.id.imageIcon)
        private val imageThumbIcon: ImageView = itemView.findViewById(R.id.imageThumbIcon)
        private var dateText: TextView? = null
        private val imageSelection: ImageView = itemView.findViewById(R.id.imageSelection)
        private val imageVideoThumb : ImageView = itemView.findViewById(R.id.imageVideoThumb)

        init {
            if (viewMode == ViewMode.LIST) {
                dateText = itemView.findViewById(R.id.textDate)
            }
        }

        fun bind(item: FileInfo, count: Int, viewMode: ViewMode, mainCategory: Category?, selected: Boolean?, pos: Int, draggedPos: Int,
                 clickListener: (Pair<FileInfo, Int>) -> Unit,
                 longClickListener: (FileInfo, Int, View) -> Unit,
                 peekPopView: PeekPopView?) {
//            Log.d("FileListAdapter", "bind:${item.fileName}, pos:$pos")
            onSelection(selected, pos, draggedPos)
            bindViewByCategory(itemView.context, item, viewMode, mainCategory, peekPopView, pos)
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

            imageIcon.setOnLongClickListener {
                val position = adapterPosition
                if (position < count && position != RecyclerView.NO_POSITION) {
                    longClickListener(item, position, it)
                }
                true
            }
        }

        private fun onSelection(selected: Boolean?, position: Int, draggedPos: Int) {
            val color = ContextCompat.getColor(itemView.context, R.color.actionModeItemSelected)

            when {
                selected == true       -> {
                    itemView.setBackgroundColor(color)
                    imageSelection.visibility = View.VISIBLE
                    imageSelection.isSelected = true

                }
                position == draggedPos -> itemView.setBackgroundColor(color)
                else                   -> {
                    itemView.setBackgroundColor(Color.TRANSPARENT)
                    imageSelection.isSelected = false
                    imageSelection.visibility = View.GONE
                }
            }
        }

        private fun bindViewByCategory(context: Context,
                                       fileInfo: FileInfo,
                                       viewMode: ViewMode,
                                       mainCategory: Category?,
                                       peekPopView: PeekPopView?,
                                       pos: Int) {
            val category = fileInfo.category
//            Log.d(TAG, "bindViewByCategory:$category, file:${fileInfo.filePath}, date:${fileInfo.date}")
            when {
                category == Category.PICKER       -> {
                    bindPickerView(fileInfo)
                }
                isGenericMusic(category)          -> bindGenericMusic(context, fileInfo, pos, peekPopView)
                isMusicCategory(category)         -> bindMusicCategory(context, fileInfo, pos, peekPopView)
                isGenericImagesCategory(category) || isGenericVideosCategory(category) -> bindGenericImagesVidsCategory(context,
                                                                                   fileInfo, pos, peekPopView)
                isAppManager(category)            -> bindAppManagerCategory(context, fileInfo, viewMode, pos, peekPopView)
                isRecentCategory(category)        -> bindGenericRecent(context, fileInfo, category, pos, peekPopView)
                isAnyLargeFilesCategory(category) -> bindLargeFilesGeneric(context, fileInfo, category, pos, peekPopView)
                isAnyCameraCategory(category)     -> bindCameraGeneric(context, fileInfo, category, pos, peekPopView)
                else                              -> {
                    bindFilesCategory(fileInfo, category, mainCategory, context, peekPopView, pos)
                }
            }
        }

        private fun isAppManager(category: Category?) = category == Category.APP_MANAGER

        private fun bindFilesCategory(fileInfo: FileInfo,
                                      category: Category?,
                                      mainCategory: Category?,
                                      context: Context,
                                      peekPopView: PeekPopView?,
                                      pos: Int) {
//            Log.d(TAG, "bindFilesCategory:$category, file:${fileInfo.filePath}, date:${fileInfo.date}")
            val fileName = fileInfo.fileName
            if (mainCategory == null) {
                category?.let { bindDate(it, fileInfo) }
            }
            else {
                bindDate(mainCategory, fileInfo)
            }
            val isDirectory = fileInfo.isDirectory
            val fileNumOrSize: String = if (isDirectory) {
                getDirectoryFileCount(context, fileInfo)
            }
            else {
                Formatter.formatFileSize(context, fileInfo.size)
            }
            textFileName.text = fileName
            textNoOfFileOrSize.text = fileNumOrSize
            toggleGalleryViewVisibility(category)
            setVideoThumbVisibility(category)
            category?.let { addPeekPop(peekPopView, imageIcon, pos, it) }
            displayThumb(context, fileInfo, category, getThumbIcon(category), imageThumbIcon)
        }

        private fun hideDateView() {
            dateText?.visibility = View.GONE
        }

        private fun showDateView() {
            dateText?.visibility = View.VISIBLE
        }

        private fun addPeekPop(peekPopView: PeekPopView?, icon: ImageView, pos : Int, category: Category?) {
            category?.let { peekPopView?.addClickView(icon, pos, it) }
        }

        private fun setVideoThumbVisibility(category: Category?) {
//            Log.d(TAG, "setVideoThumbVisibility:$category")
            if (CategoryHelper.isAnyVideoCategory(category)) {
                imageVideoThumb.visibility = View.VISIBLE
            } else {
                imageVideoThumb.visibility = View.GONE
            }
        }

        private fun getThumbIcon(category: Category?) : ImageView {
            return if (viewMode == ViewMode.GALLERY && (category != Category.FILES && category != Category.APPS)) {
                itemView.findViewById(R.id.imageThumb) as ImageView
            } else {
                imageIcon
            }
        }

        private fun bindDate(category: Category, fileInfo: FileInfo) {
            dateText?.let {
                val dateMs = if (isDateInMs(category)) {
                    fileInfo.date
                }
                else {
                    fileInfo.date * 1000
                }
                it.text = FileUtils.convertDate(dateMs)
            }
            showDateView()
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

        private fun bindGenericMusic(context: Context, fileInfo: FileInfo, pos: Int, peekPopView: PeekPopView?) {
            Log.d(TAG, "bindGenericMusic:category:${fileInfo.category}, file:${fileInfo.count}")
            val count = fileInfo.count
            val files = context.resources.getQuantityString(R.plurals.number_of_files,
                    count, count)
            textFileName.text = getCategoryName(context, fileInfo.subcategory)
            textNoOfFileOrSize.text = files
            imageIcon.setImageResource(R.drawable.ic_folder)
            hideDateView()
            toggleGalleryViewVisibility(fileInfo.category)
            addPeekPop(peekPopView, imageIcon, pos, fileInfo.category)
        }

        private fun bindMusicCategory(context: Context, fileInfo: FileInfo, pos: Int, peekPopView: PeekPopView?) {
            Log.d(TAG, "bindMusicCategory:category:${fileInfo.category}, file:${fileInfo.filePath}")
            textFileName.text = fileInfo.title
            val num = fileInfo.numTracks.toInt()
            if (num != INVALID_POS) {
                val files = context.resources.getQuantityString(R.plurals.number_of_files,
                        num, num)
                textNoOfFileOrSize.text = files
            }
            displayThumb(context, fileInfo, fileInfo.category, imageIcon, imageThumbIcon)
            hideDateView()
            addPeekPop(peekPopView, imageIcon, pos, fileInfo.category)
        }

        private fun bindGenericImagesVidsCategory(context: Context, fileInfo: FileInfo, pos: Int, peekPopView: PeekPopView?) {
            val category = fileInfo.category
            toggleGalleryViewVisibility(category)
            textFileName.text = fileInfo.fileName
            val num = fileInfo.numTracks.toInt()
            if (num != INVALID_POS) {
                val files = context.resources.getQuantityString(R.plurals.number_of_files,
                        num, num)
                textNoOfFileOrSize.text = files
            }
            hideDateView()
            displayThumb(context, fileInfo, category, getThumbIcon(category),
                    imageThumbIcon)
            addPeekPop(peekPopView, imageIcon, pos, category)
        }

        private fun toggleGalleryViewVisibility(category: Category?) {
//            Log.d(TAG, "toggleGalleryViewVisibility:$category")
            if (viewMode == ViewMode.GALLERY) {
                val imageGalleryThumb: ImageView = itemView.findViewById(R.id.imageThumb)
                if (shouldHideGalleryThumb(category)) {
                    imageGalleryThumb.visibility = View.GONE
                    imageIcon.visibility = View.VISIBLE
                    textFileName.visibility = View.VISIBLE
                }
                else if (category == Category.GENERIC_IMAGES || category == Category.GENERIC_VIDEOS ||
                        CategoryHelper.isGalleryMusicCategory(category) || category == Category.DOCS) {
                    imageIcon.visibility = View.GONE
                    imageGalleryThumb.visibility = View.VISIBLE
                    textFileName.setBackgroundColor(ContextCompat.getColor(textFileName.context, R.color.gallery_item_text_background))
                    textFileName.visibility = View.VISIBLE
                }
                else {
                    imageIcon.visibility = View.GONE
                    textFileName.visibility = View.GONE
                    imageGalleryThumb.visibility = View.VISIBLE
                }
            }
        }

        private fun bindGenericRecent(context: Context, fileInfo: FileInfo, category: Category?, pos: Int, peekPopView: PeekPopView?) {
            val count = fileInfo.count
            val files = context.resources.getQuantityString(R.plurals.number_of_files,
                    count, count)
            textFileName.text = getCategoryName(context, fileInfo.subcategory)
            textNoOfFileOrSize.text = files
            imageIcon.setImageResource(R.drawable.ic_folder)
            hideDateView()
            addPeekPop(peekPopView, imageIcon, pos, category)
        }

        private fun bindLargeFilesGeneric(context: Context, fileInfo: FileInfo, category: Category?, pos: Int, peekPopView: PeekPopView?) {
            val count = fileInfo.count
            val files = context.resources.getQuantityString(R.plurals.number_of_files,
                    count, count)
            textFileName.text = getCategoryName(context, fileInfo.subcategory)
            textNoOfFileOrSize.text = files
            imageIcon.setImageResource(R.drawable.ic_folder)
            hideDateView()
            addPeekPop(peekPopView, imageIcon, pos, category)
        }

        private fun bindCameraGeneric(context: Context, fileInfo: FileInfo, category: Category?, pos: Int, peekPopView: PeekPopView?) {
//            Log.d(TAG, "bindCameraGeneric:category:$category, file:${fileInfo.filePath}")
            val count = fileInfo.count
            val files = context.resources.getQuantityString(R.plurals.number_of_files,
                    count, count)
            textFileName.text = getCategoryName(context, fileInfo.subcategory)
            textNoOfFileOrSize.text = files
            imageIcon.setImageResource(R.drawable.ic_folder)
            toggleGalleryViewVisibility(category)
            hideDateView()
            addPeekPop(peekPopView, imageIcon, pos, category)
        }

        private fun bindAppManagerCategory(context: Context, fileInfo: FileInfo, viewMode: ViewMode, pos: Int, peekPopView: PeekPopView?) {

            textFileName.text = fileInfo.fileName
            val size = fileInfo.size
            val fileSize = Formatter.formatFileSize(context, size)
            textNoOfFileOrSize.text = fileSize
            val fileDate = FileUtils.convertDate(fileInfo.date)
            if (viewMode == ViewMode.LIST) {
                dateText?.text = fileDate
                showDateView()
            }
            displayThumb(context, fileInfo, fileInfo.category, imageIcon,
                    imageThumbIcon)
            addPeekPop(peekPopView, imageIcon, pos, fileInfo.category)
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
//                Log.d(TAG, "category:${oldItem.category}, path:${oldItem.filePath}")
                return when (oldItem.category) {
                    Category.GENERIC_IMAGES, Category.GENERIC_VIDEOS               -> oldItem.bucketId == newItem.bucketId
                    Category.RECENT_IMAGES, Category.RECENT_VIDEOS, Category.RECENT_AUDIO, Category.RECENT_DOCS,
                        Category.RECENT_APPS -> oldItem.category == newItem.category
                    else                                                           -> {
                        oldItem.filePath == newItem.filePath
                    }
                }
            }
            return oldItem.category == newItem.category
        }

        override fun areContentsTheSame(oldItem: FileInfo,
                                        newItem: FileInfo): Boolean {
            return oldItem == newItem
        }

    }


}