package com.siju.acexplorer.appmanager.view.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.siju.acexplorer.common.theme.itemSelection
import com.siju.acexplorer.common.theme.itemSelectionDark
import com.siju.acexplorer.common.theme.transparent

object LazyItemUtils {

    @Composable
    fun getBackgroundColor(selectedPos: Boolean): Color {
        val isDark = isSystemInDarkTheme()
        return if (selectedPos) {
            if (isDark) itemSelectionDark else itemSelection
        }
        else {
            transparent
        }
    }

    @Composable
    fun getSelectionDrawable(selectedPos: Boolean) =
        if (selectedPos) com.siju.acexplorer.common.R.drawable.ic_select_checked else com.siju.acexplorer.common.R.drawable.ic_select_unchecked
}