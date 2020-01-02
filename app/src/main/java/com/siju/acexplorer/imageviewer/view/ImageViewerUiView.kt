package com.siju.acexplorer.imageviewer.view

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.imageviewer.viewmodel.ImageViewerViewModel
import com.siju.acexplorer.main.model.helper.ShareHelper
import com.siju.acexplorer.main.view.dialog.DialogHelper

class ImageViewerUiView(context: Context?, attrs: AttributeSet?) : RelativeLayout(context, attrs),
        ImageViewerView, ViewPager.OnPageChangeListener, View.OnSystemUiVisibilityChangeListener,
View.OnTouchListener
{

    private lateinit var viewModel: ImageViewerViewModel
    private lateinit var activity: AppCompatActivity
    private lateinit var pager: ViewPager
    private lateinit var toolbar: Toolbar
    private lateinit var pagerAdapter: ImageViewerPagerAdapter
    private var uriList = arrayListOf<Uri?>()

    private var isTouched = false
    private var pos = 0
    private var mLastSystemUiVis: Int = 0
    private val viewHandler = Handler(Looper.getMainLooper())

    override fun setActivity(activity: AppCompatActivity) {
        this.activity = activity
    }

    override fun setPosition(pos: Int) {
        this.pos = pos
    }

    override fun setFileInfoList(list: ArrayList<Uri?>) {
        this.uriList = list
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
        setupToolbar()
        setOnSystemUiVisibilityChangeListener(this)
        pager = findViewById(R.id.pager)
        pagerAdapter = ImageViewerPagerAdapter(context, uriList)
        pager.addOnPageChangeListener(this)
        pager.adapter = pagerAdapter
        pager.setCurrentItem(pos, true)
        setNavVisibility(true)
        pager.setOnTouchListener(this)
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        Log.e(this.javaClass.simpleName, "onTOuch:$isTouched")
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> isTouched = true
        }
        return super.onTouchEvent(event)
    }

    override fun onFileInfoFetched(fileInfo: FileInfo?) {
        fileInfo?.let {
            DialogHelper.showInfoDialog(activity, fileInfo, false)
        }
    }

    override fun shareClicked() {
        ShareHelper.shareImage(activity, uriList[pager.currentItem])
    }

    override fun deleteClicked() {
        viewModel.deleteClicked(uriList[pager.currentItem])
    }

    override fun infoClicked() {
        val uri = uriList[pager.currentItem]
        Log.e("View", "info:$uri")
        uri?.let {
            viewModel.infoClicked(uri)
        }
    }

    override fun onDeleteSuccess() {
        if (uriList.size == 1) {
            activity.finish()
        }
        else {
            val currentPos = pager.currentItem
            pagerAdapter.removeItem(currentPos)
        }
    }

    override fun onDeleteFailed() {
        Toast.makeText(context, resources.getString(R.string.msg_delete_failure), Toast.LENGTH_SHORT).show()
    }

    private fun setNavVisibility(visible: Boolean) {
        Log.e(this.javaClass.simpleName, "setNavVisibility:$visible, touched:$isTouched")
        var newVis = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        if (visible && !isTouched) {
            viewHandler.postDelayed(navHider, 2000)
        }
        else {
            newVis = newVis or (View.SYSTEM_UI_FLAG_LOW_PROFILE
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }
        // Set the new desired visibility.
        systemUiVisibility = newVis
        toolbar.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    private var navHider: Runnable = Runnable {
        if (!isTouched) {
            setNavVisibility(false)
        }
    }


    override fun onSystemUiVisibilityChange(visibility: Int) {
        val diff = mLastSystemUiVis xor visibility
        mLastSystemUiVis = visibility
        if (diff and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION != 0 && visibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION == 0) {
            setNavVisibility(true)
        }
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