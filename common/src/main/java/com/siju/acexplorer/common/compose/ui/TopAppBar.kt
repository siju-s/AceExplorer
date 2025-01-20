package com.siju.acexplorer.common.compose.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.siju.acexplorer.common.R
import com.siju.acexplorer.common.compose.data.IconSource
import com.siju.acexplorer.common.compose.ui.menu.ExpandableMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithSearch(
    title: String,
    subtitle:String,
    actionModeEnabled: Boolean,
    searchQuery: TextFieldValue,
    isSearchVisible: Boolean,
    searchPlaceholderText: Int,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    onSearchToggle: () -> Unit,
    onClearSearchQuery: () -> Unit,
    onNavigationClick: () -> Unit,
    actionModeContent: @Composable () -> Unit = {},
    menuItems: @Composable () -> Unit = {},
    overflowMenuItems: @Composable (dismissMenu: () -> Unit) -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    TopAppBar(
        title = {
            if (isSearchVisible) {
                SearchField(
                    searchQuery = searchQuery,
                    placeholderText = searchPlaceholderText,
                    onSearchQueryChange = onSearchQueryChange,
                    focusRequester = focusRequester
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            } else {
                Column {
                    Text(title, style = MaterialTheme.typography.titleLarge)
                    Text(subtitle, style = MaterialTheme.typography.labelLarge)
                }
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
                menuItems()
                ExpandableMenu(
                    IconSource.Vector(Icons.Outlined.MoreVert),
                    R.string.action_more
                   ) { dismissMenu ->
                    overflowMenuItems(dismissMenu)
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