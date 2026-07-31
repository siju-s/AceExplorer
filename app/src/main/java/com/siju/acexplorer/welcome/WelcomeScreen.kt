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

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.core.view.WindowCompat
import com.siju.acexplorer.R
import com.siju.acexplorer.common.theme.LocalDim
import com.siju.acexplorer.common.theme.MyApplicationTheme
import kotlinx.coroutines.launch

private const val PAGE_BRAND = 0
private const val PAGE_FEATURES = 1
private const val PAGE_PERMISSION = 2
private const val WELCOME_PAGE_COUNT = 3

private const val DOT_ANIMATION_DURATION_MS = 250
private const val LIGHT_BACKGROUND_LUMINANCE = 0.5f

/**
 * Fresh-install onboarding: brand, feature overview, then the storage permission.
 *
 * The last page is a hard gate — [onWelcomeComplete] only fires once [hasStorageAccess] is true.
 */
@Composable
fun WelcomeScreen(
    hasStorageAccess: Boolean,
    onGrantStorageAccess: () -> Unit,
    onWelcomeComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = LocalDim.current
    val welcomeDimens = rememberWelcomeDimens()
    val pagerState = rememberPagerState(pageCount = { WELCOME_PAGE_COUNT })
    val scope = rememberCoroutineScope()

    SyncSystemBarsWithBackground()

    fun scrollToPage(page: Int) {
        scope.launch { pagerState.animateScrollToPage(page) }
    }

    BackHandler(enabled = pagerState.currentPage > PAGE_BRAND) {
        scrollToPage(pagerState.currentPage - 1)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.safeDrawingPadding()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                when (page) {
                    PAGE_BRAND -> WelcomeBrandPage()
                    PAGE_FEATURES -> WelcomeFeaturesPage()
                    else -> WelcomePermissionPage(
                        hasStorageAccess = hasStorageAccess,
                        onGrantStorageAccess = onGrantStorageAccess,
                        onGetStarted = onWelcomeComplete
                    )
                }
            }

            PageIndicator(
                selectedPage = pagerState.currentPage,
                modifier = Modifier.padding(vertical = dimens.spaceSmall)
            )

            WelcomeBottomBar(
                visible = pagerState.currentPage != PAGE_PERMISSION,
                height = welcomeDimens.bottomBarHeight,
                onSkip = { scrollToPage(PAGE_PERMISSION) },
                onNext = { scrollToPage(pagerState.currentPage + 1) }
            )
        }
    }
}

/**
 * Keeps the status and navigation bar icons readable.
 *
 * The theme here is the user's app theme, which is independent of the system dark mode that
 * `enableEdgeToEdge` uses to pick its default bar icon colour — a Light app theme on a device in
 * dark mode would otherwise draw white icons on a white background.
 */
@Composable
private fun SyncSystemBarsWithBackground() {
    val view = LocalView.current
    val lightBackground = MaterialTheme.colorScheme.background.luminance() > LIGHT_BACKGROUND_LUMINANCE
    if (view.isInEditMode) {
        return
    }
    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = lightBackground
            isAppearanceLightNavigationBars = lightBackground
        }
    }
}

/**
 * Skip and Next actions. Hidden on the permission page, which carries its own call to action.
 *
 * Skip jumps to the permission page rather than out of onboarding — there is no way into the app
 * without the permission.
 */
@Composable
private fun WelcomeBottomBar(
    visible: Boolean,
    height: Dp,
    onSkip: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = LocalDim.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = dimens.spaceMedium)
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            TextButton(onClick = onSkip) {
                Text(text = stringResource(R.string.skip))
            }
        }
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            TextButton(onClick = onNext) {
                Text(text = stringResource(R.string.next))
            }
        }
    }
}

@Composable
private fun PageIndicator(selectedPage: Int, modifier: Modifier = Modifier) {
    val dimens = LocalDim.current
    val welcomeDimens = rememberWelcomeDimens()
    val pageLabel = stringResource(
        R.string.welcome_page_indicator,
        selectedPage + 1,
        WELCOME_PAGE_COUNT
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = pageLabel },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(WELCOME_PAGE_COUNT) { page ->
            val selected = page == selectedPage
            val dotWidth by animateDpAsState(
                targetValue = if (selected) {
                    welcomeDimens.indicatorDotSelected
                } else {
                    welcomeDimens.indicatorDot
                },
                animationSpec = tween(durationMillis = DOT_ANIMATION_DURATION_MS),
                label = "pageIndicatorWidth"
            )
            val dotColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = dimens.spaceExtraSmall)
                    .size(width = dotWidth, height = welcomeDimens.indicatorDot)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WelcomeScreenPreview() {
    MyApplicationTheme {
        WelcomeScreen(
            hasStorageAccess = false,
            onGrantStorageAccess = {},
            onWelcomeComplete = {}
        )
    }
}
