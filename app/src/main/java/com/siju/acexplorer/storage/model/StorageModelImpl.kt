package com.siju.acexplorer.storage.model

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.FileConstants
import com.siju.acexplorer.main.model.data.DataFetcherFactory
import com.siju.acexplorer.main.model.data.DataLoader
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.preferences.PreferenceConstants
import com.siju.acexplorer.storage.model.operations.OperationAction
import com.siju.acexplorer.storage.model.operations.OperationHelper
import com.siju.acexplorer.storage.model.operations.Operations

private const val PREFS_NAME = "PREFS"
private const val PREFS_VIEW_MODE = "view-mode"

class StorageModelImpl(val context: Context) : StorageModel {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val globalPreference = PreferenceManager.getDefaultSharedPreferences(context)
    private val operationHelper = OperationHelper(AceApplication.appContext)
    private val _operationData = MutableLiveData<Pair<Operations, OperationAction>>()
    private val mediaObserver = MediaObserver(Handler())
    val operationData: LiveData<Pair<Operations, OperationAction>>
        get() = _operationData
    private lateinit var category: Category
    val _refreshData = MutableLiveData<Boolean>()

    override fun loadData(path: String?, category: Category): ArrayList<FileInfo> {
        this.category = category
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

    override fun getViewMode(): ViewMode {
        return if (sharedPreferences.contains(PREFS_VIEW_MODE)) {
            ViewMode.getViewModeFromValue(
                    sharedPreferences.getInt(PREFS_VIEW_MODE, ViewMode.LIST.value))
        } else {
            ViewMode.LIST
        }
    }

    override fun saveViewMode(viewMode: ViewMode?) {
        viewMode?.let {
            sharedPreferences.edit().apply {
                putInt(PREFS_VIEW_MODE, viewMode.value)
                apply()
            }
        }
    }

    override fun isDualModeEnabled() = globalPreference.getBoolean(FileConstants.PREFS_DUAL_PANE, false)

    override fun shouldShowHiddenFiles() = globalPreference.getBoolean(
            PreferenceConstants.PREFS_HIDDEN, false)

    override fun saveHiddenFileSetting(value: Boolean) {
        Log.e(this.javaClass.name, "saveHiddenFileSetting: value:$value")
        globalPreference.edit().apply {
            putBoolean(PreferenceConstants.PREFS_HIDDEN, value)
            apply()
        }
    }

    override fun getSortMode(): SortMode {
        return SortMode.getSortModeFromValue(
                globalPreference.getInt(
                        PreferenceConstants.KEY_SORT_MODE,
                        PreferenceConstants.DEFAULT_VALUE_SORT_MODE))
    }

    override fun saveSortMode(sortMode: SortMode) {
        Log.e(this.javaClass.name, "saveSortMode: value:$sortMode")
        globalPreference.edit().apply {
            putInt(PreferenceConstants.KEY_SORT_MODE, sortMode.value)
            apply()
        }
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
        saveSafUri(uri)
        val newFlags = flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent
                .FLAG_GRANT_WRITE_URI_PERMISSION)
        // Persist URI - this is required for verification of writability.
        context.contentResolver.takePersistableUriPermission(uri, newFlags)
        operationHelper.onSafSuccess(fileOperationCallback)
    }

    private fun saveSafUri(uri: Uri) {
        globalPreference.edit().putString(FileConstants.SAF_URI, uri.toString()).apply()
    }

    override fun onExit() {
        operationHelper.cleanup()
    }

    override fun onResume() {
        registerContentObserver()
    }

    override fun onPause() {
        unregisterContentObserver()
    }

    private fun registerContentObserver() {
        var uri: Uri? = null
        when (category) {
            Category.IMAGES_ALL, Category.GENERIC_IMAGES -> uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            Category.VIDEO_ALL, Category.GENERIC_VIDEOS -> uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            Category.ALL_TRACKS, Category.GENERIC_MUSIC -> uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            Category.RECENT, Category.RECENT_ALL, Category.DOCS, Category.PDF, Category.DOCS_OTHER,
            Category.COMPRESSED, Category.APPS, Category.LARGE_FILES_ALL, Category.LARGE_FILES -> uri = MediaStore.Files.getContentUri("external")
            else -> {
            }
        }
        uri?.let {
            Log.d("Model", "registerContentObserver:category:$category, uri :$uri")
            context.contentResolver.registerContentObserver(uri, true, mediaObserver)
            mediaObserver.addMediaObserverListener(mediaObserverListener)
        }
    }

    fun setRefreshDataFalse() {
        _refreshData.postValue(false)
    }

    private fun unregisterContentObserver() {
        Log.d("Model", "unregisterContentObserver:category:$category")
        context.contentResolver.unregisterContentObserver(mediaObserver)
        mediaObserver.removeMediaObserverListener(mediaObserverListener)

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