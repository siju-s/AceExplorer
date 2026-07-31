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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val COMPACT_HEIGHT_DP = 520
private const val EXPANDED_WIDTH_DP = 600

/**
 * How much room the welcome screen has to work with.
 *
 * [COMPACT] in practice means a phone in landscape, [EXPANDED] a tablet.
 */
enum class WelcomeSizeClass { COMPACT, MEDIUM, EXPANDED }

/**
 * Sizing for the welcome flow, scoped to this package.
 *
 * The app-wide `LocalDim` is a fixed set of spacing constants that is never re-provided per screen
 * size, so onboarding carries its own sizes rather than shipping fixed dp that overflows a phone in
 * landscape and looks lost on a tablet.
 */
data class WelcomeDimens(
    val sizeClass: WelcomeSizeClass,
    val contentMaxWidth: Dp,
    val appIconBadge: Dp,
    val appIconImage: Dp,
    val permissionBadge: Dp,
    val permissionIcon: Dp,
    val reasonIcon: Dp,
    val primaryButtonHeight: Dp,
    val featureCardMinWidth: Dp,
    val featureIconBadge: Dp,
    val featureIcon: Dp,
    val indicatorDot: Dp,
    val indicatorDotSelected: Dp,
    val bottomBarHeight: Dp
) {
    val isCompact: Boolean get() = sizeClass == WelcomeSizeClass.COMPACT
}

private val CompactWelcomeDimens = WelcomeDimens(
    sizeClass = WelcomeSizeClass.COMPACT,
    contentMaxWidth = 420.dp,
    appIconBadge = 72.dp,
    appIconImage = 40.dp,
    permissionBadge = 56.dp,
    permissionIcon = 28.dp,
    reasonIcon = 18.dp,
    primaryButtonHeight = 44.dp,
    featureCardMinWidth = 150.dp,
    featureIconBadge = 32.dp,
    featureIcon = 18.dp,
    indicatorDot = 6.dp,
    indicatorDotSelected = 18.dp,
    bottomBarHeight = 48.dp
)

private val MediumWelcomeDimens = WelcomeDimens(
    sizeClass = WelcomeSizeClass.MEDIUM,
    contentMaxWidth = 480.dp,
    appIconBadge = 112.dp,
    appIconImage = 64.dp,
    permissionBadge = 88.dp,
    permissionIcon = 40.dp,
    reasonIcon = 20.dp,
    primaryButtonHeight = 52.dp,
    featureCardMinWidth = 150.dp,
    featureIconBadge = 40.dp,
    featureIcon = 22.dp,
    indicatorDot = 8.dp,
    indicatorDotSelected = 24.dp,
    bottomBarHeight = 64.dp
)

private val ExpandedWelcomeDimens = WelcomeDimens(
    sizeClass = WelcomeSizeClass.EXPANDED,
    contentMaxWidth = 560.dp,
    appIconBadge = 144.dp,
    appIconImage = 84.dp,
    permissionBadge = 112.dp,
    permissionIcon = 52.dp,
    reasonIcon = 24.dp,
    primaryButtonHeight = 60.dp,
    featureCardMinWidth = 200.dp,
    featureIconBadge = 48.dp,
    featureIcon = 26.dp,
    indicatorDot = 10.dp,
    indicatorDotSelected = 30.dp,
    bottomBarHeight = 72.dp
)

/**
 * Picks the sizing tier for the current window.
 *
 * Height is checked first: a tablet held in landscape still has plenty of width, but a phone in
 * landscape has to shrink its artwork or the call to action drops below the fold.
 */
@Composable
fun rememberWelcomeDimens(): WelcomeDimens {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    val heightDp = configuration.screenHeightDp
    return remember(widthDp, heightDp) {
        when {
            heightDp < COMPACT_HEIGHT_DP -> CompactWelcomeDimens
            widthDp >= EXPANDED_WIDTH_DP -> ExpandedWelcomeDimens
            else -> MediumWelcomeDimens
        }
    }
}
