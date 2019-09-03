package com.siju.acexplorer.main.model.data.doc

import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.FileConstants
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.groups.CategoryHelper
import java.util.*

object DocumentUtils {

    fun getMediaTypeNone(): String {
        return MediaStore.Files.FileColumns.MEDIA_TYPE + " = " + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE
    }

    fun getDocMimeTypes(): String {
        val doc = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_DOC)
        val docx = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_DOCX)
        val txt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_TEXT)
        val html = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_HTML)
        val pdf = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_PDF)
        val xls = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_XLS)
        val xlxs = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_XLXS)
        val ppt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_PPT)
        val pptx = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_PPTX)

        return (MediaStore.Files.FileColumns.MIME_TYPE + " IN " + "("
                + "'" + doc + "'" + ","
                + "'" + docx + "'" + ","
                + "'" + txt + "'" + ","
                + "'" + html + "'" + ","
                + "'" + pdf + "'" + ","
                + "'" + xls + "'" + ","
                + "'" + xlxs + "'" + ","
                + "'" + ppt + "'" + ","
                + "'" + pptx + "'" + " )")
    }

    fun getCompressedMimeType(): String {
        val zip = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_ZIP)
        val tar = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_TAR)
        val tgz = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_TGZ)
        val rar = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileConstants.EXT_RAR)

        return (MediaStore.Files.FileColumns.MIME_TYPE + " IN " + "("
                + "'" + zip + "'" + ","
                + "'" + tar + "'" + ","
                + "'" + tgz + "'" + ","
                + "'" + rar + "'" + ")")
    }

    fun isDocumentFileType(extension : String) : Boolean {
        return extension == FileConstants.EXT_TEXT || extension == FileConstants.EXT_DOC ||
                extension == FileConstants.EXT_DOCX || extension == FileConstants.EXT_CSV ||
                extension == FileConstants.EXT_XLS || extension == FileConstants.EXT_XLXS ||
                extension == FileConstants.EXT_PDF || extension == FileConstants.EXT_PPT ||
                extension == FileConstants.EXT_PPTX
    }

    fun getMediaStoreImageMediaType() : String {
        return MediaStore.Files.FileColumns.MEDIA_TYPE + " = " + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
    }

    fun getMediaStoreVideoMediaType() : String {
        return MediaStore.Files.FileColumns.MEDIA_TYPE + " = " + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
    }

    fun getMediaStoreAudioMediaType() : String {
        return MediaStore.Files.FileColumns.MEDIA_TYPE + " = " + MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO
    }

    fun getLargeFilesCategoryList(fileList: ArrayList<FileInfo>): ArrayList<FileInfo> {
        // TODO("sort it")
//        SortHelper.sortRecentCategory(fileList)
        val categories = ArrayList<Category>()
        var count = 0
        val fileInfoList = ArrayList<FileInfo>()
        for (fileInfo in fileList) {
            val newCategory = CategoryHelper.getCategoryForLargeFilesFromExtension(fileInfo.extension)
            if (!categories.contains(newCategory)) {
                count = 1
                val itemFileInfo = FileInfo(newCategory,
                        CategoryHelper.getSubcategoryForLargeFilesFromExtension(
                                fileInfo.extension),
                        count)
                fileInfoList.add(itemFileInfo)
                categories.add(newCategory)
            } else {
                count++
                fileInfoList[categories.indexOf(newCategory)].count = count
            }
        }
        return fileInfoList
    }
}