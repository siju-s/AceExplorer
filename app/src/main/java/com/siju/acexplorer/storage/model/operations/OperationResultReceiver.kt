package com.siju.acexplorer.storage.model.operations

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.siju.acexplorer.logging.Logger
import com.siju.acexplorer.storage.model.operations.OperationUtils.ACTION_OP_REFRESH
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_FILES_COUNT
import com.siju.acexplorer.storage.model.operations.OperationUtils.KEY_OPERATION

private const val TAG = "OperationResultReceiver"

class OperationResultReceiver(private val operationHelper: OperationHelper) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            ACTION_OP_REFRESH  -> {
                val bundle = intent.extras
                val operation = bundle?.getSerializable(KEY_OPERATION) as Operations?
                operation?.let { onOperationResult(intent, it) }
            }
        }
    }

    private fun onOperationResult(intent: Intent, operation: Operations) {
        Logger.log(TAG, "onOperationResult: $operation")
        val count = intent.getIntExtra(KEY_FILES_COUNT, 0)
        operationHelper.onOperationCompleted(operation, count)
    }
}
