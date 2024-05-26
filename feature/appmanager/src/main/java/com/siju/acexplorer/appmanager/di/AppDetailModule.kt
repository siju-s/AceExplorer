package com.siju.acexplorer.appmanager.di

import com.siju.acexplorer.appmanager.model.AppDetailDetailModelImpl
import com.siju.acexplorer.appmanager.model.AppDetailModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class AppDetailModule {

   @Binds
   abstract fun bindAppDetail(appDetailModelImpl: AppDetailDetailModelImpl) : AppDetailModel

}