package com.siju.acexplorer.storage.model

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.common.types.FileInfo
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

    val operationData: LiveData<Pair<Operations, OperationAction>>
        get() = _operationData

    override fun loadData(path: String?, category: Category): ArrayList<FileInfo> {
        return DataLoader.fetchDataByCategory(context,
                                              DataFetcherFactory.createDataFetcher(category),
                                              category, path)
    }

    override fun getViewMode(): ViewMode {
        return if (sharedPreferences.contains(PREFS_VIEW_MODE)) {
            ViewMode.getViewModeFromValue(
                    sharedPreferences.getInt(PREFS_VIEW_MODE, ViewMode.LIST.value))
        }
        else {
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

    override fun handleSafResult(uri: Uri, flags: Int) {
        val newFlags = flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent
                .FLAG_GRANT_WRITE_URI_PERMISSION)
        // Persist URI - this is required for verification of writability.
        context.contentResolver.takePersistableUriPermission(uri, newFlags)
        operationHelper.onSafSuccess(fileOperationCallback)
    }

    override fun onExit() {
        operationHelper.cleanup()
    }

    override fun checkPasteWriteMode(destinationDir: String,
                                     files: ArrayList<FileInfo>,
                                     pasteActionInfo: ArrayList<PasteActionInfo>,
                                     operations: Operations,
                                     pasteOperationCallback: OperationHelper.PasteOperationCallback) {
        operationHelper.copyFiles(context, destinationDir, files, pasteActionInfo,
                                  pasteOperationCallback, fileOperationCallback)
    }


    private val fileOperationCallback = object : OperationHelper.FileOperationCallback {
        override fun onOperationResult(operation: Operations, operationAction: OperationAction?) {
            operationAction?.let {
                _operationData.postValue(Pair(operation, operationAction))
            }
        }
    }


}