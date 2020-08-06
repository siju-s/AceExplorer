package com.siju.acexplorer.storage.modules.picker.model

import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.storage.modules.picker.types.PickerType


interface PickerModel {

    fun getStorageList() : ArrayList<FileInfo>

    fun saveLastRingtoneDir(currentPath: String?)

    fun setArgs(args: Any)

    fun setListener(listener: Listener)

    fun getLastSavedRingtoneDir(): String?

    fun loadData(path: String?, category: Category, isRingtonePicker: Boolean): ArrayList<FileInfo>

    fun onRingtoneSelected(path: String?, ringtoneType: Int?)
    fun onFileSelected(filePath: String?)
    fun onOkButtonClicked(value: String)
    fun onCancelButtonClicked(value: PickerType)

    interface Listener {
        fun onRingtonePicker(lastSavedRingtoneDir: String?, ringtoneType: Int)
        fun onPickerResultAction(pickerResultAction: PickerResultAction)
        fun onFilePicker(path: String)
        fun onCopyPicker()
        fun onCutPicker()
        fun onContentIntentPicker(path: String)
    }
}
