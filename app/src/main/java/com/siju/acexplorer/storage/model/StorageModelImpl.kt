package com.siju.acexplorer.storage.model

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.common.PreferenceConstants.PREFS_NAME
import com.siju.acexplorer.common.SortMode
import com.siju.acexplorer.common.SortModeData
import com.siju.acexplorer.common.ViewMode
import com.siju.acexplorer.common.ViewModeData
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.helper.SafHelper.persistUriPermission
import com.siju.acexplorer.helper.SafHelper.saveSafUri
import com.siju.acexplorer.main.model.FileConstants
import com.siju.acexplorer.main.model.data.DataFetcherFactory
import com.siju.acexplorer.main.model.data.DataLoader
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.preferences.PreferenceConstants
import com.siju.acexplorer.storage.helper.SortModeHelper
import com.siju.acexplorer.storage.model.operations.OperationAction
import com.siju.acexplorer.storage.model.operations.OperationHelper
import com.siju.acexplorer.storage.model.operations.Operations
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val PREFS_VIEW_MODE_IMAGE = "view-mode-image"
private const val PREFS_VIEW_MODE_VIDEO = "view-mode-video"

class StorageModelImpl @Inject constructor(@ApplicationContext val context: Context,
                                           private val viewModeInfo: ViewModeData,
                                           private val sortModeData: SortModeData
) : StorageModel {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val globalPreference = PreferenceManager.getDefaultSharedPreferences(context)
    private val operationHelper = OperationHelper(AceApplication.appContext)
    private val _operationData = MutableLiveData<Pair<Operations, OperationAction>>()
    private val mediaObserver = MediaObserver(Handler(Looper.getMainLooper()))
    private var category: Category? = null
    val operationData: LiveData<Pair<Operations, OperationAction>>
        get() = _operationData
    @Suppress("PropertyName")
    val _refreshData = MutableLiveData<Boolean>()
    private var contentObserverRegistered = false

    override fun loadData(path: String?, category: Category): ArrayList<FileInfo> {
        this.category = category
        registerContentObserver()
        return DataLoader.fetchDataByCategory(context,
                DataFetcherFactory.createDataFetcher(category),
                category, path)
    }


    override fun loadRecentData(path: String?, category: Category): ArrayList<RecentTimeData.RecentDataItem> {
        this.category = category
        val data = DataLoader.fetchDataByCategory(context,
                DataFetcherFactory.createDataFetcher(category),
                category, path)
        return RecentTimeData.getRecentTimeData(data)
    }

    override fun getViewMode(): ViewMode = viewModeInfo.getViewMode()

    override fun getImageViewMode(): ViewMode {
        return if (sharedPreferences.contains(PREFS_VIEW_MODE_IMAGE)) {
            ViewMode.getViewModeFromValue(
                    sharedPreferences.getInt(PREFS_VIEW_MODE_IMAGE, ViewMode.GALLERY.value))
        } else {
            ViewMode.GALLERY
        }
    }

    override fun getVideoViewMode(): ViewMode {
        return if (sharedPreferences.contains(PREFS_VIEW_MODE_VIDEO)) {
            ViewMode.getViewModeFromValue(
                    sharedPreferences.getInt(PREFS_VIEW_MODE_VIDEO, ViewMode.GALLERY.value))
        } else {
            ViewMode.GALLERY
        }
    }

    override fun saveViewMode(viewMode: ViewMode?) = viewModeInfo.saveViewMode(viewMode)

    override fun saveImageViewMode(viewMode: ViewMode?) {
        viewMode?.let {
            sharedPreferences.edit().apply {
                putInt(PREFS_VIEW_MODE_IMAGE, viewMode.value)
                apply()
            }
        }
    }

    override fun saveVideoViewMode(viewMode: ViewMode?) {
        viewMode?.let {
            sharedPreferences.edit().apply {
                putInt(PREFS_VIEW_MODE_VIDEO, viewMode.value)
                apply()
            }
        }
    }

    override fun isDualModeEnabled() = globalPreference.getBoolean(FileConstants.PREFS_DUAL_PANE, false)

    override fun shouldShowHiddenFiles() = globalPreference.getBoolean(
            PreferenceConstants.PREFS_HIDDEN, false)

    override fun saveHiddenFileSetting(value: Boolean) {
        Log.d(this.javaClass.name, "saveHiddenFileSetting: value:$value")
        globalPreference.edit().apply {
            putBoolean(PreferenceConstants.PREFS_HIDDEN, value)
            apply()
        }
    }

    override fun getSortMode(): SortMode {
        return SortModeHelper.getSortMode(globalPreference, category)
    }

    override fun saveSortMode(sortMode: SortMode) {
        Log.d(this.javaClass.name, "saveSortMode: value:$sortMode")
        sortModeData.saveSortMode(sortMode)
    }

    override fun renameFile(operation: Operations, filePath: String, newName: String) {
        operationHelper.renameFile(operation, filePath, newName, fileOperationCallback)
    }

    override fun createFolder(operation: Operations, path: String, name: String) {
        operationHelper.createFolder(operation, path, name, fileOperationCallback)
    }

    override fun createFile(operation: Operations, path: String, name: String) {
        operationHelper.createFile(operation, path, name, fileOperationCallback)
    }

    override fun deleteFiles(operation: Operations, files: ArrayList<String>) {
        operationHelper.deleteFiles(operation, files, fileOperationCallback)
    }

    override fun addToFavorite(favList: ArrayList<String>) {
        operationHelper.addToFavorite(context, favList, fileOperationCallback)
    }

    override fun deleteFavorite(favPathList: java.util.ArrayList<String>) {
        operationHelper.deleteFavorite(context, favPathList, fileOperationCallback)
    }

    override fun setPermissions(path: String, permissions: String, dir: Boolean) {
        operationHelper.setPermissions(Operations.PERMISSIONS, path, permissions, dir, fileOperationCallback)
    }

    override fun handleSafResult(uri: Uri, flags: Int) {
        saveSafUri(globalPreference, uri)
        persistUriPermission(context, uri)
        operationHelper.onSafSuccess(fileOperationCallback)
    }

    override fun onExit() {
        operationHelper.cleanup()
        unregisterContentObserver()
    }

    override fun onResume() {
        registerContentObserver()
    }

    override fun onPause() {
    }

    private fun registerContentObserver() {
        if (contentObserverRegistered || category == null) {
            return
        }
        val uri: Uri? = when (category) {
            Category.IMAGES_ALL, Category.GENERIC_IMAGES -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            Category.VIDEO_ALL, Category.GENERIC_VIDEOS -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            Category.ALL_TRACKS, Category.GENERIC_MUSIC -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            else -> MediaStore.Files.getContentUri("external")
        }
        uri?.let {
            context.contentResolver.registerContentObserver(uri, true, mediaObserver)
            mediaObserver.addMediaObserverListener(mediaObserverListener)
        }
        Log.d("StorageModel", "registerContentObserver: $uri")
        contentObserverRegistered = true
    }

    fun setRefreshDataFalse() {
        _refreshData.postValue(false)
    }

    private fun unregisterContentObserver() {
        if (!contentObserverRegistered) {
            return
        }
        Log.d("Model", "unregisterContentObserver:category:$category")
        context.contentResolver.unregisterContentObserver(mediaObserver)
        mediaObserver.removeMediaObserverListener(mediaObserverListener)
        contentObserverRegistered = false

    }

    private val mediaObserverListener = object : MediaObserver.MediaObserverListener {
        override fun onMediaChanged(uri: Uri) {
            Log.d("Model", "onMediaChanged:uri:$uri")
            _refreshData.postValue(true)
        }
    }

    override fun checkPasteWriteMode(destinationDir: String,
                                     files: ArrayList<FileInfo>,
                                     pasteActionInfo: ArrayList<PasteActionInfo>,
                                     operations: Operations,
                                     pasteOperationCallback: OperationHelper.PasteOperationCallback) {
        when (operations) {
            Operations.CUT  -> operationHelper.moveFiles(context, destinationDir, files,
                                                         pasteActionInfo,
                                                         pasteOperationCallback,
                                                         fileOperationCallback)
            Operations.COPY -> operationHelper.copyFiles(context, destinationDir, files,
                                                         pasteActionInfo,
                                                         pasteOperationCallback,
                                                         fileOperationCallback)
            else -> {}
        }
    }


    override fun extractFile(sourceFilePath: String, destinationDir: String, newName: String,
                             zipOperationCallback: OperationHelper.ZipOperationCallback) {
        operationHelper.extractFile(context, sourceFilePath, destinationDir, newName,
                                    zipOperationCallback,
                                    fileOperationCallback)
    }

    override fun compressFile(destinationDir: String, filesToArchive: ArrayList<FileInfo>,
                              zipOperationCallback: OperationHelper.ZipOperationCallback) {
        operationHelper.compressFile(context, destinationDir, filesToArchive, zipOperationCallback,
                                     fileOperationCallback)
    }


    private val fileOperationCallback = object : OperationHelper.FileOperationCallback {
        override fun onOperationResult(operation: Operations, operationAction: OperationAction?) {
            operationAction?.let {
                _operationData.postValue(Pair(operation, operationAction))
            }
        }
    }

}