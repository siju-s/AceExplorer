package com.siju.acexplorer.imageviewer

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Process
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.siju.acexplorer.R
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.imageviewer.model.ImageViewerModelImpl
import com.siju.acexplorer.imageviewer.presenter.ImageViewerPresenter
import com.siju.acexplorer.imageviewer.presenter.ImageViewerPresenterImpl
import com.siju.acexplorer.imageviewer.view.ImageViewerUiView
import com.siju.acexplorer.imageviewer.view.ImageViewerView
import com.siju.acexplorer.imageviewer.viewmodel.ImageViewerViewModel
import com.siju.acexplorer.imageviewer.viewmodel.ImageViewerViewModelFactory

const val KEY_POS       = "pos"
const val SAF_REQUEST_ID = 6000

class ImageViewerActivity : AppCompatActivity() {

    private lateinit var view: ImageViewerView
    private lateinit var viewModel : ImageViewerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_viewer_base)

        val intent = intent
        if (intent == null) {
            finish()
        }
        else {
            setup(intent)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun setup(intent: Intent) {
        var pos = 0
        var list = arrayListOf<Uri?>()
        var pathList = arrayListOf<String?>()

        var writePermission = PackageManager.PERMISSION_GRANTED
        if (intent.getIntExtra(KEY_POS, -1) == -1) {
            var uri = intent.data
            val extras = intent.extras
            if (extras != null && extras.containsKey(Intent.EXTRA_STREAM)) {
                uri = extras.getParcelable(Intent.EXTRA_STREAM)
            }
            if (uri == null) {
                finish()
            }
            else {
                list.add(uri)
                writePermission = checkUriPermission(uri, Process.myPid(), Process.myUid(), Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        }
        else {
            pos = intent.getIntExtra(KEY_POS, 0)
            val imageViewerDataHolder = ImageViewerDataHolder.getInstance()
            if (imageViewerDataHolder == null) {
                finish()
            }
            else {
                list = imageViewerDataHolder.getUriList()
                pathList = imageViewerDataHolder.getPathList()
            }
        }
        setupUI(pos, list, pathList, writePermission == PackageManager.PERMISSION_GRANTED)
    }

    private fun setupUI(pos: Int, list: ArrayList<Uri?>, pathList: ArrayList<String?>, hasWriteAccess: Boolean) {
        view = findViewById<ImageViewerUiView>(R.id.container)
        view.setActivity(this)
        view.setPosition(pos)
        view.setUriList(list)
        if (!hasWriteAccess) {
            view.setNoWriteAccess()
        }
        if (pathList.isNotEmpty()) {
            view.setPathList(pathList)
        }
        val model = ImageViewerModelImpl(this)

        val presenter = ImageViewerPresenterImpl(view, model)
        setupViewModel(view, presenter)
        presenter.inflateView()
        setupListener()
    }

    private fun setupViewModel(view: ImageViewerView, presenter : ImageViewerPresenter) {
        val factory = ImageViewerViewModelFactory(view, presenter)
        viewModel = ViewModelProvider(this, factory).get(ImageViewerViewModel::class.java)
        view.setViewModel(viewModel)
    }

    private fun setupListener() {
        viewModel.fileData.observe(this, {
            view.onFileInfoFetched(it)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == SAF_REQUEST_ID) {
            if (resultCode == Activity.RESULT_OK) {
                val uri = intent?.data
                if (uri == null) {
                    view.onDeleteFailed()
                }
                else {
                    view.handleSafResult(uri, intent.flags)
                }
            }
            else {
                Analytics.logger.safResult(false)
                view.onDeleteFailed()
            }
        }
    }
}