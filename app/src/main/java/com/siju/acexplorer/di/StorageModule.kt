package com.siju.acexplorer.di

import com.siju.acexplorer.storage.model.StorageModel
import com.siju.acexplorer.storage.model.StorageModelImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class StorageModule {

   @Binds
   abstract fun bindStorageModel(storageModelImpl: StorageModelImpl) : StorageModel

}