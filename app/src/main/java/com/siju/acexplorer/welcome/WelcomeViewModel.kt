/*
 * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.siju.acexplorer.welcome

import androidx.lifecycle.ViewModel
import com.siju.acexplorer.utils.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * State of the onboarding flow.
 *
 * @param hasStorageAccess whether All Files Access is currently held.
 * @param welcomeFinished set once onboarding has been completed and persisted, which is the signal
 * for the UI to move on to the home screen.
 */
data class WelcomeUiState(
    val hasStorageAccess: Boolean = false,
    val welcomeFinished: Boolean = false
)

/**
 * Owns the onboarding state: the storage permission and the first-launch flag.
 *
 * The permission is granted in system settings, so it has to be re-read every time the activity
 * comes back to the foreground rather than trusting an activity result.
 */
@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val storageAccessChecker: StorageAccessChecker,
    private val prefManager: PrefManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        WelcomeUiState(hasStorageAccess = storageAccessChecker.isStorageAccessGranted())
    )
    val uiState: StateFlow<WelcomeUiState> = _uiState.asStateFlow()

    /**
     * Whether onboarding should be shown at all. Read once, before the first frame, so a returning
     * user never sees a flash of the welcome screen.
     */
    val isFirstLaunch: Boolean = prefManager.isFirstTimeLaunch

    fun refreshStorageAccess() {
        _uiState.value = _uiState.value.copy(
            hasStorageAccess = storageAccessChecker.isStorageAccessGranted()
        )
    }

    /**
     * Persists that onboarding is done. Ignored without the permission — the app cannot list a
     * single file without it, so letting the user through would only mean an empty home screen.
     */
    fun onWelcomeCompleted() {
        if (!_uiState.value.hasStorageAccess) {
            return
        }
        prefManager.setFirstTimeLaunch()
        _uiState.value = _uiState.value.copy(welcomeFinished = true)
    }
}
