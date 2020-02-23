package com.siju.acexplorer.storage.presenter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.siju.acexplorer.R
import com.siju.acexplorer.analytics.Analytics
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.view.dialog.DialogHelper
import com.siju.acexplorer.storage.helper.RecentDataConverter
import com.siju.acexplorer.storage.model.RecentTimeData
import com.siju.acexplorer.storage.model.RecentTimeHelper
import com.siju.acexplorer.storage.model.StorageModel
import com.siju.acexplorer.storage.model.operations.Operations
import com.siju.acexplorer.storage.view.MultiSelectionHelper
import com.siju.acexplorer.storage.viewmodel.FileListViewModel

class OperationPresenterImpl(private val viewModel: FileListViewModel, private val multiSelectionHelper: MultiSelectionHelper,
                             private val model: StorageModel) : OperationPresenter {

    private val _multiSelectionOperationData = MutableLiveData<Pair<Operations, ArrayList<FileInfo>>>()
    private val _singleOpData = MutableLiveData<Pair<Operations, FileInfo>>()
    private val _noOpData = MutableLiveData<Pair<Operations, String>>()

    override var currentDir: String? = null

    override var category: Category? = null

    override fun getMultiSelectionOpData() = _multiSelectionOperationData

    override fun getPasteOpData() = pasteOpPresenter._pasteOpData

    override fun getSingleOpData() = _singleOpData

    override fun getNoOpData() = _noOpData

    override fun getPasteData() = pasteOpPresenter._pasteData

    override fun getDragEvent() = dragOperation.dragEvent

    override val showDragDialog: LiveData<Triple<String?, ArrayList<FileInfo>, DialogHelper.DragDialogListener>>
        get() = dragOperation.showDragDialog

    private val pasteOpPresenter = PasteOpPresenter(model)
    private val dragOperation = DragOperation(viewModel, this)

    private var isPasteVisible = false

    override val getPasteConflictListener: DialogHelper.PasteConflictListener
        get() = pasteOpPresenter.pasteConflictListener

    override fun onFabClicked(operation: Operations, path: String?) {
        when (operation) {
            Operations.FOLDER_CREATION, Operations.FILE_CREATION -> {
                Analytics.logger.operationClicked(Analytics.Logger.EV_FAB)
                path?.let {
                    _noOpData.value = Pair(operation, path)
                }
            }
            else -> {
            }
        }
    }

    override fun onUpTouchEvent() {
        dragOperation.onUpTouchEvent()
    }

    override fun onMoveTouchEvent(category: Category) {
        dragOperation.onMoveTouchEvent(multiSelectionHelper.getSelectedCount(), category)
    }

    override fun isDragNotStarted() = dragOperation.isDragNotStarted()

    override fun showPasteDialog() = pasteOpPresenter._showPasteDialog

    override fun handleMenuItemClick(itemId: Int) {
        when (itemId) {
            R.id.action_edit -> {
                onEditClicked()
            }

            R.id.action_hide -> {
                onHideClicked()
            }

            R.id.action_info -> {
                onInfoClicked()
            }

            R.id.action_delete -> {
                onDeleteClicked()
            }

            R.id.action_share -> {
                onShareClicked()
            }

            R.id.action_copy -> {
                onCopyClicked()
            }

            R.id.action_cut -> {
                onCutClicked()
            }

            R.id.action_paste -> {
                onPasteClicked()
            }

            R.id.action_extract -> {
                onExtractClicked()
            }

            R.id.action_archive -> {
                onArchiveClicked()
            }

            R.id.action_fav -> {
                onAddToFavClicked()
            }

            R.id.action_delete_fav -> {
                onDeleteFavClicked()
            }
            R.id.action_permissions -> {
                onPermissionClicked()
            }
            R.id.action_select_all -> {
                onSelectAllClicked()
            }
        }
    }

    private fun onSelectAllClicked() {
        if (RecentTimeHelper.isRecentTimeLineCategory(category)) {
            onRecentSelectAllClicked()
            return
        }
        val list = viewModel.fileData.value
        list?.let {
            if (multiSelectionHelper.getSelectedCount() < list.size) {
                selectAllItems(list)
            }
            else {
                unselectAllItems()
            }
            viewModel.handleActionModeClick(null)
        }
    }

    private fun onRecentSelectAllClicked() {
        val data = RecentDataConverter.getRecentItemListWithHeaderCount(viewModel.recentFileData.value?.second)
        val headerCount = data.first.size
        val fileList = data.second
        if (multiSelectionHelper.getSelectedCount() + headerCount < fileList.size ) {
            selectAllRecentItems(fileList, data.first, viewModel.recentFileData.value?.second)
        }
        else {
            unselectAllItems()
        }
        viewModel.handleActionModeClick(null)
    }

    private fun selectAllRecentItems(list: ArrayList<FileInfo>, headerPosList: ArrayList<Int>, dataItem: ArrayList<RecentTimeData.RecentDataItem>?) {
        for (item in list.withIndex()) {
            if (!headerPosList.contains(item.index)) {
                val recentItem = dataItem?.get(item.index)
                if (recentItem is RecentTimeData.RecentDataItem.Item) {
                    multiSelectionHelper.selectAll(item.index, recentItem.headerType)
                }
            }
        }
        multiSelectionHelper.refresh()
    }

    private fun selectAllItems(list: ArrayList<FileInfo>) {
        for (item in list.withIndex()) {
            multiSelectionHelper.selectAll(item.index)
        }
        multiSelectionHelper.refresh()
    }

    private fun unselectAllItems() {
        multiSelectionHelper.unselectAll()
        viewModel.endActionMode()
    }

    private fun onDeleteFavClicked() {
        if (hasSelectedItems()) {
            Analytics.logger.operationClicked(Analytics.Logger.EV_DELETE_FAV)
            val favList = arrayListOf<FileInfo>()
            val selectedItems = multiSelectionHelper.selectedItems
            for (i in 0 until selectedItems.size()) {
                val fileInfo = viewModel.fileData.value?.get(selectedItems.keyAt(i))
                fileInfo?.let { favList.add(it) }
            }
            viewModel.endActionMode()
            _multiSelectionOperationData.value = Pair(Operations.DELETE_FAVORITE, favList)
        }
    }

    private fun onPermissionClicked() {
        if (hasSelectedItems()) {
            Analytics.logger.operationClicked(Analytics.Logger.EV_PERMISSIONS)
            val fileInfo = viewModel.fileData.value?.get(multiSelectionHelper.selectedItems.keyAt(0))
            viewModel.endActionMode()
            fileInfo?.let {
                _singleOpData.value = Pair(Operations.PERMISSIONS, fileInfo)
            }
        }
    }


    private fun onAddToFavClicked() {
        if (hasSelectedItems()) {
            Analytics.logger.operationClicked(Analytics.Logger.EV_ADD_FAV)
            val favList = arrayListOf<FileInfo>()
            val selectedItems = multiSelectionHelper.selectedItems
            for (i in 0 until selectedItems.size()) {
                val fileInfo = viewModel.fileData.value?.get(selectedItems.keyAt(i))
                if (fileInfo?.isDirectory == true) {
                    favList.add(fileInfo)
                }
            }
            viewModel.endActionMode()
            _multiSelectionOperationData.value = Pair(Operations.FAVORITE, favList)
        }
    }

    private fun onArchiveClicked() {
        if (hasSelectedItems()) {
            Analytics.logger.operationClicked(Analytics.Logger.EV_ARCHIVE)
            val filesToArchive = arrayListOf<FileInfo>()
            val selectedItems = multiSelectionHelper.selectedItems
            for (i in 0 until selectedItems.size()) {
                val fileInfo = viewModel.fileData.value?.get(selectedItems.keyAt(i))
                fileInfo?.let { filesToArchive.add(it) }
            }
            viewModel.endActionMode()
            _multiSelectionOperationData.value = Pair(Operations.COMPRESS, filesToArchive)
        }
    }

    private fun onExtractClicked() {
        if (hasSelectedItems()) {
            Analytics.logger.operationClicked(Analytics.Logger.EV_EXTRACT)
            val fileInfo = viewModel.fileData.value?.get(multiSelectionHelper.selectedItems.keyAt(0))
            viewModel.endActionMode()
            fileInfo?.let {
                _singleOpData.value = Pair(Operations.EXTRACT, fileInfo)
            }
        }
    }

    private fun onPasteClicked() {
        val operations = _multiSelectionOperationData.value?.first
        if (operations == Operations.COPY || operations == Operations.CUT) {
            Analytics.logger.operationClicked(Analytics.Logger.EV_PASTE)
            val list = _multiSelectionOperationData.value?.second
            viewModel.endActionMode()
            list?.let {
                pasteOpPresenter.createPasteOpData(operations, list, currentDir)
            }
        }
    }

    private fun onCutClicked() {
        if (hasSelectedItems()) {
            Analytics.logger.operationClicked(Analytics.Logger.EV_CUT)
            val filesToMove = arrayListOf<FileInfo>()
            val selectedItems = multiSelectionHelper.selectedItems
            for (i in 0 until selectedItems.size()) {
                val fileInfo = viewModel.fileData.value?.get(selectedItems.keyAt(i))
                fileInfo?.let { filesToMove.add(it) }
            }
            viewModel.endActionMode()
            isPasteVisible = true
            _multiSelectionOperationData.value = Pair(Operations.CUT, filesToMove)
        }
    }

    private fun onCopyClicked() {
        if (hasSelectedItems()) {
            Analytics.logger.operationClicked(Analytics.Logger.EV_COPY)
            val filesToCopy = arrayListOf<FileInfo>()
            val selectedItems = multiSelectionHelper.selectedItems
            for (i in 0 until selectedItems.size()) {
                val fileInfo = viewModel.fileData.value?.get(selectedItems.keyAt(i))
                fileInfo?.let { filesToCopy.add(it) }
            }
            viewModel.endActionMode()
            isPasteVisible = true
            _multiSelectionOperationData.value = Pair(Operations.COPY, filesToCopy)
        }
    }

    private fun onShareClicked() {
        if (hasSelectedItems()) {
            Analytics.logger.operationClicked(Analytics.Logger.EV_SHARE)
            val filesToShare = arrayListOf<FileInfo>()
            val selectedItems = multiSelectionHelper.selectedItems
            for (i in 0 until selectedItems.size()) {
                val index = selectedItems.keyAt(i)
                val fileInfo =  if (RecentTimeHelper.isRecentTimeLineCategory(category)) {
                    RecentDataConverter.getRecentItemList(viewModel.recentFileData.value?.second)[index]
                }
                else {
                    viewModel.fileData.value?.get(index)
                }
                if (fileInfo?.isDirectory == false) {
                    filesToShare.add(fileInfo)
                }
            }
            viewModel.endActionMode()
            _multiSelectionOperationData.value = Pair(Operations.SHARE, filesToShare)
        }
    }

    private fun onDeleteClicked() {
        if (hasSelectedItems()) {
            Analytics.logger.operationClicked(Analytics.Logger.EV_DELETE)
            val filesToDelete = arrayListOf<FileInfo>()
            val selectedItems = multiSelectionHelper.selectedItems
            for (i in 0 until selectedItems.size()) {
                val index = selectedItems.keyAt(i)
                val fileInfo =  if (RecentTimeHelper.isRecentTimeLineCategory(category)) {
                    RecentDataConverter.getRecentItemList(viewModel.recentFileData.value?.second)[index]
                }
                else {
                    viewModel.fileData.value?.get(index)
                }
                fileInfo?.let { filesToDelete.add(it) }
            }
            viewModel.endActionMode()
            _multiSelectionOperationData.value = Pair(Operations.DELETE, filesToDelete)
        }
    }

    private fun onInfoClicked() {
        if (hasSelectedItems()) {
            Analytics.logger.operationClicked(Analytics.Logger.EV_PROPERTIES)
            val index = multiSelectionHelper.selectedItems.keyAt(0)
            val fileInfo =  if (RecentTimeHelper.isRecentTimeLineCategory(category)) {
                RecentDataConverter.getRecentItemList(viewModel.recentFileData.value?.second)[index]
            }
            else {
                viewModel.fileData.value?.get(index)
            }
            viewModel.endActionMode()
            fileInfo?.let {
                _singleOpData.value = Pair(Operations.INFO, fileInfo)
            }
        }
    }

    private fun onHideClicked() {
        if (hasSelectedItems()) {
            Analytics.logger.operationClicked(Analytics.Logger.EV_HIDE)
            val index = multiSelectionHelper.selectedItems.keyAt(0)
            val fileInfo =  if (RecentTimeHelper.isRecentTimeLineCategory(category)) {
                RecentDataConverter.getRecentItemList(viewModel.recentFileData.value?.second)[index]
            }
            else {
                viewModel.fileData.value?.get(index)
            }
            viewModel.endActionMode()
            fileInfo?.let {
                _singleOpData.value = Pair(Operations.HIDE, fileInfo)
                onHideOperation(it)
            }
        }
    }

    private fun onHideOperation(fileInfo: FileInfo) {
        val fileName = fileInfo.fileName ?: return
        val newName = if (fileName.startsWith(".")) {
            fileName.substring(1)
        } else {
            ".$fileName"
        }
        onOperation(Operations.HIDE, newName)
    }

    override fun onOperation(operation: Operations?, newFileName: String?) {
        when (operation) {
            Operations.RENAME, Operations.HIDE -> {
                rename(newFileName, operation)
            }
            Operations.FOLDER_CREATION -> {
                createFolder(newFileName, operation)
            }

            Operations.FILE_CREATION -> {
                createFile(newFileName, operation)
            }

            Operations.COMPRESS -> {
                compressFiles(newFileName)
            }
            else -> {
            }
        }
    }

    private fun compressFiles(name: String?) {
        val filesToArchive = _multiSelectionOperationData.value?.second
        filesToArchive?.let {
            currentDir?.let { currentDir ->
                val destinationDir = "$currentDir/$name.zip"
                model.compressFile(destinationDir, filesToArchive,
                        viewModel.zipOperationCallback)
            }
        }
    }

    private fun createFile(name: String?, operation: Operations) {
        val path = _noOpData.value?.second
        if (path != null && name != null) {
            model.createFile(operation, path, name)
        }
    }

    private fun createFolder(name: String?, operation: Operations) {
        val path = _noOpData.value?.second
        if (path != null && name != null) {
            model.createFolder(operation, path, name)
        }
    }

    private fun rename(name: String?, operation: Operations) {
        val path = _singleOpData.value?.second?.filePath
        if (path != null && name != null) {
            model.renameFile(operation, path, name)
        }
    }

    private fun onEditClicked() {
        if (hasSelectedItems()) {
            Analytics.logger.operationClicked(Analytics.Logger.EV_RENAME)
            val index = multiSelectionHelper.selectedItems.keyAt(0)
            val fileInfo =  if (RecentTimeHelper.isRecentTimeLineCategory(category)) {
                RecentDataConverter.getRecentItemList(viewModel.recentFileData.value?.second)[index]
            }
            else {
                viewModel.fileData.value?.get(index)
            }
            viewModel.endActionMode()
            fileInfo?.let {
                _singleOpData.value = Pair(Operations.RENAME, fileInfo)
            }
        }
    }

    override fun checkPasteConflict(path: String, operationData: Pair<Operations, ArrayList<FileInfo>>) {
        pasteOpPresenter.checkPasteConflict(path, operationData)
    }

    override fun onPasteAction(operation: Operations, filesToPaste: ArrayList<FileInfo>, destinationDir: String?) {
        pasteOpPresenter.createPasteOpData(operation, filesToPaste, destinationDir)
        viewModel.endActionMode()
    }

    override fun toggleDragData(fileInfo: FileInfo) {
        dragOperation.toggleDragData(fileInfo)
    }

    override fun clearDraggedData() {
        dragOperation.clearDragData()
    }

    override fun onDragDropEvent(pos: Int) {
        dragOperation.onDragDropEvent(pos, currentDir)
    }

    override fun setLongPressedTime(longPressedTime: Long) {
        dragOperation.setLongPressedTime(longPressedTime)
    }

    override fun onDragStarted() {
        dragOperation.onDragStarted()
    }

    override fun onDragEnded() {
        isPasteVisible = false
        dragOperation.dragEnded()
    }

    override fun isNotPasteOperation() = !isPasteVisible

    private fun hasSelectedItems() = multiSelectionHelper.hasSelectedItems()


}