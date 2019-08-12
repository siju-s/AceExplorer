package com.siju.acexplorer.storage.model.operations

import com.siju.acexplorer.common.types.FileInfo

class PasteConflictCheckData(val files: ArrayList<FileInfo>,
                             val conflictFiles: ArrayList<FileInfo>, val destFiles : ArrayList<FileInfo>,
                             val destinationDir: String, val operations: Operations)