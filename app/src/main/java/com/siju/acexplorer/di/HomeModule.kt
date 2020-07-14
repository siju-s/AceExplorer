package com.siju.acexplorer.di

import com.siju.acexplorer.home.model.HomeModel
import com.siju.acexplorer.home.model.HomeModelImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class HomeModule {

   @Binds
   abstract fun bindHomeModel(homeModelImpl: HomeModelImpl) : HomeModel

}