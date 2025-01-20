package com.siju.acexplorer.common.compose.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.siju.acexplorer.common.R
import com.siju.acexplorer.common.compose.data.IconSource

@Composable
fun ExpandableMenu(
    iconSource: IconSource,
    iconContentDescription: Int,
    modifier: Modifier = Modifier
        .widthIn(min = 150.dp)
        .background(MaterialTheme.colorScheme.primaryContainer),
    showBadge: Boolean = false,
    content: @Composable (dismissMenu: () -> Unit) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    BadgedBox(
        badge = {
            if (showBadge) {
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = dimensionResource(R.dimen.padding_5_neg), y = dimensionResource(R.dimen.padding_10)),
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            }
        }
    ) {
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
    }
    DropdownMenu(
        expanded = showMenu,
        modifier = modifier,
        onDismissRequest = { showMenu = false }
    ) {
        content { showMenu = false }
    }
}