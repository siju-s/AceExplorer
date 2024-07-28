package com.siju.acexplorer.common.compose.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.text.input.TextFieldValue
import com.siju.acexplorer.common.ViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithSearch(
    title: String,
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    isSearchVisible: Boolean,
    onSearchToggle: () -> Unit,
    onClearSearchQuery: () -> Unit,
    onViewModeSelected: (ViewMode) -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var expanded by remember { mutableStateOf(false) }

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
            SearchIcon(
                isSearchVisible = isSearchVisible, onSearchToggle = {
                    if (isSearchVisible) {
                        onClearSearchQuery()
                        focusManager.clearFocus()
                    }
                    onSearchToggle()
                }
            )
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More Options")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                ViewMode.entries.forEach { viewMode ->
                    DropdownMenuItem(
                        text = { Text(text = LocalContext.current.getString(viewMode.resourceId)) },
                        onClick = {
                            onViewModeSelected(viewMode)
                            expanded = false
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )


}