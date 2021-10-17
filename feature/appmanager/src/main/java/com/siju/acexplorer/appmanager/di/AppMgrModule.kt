package com.siju.acexplorer.appmanager.di

import android.content.Context
import androidx.preference.PreferenceManager
import com.siju.acexplorer.appmanager.model.AppMgrModel
import com.siju.acexplorer.appmanager.model.AppMgrModelImpl
import com.siju.acexplorer.appmanager.selection.MultiSelection
import com.siju.acexplorer.appmanager.selection.MultiSelectionImpl
import com.siju.acexplorer.common.SortModeData
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
abstract class AppMgrModule {

   @Binds
   abstract fun bindAppMgrModel(appMgrModel: AppMgrModelImpl) : AppMgrModel

   @Binds
   abstract fun bindMultiSelection(multiSelection: MultiSelectionImpl) : MultiSelection

   companion object {
      @Provides
      fun bindSortModeData(@ApplicationContext context: Context): SortModeData =
         SortModeData(PreferenceManager.getDefaultSharedPreferences(context))

   }

}