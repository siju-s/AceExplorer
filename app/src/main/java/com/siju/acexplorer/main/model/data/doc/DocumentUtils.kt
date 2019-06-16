package com.siju.acexplorer.main.model.data.doc

import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.siju.acexplorer.main.model.FileConstants
import com.siju.acexplorer.main.model.groups.Category

object DocumentUtils {

    fun getMediaTypeNone(): String {
        return MediaStore.Files.FileColumns.MEDIA_TYPE + " = " + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE
    }

    fun isLargeFilesCategory(category: Category): Boolean {
        return Category.LARGE_FILES == category
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
}