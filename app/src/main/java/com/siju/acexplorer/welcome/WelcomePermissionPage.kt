/*
 * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.siju.acexplorer.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.siju.acexplorer.R
import com.siju.acexplorer.common.theme.LocalDim
import com.siju.acexplorer.common.theme.MyApplicationTheme

/**
 * Final page: explains why All Files Access is needed before sending the user to system settings.
 *
 * There is deliberately no way past this page without the permission — the app cannot list a single
 * file without it, so letting the user through would only mean an empty home screen.
 */
@Composable
fun WelcomePermissionPage(
    hasStorageAccess: Boolean,
    onGrantStorageAccess: () -> Unit,
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = LocalDim.current
    val welcomeDimens = rememberWelcomeDimens()
    // Landscape on a phone has no room for full-size gaps; the button has to stay above the fold.
    val blockSpacing = if (welcomeDimens.isCompact) dimens.spaceMedium else dimens.spaceLarge

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimens.spaceLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(welcomeDimens.permissionBadge)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            // Tinted with onBackground rather than primary: the app's primary is a dark purple that
            // all but disappears against the primary-tinted badge behind it.
            if (hasStorageAccess) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(welcomeDimens.permissionIcon)
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_welcome_storage_access),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(welcomeDimens.permissionIcon)
                )
            }
        }

        Text(
            text = stringResource(
                if (hasStorageAccess) {
                    R.string.welcome_permission_granted_title
                } else {
                    R.string.welcome_permission_title
                }
            ),
            style = if (welcomeDimens.isCompact) {
                MaterialTheme.typography.headlineMedium
            } else {
                MaterialTheme.typography.displayLarge
            },
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = blockSpacing)
        )
        Text(
            text = stringResource(
                if (hasStorageAccess) {
                    R.string.welcome_permission_granted_message
                } else {
                    R.string.welcome_permission_message
                }
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = dimens.spaceMedium)
        )

        if (!hasStorageAccess) {
            Column(modifier = Modifier.padding(top = blockSpacing)) {
                PermissionReason(textRes = R.string.welcome_permission_reason_browse)
                PermissionReason(textRes = R.string.welcome_permission_reason_manage)
                PermissionReason(textRes = R.string.welcome_permission_reason_private)
            }
        }

        Button(
            onClick = if (hasStorageAccess) onGetStarted else onGrantStorageAccess,
            modifier = Modifier
                .padding(top = blockSpacing)
                .widthIn(max = welcomeDimens.contentMaxWidth)
                .fillMaxWidth()
                .height(welcomeDimens.primaryButtonHeight)
        ) {
            Text(
                text = stringResource(
                    if (hasStorageAccess) {
                        R.string.welcome_get_started
                    } else {
                        R.string.welcome_permission_grant
                    }
                ),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (!hasStorageAccess) {
            Text(
                text = stringResource(R.string.welcome_permission_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = dimens.spaceMedium)
            )
        }
    }
}

@Composable
private fun PermissionReason(textRes: Int, modifier: Modifier = Modifier) {
    val dimens = LocalDim.current
    val welcomeDimens = rememberWelcomeDimens()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = dimens.spaceSmall),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(welcomeDimens.reasonIcon)
        )
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
            modifier = Modifier.padding(start = dimens.spaceSmall)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomePermissionPagePreview() {
    MyApplicationTheme {
        WelcomePermissionPage(
            hasStorageAccess = false,
            onGrantStorageAccess = {},
            onGetStarted = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomePermissionPageGrantedPreview() {
    MyApplicationTheme {
        WelcomePermissionPage(
            hasStorageAccess = true,
            onGrantStorageAccess = {},
            onGetStarted = {}
        )
    }
}
