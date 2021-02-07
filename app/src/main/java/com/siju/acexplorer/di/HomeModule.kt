package com.siju.acexplorer.di

import com.siju.acexplorer.home.model.HomeModel
import com.siju.acexplorer.home.model.HomeModelImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class HomeModule {

   @Binds
   abstract fun bindHomeModel(homeModelImpl: HomeModelImpl) : HomeModel

}