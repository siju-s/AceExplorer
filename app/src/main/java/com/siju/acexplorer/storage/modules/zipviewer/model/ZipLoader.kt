package com.siju.acexplorer.storage.modules.zipviewer.model

import android.content.Context
import android.net.Uri
import android.util.Log
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.RootHelper
import com.siju.acexplorer.main.model.helper.SortHelper
import com.siju.acexplorer.storage.model.ZipModel
import java.io.File
import java.io.IOException
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

private const val TAG = "ZipLoader"

class ZipLoader(val context: Context) {

    private var zipElementsResultCallback: ZipElementsResultCallback? = null
    private var elements = arrayListOf<ZipModel>()
    private var totalZipList = arrayListOf<ZipModel>()

    fun getZipContents(dir: String?, parentZipPath: String,
                       zipElementsResultCallback: ZipElementsResultCallback?): ArrayList<FileInfo> {
        this.zipElementsResultCallback = zipElementsResultCallback
        elements = arrayListOf()
        var path = dir
        if (path?.endsWith("/") == true) {
            path = path.substring(0, path.length - 1)
        }
        try {
            traverseZipElements(path, elements, totalZipList)
        }
        catch (exception: IOException) {
        }

        Collections.sort(elements, SortHelper.comparatorByNameZipViewer)
        zipElementsResultCallback?.onZipElementsFetched(elements)
        val list = populateZipFileInfo(parentZipPath, elements)
        Collections.sort(list, SortHelper.comparatorByNameZip)
        return list
    }


    @Throws(IOException::class, ZipException::class)
    fun populateTotalZipList(parentZipPath: String): ArrayList<ZipModel> {
        totalZipList = arrayListOf()
        return if (File(parentZipPath).canRead()) {
            getZipParentContents(parentZipPath, totalZipList)
        }
        else {
            getZipEntries(parentZipPath, totalZipList)
        }
    }

