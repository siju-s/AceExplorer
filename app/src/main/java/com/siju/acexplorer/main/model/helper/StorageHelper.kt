package com.siju.acexplorer.main.model.helper

import com.siju.acexplorer.R
import com.siju.acexplorer.main.model.StorageUtils
import com.siju.acexplorer.main.model.groups.STORAGE_SDCARD1
import java.io.File

private const val STORAGE_EMULATED_LEGACY = "/storage/emulated/legacy"
private const val STORAGE_EMULATED_0 = "/storage/emulated/0"
object StorageHelper {

    fun getStorageProperties(path: String, file: File): Triple<Int, String, StorageUtils.StorageType> {
        val icon: Int
        val storageType: StorageUtils.StorageType
        val name: String
        when {
            isInternalStorage(path) -> {
                icon = R.drawable.ic_phone_white
                name = ""
                storageType = StorageUtils.StorageType.INTERNAL
            }
            isExternalStorage(path) -> {
                icon = R.drawable.ic_ext_white
                storageType = StorageUtils.StorageType.EXTERNAL
                name = path
            }
            else -> {
                name = file.name
                icon = R.drawable.ic_ext_white
                storageType = StorageUtils.StorageType.EXTERNAL
            }
        }
        return Triple(icon, name, storageType)
    }

    private fun isExternalStorage(path: String) = STORAGE_SDCARD1 == path

    private fun isInternalStorage(path: String) =
            STORAGE_EMULATED_LEGACY == path || STORAGE_EMULATED_0 == path
}