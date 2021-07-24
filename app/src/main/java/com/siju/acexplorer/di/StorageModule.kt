package com.siju.acexplorer.di

import android.content.Context
import com.siju.acexplorer.common.PreferenceConstants.PREFS_NAME
import com.siju.acexplorer.common.ViewModeData
import com.siju.acexplorer.storage.model.StorageModel
import com.siju.acexplorer.storage.model.StorageModelImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
abstract class StorageModule {

   @Binds
   abstract fun bindStorageModel(storageModelImpl: StorageModelImpl) : StorageModel

   companion object {
      @Provides
      fun getViewModeInfo(@ApplicationContext context: Context) =
         ViewModeData(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
   }
}