package com.siju.acexplorer.main.model.helper

import android.content.Context
import android.content.Intent

object IntentResolver {

    fun canHandleIntent(context: Context, intent: Intent) =
            intent.resolveActivity(context.packageManager) != null
}