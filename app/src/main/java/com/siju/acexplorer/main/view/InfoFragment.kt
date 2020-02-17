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
import com.siju.acexplorer.main.viewmodel.InfoSharedViewModel
import com.siju.acexplorer.utils.Clipboard
import com.siju.acexplorer.utils.ThumbnailUtils
import java.util.*

private const val TAG_INFO = "Info"

class InfoFragment : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(fragmentManager: FragmentManager?): InfoFragment {
            val infoFragment = InfoFragment()
            fragmentManager?.let { infoFragment.show(it, TAG_INFO) }
            return infoFragment
        }
    }

    private var infoSharedViewModel: InfoSharedViewModel? = null
    private lateinit var category: Category
    private lateinit var fileInfo: FileInfo

    private var bottomSheetDialog: BottomSheetDialog? = null
    private var sheetView: View? = null
    private var uri: Uri? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("Info", "attach")
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        Log.d("Info", "onCreateDialog")
        sheetView = LayoutInflater.from(context).inflate(R.layout.media_info, null)
        sheetView?.let {
            bottomSheetDialog?.setContentView(it)
        }
        infoSharedViewModel = activity?.let { ViewModelProvider(it).get(InfoSharedViewModel::class.java) }
        setHasOptionsMenu(true)
        initObservers()
        return bottomSheetDialog as BottomSheetDialog
    }

    private fun initObservers() {
        Log.d("INfo", "fileInfo:${infoSharedViewModel?.fileInfo?.value}")
        infoSharedViewModel?.fileInfo?.observe(this, androidx.lifecycle.Observer {
            it?.apply {
                this@InfoFragment.fileInfo = it
                this@InfoFragment.category = it.category
                showInfo(fileInfo, category)
            }
        })
        infoSharedViewModel?.uri?.observe(this, androidx.lifecycle.Observer {
            it?.apply {
                this@InfoFragment.uri = it
            }
        })
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
        bindSize(sizeText)

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

    private fun bindSize(sizeText: TextView?) {
        val isDirectory = fileInfo.isDirectory
        val fileNoOrSize: String
        fileNoOrSize = if (isDirectory) {
            when (val childFileListSize = fileInfo.size.toInt()) {
                0 -> {
                    context!!.getString(R.string.empty)
                }
                -1 -> {
                    ""
                }
                else -> {
                    context!!.resources.getQuantityString(R.plurals.number_of_files,
                            childFileListSize,
                            childFileListSize)
                }
            }
        } else {
            val size = fileInfo.size
            Formatter.formatFileSize(context, size)
        }
        sizeText?.text = fileNoOrSize
    }

    private fun setIconListener(icon: ImageView, path: String?, fileInfo: FileInfo) {
        if (category == Category.AUDIO || CategoryHelper.isAnyVideoCategory(category) ||
                CategoryHelper.isAnyImagesCategory(category) && path != null) {
            icon.setOnClickListener {
                ViewHelper.viewFile(it.context, path, fileInfo.extension)
            }
        }
    }

    private fun setIcon(context: Context?, icon: ImageView, category: Category, uri: Uri?) {
        if (context == null) {
            return
        }
        ThumbnailUtils.displayThumb(context, fileInfo, category, icon, null, uri)
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
        if (::fileInfo.isInitialized) {
            if (fileInfo.isDirectory) {
                menu.findItem(R.id.action_share).isVisible = false
            }
            if (fileInfo.filePath == null) {
                menu.findItem(R.id.action_copy_path).isVisible = false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> dismiss()
            R.id.action_share -> {
                context?.let { ShareHelper.shareMedia(it, fileInfo.category, uri) }
            }

            R.id.action_copy_path -> {
                Analytics.getLogger().pathCopied()
                Clipboard.copyTextToClipBoard(context, fileInfo.filePath)
                Toast.makeText(context, context!!.getString(R.string.text_copied_clipboard), Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }


}