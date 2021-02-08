package com.siju.acexplorer.home.edit.di

import com.siju.acexplorer.home.edit.model.CategoryEditModel
import com.siju.acexplorer.home.edit.model.CategoryEditModelImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class CategoryEditModule {

   @Binds
   abstract fun bindCategoryEditModel(categoryEditModelImpl: CategoryEditModelImpl) : CategoryEditModel

}