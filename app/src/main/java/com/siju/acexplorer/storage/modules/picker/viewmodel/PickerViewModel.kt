package com.siju.acexplorer.storage.modules.picker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.permission.PermissionHelper
import com.siju.acexplorer.storage.modules.picker.model.PickerModel
import com.siju.acexplorer.storage.modules.picker.model.PickerResultAction
import com.siju.acexplorer.storage.modules.picker.types.PickerType
import com.siju.acexplorer.utils.ScrollInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

class PickerViewModel(val model: PickerModel) : ViewModel(), PickerModel.Listener {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private lateinit var permissionHelper: PermissionHelper
    lateinit var permissionStatus: LiveData<PermissionHelper.PermissionState>

    private val _storageList = MutableLiveData<ArrayList<FileInfo>>()
    val storage: LiveData<ArrayList<FileInfo>>
        get() = _storageList

    private val _pickerInfo = MutableLiveData<Triple<PickerType, String?, Int>>()
    val pickerInfo: LiveData<Triple<PickerType, String?, Int>>
        get() = _pickerInfo

    private var rootStorageList = false
    private val _currentPath = MutableLiveData<String>()

    val currentPath: LiveData<String>
        get() = _currentPath

    private val _result = MutableLiveData<PickerResultAction>()
    val result: LiveData<PickerResultAction>
        get() = _result

    private val _fileData = MutableLiveData<ArrayList<FileInfo>>()
    val fileData: LiveData<ArrayList<FileInfo>>
        get() = _fileData

    private val _showEmptyText = MutableLiveData<Pair<PickerType, Boolean>>()
    val showEmptyText: LiveData<Pair<PickerType, Boolean>>
        get() = _showEmptyText

    private val _directoryClicked = MutableLiveData<Boolean>()
    val directoryClicked: LiveData<Boolean>
        get() = _directoryClicked

    private val _scrollInfo = MutableLiveData<ScrollInfo>()

    val scrollInfo: LiveData<ScrollInfo>
        get() = _scrollInfo

    private var scrollPosition = hashMapOf<String?, ScrollInfo>()

    init {
        model.setListener(this)
    }

    fun setArgs(args: Any) {
        model.setArgs(args)
    }

    fun fetchStorageList() {
        uiScope.launch(Dispatchers.IO) {
            _storageList.postValue(model.getStorageList())
        }
    }

    fun setPermissionHelper(permissionHelper: PermissionHelper) {
        this.permissionHelper = permissionHelper
        permissionStatus = permissionHelper.permissionStatus
        permissionHelper.checkPermissions()
    }

    fun onPermissionResult() {
        permissionHelper.onPermissionResult()
    }

    fun onResume() {
        permissionHelper.onForeground()
    }

    fun requestPermissions() {
        permissionHelper.requestPermission()
    }

    fun showPermissionRationale() {
        permissionHelper.showRationale()
    }

    override fun onRingtonePicker(lastSavedRingtoneDir: String?, ringtoneType: Int) {
        var directory = lastSavedRingtoneDir
        if (lastSavedRingtoneDir == null || !File(lastSavedRingtoneDir).exists()) {
            directory = StorageUtils.internalStorage
        }
        _pickerInfo.value = Triple(PickerType.RINGTONE, directory, ringtoneType)
        _currentPath.value = directory
    }

    override fun onFilePicker(path: String) {
        _pickerInfo.value  = Triple(PickerType.FILE, path, 0)
        _currentPath.value = path
    }

    fun handleItemClick(fileInfo: FileInfo) {
        val filePath = fileInfo.filePath
        when {
            filePath != null && File(filePath).isDirectory -> {
                setRootStorageList(false)
                _directoryClicked.postValue(true)
                _currentPath.postValue(filePath)
            }
            rootStorageList            -> {
                setRootStorageList(false)
                _currentPath.postValue(filePath)
            }
            else                       -> when (pickerInfo.value?.first) {
                PickerType.RINGTONE -> {
                    model.onRingtoneSelected(filePath, pickerInfo.value?.third)
                }
                PickerType.FILE     -> {
                    model.onFileSelected(filePath)
                }
                else -> {}
            }
        }
    }

    private fun handleScrollPosition() {
        if (scrollPosition.containsKey(currentPath.value)) {
            val scrollInfo = scrollPosition[currentPath.value]
            _scrollInfo.postValue(scrollInfo)
        }
    }

    fun saveScrollInfo(scrollInfo: ScrollInfo) {
        scrollPosition[currentPath.value] = scrollInfo
    }

    private fun removeScrolledPos() {
        scrollPosition.remove(currentPath.value)
    }

    private fun setRootStorageList(value: Boolean) {
        rootStorageList = value
    }

    fun loadData(path: String?) {
        if (path.isNullOrEmpty()) {
            return
        }
        uiScope.launch(Dispatchers.IO) {
            val pickerInfo = pickerInfo.value
            val data = model.loadData(path, Category.FILES,
                                      pickerInfo?.first == PickerType.RINGTONE)
            pickerInfo?.let {
                if (data.isEmpty()) {
                    _showEmptyText.postValue(Pair(it.first, true))
                }
                else {
                    _showEmptyText.postValue(Pair(it.first, false))
                }
            }
            _fileData.postValue(data)
            handleScrollPosition()
        }
    }

    fun okButtonClicked() {
        currentPath.value?.let { model.onOkButtonClicked(it) }
    }

    override fun onPickerResultAction(pickerResultAction: PickerResultAction) {
        _result.postValue(pickerResultAction)
    }

    fun onCancelButtonClicked() {
        pickerInfo.value?.first?.let { model.onCancelButtonClicked(it) }
    }

    fun onNavigationBackButtonPressed() {
        _storageList.value?.let {
            for (storage in it) {
                if (isMainStorageList(storage, _currentPath.value)) {
                    setRootStorageList(true)
                    removeScrolledPos()
                    _currentPath.value = ""
                    _fileData.postValue(_storageList.value)
                    return
                }
            }
        }
        if (!rootStorageList) {
            val path = _currentPath.value
            path?.let {
                val parentPath = File(it).parent
                removeScrolledPos()
                _currentPath.value = parentPath
            }
        }
    }

    private fun isMainStorageList(storage: FileInfo,
                                  parentPath: String?) = storage.filePath == parentPath

    fun onBackPressed() {
        if (rootStorageList) {
            _pickerInfo.value?.first?.let { model.onCancelButtonClicked(it) }
        }
        else {
            onNavigationBackButtonPressed()
        }
    }


}
