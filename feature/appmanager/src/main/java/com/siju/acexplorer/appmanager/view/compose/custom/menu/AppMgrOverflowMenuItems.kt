package com.siju.acexplorer.appmanager.view.compose.custom.menu

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.siju.acexplorer.appmanager.view.compose.data.AppMgrMenuItem
import com.siju.acexplorer.common.R
import com.siju.acexplorer.common.ViewMode
import com.siju.acexplorer.common.compose.ui.custom.EnumDropdownMenu
import com.siju.acexplorer.common.compose.ui.menu.DropdownMenuTrailingIcon

@Composable
fun AppMgrOverflowMenuItems(
    selectedViewMode: ViewMode,
    dismissMenu: () -> Unit,
    onMenuItemClicked: (AppMgrMenuItem) -> Unit
) {
    var showSubMenu by remember { mutableStateOf(false) }

    if (showSubMenu) {
        EnumDropdownMenu(
            selectedViewMode,
            ViewMode.entries.toTypedArray(),
            resourceIdProvider = { it.resourceId },
            dismissMenu,
            onMenuItemClicked = { viewMode ->
                onMenuItemClicked(AppMgrMenuItem.ViewMode(viewMode))
            }
        )
    } else {
        DropdownMenuTrailingIcon(R.string.action_view) {
            showSubMenu = true
        }
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.action_sort)) },
            onClick = {
                dismissMenu()
                onMenuItemClicked(AppMgrMenuItem.Sort)
            }
        )
    }
}