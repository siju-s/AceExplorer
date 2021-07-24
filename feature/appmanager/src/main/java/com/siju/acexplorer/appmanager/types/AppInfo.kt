package com.siju.acexplorer.appmanager.types

import com.siju.acexplorer.appmanager.filter.AppSource
import com.siju.acexplorer.appmanager.filter.AppType

data class AppInfo(val name : String,
                   val packageName : String,
                   val appType: AppType,
                   val source : AppSource,
                   val apkDir : String,
                   val size : Long,
                   val installDate : Long,
                   val updatedDate : Long
                   )
