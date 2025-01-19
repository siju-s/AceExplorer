package com.siju.acexplorer.common.compose.ui.menu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun DropdownMenuTrailingIcon(textResId: Int, showSubMenuClicked: () -> Unit) {
    DropdownMenuItem(
        text = { Text(text = stringResource(textResId)) },
        onClick = showSubMenuClicked,
        trailingIcon = {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null
            )
        }
    )
}