package com.siju.acexplorer.di

import android.content.Context
import androidx.preference.PreferenceManager
import com.siju.acexplorer.common.SortModeData
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.migration.DisableInstallInCheck

@Module
@DisableInstallInCheck
object SortModeModule {
    @Provides
    fun getSortModeInfo(@ApplicationContext context: Context) =
        SortModeData(PreferenceManager.getDefaultSharedPreferences(context))

}