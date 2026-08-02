package com.siju.acexplorer.storage.modules.zipviewer.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.main.model.FileConstants.EXT_ZIP
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.ViewHelper.EXT_APK
import com.siju.acexplorer.main.view.dialog.DialogHelper
import com.siju.acexplorer.storage.model.ZipModel
import com.siju.acexplorer.storage.model.task.ExtractZipEntry
import com.siju.acexplorer.storage.modules.zipviewer.ZipViewerCallback
import com.siju.acexplorer.storage.modules.zipviewer.model.ZipLoader
import com.siju.acexplorer.storage.modules.zipviewer.model.ZipViewerModel
import com.siju.acexplorer.utils.InstallHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipException

private const val DELIMITER_SLASH = "/"
private const val TAG = "ZipViewerViewModel"
class ZipViewerViewModel(val model: ZipViewerModel, private val zipViewerCallback: ZipViewerCallback) : ViewModel() {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var newPath: String? = null
    private var scrollDir: String? = null
    var apkPath: String? = null

    // Starts empty rather than lateinit: a zip that fails to open never reaches the fetch callback,
    // and leaving the property unset crashed the back press that follows.
    private var zipElements: ArrayList<ZipModel> = arrayListOf()

    private val _viewFileEvent = MutableLiveData<Pair<String, String?>>()
    val viewFileEvent: LiveData<Pair<String, String?>>
        get() = _viewFileEvent

    private val _installAppEvent = MutableLiveData<Pair<Boolean, String?>>()

    val installAppEvent: LiveData<Pair<Boolean, String?>>
        get() = _installAppEvent

    private var currentDir : String? = null
    private lateinit var parentZipPath: String
    private var zipEntryFileName : String? = null
    private var zipEntry : ZipEntry? = null
    private val _zipFailEvent = MutableLiveData<Boolean>()

    val zipFailEvent: LiveData<Boolean>
        get() = _zipFailEvent

    private val _zipLoading = MutableLiveData<Boolean>()

    val zipLoading: LiveData<Boolean>
        get() = _zipLoading

    private var populateJob: Job? = null
    private var populateFailed = false

