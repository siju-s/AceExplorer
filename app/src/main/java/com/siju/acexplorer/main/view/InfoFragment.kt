package com.siju.acexplorer.main.view


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.format.Formatter
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.ShareHelper
import java.util.*


class InfoFragment : BottomSheetDialogFragment() {

    companion object {
        fun newInstance() = InfoFragment()
    }

    private lateinit var category: Category
    private lateinit var fileInfo: FileInfo

    private var bottomSheetDialog: BottomSheetDialog? = null
    private var sheetView        : View? = null
    private var uri              : Uri? = null

    fun setFileInfo(fileInfo: FileInfo) {
        this.fileInfo = fileInfo
    }

    fun setCategory(category: Category) {
        this.category = category
    }

    fun setFileUri(uri: Uri?) {
        this.uri = uri
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        sheetView = LayoutInflater.from(context).inflate(R.layout.media_info, null)
        sheetView?.let {
            bottomSheetDialog?.setContentView(it)
        }
        showInfo(fileInfo, category)
        setHasOptionsMenu(true)
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
        setupToolbar()
        bindViews(fileInfo, category)
    }

    private fun bindViews(fileInfo: FileInfo, category: Category) {
        val path = fileInfo.filePath
        val pathText = sheetView?.findViewById<TextView>(R.id.textPath)

        if (path == null) {
            pathText?.visibility = View.GONE
            sheetView?.findViewById<TextView>(R.id.textPathPlaceholder)?.visibility = View.GONE
        } else {
            pathText?.text = path
        }

        val icon = sheetView?.findViewById<ImageView>(R.id.imageIcon)
        icon?.let { setIcon(context, it, uri) }

        val nameText = sheetView?.findViewById<TextView>(R.id.textName)
        nameText?.text = fileInfo.fileName

        val dateText = sheetView?.findViewById<TextView>(R.id.textDateModified)
        bindDate(dateText, category, fileInfo)

        val sizeText = sheetView?.findViewById<TextView>(R.id.textFileSize)
        sizeText?.text = Formatter.formatFileSize(context, fileInfo.size)

        val context = context
        if (CategoryHelper.isAnyImagesCategory(category) && path != null && context != null) {
            val resolutionText = sheetView?.findViewById<TextView>(R.id.textResolution)
            resolutionText?.text = String.format(Locale.getDefault(), context.getString(R.string.resolution_format),
                    fileInfo.width, fileInfo.height)
        } else {
            sheetView?.findViewById<TextView>(R.id.textResolution)?.visibility = View.GONE
            sheetView?.findViewById<TextView>(R.id.textResolutionPlaceholder)?.visibility = View.GONE
        }
    }

    private fun setIcon(context: Context?, icon: ImageView, uri: Uri?) {
        context?.let {
            Glide.with(it).load(uri)
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                    .into(icon)
        }
    }

    private fun setupToolbar() {
        val toolbar = sheetView?.findViewById<Toolbar>(R.id.toolbar)
        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.supportActionBar?.title = ""
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.media_info_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> dismiss()
            R.id.action_share -> {
                context?.let { ShareHelper.shareImage(it, uri) }
            }
        }
        return super.onOptionsItemSelected(item)
    }


}