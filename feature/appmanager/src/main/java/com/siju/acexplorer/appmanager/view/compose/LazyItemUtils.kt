package com.siju.acexplorer.appmanager.view.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.siju.acexplorer.common.theme.itemSelection
import com.siju.acexplorer.common.theme.itemSelectionDark
import com.siju.acexplorer.common.theme.transparent

object LazyItemUtils {

    @Composable
    fun getBackgroundColor(selected: Boolean): Color {
        val isDark = isSystemInDarkTheme()
        return remember(selected, isDark) {
            if (selected) {
                if (isDark) itemSelectionDark else itemSelection
            } else {
                transparent
            }
        }
    }
}
