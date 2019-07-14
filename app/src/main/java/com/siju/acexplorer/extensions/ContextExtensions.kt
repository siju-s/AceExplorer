package com.siju.acexplorer.extensions

import android.content.Context
import android.content.Intent

fun Context.canHandleIntent(intent: Intent) = this.packageManager.resolveActivity(intent, 0) != null