package com.siju.acexplorer.storage.model.operations

class OperationData private constructor(var arg1 : String, var arg2 : String) {

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
    }
}