package com.siju.acexplorer.imageviewer.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo




class ImageViewerUiView(context: Context?, attrs: AttributeSet?) : RelativeLayout(context, attrs),
        ImageViewerView, ViewPager.OnPageChangeListener, View.OnSystemUiVisibilityChangeListener {

    private lateinit var activity: AppCompatActivity
    private lateinit var pager: ViewPager
    private lateinit var toolbar: Toolbar
    private var pos = 0
    private var fileList = arrayListOf<FileInfo>()
    private lateinit var pagerAdapter: ImageViewerPagerAdapter
    private var mLastSystemUiVis: Int = 0
    override fun setActivity(activity: AppCompatActivity) {
        this.activity = activity
    }

    override fun setPosition(pos: Int) {
        this.pos = pos
    }

    override fun setFileInfoList(list: ArrayList<FileInfo>) {
        this.fileList = list
    }

    override fun inflate() {
        LayoutInflater.from(context).inflate(R.layout.image_viewer, this, true)
        setupUI()
    }

    private fun setupUI() {
        setupToolbar()
        setOnSystemUiVisibilityChangeListener(this)
        pager = findViewById(R.id.pager)
        pagerAdapter = ImageViewerPagerAdapter(context, fileList)
        pager.addOnPageChangeListener(this)
        pager.adapter = pagerAdapter
        pager.setCurrentItem(pos, true)
        setNavVisibility(true)
    }


    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.setHomeButtonEnabled(true)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setToolbarTitle(title : String?) {
        activity.supportActionBar?.title = title
    }

    private fun setNavVisibility(visible: Boolean) {
        var newVis = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        if (!visible) {
            newVis = newVis or (View.SYSTEM_UI_FLAG_LOW_PROFILE or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }

        // If we are now visible, schedule a timer for us to go invisible.
        if (visible) {
            handler?.postDelayed(mNavHider, 2000)
        }

        // Set the new desired visibility.
        systemUiVisibility = newVis
        toolbar.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    private var mNavHider: Runnable = Runnable { setNavVisibility(false) }


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
        Log.e(this.javaClass.simpleName, "onPageSelected:${fileList[position].fileName}")
        setToolbarTitle(fileList[position].fileName)
    }


}