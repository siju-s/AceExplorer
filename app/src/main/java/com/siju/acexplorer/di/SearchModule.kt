package com.siju.acexplorer.di

import com.siju.acexplorer.search.model.SearchModel
import com.siju.acexplorer.search.model.SearchModelImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class SearchModule {

   @Binds
   abstract fun bindSearchModel(searchModelImpl: SearchModelImpl) : SearchModel

}