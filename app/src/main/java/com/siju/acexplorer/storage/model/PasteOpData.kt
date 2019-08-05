package com.siju.acexplorer.storage.model

import com.siju.acexplorer.common.types.FileInfo
import com.siju.acexplorer.storage.model.operations.Operations

class PasteOpData(val baseOperation : Operations , val operations: Operations, val filesToPaste : ArrayList<FileInfo>,
                  val destinationDir : String?) {
}