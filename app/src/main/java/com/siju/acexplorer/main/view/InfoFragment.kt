package com.siju.acexplorer.main.view


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.siju.acexplorer.R
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.ShareHelper
import com.siju.acexplorer.main.model.helper.ViewHelper
import com.siju.acexplorer.utils.Clipboard
import com.siju.acexplorer.utils.ThumbnailUtils
import java.util.*

private const val TAG_INFO = "Info"

private const val KEY_URI = "uri"
private const val KEY_FILEINFO = "fileinfo"

class InfoFragment : BottomSheetDialogFragment() {

    companion object {

        fun newInstance(fragmentManager: FragmentManager?, uri: Uri?, fileInfo: FileInfo): InfoFragment {
            val infoFragment = InfoFragment()
            val args = Bundle()
            args.putString(KEY_URI, uri?.toString())
            args.putParcelable(KEY_FILEINFO, fileInfo)
            infoFragment.arguments = args
            fragmentManager?.let { infoFragment.show(it, TAG_INFO) }
            return infoFragment
        }
    }

    private var category: Category? = null
    private var fileInfo : FileInfo? = null
    private var uri : Uri? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var sheetView: View? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("Info", "attach")
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        sheetView = LayoutInflater.from(context).inflate(R.layout.media_info, null)
        sheetView?.let {
            bottomSheetDialog?.setContentView(it)
        }
        setHasOptionsMenu(true)
        setup()
        return bottomSheetDialog as BottomSheetDialog
    }

    private fun setup() {
        arguments?.let { args ->
            val uriString = args.getString(KEY_URI)
            if (uriString != null) {
                uri = Uri.parse(uriString)
            }
            fileInfo = args.getParcelable(KEY_FILEINFO)
            fileInfo?.let { fileInfo ->
                showInfo(fileInfo, fileInfo.category, uri)
            }
        }
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

    private fun showInfo(fileInfo: FileInfo, category: Category?, uri: Uri?) {
        setupUI(fileInfo, category, uri)
    }

    private fun setupUI(fileInfo: FileInfo, category: Category?, uri: Uri?) {
        setupToolbar()
        bindViews(fileInfo, category, uri)
    }

    private fun bindViews(fileInfo: FileInfo, category: Category?, uri: Uri?) {
        val path = fileInfo.filePath
        val pathText = sheetView?.findViewById<TextView>(R.id.textPath)
        val pathPlaceholder = sheetView?.findViewById<TextView>(R.id.textPathPlaceholder)

        if (CategoryHelper.isAppManager(category)) {
            pathPlaceholder?.text = getString(R.string.package_name)
        }

        if (path == null) {
            pathText?.visibility = View.GONE
            pathPlaceholder?.visibility = View.GONE
        } else {
            pathText?.text = path
        }

        val imageThumbIcon = sheetView?.findViewById<ImageView>(R.id.imageThumbIcon)
        if (category == Category.AUDIO || CategoryHelper.isAnyVideoCategory(category)) {
            imageThumbIcon?.visibility = View.VISIBLE
        }

        val icon = sheetView?.findViewById<ImageView>(R.id.imageIcon)
        icon?.let {
            setIcon(context, it, category, uri)
            setIconListener(icon, path, fileInfo)
        }

        val nameText = sheetView?.findViewById<TextView>(R.id.textName)
        nameText?.text = fileInfo.fileName

        val dateText = sheetView?.findViewById<TextView>(R.id.textDateModified)
        bindDate(dateText, category, fileInfo)

        val sizeText = sheetView?.findViewById<TextView>(R.id.textFileSize)
        sizeText?.text = Formatter.formatFileSize(context, fileInfo.size)
        bindSize(sizeText, fileInfo)

        val context = context
        if (CategoryHelper.isAnyImagesCategory(category) && path != null && context != null && fileInfo.width != 0L) {
            val resolutionText = sheetView?.findViewById<TextView>(R.id.textResolution)
            resolutionText?.text = String.format(Locale.getDefault(), context.getString(R.string.resolution_format),
                    fileInfo.width, fileInfo.height)
        } else {
            sheetView?.findViewById<TextView>(R.id.textResolution)?.visibility = View.GONE
            sheetView?.findViewById<TextView>(R.id.textResolutionPlaceholder)?.visibility = View.GONE
        }
    }

    private fun bindSize(sizeText: TextView?, fileInfo: FileInfo) {
        if (sizeText == null) {
            return
        }
        val context = sizeText.context
        val isDirectory = fileInfo.isDirectory
        val fileNoOrSize: String
        fileNoOrSize = if (isDirectory) {
            when (val childFileListSize = fileInfo.size.toInt()) {
                0 -> {
                    context.getString(R.string.empty)
                }
                -1 -> {
                    ""
                }
                else -> {
                    context.resources.getQuantityString(R.plurals.number_of_files,
                            childFileListSize,
                            childFileListSize)
                }
            }
        } else {
            val size = fileInfo.size
            Formatter.formatFileSize(context, size)
        }
        sizeText.text = fileNoOrSize
    }

    private fun setIconListener(icon: ImageView, path: String?, fileInfo: FileInfo) {
        if (category == Category.AUDIO || CategoryHelper.isAnyVideoCategory(category) ||
                CategoryHelper.isAnyImagesCategory(category) && path != null) {
            icon.setOnClickListener {
                ViewHelper.viewFile(it.context, path, fileInfo.extension)
            }
        }
    }

    private fun setIcon(context: Context?, icon: ImageView, category: Category?, uri: Uri?) {
        if (context != null) {
            fileInfo?.let { ThumbnailUtils.displayThumb(context, it, category, icon, null, uri) }
        }
    }

    private fun setupToolbar() {
        val toolbar = sheetView?.findViewById<Toolbar>(R.id.toolbar)
        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.supportActionBar?.title = ""
    }

    private fun bindDate(dateText: TextView?, category: Category?,
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
        fileInfo?.let {fileInfo ->
            if (fileInfo.isDirectory || category == Category.APP_MANAGER) {
                menu.findItem(R.id.action_share).isVisible = false
            }
            if (fileInfo.filePath == null || category == Category.APP_MANAGER) {
                menu.findItem(R.id.action_copy_path).isVisible = false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> dismiss()
            R.id.action_share -> {
                context?.let { ShareHelper.shareMedia(it, fileInfo?.category, uri) }
            }

            R.id.action_copy_path -> {
                onCopyPathClicked()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onCopyPathClicked() {
        Analytics.logger.pathCopied()
        fileInfo?.let {
            Clipboard.copyTextToClipBoard(context, fileInfo?.filePath)
            context?.let {
                Toast.makeText(it, it.getString(R.string.text_copied_clipboard), Toast.LENGTH_SHORT).show()
            }
        }
    }


}