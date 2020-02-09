package com.siju.acexplorer.imageviewer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.imageviewer.model.ImageViewerModelImpl
import com.siju.acexplorer.imageviewer.presenter.ImageViewerPresenter
import com.siju.acexplorer.imageviewer.presenter.ImageViewerPresenterImpl
import com.siju.acexplorer.imageviewer.view.ImageViewerUiView
import com.siju.acexplorer.imageviewer.view.ImageViewerView
import com.siju.acexplorer.imageviewer.viewmodel.ImageViewerViewModel
import com.siju.acexplorer.imageviewer.viewmodel.ImageViewerViewModelFactory

const val KEY_POS       = "pos"
const val KEY_URI_LIST  = "list"
const val KEY_PATH_LIST = "paths"

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

        if (intent.getIntExtra(KEY_POS, -1) == -1) {
            val uri = intent.data
            if (uri == null) {
                finish()
            }
            else {
                list.add(uri)
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
        setupUI(pos, list, pathList)
    }

    private fun setupUI(pos: Int, list: ArrayList<Uri?>, pathList: ArrayList<String?>) {
        view = findViewById<ImageViewerUiView>(R.id.container)
        view.setActivity(this)
        view.setPosition(pos)
        view.setUriList(list)
        if (pathList.isNotEmpty()) {
            view.setPathList(pathList)
        }
        val model = ImageViewerModelImpl(AceApplication.appContext)

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
        viewModel.fileData.observe(this, Observer{
            view.onFileInfoFetched(it)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        ImageViewerDataHolder.getInstance()?.clearData()
    }

}