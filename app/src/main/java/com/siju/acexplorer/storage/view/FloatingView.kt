package com.siju.acexplorer.storage.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.getbase.floatingactionbutton.FloatingActionButton
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.siju.acexplorer.R
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.theme.Theme

@SuppressLint("ClickableViewAccessibility")
class FloatingView(view: View,
                   private val baseFileListFragment: BaseFileListFragment) : View.OnClickListener {

    private val context: Context = view.context
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
        fabContainer.background.alpha = 0
    }

    private fun makeFabMenuOpaque() {
        fabContainer.background.alpha = 240
    }

    fun setTheme(theme: Theme) {
        val backgroundColor = if (Theme.isDarkColoredTheme(context.resources, theme)) {
            R.color.dark_overlay
        }
            else {
            R.color.whiteOverlay
        }
        fabContainer.setBackgroundColor(ContextCompat.getColor(context, backgroundColor))
        makeFabMenuTransparent()
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
