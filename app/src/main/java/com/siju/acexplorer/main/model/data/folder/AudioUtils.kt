package com.siju.acexplorer.main.model.data.folder

import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.main.model.HiddenFileHelper
import com.siju.acexplorer.main.model.groups.Category
import com.siju.acexplorer.main.model.helper.FileUtils
import com.siju.acexplorer.main.model.helper.RootHelper
import java.io.File
import java.util.*

private const val EXT_MP3 = ".mp3"
private const val EXT_AAC = ".aac"
private const val EXT_M4A = ".m4a"
private const val EXT_FLAC = ".flac"
private const val EXT_WAV = ".wav"
private const val EXT_OGG = ".ogg"
private const val EXT_AMR = ".amr"
private const val EXT_OPUS = ".opus"

object AudioUtils {

    fun getFolderAudioFileList(sourceFile: File, showHidden: Boolean): ArrayList<FileInfo> {
        val filesList = ArrayList<FileInfo>()
        val listFiles = sourceFile.listFiles { _, name ->
            isAudioFile(name)

        } ?: return filesList

        for (file in listFiles) {
            val filePath = file.absolutePath
            val size = file.length()
            var extension: String?
            var category: Category

            // Don't show hidden files by default
            if (HiddenFileHelper.shouldSkipHiddenFiles(file, showHidden)) {
                continue
            }
            extension = FileUtils.getExtension(filePath)
            category = FileUtils.getCategoryFromExtension(extension)
            val date = file.lastModified()

            val fileInfo = FileInfo(category, file.name, filePath, date, size,
                    false, extension, RootHelper.parseFilePermission(file), false)
            filesList.add(fileInfo)
        }
        return filesList
    }


    private fun isAudioFile(name : String?) : Boolean {
        return name?.endsWith(EXT_MP3, true) == true ||
                name?.endsWith(EXT_AAC, true) == true ||
                name?.endsWith(EXT_M4A, true) == true ||
                name?.endsWith(EXT_FLAC, true) == true ||
                name?.endsWith(EXT_WAV, true) == true ||
                name?.endsWith(EXT_OGG, true) == true ||
                name?.endsWith(EXT_AMR, true) == true ||
                name?.endsWith(EXT_OPUS, true) == true
    }
 }