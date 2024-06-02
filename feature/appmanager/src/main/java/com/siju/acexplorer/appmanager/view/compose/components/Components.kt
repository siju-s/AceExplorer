package com.siju.acexplorer.appmanager.view.compose.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BodyText(text : String, modifier: Modifier = Modifier, maxLines: Int = 1) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier,
        maxLines = maxLines,
    )
}