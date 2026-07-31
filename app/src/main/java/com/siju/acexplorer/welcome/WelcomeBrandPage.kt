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

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.siju.acexplorer.R
import com.siju.acexplorer.common.theme.LocalDim
import com.siju.acexplorer.common.theme.MyApplicationTheme

private const val ICON_ENTRANCE_DURATION_MS = 500
private const val TEXT_ENTRANCE_DURATION_MS = 400
private const val TEXT_ENTRANCE_DELAY_MS = 200

/**
 * First page: introduces the app and sets the visual tone using the user's chosen theme.
 */
@Composable
fun WelcomeBrandPage(modifier: Modifier = Modifier) {
    val dimens = LocalDim.current
    val welcomeDimens = rememberWelcomeDimens()
    // Saved so the entrance animation does not replay on every rotation.
    var entered by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    val iconScale by animateFloatAsState(
        targetValue = if (entered) 1f else 0.7f,
        animationSpec = tween(durationMillis = ICON_ENTRANCE_DURATION_MS, easing = FastOutSlowInEasing),
        label = "brandIconScale"
    )
    val iconAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(durationMillis = ICON_ENTRANCE_DURATION_MS),
        label = "brandIconAlpha"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(
            durationMillis = TEXT_ENTRANCE_DURATION_MS,
            delayMillis = TEXT_ENTRANCE_DELAY_MS
        ),
        label = "brandTextAlpha"
    )

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
                .scale(iconScale)
                .alpha(iconAlpha)
                .size(welcomeDimens.appIconBadge)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.mipmap.ic_launcher),
                contentDescription = stringResource(R.string.welcome_app_icon),
                modifier = Modifier.size(welcomeDimens.appIconImage)
            )
        }

        Text(
            text = stringResource(R.string.app_name),
            style = if (welcomeDimens.isCompact) {
                MaterialTheme.typography.headlineMedium
            } else {
                MaterialTheme.typography.displayLarge
            },
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .alpha(textAlpha)
                .padding(top = if (welcomeDimens.isCompact) dimens.spaceMedium else dimens.spaceLarge)
        )
        Text(
            text = stringResource(R.string.welcome_brand_tagline),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .alpha(textAlpha)
                .padding(top = dimens.spaceSmall)
        )
        Text(
            text = stringResource(R.string.welcome_brand_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .alpha(textAlpha)
                .padding(top = dimens.spaceMedium)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomeBrandPagePreview() {
    MyApplicationTheme {
        WelcomeBrandPage()
    }
}
