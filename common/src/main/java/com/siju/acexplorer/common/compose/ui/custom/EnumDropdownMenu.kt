package com.siju.acexplorer.common.compose.ui.custom

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun <T : Enum<T>> EnumDropdownMenu(
    selectedItem: T,
    enumEntries: Array<T>,
    resourceIdProvider: (T) -> Int,
    dismissMenu: () -> Unit,
    onMenuItemClicked: (T) -> Unit
) {
    enumEntries.forEach { entry ->
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = (entry == selectedItem),
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(resourceIdProvider(entry)))
                }
            },
            onClick = {
                dismissMenu()
                onMenuItemClicked(entry)
            }
        )
    }
}
