package com.siju.acexplorer.common.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.siju.acexplorer.common.utils.SdkHelper

private val DarkColorScheme = darkColorScheme(
    // A light tone, unlike the light scheme's primary: the tone-30 purple used there measures
    // 1.75:1 against this near-black background, far below the 4.5:1 needed for text.
    primary = darkPrimary,
    primaryContainer = gray,
    secondary = purpleDark,
    onPrimary = darkOnPrimary,
    onPrimaryContainer = dark_onBackground,
    onSecondary = Color.White,
    background = black,
    onBackground = dark_onBackground,
    surface = black,
    onSurface = textColorDark
)

private val LightColorScheme = lightColorScheme(
    primary = purple,
    primaryContainer = primaryContainerLight,
    secondary = purpleDark,
    onPrimary = Color.White,
    onPrimaryContainer = Color.White,
    onSecondary = Color.White,
    background = light_background,
    onBackground = light_onBackground,
    onSurface = textColorLight
)

@Composable
fun MyApplicationTheme(
    appTheme: Theme = Theme.DARK,
    isDarkMode: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit) {

    val colorScheme : ColorScheme = when (appTheme) {
        Theme.DEVICE -> {
            if (SdkHelper.isAtleastAndroid12) {
                val context = LocalContext.current
                if (isDarkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            else if (isDarkMode) {
                DarkColorScheme
            } else {
                LightColorScheme
            }
        }
        Theme.LIGHT -> {
            LightColorScheme
        }
        Theme.DARK -> {
            DarkColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}