package com.siju.acexplorer.storage.presenter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.storage.model.backstack.BackStackInfo
import com.siju.acexplorer.storage.model.operations.OperationHelper
import com.siju.acexplorer.storage.model.operations.Operations
import com.siju.acexplorer.storage.modules.zipviewer.ZipViewerCallback
import com.siju.acexplorer.storage.view.Navigation
import com.siju.acexplorer.storage.viewmodel.FileListViewModel

class ZipPresenterImpl(private val viewModel: FileListViewModel, private val navigation : Navigation,
                       private val backStackInfo: BackStackInfo) : ZipPresenter {
    override var isZipMode = false
    private val _showCompressDialog = MutableLiveData<Triple<Operations, String, ArrayList<FileInfo>>>()
    private val _showZipDialog = MutableLiveData<Triple<Operations, String, String>>()

    override val showCompressDialog: LiveData<Triple<Operations, String, ArrayList<FileInfo>>>
        get() = _showCompressDialog

    override val getShowZipDialogLiveData: LiveData<Triple<Operations, String, String>>
        get() = _showZipDialog


    override val zipCallback = object : ZipViewerCallback {
        override fun removeZipScrollPos(newPath: String?) {

        }
        override fun onZipModeEnd(dir: String?) {
            isZipMode = false
            if (dir == null) {
                backStackInfo.removeLastEntry()
            }
            else {
                removeBackStackEntry(dir)
            }
            viewModel.onZipModeEnd(backStackInfo.getCurrentBackStack()?.first)
        }

        override fun calculateZipScroll(dir: String) {
        }

        override fun onZipContentsLoaded(data: ArrayList<FileInfo>) {
            viewModel.onZipContentsLoaded(data)
        }

        override fun openZipViewer(currentDir: String) {
        }

        override fun setNavDirectory(path: String, isHomeScreenEnabled: Boolean,
                                     category: Category) {
            navigation.setNavDirectory(path, category)
        }

        override fun addToBackStack(path: String, category: Category) {
            backStackInfo.addToBackStack(path, category)
        }

        override fun removeFromBackStack() {
            backStackInfo.removeLastEntry()
        }

        override fun setInitialDir(path: String) {
            navigation.setInitialDir(path, Category.FILES)
        }

    }

    private fun removeBackStackEntry(dir: String?) {
        var pos = 0
        val backStack = backStackInfo.getBackStack()
        for (index in 0 until backStack.size) {
            if (backStack[index].filePath == dir) {
                pos = index
                break
            }
        }
        for (index in backStack.size - 1 downTo pos) {
            if (index > 0) {
                backStackInfo.removeEntryAtIndex(index)
            }
        }
    }

    override val zipOperationCallback = object : OperationHelper.ZipOperationCallback {
        override fun onZipOperationStarted(operation: Operations, destinationDir: String,
                                           filesToArchive: ArrayList<FileInfo>) {
            _showCompressDialog.postValue(Triple(operation, destinationDir, filesToArchive))
        }

        override fun onZipOperationStarted(operation: Operations, sourceFilePath: String,
                                           destinationDir: String) {
            _showZipDialog.postValue(Triple(operation, sourceFilePath, destinationDir))
        }
    }
}