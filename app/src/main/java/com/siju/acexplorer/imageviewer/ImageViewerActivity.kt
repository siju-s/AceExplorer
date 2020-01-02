package com.siju.acexplorer.imageviewer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.imageviewer.model.ImageViewerModelImpl
import com.siju.acexplorer.imageviewer.presenter.ImageViewerPresenter
import com.siju.acexplorer.imageviewer.presenter.ImageViewerPresenterImpl
import com.siju.acexplorer.imageviewer.view.ImageViewerUiView
import com.siju.acexplorer.imageviewer.view.ImageViewerView
import com.siju.acexplorer.imageviewer.viewmodel.ImageViewerViewModel
import com.siju.acexplorer.imageviewer.viewmodel.ImageViewerViewModelFactory

private const val KEY_POS = "pos"
private const val KEY_URI_LIST = "list"

class ImageViewerActivity : AppCompatActivity() {

    private lateinit var view: ImageViewerView
    private lateinit var viewModel : ImageViewerViewModel

    companion object {
        fun createImageViewer(context: Context, data: ArrayList<Uri?>, pos: Int) {
            val intent = Intent(context, ImageViewerActivity::class.java)
            intent.putExtra(KEY_POS, pos)
            intent.putExtra(KEY_URI_LIST, data)
            context.startActivity(intent)
        }
    }

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


    private fun setup(intent: Intent) {
        var pos = 0
        var list = arrayListOf<Uri?>()
        if (isViewIntent(intent)) {
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
            list = intent.getSerializableExtra(KEY_URI_LIST) as ArrayList<Uri?>

        }
        setupUI(pos, list)
    }

    private fun isViewIntent(intent: Intent) = intent.action == Intent.ACTION_VIEW

    private fun setupUI(pos: Int, list: ArrayList<Uri?>) {
        view = findViewById<ImageViewerUiView>(R.id.container)
        view.setActivity(this)
        view.setPosition(pos)
        view.setFileInfoList(list)

        val model = ImageViewerModelImpl(AceApplication.appContext)

        val presenter = ImageViewerPresenterImpl(view, model)
        setupViewModel(view, presenter)
        presenter.inflateView()
        setupListener()
    }

    private fun setupViewModel(view: ImageViewerView, presenter : ImageViewerPresenter) {
        val factory = ImageViewerViewModelFactory(view, presenter)
        viewModel = ViewModelProviders.of(this, factory).get(ImageViewerViewModel::class.java)
        view.setViewModel(viewModel)
    }

    private fun setupListener() {
        viewModel.fileData.observe(this, Observer{
            view.onFileInfoFetched(it)
        })
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_image_viewer, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
            R.id.action_share -> view.shareClicked()
            R.id.action_info -> view.infoClicked()
            R.id.action_delete -> view.deleteClicked()
        }
        return super.onOptionsItemSelected(item)
    }

}