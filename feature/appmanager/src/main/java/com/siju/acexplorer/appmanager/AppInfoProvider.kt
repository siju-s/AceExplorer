package com.siju.acexplorer.appmanager

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.siju.acexplorer.appmanager.filter.AppSource
import com.siju.acexplorer.appmanager.filter.AppType
import com.siju.acexplorer.appmanager.types.AppInfo

class AppInfoProvider : PreviewParameterProvider<AppInfo> {
    override val values: Sequence<AppInfo> = listOf(AppInfo("Ace", "com.siju.acexplorer", AppType.USER_APP,AppSource.PLAYSTORE, "", 0, 0L, 0L),
        AppInfo("AceDev", "com.siju.acexplorer.dev", AppType.USER_APP,AppSource.PLAYSTORE, "", 0, 200000L, 0L)).asSequence()
}