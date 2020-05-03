package com.siju.acexplorer.imageviewer.view

import android.annotation.SuppressLint
import android.app.RecoverableSecurityException
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.extensions.showToast
import com.siju.acexplorer.helper.MediaScannerHelper
import com.siju.acexplorer.helper.SafHelper
import com.siju.acexplorer.imageviewer.SAF_REQUEST_ID
import com.siju.acexplorer.imageviewer.viewmodel.ImageViewerViewModel
import com.siju.acexplorer.main.model.helper.SdkHelper
import com.siju.acexplorer.main.model.helper.UriHelper
import com.siju.acexplorer.main.view.InfoFragment
import com.siju.acexplorer.main.view.dialog.DialogHelper
import com.siju.acexplorer.main.viewmodel.InfoSharedViewModel



const val REQUEST_CODE_DELETE = 1000

class ImageViewerUiView(context: Context?, attrs: AttributeSet?) : RelativeLayout(context, attrs),
        ImageViewerView,
        ViewPager.OnPageChangeListener,
        View.OnClickListener,
        PopupMenu.OnMenuItemClickListener {

    private var noWriteAccess = false
    private lateinit var viewModel: ImageViewerViewModel
    private lateinit var infoSharedViewModel: InfoSharedViewModel
    private lateinit var activity: AppCompatActivity
    private lateinit var pager: ViewPager
    private lateinit var pagerAdapter: ImageViewerPagerAdapter
    private lateinit var topContainer: RelativeLayout
    private lateinit var backButton: ImageButton
    private lateinit var shareButton: ImageButton
    private lateinit var overflowButton: ImageButton
    private lateinit var titleText: TextView
    private var uriList = arrayListOf<Uri?>()
    private var safDocument: DocumentFile? = null

    private var pathList = arrayListOf<String?>()
    private var pos = 0
    private var safRequestComplete = false

    override fun setActivity(activity: AppCompatActivity) {
        this.activity = activity
    }

    override fun setPosition(pos: Int) {
        this.pos = pos
    }

    override fun setUriList(list: ArrayList<Uri?>) {
        this.uriList = list
    }

    override fun setPathList(pathList: ArrayList<String?>) {
        this.pathList = pathList
    }

    override fun setNoWriteAccess() {
        noWriteAccess = true
    }

    override fun inflate() {
        LayoutInflater.from(context).inflate(R.layout.image_viewer, this, true)
        infoSharedViewModel = ViewModelProvider(activity).get(InfoSharedViewModel::class.java)
        setupUI()
    }

    override fun setViewModel(viewModel: ImageViewerViewModel) {
        this.viewModel = viewModel
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupUI() {
        setupTopContainer()
        pager = findViewById(R.id.pager)
        pagerAdapter = ImageViewerPagerAdapter(context, uriList)
        pager.addOnPageChangeListener(this)
        pager.adapter = pagerAdapter
        pager.setCurrentItem(pos, true)
    }

    private fun setupTopContainer() {
        topContainer = findViewById(R.id.topView)
        backButton = findViewById(R.id.imgButtonBack)
        shareButton = findViewById(R.id.imgButtonShare)
        overflowButton = findViewById(R.id.imgButtonOverflow)
        titleText = findViewById(R.id.titleText)
        shareButton.setOnClickListener(this)
        overflowButton.setOnClickListener(this)
        backButton.setOnClickListener(this)
    }


    override fun onFileInfoFetched(fileInfo: FileInfo?) {
        fileInfo?.let {
            infoSharedViewModel.apply {
                setFileInfo(it)
                setUri(uriList[pager.currentItem])
            }
            InfoFragment.newInstance(activity.supportFragmentManager)
        }
    }

    override fun shareClicked() {
        viewModel.shareClicked(uriList[pager.currentItem])
    }

    private fun showDeleteDialog(context: Context, uri: Uri) {
        DialogHelper.showDeleteDialog(context, uri, deleteDialogListener)
    }

    private val deleteDialogListener = object : DialogHelper.DeleteDialogListener {
        override fun onPositiveButtonClick(view: View, isTrashEnabled: Boolean, filesToDelete: java.util.ArrayList<FileInfo>) {

        }

        override fun onPositiveButtonClick(view: View?, isTrashEnabled: Boolean, filesToDelete: Uri) {
            deleteDialogButtonClicked(uriList[pager.currentItem])
        }

    }

    private fun deleteDialogButtonClicked(uri: Uri?) {
        uri ?: return
        try {
            viewModel.deleteClicked(uri)
        } catch (exception: SecurityException) {
            if (SdkHelper.isAtleastAndroid10 && exception is RecoverableSecurityException) {
                activity.startIntentSenderForResult(exception.userAction.actionIntent.intentSender, REQUEST_CODE_DELETE, null, 0, 0, 0)
            } else if (safRequestComplete) {
                onDeleteFailed()
                safRequestComplete = false
            } else {
                safDocument = DocumentFile.fromSingleUri(context, uri)
                showSafDialog(UriHelper.getUriPath(uri))
            }
        }
    }

    override fun handleSafResult(uri: Uri, flags: Int) {
        Log.d("ImageViewerView", "handleSafResult() called with: uri = $uri, flags = $flags}, safUri:${safDocument?.uri}")
        SafHelper.saveSafUri(PreferenceManager.getDefaultSharedPreferences(context), uri)
        SafHelper.persistUriPermission(context, uri)
        safRequestComplete = true
        var documentFile = DocumentFile.fromTreeUri(context, uri)
        val uriToDelete = uriList[pager.currentItem]
        val uriPath = UriHelper.getUriPath(uriToDelete)
        if (documentFile == null || uriToDelete == null || uriPath == null) {
            onDeleteFailed()
            return
        }

        val parts = uriPath.split("/")

        for (i in 3 until parts.size) {
            documentFile = documentFile?.findFile(parts[i])
        }

        if (documentFile == null) {
            onDeleteFailed()
        }
        else {
            if (documentFile.delete()) {
                onDeleteSuccess()
                MediaScannerHelper.scanFiles(context, arrayOf(uriPath))
            }
        }
    }

    private fun showSafDialog(path: String?) {
        if (path == null) {
            onDeleteFailed()
        } else {
            DialogHelper.showSAFDialog(context, path, dialogListener)
        }
    }

    private val dialogListener = object : DialogHelper.AlertDialogListener {
        override fun onPositiveButtonClick(view: View) {
            SafHelper.triggerStorageAccessFramework(activity, SAF_REQUEST_ID)
        }

        override fun onNegativeButtonClick(view: View) {
            onDeleteFailed()
        }

        override fun onNeutralButtonClick(view: View) {

        }
    }

    override fun deleteClicked() {
        uriList[pager.currentItem]?.let { showDeleteDialog(context, it) }
    }

    override fun infoClicked() {
        val uri = uriList[pager.currentItem]
        Log.d("View", "info:$uri")
        if (pathList.isNotEmpty()) {
            val path = pathList[pager.currentItem]
            path?.let {
                viewModel.infoClicked(path)
            }
        } else {
            uri?.let {
                viewModel.infoClicked(uri)
            }
        }
    }

    override fun onDeleteSuccess() {
        context?.showToast(resources.getQuantityString(R.plurals.number_of_files, 1,
                1) +
                " " + resources.getString(R.string.msg_delete_success))
        if (uriList.size == 1) {
            activity.finish()
        } else {
            val currentPos = pager.currentItem
            pagerAdapter.removeItem(currentPos)
        }
    }

    override fun onDeleteFailed() {
        Toast.makeText(context, resources.getString(R.string.msg_delete_failure), Toast.LENGTH_SHORT).show()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgButtonShare -> shareClicked()
            R.id.imgButtonBack -> activity.finish()
            R.id.imgButtonOverflow -> createPopupMenu(v)
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_info -> infoClicked()
            R.id.action_delete -> deleteClicked()
        }
        return false
    }

    private fun createPopupMenu(view: View) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.image_viewer, popupMenu.menu)
        if (noWriteAccess) {
            popupMenu.menu.findItem(R.id.action_delete).isVisible = false
        }
        popupMenu.setOnMenuItemClickListener(this)
        popupMenu.show()
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
//        Log.d(this.javaClass.simpleName, "onPageSelected:${uriList[position].fileName}")
//        setToolbarTitle(uriList[position].fileName)
    }


}