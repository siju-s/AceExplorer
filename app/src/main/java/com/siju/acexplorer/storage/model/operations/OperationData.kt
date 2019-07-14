package com.siju.acexplorer.storage.model.operations

class OperationData private constructor(var arg1 : String, var arg2 : String) {

    private var paths = emptyList<String>()

    constructor(paths : ArrayList<String>) : this("null", "null") {
        this.paths = paths
    }
    companion object {
        fun createRenameOperation(filePath: String, fileName: String) : OperationData {
            return OperationData(filePath, fileName)
        }

        fun createNewFolderOperation(filePath: String, name: String): OperationData {
            return OperationData(filePath, name)
        }

        fun createNewFileOperation(filePath: String, name: String): OperationData {
            return OperationData(filePath, name)
        }

        fun createDeleteOperation(filePaths : ArrayList<String>) : OperationData  {
            return OperationData(filePaths)
        }
    }
}