    private fun getZipParentContents(parentZipPath: String,
                                     totalZipList: ArrayList<ZipModel>): ArrayList<ZipModel> {
        var zipFile : ZipFile? = null
        try {
            zipFile = ZipFile(parentZipPath)
            val entries = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement() as ZipEntry
                totalZipList.add(createZipModel(entry))
            }
            zipFile.close()
        }
        catch (exception : ZipException) {
             //TODO Handle opening encrypted zip file
            throw ZipException("Cannot open this file")
        }
        finally {
            zipFile?.close()
        }
        return totalZipList
    }

    private fun getZipEntries(parentZipPath: String,
                              totalZipList: ArrayList<ZipModel>): ArrayList<ZipModel> {
        val uri = Uri.parse(parentZipPath)
        val zipInputStream = ZipInputStream(context.contentResolver.openInputStream(uri))
        var zipEntry = zipInputStream.nextEntry
        while (zipEntry != null) {
            totalZipList.add(createZipModel(zipEntry))
            zipEntry = zipInputStream.nextEntry
        }
        zipInputStream.close()
        return totalZipList
    }

    private fun createZipModel(zipEntry: ZipEntry) =
            ZipModel(zipEntry, zipEntry.time, zipEntry.size, zipEntry.isDirectory)

    private fun traverseZipElements(dir: String?, elements: ArrayList<ZipModel>,
                                    totalZipList: ArrayList<ZipModel>) {
        val entriesList = arrayListOf<String?>()
        for (entry in totalZipList) {
            val entryName = entry.name ?: continue
            val file = File(entryName)

            when {
                dir.isNullOrBlank()       -> addZipEntry(entry, file, entriesList, entryName,
                                                         elements)
                isFileZipEntry(dir, file) -> addFileZipEntry(entriesList, entryName, elements,
                                                             entry)
                else                      -> {
                    val entryNameStartIndex = dir.length + 1
                    if (entryName.startsWith(
                                    dir + File.separator) && entryName.length > entryNameStartIndex) {
                        val path1 = entryName.substring(entryNameStartIndex)
                        val index = entryNameStartIndex + path1.indexOf(File.separator)
                        val path = entryName.substring(0, index + 1)

                        addDirectoryZipEntry(entriesList, path, entry, elements)
                    }
                }
            }
        }
    }

    private fun isFileZipEntry(dir: String?,
                               file: File) = file.parent == dir || file.parent == File.separator + dir

    private fun addZipEntry(entry: ZipModel, file: File,
                            entriesList: ArrayList<String?>,
                            entryName: String?,
                            elements: ArrayList<ZipModel>) {
        if (file.parent.isNullOrEmpty() || file.parent == File.separator) {
            addFileZipEntry(entriesList, entryName, elements, entry)
        }
        else {
            entryName?.let { addDirectoryZipEntry(elements, entriesList, entry, it) }
        }
    }

    private fun addFileZipEntry(entriesList: ArrayList<String?>,
                                entryName: String?,
                                elements: ArrayList<ZipModel>,
                                zipEntry: ZipModel) {
        if (!entriesList.contains(entryName)) {
            elements.add(createZipModel(entryName, zipEntry))
            Log.d(TAG, "addFileZipEntry:entryName:$entryName, elements size:${elements.size}")
            entriesList.add(entryName)
        }
    }

    private fun createZipModel(entryName: String?, entry: ZipModel) =
            ZipModel(ZipEntry(entryName), entry.time, entry.size,
                     entry.isDirectory)

    private fun addDirectoryZipEntry(elements: ArrayList<ZipModel>,
                                     entriesList: ArrayList<String?>, entry: ZipModel,
                                     entryName: String) {
        var name = entryName
        var hasSeparator = false
        if (name.startsWith(File.separator)) {
            hasSeparator = true
            name = name.substring(1)
        }
        var path = name.substring(0, name.indexOf(File.separator) + 1)
        if (hasSeparator) {
            path = "/$path"
        }
        addDirectoryZipEntry(entriesList, path, entry, elements)
    }

    private fun addDirectoryZipEntry(entriesList: ArrayList<String?>,
                                     path: String,
                                     zipEntry: ZipModel,
                                     elements: ArrayList<ZipModel>) {
        val zipModel: ZipModel
        if (!entriesList.contains(path)) {
            zipModel = ZipModel(ZipEntry(path), zipEntry.time, zipEntry
                    .size, true)
            elements.add(zipModel)
            Log.d(TAG, "addFileZipEntry:entryName:$path, elements size:${elements.size}")
            entriesList.add(path)
        }
    }

    private fun populateZipFileInfo(parentZipPath: String,
                                    elements: ArrayList<ZipModel>): ArrayList<FileInfo> {
        val fileInfoList = arrayListOf<FileInfo>()
        for (model in elements) {
            var name = model.name
            if (name?.startsWith("/") == true) {
                name = name.substring(1)
            }
            val isDirectory = model.isDirectory
            val size: Long
            size = if (isDirectory) {
                getDirectoryCount(name)
            }
            else {
                model.size
            }
            val date = model.time
            val extension: String?

            if (isDirectory) {
                name = name?.substring(0, name.length - 1)
                name = name?.substring(name.lastIndexOf(File.separator) + 1)
                extension = null
            }
            else {
                name = name?.substring(name.lastIndexOf(File.separator) + 1)
                extension = name?.substring(name.lastIndexOf(".") + 1)
            }

            val path = parentZipPath + File.separator + name

            val fileInfo = FileInfo(Category.COMPRESSED, name, path, date, size,
                                    isDirectory, extension,
                                    RootHelper.parseFilePermission(File(path)), false)
            fileInfoList.add(fileInfo)
        }
        return fileInfoList
    }

    private fun getDirectoryCount(entryName: String?): Long {
        val size: Long
        var count = 0
        for (zipModel in totalZipList) {
            var modelName = zipModel.entry?.name
            if (modelName?.startsWith(File.separator) == true)
                modelName = modelName.substring(1)

            if (entryName != modelName && modelName?.startsWith(entryName.toString()) == true) {
                count++
            }
        }
        size = count.toLong()
        return size
    }


    interface ZipElementsResultCallback {

        fun onZipElementsFetched(zipElements: ArrayList<ZipModel>)
    }
}