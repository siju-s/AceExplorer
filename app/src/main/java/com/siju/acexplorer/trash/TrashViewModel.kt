package com.siju.acexplorer.trash

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siju.acexplorer.storage.model.trash.TrashEntry
import com.siju.acexplorer.storage.model.trash.TrashPreferences
import com.siju.acexplorer.storage.model.trash.TrashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val trashRepository: TrashRepository,
    private val trashPreferences: TrashPreferences
) : ViewModel() {

    val entries: LiveData<List<TrashEntry>> = trashRepository.observeEntries()

    val totalSize: LiveData<Long> = trashRepository.observeTotalSize()

    val retentionDays: Int
        get() = trashPreferences.retentionDays

    /**
     * Drops items past the retention window. Called when the screen opens rather than from a
     * scheduled job, which is accurate enough for a window measured in days.
     */
    fun purgeExpired() {
        viewModelScope.launch {
            trashRepository.purgeOlderThan(trashPreferences.retentionDays)
        }
    }

    fun restore(ids: List<Long>) {
        viewModelScope.launch {
            trashRepository.restore(ids)
        }
    }

    fun deleteForever(ids: List<Long>) {
        viewModelScope.launch {
            trashRepository.deleteForever(ids)
        }
    }

    fun emptyAll() {
        viewModelScope.launch {
            trashRepository.emptyAll()
        }
    }
}
