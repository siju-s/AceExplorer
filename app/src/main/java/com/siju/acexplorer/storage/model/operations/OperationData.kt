package com.siju.acexplorer.storage.model.operations

import com.siju.acexplorer.common.types.FileInfo

class OperationData private constructor(var arg1: String, var arg2: String) {

    private var paths = emptyList<String>()
    private var filesList = arrayListOf<FileInfo>()

    constructor(paths: ArrayList<String>) : this("null", "null") {
        this.paths = paths
    }

    constructor(destinationDir: String, filesToCopy: java.util.ArrayList<FileInfo>) : this(
            destinationDir, "null") {
        this.arg1 = destinationDir
        this.filesList = filesToCopy
    }

    companion object {

        fun createRenameOperation(filePath: String, fileName: String): OperationData {
            return OperationData(filePath, fileName)
        }

        fun createNewFolderOperation(filePath: String, name: String): OperationData {
            return OperationData(filePath, name)
        }

        fun createNewFileOperation(filePath: String, name: String): OperationData {
            return OperationData(filePath, name)
        }

        fun createDeleteOperation(filePaths: ArrayList<String>): OperationData {
            return OperationData(filePaths)
        }

        fun createCopyOperation(destinationDir: String,
                                filesToCopy: ArrayList<FileInfo>): OperationData {
            return OperationData(destinationDir, filesToCopy)
        }

        fun createExtractOperation(sourceFilePath: String, destinationDir: String): OperationData {
            return OperationData(sourceFilePath, destinationDir)
        }

    }
}