    /**
     * Reads the zip's central directory off the main thread.
     *
     * A zip with many entries takes a noticeable time to parse, so this reports loading and
     * [loadData] waits on [populateJob] rather than racing it for a list that is not filled yet.
     */
    fun populateTotalZipList(parentZipPath: String) {
        populateFailed = false
        _zipLoading.value = true
        populateJob = uiScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    model.populateZipList(parentZipPath)
                }
            }
            catch (ex: IOException) {
                Log.e(TAG, "Failed to read zip: $parentZipPath", ex)
                populateFailed = true
                _zipLoading.value = false
                setZipFailEvent(true)
                // Leaves zip mode so the caller returns to the folder the zip lives in, rather
                // than sitting in a zip that was never opened.
                endZipMode(null)
            }
        }
    }

    fun loadData(path : String?, parentZipPath : String) {
        this.parentZipPath = parentZipPath
        uiScope.launch {
            populateJob?.join()
            if (populateFailed) {
                return@launch
            }
            // Navigation is pushed only once the zip is known to be readable, so a broken archive
            // no longer leaves the breadcrumb pointing inside it.
            if (path == null) {
                zipViewerCallback.setInitialDir(parentZipPath)
                setNavDirectory(parentZipPath)
                addToBackStack(parentZipPath)
            }
            try {
                val data = withContext(Dispatchers.IO) {
                    model.loadData(path, parentZipPath, zipElementsResultCallback)
                }
                _zipLoading.value = false
                zipViewerCallback.onZipContentsLoaded(data)
            }
            catch (ex: IOException) {
                Log.e(TAG, "Failed to list zip contents: $parentZipPath", ex)
                _zipLoading.value = false
                setZipFailEvent(true)
            }
        }
    }

    fun onFileClicked(position: Int) {
        if (isZipExtension(parentZipPath)) {
            val name = zipElements[position].name?.substringAfterLast(DELIMITER_SLASH)
            val zipEntry = ZipEntry(zipElements[position].entry)
            name?.let { model.onFileClicked(it, zipEntry, parentZipPath, zipFileViewCallback) }
        }
    }

    private fun isZipExtension(name: String?) = name?.endsWith(EXT_ZIP) == true || name?.endsWith(EXT_APK)== true

    fun endViewFileEvent() {
        _viewFileEvent.value = null
    }

    fun onDirectoryClicked(position: Int) {
        var name = zipElements[position].name
        if (name?.startsWith(DELIMITER_SLASH) == true) {
            name = name.substring(1)
        }
        val name1 = name?.substring(0, name.length - 1)
        setZipEntryInfo(zipElements[position].entry, name1)

        if (isDirectory(zipEntryFileName)) {
            val dirPath = zipEntryFileName?.substringBeforeLast(DELIMITER_SLASH)
            scrollDir = parentZipPath + DELIMITER_SLASH + dirPath
            scrollDir?.let {
                zipViewerCallback.calculateZipScroll(it)
            }
        }
        else {
            scrollDir = null
        }
        viewZipContents(position)
    }

    private fun viewZipContents(position: Int) {
        currentDir = zipElements[position].name
        newPath = getZipEntryPath()

        newPath?.let {
            if (it.endsWith(DELIMITER_SLASH)) {
                newPath = it.substring(0, it.length - 1)
            }
        }
        loadData(currentDir, parentZipPath)
        newPath?.let {
            setNavDirectory(it)
            addToBackStack(it)
        }
    }

    private fun getZipEntryPath(): String {
        return if (currentDir?.startsWith(DELIMITER_SLASH) == true) {
            parentZipPath + currentDir
        }
        else {
            parentZipPath + File.separator + currentDir
        }
    }


    private fun isDirectory(
            zipEntryFileName: String?) = zipEntryFileName?.contains(DELIMITER_SLASH) == true

    private fun setZipEntryInfo(zipEntry: ZipEntry?, zipEntryFileName : String?) {
        this.zipEntry = zipEntry
        this.zipEntryFileName = zipEntryFileName
    }

    fun checkZipMode(dir: String?) {
        if (isAtParentZipPath(dir)) {
            endZipMode(dir)
        }
        else {
            reloadData()
        }
    }

    private fun reloadData() {
        zipViewerCallback.removeFromBackStack()
        zipViewerCallback.removeZipScrollPos(newPath)
        currentDir?.let {
            currentDir = File(it).parent
        }
        if (currentDir == File.separator) {
            currentDir = null
        }
        loadData(currentDir, parentZipPath)
        newPath = if (currentDir == null || currentDir == File.separator) {
            parentZipPath
        }
        else {
            getZipEntryPath()
        }
        newPath?.let {
            setNavDirectory(it)
            addToBackStack(it)
        }
    }

    private fun isAtParentZipPath(dir: String?) =
            currentDir.isNullOrEmpty() || dir == null || !dir.contains(parentZipPath)

    fun endZipMode(dir: String?) {
        currentDir = null
        zipElements.clear()
        with(zipViewerCallback) {
            removeZipScrollPos(parentZipPath)
            onZipModeEnd(dir)
        }
        model.clearCache()
    }

    fun onBackPressed() {
        var path = currentDir
        currentDir?.let {
            path = parentZipPath + File.separator + currentDir
        }
        checkZipMode(path)
    }

    private fun setNavDirectory(path: String, category: Category = Category.FILES)  {
        zipViewerCallback.setNavDirectory(path, true, category)
    }

    private fun addToBackStack(path: String, category: Category= Category.FILES) {
        zipViewerCallback.addToBackStack(path, category)
    }

    fun setZipFailEvent(value: Boolean) {
        _zipFailEvent.postValue(value)
    }

    private val zipElementsResultCallback = object : ZipLoader.ZipElementsResultCallback {

        override fun onZipElementsFetched(zipElements: ArrayList<ZipModel>) {
            this@ZipViewerViewModel.zipElements = zipElements
        }
    }

    private val zipFileViewCallback = object : ExtractZipEntry.ZipFileViewCallback {
        override fun openZipFile(outputDir: String, extension: String) {
            _viewFileEvent.postValue(Pair(outputDir, extension))
        }

    }

    val apkDialogListener = object : DialogHelper.ApkDialogListener {

        override fun onInstallClicked(path: String?) {
            val canInstall = InstallHelper.canInstallApp(AceApplication.appContext)
            apkPath = path
            _installAppEvent.value = Pair(canInstall, path)
        }

        override fun onCancelClicked() {
        }

        override fun onOpenApkClicked(path: String?) {
        }

    }
}