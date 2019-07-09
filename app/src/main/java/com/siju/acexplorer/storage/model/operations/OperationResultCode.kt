package com.siju.acexplorer.storage.model.operations

enum class OperationResultCode(val code : Int) {
    SUCCESS(0),
    SAF(1),
    FAIL(-1),
    FILE_EXISTS(-2),
    INVALID_FILE(-3)
}