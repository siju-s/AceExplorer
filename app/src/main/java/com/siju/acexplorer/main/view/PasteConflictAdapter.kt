package com.siju.acexplorer.main.view

import android.content.Context
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.helper.FileUtils.convertDate
import com.siju.acexplorer.theme.Theme
import com.siju.acexplorer.theme.Theme.Companion.getTheme
import com.siju.acexplorer.utils.ThumbnailUtils.displayThumb
import java.io.File

class PasteConflictAdapter(private val context: Context, private val conflictFileInfoList: List<FileInfo>) : BaseAdapter() {
    private val theme = getTheme(context)

    override fun getCount(): Int {
        return conflictFileInfoList.size
    }

    override fun getItem(position: Int): Any {
        return conflictFileInfoList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val fileInfoHolder: FileInfoHolder
        val view: View
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.paste_conflict_file_info,
                    parent, false)
            fileInfoHolder = FileInfoHolder(view)
            view?.tag = fileInfoHolder
        } else {
            view = convertView
            fileInfoHolder = convertView.tag as FileInfoHolder
        }
        bindData(position, fileInfoHolder)
        return view
    }

    private fun bindData(position: Int, fileInfoHolder: FileInfoHolder) {
        if (position == 0) {
            fileInfoHolder.headerText.text = context.getString(R.string.header_source)
        } else {
            fileInfoHolder.headerText.text = context.getString(R.string.header_destination)
        }
        setCardBg(fileInfoHolder)

        val conflictFileInfo = conflictFileInfoList[position]
        val filePath = conflictFileInfo.filePath
        displayThumb(context, conflictFileInfo, conflictFileInfo.category, fileInfoHolder.imageIcon, null, null)
        fileInfoHolder.titleText.text = conflictFileInfo.fileName
        fileInfoHolder.pathText.text = filePath

        filePath?.let {
            val sourceFile = File(filePath)
            val date = sourceFile.lastModified()
            val fileModifiedDate = convertDate(date)
            val size = sourceFile.length()
            val fileSize = Formatter.formatFileSize(context, size)

            fileInfoHolder.dateText.text = fileModifiedDate
            fileInfoHolder.sizeText.text = fileSize
        }
    }

    private fun setCardBg(fileInfoHolder: FileInfoHolder) {
        fileInfoHolder.cardView.setCardBackgroundColor(if (Theme.isDarkColoredTheme(context.resources, theme)) {
            ContextCompat.getColor(context, R.color.dark_home_card_bg)
        } else {
            ContextCompat.getColor(context, R.color.light_home_card_bg)
        })
    }

    private class FileInfoHolder internal constructor(view: View) {
        var cardView: CardView = view.findViewById(R.id.cardView)
        var imageIcon: ImageView = view.findViewById(R.id.imageFileIcon)
        var headerText: TextView = view.findViewById(R.id.header)
        var titleText: TextView = view.findViewById(R.id.textFileName)
        var dateText: TextView = view.findViewById(R.id.textFileDate)
        var sizeText: TextView = view.findViewById(R.id.textFileSize)
        var pathText: TextView = view.findViewById(R.id.textFilePath)
    }

}