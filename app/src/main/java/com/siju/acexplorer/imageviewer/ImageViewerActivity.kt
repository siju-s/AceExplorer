package com.siju.acexplorer.imageviewer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.imageviewer.model.ImageViewerModelImpl
import com.siju.acexplorer.imageviewer.presenter.ImageViewerPresenterImpl
import com.siju.acexplorer.imageviewer.view.ImageViewerUiView
import com.siju.acexplorer.imageviewer.view.ImageViewerView

private const val KEY_POS = "pos"
private const val KEY_FILE_LIST = "list"

class ImageViewerActivity : AppCompatActivity() {

    companion object {
        fun createImageViewer(context : Context, data : ArrayList<FileInfo>, pos : Int) {
            val intent = Intent(context, ImageViewerActivity::class.java)
            intent.putExtra(KEY_POS, pos)
            intent.putExtra(KEY_FILE_LIST, data)
            context.startActivity(intent)
        }
    }

    private lateinit var view: ImageViewerView

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
        val pos = intent.getIntExtra(KEY_POS, 0)
        val list = intent.getParcelableArrayListExtra<FileInfo>(KEY_FILE_LIST)

        view = findViewById<ImageViewerUiView>(R.id.container)
        view.setActivity(this)
        view.setPosition(pos)
        view.setFileInfoList(list)

        val model = ImageViewerModelImpl()

        val presenter = ImageViewerPresenterImpl(view, model)
        presenter.inflateView()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_image_viewer, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}