package com.siju.acexplorer.storage.modules.zipviewer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.main.model.FileConstants.EXT_ZIP
import com.siju.acexplorer.main.model.groups.Category
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
import java.io.File
import java.util.*
import java.util.zip.ZipEntry

private const val TAG = "ZipViewerViewModel"
private const val DELIMITER_SLASH = "/"
class ZipViewerViewModel(val model: ZipViewerModel, val zipViewerCallback: ZipViewerCallback) : ViewModel() {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var newPath: String? = null
    private var scrollDir: String? = null
    var apkPath: String? = null

    private lateinit var zipElements: ArrayList<ZipModel>

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

    fun populateTotalZipList(parentZipPath: String) {
        model.populateZipList(parentZipPath)
    }

    fun loadData(path : String?, parentZipPath : String) {
        this.parentZipPath = parentZipPath
        var dir = path
        if (dir == null) {
            dir = parentZipPath
            zipViewerCallback.setInitialDir(dir)
            setNavDirectory(dir)
            addToBackStack(dir)
        }
          uiScope.launch(Dispatchers.IO) {
            val data = model.loadData(path, parentZipPath, zipElementsResultCallback)
            zipViewerCallback.onZipContentsLoaded(data)
        }
    }

    fun onFileClicked(position: Int) {
        if (isZipExtension(parentZipPath)) {
            val name = zipElements[position].name?.substringAfterLast(DELIMITER_SLASH)
            val zipEntry = ZipEntry(zipElements[position].entry)
            name?.let { model.onFileClicked(it, zipEntry, parentZipPath, zipFileViewCallback) }
        }
    }

    private fun isZipExtension(name: String?) = name?.endsWith(EXT_ZIP) == true


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
            zipViewerCallback.calculateZipScroll(scrollDir!!)
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
        zipViewerCallback.removeZipScrollPos(newPath!!)
        currentDir = File(currentDir).parent
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
            removeFromBackStack()
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

        override fun onOpenApkClicked(path: String) {
        }

    }
}