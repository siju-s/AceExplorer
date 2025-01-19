package com.siju.acexplorer.common.compose.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun SearchField(
    searchQuery: TextFieldValue,
    placeholderText: Int,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    focusRequester: FocusRequester
) {
    TextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text(stringResource(id = placeholderText)) },
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.White,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color.White,
            focusedTextColor = Color.White
        )
    )
}

@Composable
fun SearchIcon(
    isSearchVisible: Boolean,
    onSearchToggle: () -> Unit
) {
    IconButton(onClick = onSearchToggle) {
        Icon(
            imageVector = if (isSearchVisible) Icons.Default.Close else Icons.Default.Search,
            contentDescription = if (isSearchVisible) "Close Search" else "Open Search",
            tint = Color.White
        )
    }
}