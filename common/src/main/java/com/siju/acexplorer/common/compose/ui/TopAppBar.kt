package com.siju.acexplorer.common.compose.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.siju.acexplorer.common.R
import com.siju.acexplorer.common.ViewMode
import com.siju.acexplorer.common.compose.data.MenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithSearch(
    title: String,
    actionModeEnabled: Boolean,
    searchQuery: TextFieldValue,
    isSearchVisible: Boolean,
    onMenuItemClicked:(MenuItem) -> Unit,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    onSearchToggle: () -> Unit,
    onClearSearchQuery: () -> Unit,
    actionModeContent: @Composable () -> Unit = {},
    onNavigationClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    TopAppBar(
        title = {
            if (isSearchVisible) {
                SearchField(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    focusRequester = focusRequester
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            } else {
                Text(title)
            }
        },
        actions = {
            if (!actionModeEnabled) {
                SearchIcon(
                    isSearchVisible = isSearchVisible, onSearchToggle = {
                        if (isSearchVisible) {
                            onClearSearchQuery()
                            focusManager.clearFocus()
                        }
                        onSearchToggle()
                    }
                )
                OverflowMenu { dismissMenu ->
                    MenuItems(dismissMenu, onMenuItemClicked)
                }
            }
            actionModeContent()
        },
        navigationIcon = {
            if (actionModeEnabled) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.action_more),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    )
}

@Composable
private fun MenuItems(
    dismissMenu: () -> Unit,
    onMenuItemClicked:(MenuItem) -> Unit
) {
    val context = LocalContext.current
    var showSubMenu by remember { mutableStateOf(false) }

    if (!showSubMenu) {
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.action_view)) },
            onClick = {
                showSubMenu = true
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.action_sort)) },
            onClick = {
                dismissMenu()
                onMenuItemClicked(MenuItem.Sort)
            }
        )
    } else {
        ViewMode.entries.forEach { viewMode ->
            DropdownMenuItem(
                text = { Text(text = context.getString(viewMode.resourceId)) },
                onClick = {
                    dismissMenu()
                    onMenuItemClicked(MenuItem.ViewMode(viewMode))
                }
            )
        }
    }
}