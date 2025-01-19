package com.siju.acexplorer.common.compose.ui.menu

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.siju.acexplorer.common.compose.data.IconSource

@Composable
fun ExpandableMenu(
    iconSource: IconSource,
    iconContentDescription: Int,
    content: @Composable (dismissMenu: () -> Unit) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    IconButton(onClick = {
        showMenu = !showMenu
    }) {
        when (iconSource) {
            is IconSource.Painter -> {
                Icon(
                    painter = painterResource(iconSource.resId),
                    contentDescription = stringResource(iconContentDescription),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            is IconSource.Vector -> {
                Icon(
                    imageVector = iconSource.imageVector,
                    contentDescription = stringResource(iconContentDescription),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {
        content { showMenu = false }
    }
}