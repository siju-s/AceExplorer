package com.siju.acexplorer.extensions

import android.content.Context
import android.content.Intent
import android.widget.Toast

fun Context.canHandleIntent(intent: Intent) = this.packageManager.resolveActivity(intent, 0) != null

fun Context?.showToast(msg: String?, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, duration).show()
}