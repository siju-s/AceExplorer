package com.siju.acexplorer.main.view


import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.utils.ThumbnailUtils
import java.util.*


class InfoFragment : BottomSheetDialogFragment() {

    companion object {
        fun newInstance() = InfoFragment()
    }

    private lateinit var category: Category
    private lateinit var fileInfo: FileInfo
    //    private lateinit var dialogMediaInfoBinding : MediaInfoBinding
    private lateinit var collapsingToolbarLayout: CollapsingToolbarLayout
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var sheetView: View? = null

    fun setFileInfo(fileInfo: FileInfo) {
        this.fileInfo = fileInfo
    }

    fun setCategory(category: Category) {
        this.category = category
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        sheetView = LayoutInflater.from(context).inflate(R.layout.media_info, null)
        bottomSheetDialog?.setContentView(sheetView)
        showInfo(fileInfo, category)
        return bottomSheetDialog as BottomSheetDialog
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        dialog?.let {
            val sheetView = it.findViewById<View>(R.id.design_bottom_sheet)
            sheetView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        val view = sheetView
        view?.post {
            val parent = view.parent as View
            val params = parent.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior as BottomSheetBehavior
            behavior.peekHeight = view.measuredHeight
        }
    }

    private fun showInfo(fileInfo: FileInfo, category: Category) {
        setupUI(fileInfo, category)
    }

    private fun setupUI(fileInfo: FileInfo, category: Category) {
        val collapsingToolbarLayout = sheetView?.findViewById<CollapsingToolbarLayout>(R.id.collapsingToolbar)

        val icon = sheetView?.findViewById<ImageView>(R.id.imageIcon)
        val path = fileInfo.filePath
        val pathText = sheetView?.findViewById<TextView>(R.id.textPath)

        if (path == null) {
            pathText?.visibility = View.GONE
            sheetView?.findViewById<TextView>(R.id.textPathPlaceholder)?.visibility = View.GONE
        }
        else {
            ThumbnailUtils.displayThumb(context, fileInfo, fileInfo.category, icon, null)
            pathText?.text = path
        }

        val nameText = sheetView?.findViewById<TextView>(R.id.textName)
        nameText?.text = fileInfo.fileName

        val dateText = sheetView?.findViewById<TextView>(R.id.textDateModified)
        bindDate(dateText, category, fileInfo)

        val sizeText = sheetView?.findViewById<TextView>(R.id.textFileSize)
        sizeText?.text = Formatter.formatFileSize(context, fileInfo.size)

        val context = context
        if (CategoryHelper.isAnyImagesCategory(category) && context != null) {
            val resolutionText = sheetView?.findViewById<TextView>(R.id.textResolution)
            resolutionText?.text = String.format(Locale.getDefault(), context.getString(R.string.resolution_format),
                    fileInfo.width, fileInfo.height)
        } else {
            sheetView?.findViewById<TextView>(R.id.textResolution)?.visibility = View.GONE
            sheetView?.findViewById<TextView>(R.id.textResolutionPlaceholder)?.visibility = View.GONE
        }

    }

    private fun bindDate(dateText: TextView?, category: Category,
                         fileInfo: FileInfo) {
        dateText?.let {
            val dateMs = if (CategoryHelper.isDateInMs(category)) {
                fileInfo.date
            } else {
                fileInfo.date * 1000
            }
            it.text = FileUtils.convertDate(dateMs)
        }
    }
}