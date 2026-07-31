package com.siju.acexplorer.trash

import android.text.format.Formatter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.siju.acexplorer.R
import com.siju.acexplorer.storage.model.trash.TrashEntry
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    entries: List<TrashEntry>,
    totalSize: Long,
    retentionDays: Int,
    onNavigateBack: () -> Unit,
    onRestore: (List<Long>) -> Unit,
    onDeleteForever: (List<Long>) -> Unit,
    onEmptyAll: () -> Unit
) {
    var selectedIds by remember { mutableStateOf(emptySet<Long>()) }
    var confirmation by remember { mutableStateOf<TrashConfirmation?>(null) }

    // Entries can disappear underneath the selection when a purge runs while the screen is open.
    val validSelectedIds = selectedIds.intersect(entries.map { entry -> entry.id }.toSet())
    val inSelectionMode = validSelectedIds.isNotEmpty()

    Scaffold(
        topBar = {
            TrashTopBar(
                selectedCount = validSelectedIds.size,
                canEmpty = entries.isNotEmpty(),
                onNavigateBack = onNavigateBack,
                onClearSelection = { selectedIds = emptySet() },
                onRestoreSelected = {
                    onRestore(validSelectedIds.toList())
                    selectedIds = emptySet()
                },
                onDeleteSelected = { confirmation = TrashConfirmation.DeleteSelected(validSelectedIds.toList()) },
                onEmptyAll = { confirmation = TrashConfirmation.EmptyAll(entries.size, totalSize) }
            )
        }
    ) { innerPadding ->
        if (entries.isEmpty()) {
            EmptyTrashMessage(Modifier.padding(innerPadding))
            return@Scaffold
        }

        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            items(entries, key = { entry -> entry.id }) { entry ->
                TrashRow(
                    entry = entry,
                    retentionDays = retentionDays,
                    selected = entry.id in validSelectedIds,
                    inSelectionMode = inSelectionMode,
                    onToggle = {
                        selectedIds = if (entry.id in validSelectedIds) {
                            validSelectedIds - entry.id
                        }
                        else {
                            validSelectedIds + entry.id
                        }
                    }
                )
            }
        }
    }

    confirmation?.let { pendingConfirmation ->
        TrashConfirmationDialog(
            confirmation = pendingConfirmation,
            onDismiss = { confirmation = null },
            onConfirm = {
                when (pendingConfirmation) {
                    is TrashConfirmation.EmptyAll -> onEmptyAll()
                    is TrashConfirmation.DeleteSelected -> onDeleteForever(pendingConfirmation.ids)
                }
                selectedIds = emptySet()
                confirmation = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrashTopBar(
    selectedCount: Int,
    canEmpty: Boolean,
    onNavigateBack: () -> Unit,
    onClearSelection: () -> Unit,
    onRestoreSelected: () -> Unit,
    onDeleteSelected: () -> Unit,
    onEmptyAll: () -> Unit
) {
    val inSelectionMode = selectedCount > 0

    TopAppBar(
        // Matches the toolbar shade the rest of the app uses.
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        title = {
            Text(
                if (inSelectionMode) selectedCount.toString()
                else stringResource(R.string.trash_title)
            )
        },
        navigationIcon = {
            IconButton(onClick = if (inSelectionMode) onClearSelection else onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack,
                     contentDescription = stringResource(R.string.navigate_back))
            }
        },
        actions = {
            if (inSelectionMode) {
                IconButton(onClick = onRestoreSelected) {
                    Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.trash_restore))
                }
                IconButton(onClick = onDeleteSelected) {
                    Icon(Icons.Filled.Delete,
                         contentDescription = stringResource(R.string.trash_delete_forever))
                }
            }
            else if (canEmpty) {
                TextButton(onClick = onEmptyAll) {
                    Text(stringResource(R.string.trash_empty_action))
                }
            }
        }
    )
}

@Composable
private fun TrashRow(
    entry: TrashEntry,
    retentionDays: Int,
    selected: Boolean,
    inSelectionMode: Boolean,
    onToggle: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = selected, onValueChange = { onToggle() })
            .background(
                if (selected) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = painterResource(
                if (entry.isDirectory) R.drawable.ic_folder else R.drawable.ic_doc
            ),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.displayName,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.trash_original_location, entry.originalParentPath),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${Formatter.formatFileSize(context, entry.size)} · ${daysLeftLabel(entry, retentionDays)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        if (inSelectionMode) {
            Checkbox(checked = selected, onCheckedChange = { onToggle() })
        }
    }
}

@Composable
private fun daysLeftLabel(entry: TrashEntry, retentionDays: Int): String {
    val daysHeld = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - entry.deletedAt).toInt()
    val daysLeft = (retentionDays - daysHeld).coerceAtLeast(0)
    return if (daysLeft <= 1) stringResource(R.string.trash_last_day)
    else stringResource(R.string.trash_days_left, daysLeft)
}

@Composable
private fun EmptyTrashMessage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.trash_empty_title),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = stringResource(R.string.trash_empty_summary),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun TrashConfirmationDialog(
    confirmation: TrashConfirmation,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val context = LocalContext.current
    val message = when (confirmation) {
        is TrashConfirmation.EmptyAll -> pluralStringResource(
            R.plurals.trash_empty_confirm,
            confirmation.itemCount,
            confirmation.itemCount,
            Formatter.formatFileSize(context, confirmation.totalSize)
        )

        is TrashConfirmation.DeleteSelected -> pluralStringResource(
            R.plurals.trash_delete_forever_confirm,
            confirmation.ids.size,
            confirmation.ids.size
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.trash_delete_forever)) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.trash_delete_forever))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(com.siju.acexplorer.common.R.string.dialog_cancel))
            }
        }
    )
}

/** A destructive action waiting on the user to confirm it. */
private sealed interface TrashConfirmation {
    data class EmptyAll(val itemCount: Int, val totalSize: Long) : TrashConfirmation
    data class DeleteSelected(val ids: List<Long>) : TrashConfirmation
}
