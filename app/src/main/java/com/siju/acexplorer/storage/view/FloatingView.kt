package com.siju.acexplorer.storage.view

import android.annotation.SuppressLint
import android.app.Dialog
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
class FloatingView constructor(view: View) : View.OnClickListener {

    private val context: Context = view.context
    private lateinit var fabContainer: FrameLayout
    private lateinit var fabCreateMenu: FloatingActionsMenu
    private lateinit var fabCreateFolder: FloatingActionButton
    private lateinit var fabCreateFile: FloatingActionButton
    private lateinit var fabOperation: FloatingActionButton
    private val dialog: Dialog? = null

    internal val isFabExpanded: Boolean
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
                    fabCreateMenu.collapse()
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
        val backgroundColor = when (theme) {
            Theme.DARK  -> R.color.dark_overlay
            Theme.LIGHT -> R.color.whiteOverlay
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
        when (view.id) {
            R.id.fabCreateFile   -> {
                Analytics.getLogger().operationClicked(Analytics.Logger.EV_FAB)
                //                showCreateFileDialog();
            }
            R.id.fabCreateFolder -> {
                Analytics.getLogger().operationClicked(Analytics.Logger.EV_FAB)
                //                showCreateDirDialog();
            }
        }
        fabCreateMenu.collapse()
    }

    //    private void showCreateFileDialog() {
    //        String title = getContext().getString(R.string.new_file);
    //        String[] texts = new String[]{title, getContext().getString(R.string.enter_name), getContext
    //                ().getString(R.string.create), getContext().getString(R.string.dialog_cancel)};
    //        DialogHelper.showInputDialog(getContext(), texts, Operations.FILE_CREATION, null,
    //                                     dialogListener);
    //    }
    //
    //    void showCreateDirDialog() {
    //        String title = getContext().getString(R.string.new_folder);
    //        String[] texts = new String[]{title, getContext().getString(R.string.enter_name), getContext
    //                ().getString(R.string
    //                                     .create), getContext().getString(R.string.dialog_cancel)};
    //        DialogHelper.showInputDialog(getContext(), texts, Operations.FOLDER_CREATION, null,
    //                                     dialogListener);
    //    }
    //
    //    private DialogHelper.DialogCallback dialogListener = new DialogHelper.DialogCallback() {
    //
    //
    //        @Override
    //        public void onPositiveButtonClick(Dialog dialog, Operations operation, String name) {
    //            FloatingView.this.dialog = dialog;
    //            storagesUiView.setDialog(dialog);
    //            switch (operation) {
    //                case FOLDER_CREATION:
    //                    storagesUiView.createDir(name);
    //                    break;
    //                case FILE_CREATION:
    //                    storagesUiView.createFile(name);
    //                    break;
    //            }
    //        }
    //
    //        @Override
    //        public void onNegativeButtonClick(Operations operations) {
    //
    //        }
    //    };

    fun dismissDialog() {
        dialog?.dismiss()
    }
}
