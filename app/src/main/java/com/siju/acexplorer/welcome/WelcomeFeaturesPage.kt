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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.siju.acexplorer.R
import com.siju.acexplorer.common.theme.LocalDim
import com.siju.acexplorer.common.theme.MyApplicationTheme
import kotlinx.coroutines.delay

private const val CARD_STAGGER_DELAY_MS = 60L
private const val CARD_ENTRANCE_DURATION_MS = 320
private val CARD_CORNER_RADIUS = 20.dp
private val CARD_ENTRANCE_OFFSET = 20.dp

/**
 * Second page: the whole feature set at a glance.
 *
 * Rendered from [WelcomeFeature] so shipping a new feature never means touching this layout.
 */
@Composable
fun WelcomeFeaturesPage(modifier: Modifier = Modifier) {
    val dimens = LocalDim.current
    val welcomeDimens = rememberWelcomeDimens()
    val features = remember { WelcomeFeature.entries }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimens.spaceMedium)
    ) {
        Text(
            text = stringResource(R.string.welcome_features_title),
            style = if (welcomeDimens.isCompact) {
                MaterialTheme.typography.headlineSmall
            } else {
                MaterialTheme.typography.displayLarge
            },
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(
                top = if (welcomeDimens.isCompact) dimens.spaceSmall else dimens.spaceMedium
            )
        )
        Text(
            text = stringResource(R.string.welcome_features_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = dimens.spaceExtraSmall)
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = welcomeDimens.featureCardMinWidth),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                vertical = if (welcomeDimens.isCompact) dimens.spaceSmall else dimens.spaceMedium
            ),
            horizontalArrangement = Arrangement.spacedBy(dimens.spaceSmall),
            verticalArrangement = Arrangement.spacedBy(dimens.spaceSmall)
        ) {
            itemsIndexed(features, key = { _, feature -> feature.name }) { index, feature ->
                FeatureCard(feature = feature, position = index)
            }
        }
    }
}

@Composable
private fun FeatureCard(feature: WelcomeFeature, position: Int, modifier: Modifier = Modifier) {
    val dimens = LocalDim.current
    val welcomeDimens = rememberWelcomeDimens()
    // Saved so the staggered entrance does not replay on every rotation.
    var entered by rememberSaveable(feature) { mutableStateOf(false) }
    LaunchedEffect(feature) {
        delay(position * CARD_STAGGER_DELAY_MS)
        entered = true
    }

    val cardAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(durationMillis = CARD_ENTRANCE_DURATION_MS),
        label = "featureCardAlpha"
    )
    val cardOffset by animateDpAsState(
        targetValue = if (entered) 0.dp else CARD_ENTRANCE_OFFSET,
        animationSpec = tween(durationMillis = CARD_ENTRANCE_DURATION_MS, easing = FastOutSlowInEasing),
        label = "featureCardOffset"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .offset(y = cardOffset)
            .alpha(cardAlpha)
            .clip(RoundedCornerShape(CARD_CORNER_RADIUS))
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f))
            .padding(dimens.spaceMedium)
    ) {
        Box(
            modifier = Modifier
                .size(welcomeDimens.featureIconBadge)
                .clip(RoundedCornerShape(dimens.spaceSmall))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(feature.iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(welcomeDimens.featureIcon)
            )
        }
        Text(
            text = stringResource(feature.titleRes),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = dimens.spaceSmall)
        )
        Text(
            text = stringResource(feature.descriptionRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = dimens.spaceXXSmall)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomeFeaturesPagePreview() {
    MyApplicationTheme {
        WelcomeFeaturesPage()
    }
}
