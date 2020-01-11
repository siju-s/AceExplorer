package com.siju.acexplorer.imageviewer.view

import android.annotation.SuppressLint
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
import androidx.viewpager.widget.ViewPager
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.imageviewer.viewmodel.ImageViewerViewModel
import com.siju.acexplorer.main.view.InfoFragment


class ImageViewerUiView(context: Context?, attrs: AttributeSet?) : RelativeLayout(context, attrs),
        ImageViewerView,
        ViewPager.OnPageChangeListener,
        View.OnClickListener,
        PopupMenu.OnMenuItemClickListener {

    private lateinit var viewModel: ImageViewerViewModel
    private lateinit var activity: AppCompatActivity
    private lateinit var pager: ViewPager
    private lateinit var pagerAdapter: ImageViewerPagerAdapter
    private lateinit var topContainer: RelativeLayout
    private lateinit var backButton: ImageButton
    private lateinit var shareButton: ImageButton
    private lateinit var overflowButton: ImageButton
    private lateinit var titleText: TextView

    private var uriList = arrayListOf<Uri?>()
    private var pathList = arrayListOf<String?>()

    private var pos = 0

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

    override fun inflate() {
        LayoutInflater.from(context).inflate(R.layout.image_viewer, this, true)
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
            InfoFragment.newInstance(activity.supportFragmentManager, fileInfo, uriList[pager.currentItem])
        }
    }

    override fun shareClicked() {
        viewModel.shareClicked(uriList[pager.currentItem])
    }

    override fun deleteClicked() {
        viewModel.deleteClicked(uriList[pager.currentItem])
    }

    override fun infoClicked() {
        val uri = uriList[pager.currentItem]
        Log.e("View", "info:$uri")
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
        popupMenu.setOnMenuItemClickListener(this)
        popupMenu.show()
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
//        Log.e(this.javaClass.simpleName, "onPageSelected:${uriList[position].fileName}")
//        setToolbarTitle(uriList[position].fileName)
    }


}