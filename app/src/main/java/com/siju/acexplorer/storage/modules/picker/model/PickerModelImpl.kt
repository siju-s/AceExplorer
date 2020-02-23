package com.siju.acexplorer.storage.modules.picker.model

import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.siju.acexplorer.AceApplication
import com.siju.acexplorer.R
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.main.model.data.DataFetcherFactory
import com.siju.acexplorer.main.model.data.DataLoader
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.StorageHelper
import com.siju.acexplorer.main.model.helper.UriHelper
import com.siju.acexplorer.storage.modules.picker.ringtone.RingtoneHelper
import com.siju.acexplorer.storage.modules.picker.types.PickerType
import com.siju.acexplorer.storage.modules.picker.view.KEY_PICKER_TYPE
import com.siju.acexplorer.storage.modules.picker.view.RINGTONE_TYPE
import com.siju.acexplorer.theme.Theme
import java.io.File


private const val RINGTONE_PICKER_PATH = "ringtone_picker_path"

class PickerModelImpl : PickerModel {
    private var listener: PickerModel.Listener? = null
    private val preferences: SharedPreferences
    val theme = MutableLiveData<Theme>()
    private val context = AceApplication.appContext

    init {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        setupTheme()
    }

    private fun setupTheme() {
        theme.postValue(Theme.getTheme(context))
    }

    override fun setListener(listener: PickerModel.Listener) {
        this.listener = listener
    }

    override fun setArgs(args: Any) {
        Log.d("PickerModelImpl", "setArgs:$args")
        val arguments = args as Bundle
        when (arguments.getSerializable(KEY_PICKER_TYPE) as PickerType) {
            PickerType.RINGTONE -> {
                val ringtoneType = arguments.getInt(RINGTONE_TYPE)
                listener?.onRingtonePicker(getLastSavedRingtoneDir(), ringtoneType)
            }
            PickerType.FILE -> {
                listener?.onFilePicker(StorageUtils.internalStorage)
            }
            else -> {}
        }
    }

    override fun getStorageList(): ArrayList<FileInfo> {
        val storagePaths = StorageUtils.storageDirectories
        val storageList = arrayListOf<FileInfo>()
        for (path in storagePaths) {
            val file = File(path)
            val triple = StorageHelper.getStorageProperties(path, file)
            val icon = triple.first
            var name = triple.second
            if (triple.third == StorageUtils.StorageType.INTERNAL) {
                name = context.getString(R.string.nav_menu_internal_storage)
            }
            val picker = FileInfo.createPicker(Category.PICKER, name, path, icon)
            storageList.add(picker)
        }
        return storageList
    }

    override fun saveLastRingtoneDir(currentPath: String?) {
        currentPath?.let {
            val editor = preferences.edit()
            editor.putString(RINGTONE_PICKER_PATH, currentPath).apply()
        }
    }

    override fun getLastSavedRingtoneDir(): String? {
        return preferences.getString(RINGTONE_PICKER_PATH, null)
    }

    override fun loadData(path: String?, category: Category, isRingtonePicker : Boolean): ArrayList<FileInfo> {
        return DataLoader.fetchDataByCategory(context,
                                              DataFetcherFactory.createDataFetcher(category),
                                              category, path, isRingtonePicker)
    }

    override fun onRingtoneSelected(path: String?, ringtoneType: Int?) {
        if (path == null) {
            listener?.onPickerResultAction(PickerResultAction(PickerAction.RINGTONE_PICK, false, null))
            return
        }
        val uri = ringtoneType?.let {
            RingtoneHelper.getCustomRingtoneUri(context.contentResolver, path, it)
        }

        if (uri == null) {
            listener?.onPickerResultAction(PickerResultAction(PickerAction.RINGTONE_PICK, false, null))
        }
        else {
            val intent = Intent()
            with(intent) {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, ringtoneType)
                putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, uri)
                data = uri
            }
            saveLastRingtoneDir(File(path).parent)
            listener?.onPickerResultAction(PickerResultAction(PickerAction.RINGTONE_PICK, true, intent))
        }
    }

    override fun onFileSelected(filePath: String?) {
        val intent = Intent()
        intent.data = UriHelper.createContentUri(context, filePath)
        listener?.onPickerResultAction(PickerResultAction(PickerAction.FILE_PICK, true, intent))
    }

    override fun onOkButtonClicked(value: String) {
        val intent = Intent()
        intent.putExtra(KEY_PICKER_SELECTED_PATH, value)
        listener?.onPickerResultAction(PickerResultAction(PickerAction.OK, true, intent))
    }

    override fun onCancelButtonClicked(value: PickerType) {
        val intent = Intent()
        intent.putExtra(KEY_PICKER_TYPE, value)
        listener?.onPickerResultAction(PickerResultAction(PickerAction.CANCEL, false, intent))
    }

    companion object {
        const val KEY_PICKER_SELECTED_PATH = "PATH"
    }
}
