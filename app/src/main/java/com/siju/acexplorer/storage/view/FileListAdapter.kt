package com.siju.acexplorer.storage.view

import android.content.Context
import android.text.format.Formatter
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
import com.siju.acexplorer.main.model.groups.CategoryHelper.*
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.utils.ThumbnailUtils.displayThumb

private const val INVALID_POS = -1

class FileListAdapter internal constructor(private val clickListener: (FileInfo) -> Unit) :
        ListAdapter<FileInfo, FileListAdapter.ViewHolder>(FileInfoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = getItem(position)
        viewHolder.bind(item, clickListener)
    }

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textFileName: TextView = itemView.findViewById(R.id.textFolderName)
        private val textNoOfFileOrSize: TextView = itemView.findViewById(R.id.textSecondLine)
        private val imageIcon: ImageView = itemView.findViewById(R.id.imageIcon)
        private val imageThumbIcon: ImageView = itemView.findViewById(R.id.imageThumbIcon)

        fun bind(item: FileInfo, clickListener: (FileInfo) -> Unit) {
            bindViewByCategory(itemView.context, item)
            itemView.setOnClickListener { clickListener(item) }
        }

        private fun bindViewByCategory(context: Context, fileInfo: FileInfo) {
            val category = fileInfo.category
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
                isAppManager(category)            -> bindAppManagerCategory(context, fileInfo)
                isRecentCategory(category)        -> bindGenericRecent(context, fileInfo)
                else                              -> {
                    bindFilesCategory(fileInfo, category, context)
                }
            }
        }

        private fun isAppManager(category: Category?) = category == Category.APP_MANAGER

        private fun bindFilesCategory(fileInfo: FileInfo,
                                      category: Category?,
                                      context: Context) {
            val fileName = fileInfo.fileName
            val dateMs = if (isDateInMs(category)) {
                fileInfo.date
            }
            else {
                fileInfo.date * 1000
            }
            val fileDate = FileUtils.convertDate(dateMs)
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
            displayThumb(context, fileInfo, category, imageIcon,
                         imageThumbIcon)
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
            displayThumb(context, fileInfo, fileInfo.category, imageIcon,
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
            displayThumb(context, fileInfo, fileInfo.category, imageIcon,
                         imageThumbIcon)
        }

        private fun bindGenericRecent(context: Context, fileInfo: FileInfo) {
            val count = fileInfo.count
            val files = context.resources.getQuantityString(R.plurals.number_of_files,
                                                            count, count)
            textFileName.text = getCategoryName(context, fileInfo.subcategory)
            textNoOfFileOrSize.text = files
            imageIcon.setImageResource(R.drawable.ic_folder)
        }

        private fun bindAppManagerCategory(context: Context, fileInfo: FileInfo) {

            textFileName.text = fileInfo.fileName
            val size = fileInfo.size
            val fileSize = Formatter.formatFileSize(context, size)
            textNoOfFileOrSize.text = fileSize
            val fileDate = FileUtils.convertDate(fileInfo.date)
//            if (mViewMode == ViewMode.LIST) {
//                textFileModifiedDate.setText(fileDate)
//            }
            displayThumb(context, fileInfo, fileInfo.category, imageIcon,
                         imageThumbIcon)
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.library_item,
                                                                       parent, false)
                return ViewHolder(view)
            }
        }
    }

    class FileInfoDiffCallback : DiffUtil.ItemCallback<FileInfo>() {
        override fun areItemsTheSame(oldItem: FileInfo,
                                     newItem: FileInfo) = oldItem.category == newItem.category

        override fun areContentsTheSame(oldItem: FileInfo,
                                        newItem: FileInfo): Boolean = oldItem == newItem

    }


}