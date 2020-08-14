package com.siju.acexplorer.storage.view

import android.annotation.SuppressLint
import android.view.View
import android.widget.FrameLayout
import com.getbase.floatingactionbutton.FloatingActionButton
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.siju.acexplorer.R
import com.siju.acexplorer.analytics.Analytics

@SuppressLint("ClickableViewAccessibility")
class FloatingView(view: View,
                   private val baseFileListFragment: BaseFileListFragment) : View.OnClickListener {

    private lateinit var fabContainer: FrameLayout
    private lateinit var fabCreateMenu: FloatingActionsMenu
    private lateinit var fabCreateFolder: FloatingActionButton
    private lateinit var fabCreateFile: FloatingActionButton
    private lateinit var fabOperation: FloatingActionButton

    val isFabExpanded: Boolean
        get() = fabCreateMenu.isExpanded

    init {
        initializeViews(view)
        setListeners()
        makeFabMenuTransparent()
    }

    private fun initializeViews(view: View) {
        fabContainer = view.findViewById(R.id.frameLayoutFab)
        fabCreateMenu = view.findViewById(R.id.fabCreate)
        fabCreateFolder = view.findViewById(R.id.fabCreateFolder)
        fabCreateFile = view.findViewById(R.id.fabCreateFile)
        fabOperation = view.findViewById(R.id.fabOperation)
    }

    private fun setListeners() {
        fabCreateFile.setOnClickListener(this)
        fabCreateFolder.setOnClickListener(this)
        fabOperation.setOnClickListener(this)

        fabCreateMenu.setOnFloatingActionsMenuUpdateListener(object :
                                                                     FloatingActionsMenu.OnFloatingActionsMenuUpdateListener {

            override fun onMenuExpanded() {
                makeFabMenuOpaque()
                fabContainer.setOnTouchListener { _, _ ->
                    collapseFab()
                    true
                }
            }

            override fun onMenuCollapsed() {
                makeFabMenuTransparent()
                fabContainer.setOnTouchListener(null)
            }
        })
    }

    private fun makeFabMenuTransparent() {
        val background = fabContainer.background
        background?.let {
            it.alpha = 0
        }
    }

    private fun makeFabMenuOpaque() {
        val background = fabContainer.background
        background?.let {
            it.alpha = 240
        }
    }

    fun showFab() {
        fabContainer.visibility = View.VISIBLE
    }

    fun hideFab() {
        fabContainer.visibility = View.GONE
    }

    fun collapseFab() {
        fabCreateMenu.collapse()
    }

    override fun onClick(view: View) {
        Analytics.logger.operationClicked(Analytics.Logger.EV_FAB)
        when (view.id) {
            R.id.fabCreateFile   -> {
                baseFileListFragment.onCreateFileClicked()
            }
            R.id.fabCreateFolder -> {
                baseFileListFragment.onCreateDirClicked()
            }
        }
        fabCreateMenu.collapse()
    }
}
