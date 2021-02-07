package com.siju.acexplorer.storage.modules.picker.di

import com.siju.acexplorer.storage.modules.picker.model.PickerModel
import com.siju.acexplorer.storage.modules.picker.model.PickerModelImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class PickerModule {

   @Binds
   abstract fun bindPickerModel(pickerModelImpl: PickerModelImpl) : PickerModel

}