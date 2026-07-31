package com.siju.acexplorer.tools

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.siju.acexplorer.storage.model.trash.TrashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ToolsViewModel @Inject constructor(trashRepository: TrashRepository) : ViewModel() {

    /** Drives the size shown under the Recycle Bin entry. */
    val trashSize: LiveData<Long> = trashRepository.observeTotalSize()
}
