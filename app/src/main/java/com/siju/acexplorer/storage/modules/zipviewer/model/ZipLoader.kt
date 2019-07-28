package com.siju.acexplorer.storage.modules.zipviewer.model

import android.content.Context
import android.net.Uri
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.RootHelper
import com.siju.acexplorer.main.model.helper.SortHelper
import com.siju.acexplorer.storage.model.ZipModel
import com.siju.acexplorer.storage.modules.zipviewer.helper.ZipFormats
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

private const val BUFFER_SIZE_UNZIP_BYTES = 20480

class ZipLoader(val context: Context) {

    private var zipElementsResultCallback: ZipElementsResultCallback? = null
    private val elements = arrayListOf<ZipModel>()
    private var totalZipList = arrayListOf<ZipModel>()

    fun unzipEntry(entry: ZipEntry, path: String, outputPath: String,
                   zipElementsResultCallback: ZipElementsResultCallback?) {
        val zipFormat = ZipFormats.getFormatFromExt(
                entry.name.substringAfterLast("."))
        if (zipFormat == ZipFormats.ZIP) {
            unzipEntry(ZipFile(path), entry, File(outputPath), zipElementsResultCallback)
        }
    }

    @Throws(IOException::class)
    private fun unzipEntry(zipFile: ZipFile, entry: ZipEntry, outputFile: File,
                           zipElementsResultCallback: ZipElementsResultCallback?) {

        val inputStream = BufferedInputStream(zipFile.getInputStream(entry))
        val outputStream = BufferedOutputStream(FileOutputStream(outputFile))
        try {
            val buf = ByteArray(BUFFER_SIZE_UNZIP_BYTES)
            var len = inputStream.read(buf)
            while (len > 0) {
                outputStream.write(buf, 0, len)
                len = inputStream.read(buf)
            }
        }
        finally {
            try {
                inputStream.close()
            }
            catch (e: IOException) {
                //closing quietly
            }

            try {
                outputStream.close()
            }
            catch (e: IOException) {
                //closing quietly
            }

        }
        getZipContents("", outputFile.absolutePath, zipElementsResultCallback)
    }

    fun getZipContents(dir: String?, parentZipPath: String,
                       zipElementsResultCallback: ZipElementsResultCallback?): ArrayList<FileInfo> {
        this.zipElementsResultCallback = zipElementsResultCallback
        try {
            totalZipList = populateTotalZipList(parentZipPath)
            traverseZipElements(dir, elements, totalZipList)
        }
        catch (exception: IOException) {
        }

        Collections.sort(elements, SortHelper.comparatorByNameZipViewer)
        zipElementsResultCallback?.onZipElementsFetched(elements)
        val list = populateZipFileInfo(parentZipPath, elements)
        Collections.sort(list, SortHelper.comparatorByNameZip)
        return list
    }

    @Throws(IOException::class)
    private fun populateTotalZipList(parentZipPath: String): ArrayList<ZipModel> {
        val totalZipList = arrayListOf<ZipModel>()
        return if (File(parentZipPath).canRead()) {
            getZipParentContents(parentZipPath, totalZipList)
        }
        else {
            getZipEntries(parentZipPath, totalZipList)
        }
    }

    private fun getZipParentContents(parentZipPath: String,
                                     totalZipList: ArrayList<ZipModel>): ArrayList<ZipModel> {
        val zipFile = ZipFile(parentZipPath)
        val entries = zipFile.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement() as ZipEntry
            totalZipList.add(createZipModel(entry))
        }
        zipFile.close()
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
            val entryName = entry.name
            val file = File(entryName)
            when {
                dir.isNullOrBlank()       -> addZipEntry(entry, file, entriesList, entryName,
                                                         elements)
                isFileZipEntry(dir, file) -> addFileZipEntry(entriesList, entryName, elements,
                                                             entry)
                else                      -> {
                    if (entryName == null) {
                        return
                    }
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
            addDirectoryZipEntry(elements, entriesList, entry, entryName!!)
        }
    }

    private fun addFileZipEntry(entriesList: ArrayList<String?>,
                                entryName: String?,
                                elements: ArrayList<ZipModel>,
                                zipEntry: ZipModel) {
        if (!entriesList.contains(entryName)) {
            elements.add(createZipModel(entryName, zipEntry))
            entriesList.add(entryName)
        }
    }

    private fun createZipModel(entryName: String?, entry: ZipModel) =
            ZipModel(ZipEntry(entryName), entry.time, entry.size,
                     entry.isDirectory)

    private fun addDirectoryZipEntry(elements: ArrayList<ZipModel>,
                                     entriesList: ArrayList<String?>, entry: ZipModel,
                                     entryName: String) {
        var entryName = entryName
        var hasSeparator = false
        if (entryName.startsWith(File.separator)) {
            hasSeparator = true
            entryName = entryName.substring(1)
        }
        var path = entryName.substring(0, entryName.indexOf(File.separator) + 1)
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