package com.siju.acexplorer.common.compose.data

import androidx.compose.ui.graphics.vector.ImageVector

sealed class IconSource {
    data class Vector(val imageVector: ImageVector) : IconSource()
    data class Painter(val resId: Int) : IconSource()
}