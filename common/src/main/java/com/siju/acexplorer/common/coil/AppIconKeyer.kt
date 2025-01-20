package com.siju.acexplorer.common.coil

import android.content.pm.ApplicationInfo
import coil3.key.Keyer
import coil3.request.Options

class AppIconKeyer : Keyer<ApplicationInfo> {

    override fun key(data: ApplicationInfo, options: Options): String? {
        return data.packageName
    }
}