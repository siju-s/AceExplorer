package com.siju.acexplorer.appmanager.di

import android.content.Context
import com.siju.acexplorer.common.PreferenceConstants
import com.siju.acexplorer.common.ViewModeData
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.migration.DisableInstallInCheck

@Module
@DisableInstallInCheck
object ViewModeModule {

    @Provides
    fun getViewModeInfo(@ApplicationContext context: Context) =
        ViewModeData(context.getSharedPreferences(PreferenceConstants.PREFS_NAME, Context.MODE_PRIVATE))
}