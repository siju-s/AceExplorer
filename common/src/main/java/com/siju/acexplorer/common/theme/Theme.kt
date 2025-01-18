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
    primary = primaryPurple,
    primaryContainer = primaryContainerDark,
    secondary = primaryPurpleDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    background = dark_background,
    onBackground = dark_onBackground,
    onSurface = textColorDark
)

private val LightColorScheme = lightColorScheme(
    primary = primaryPurple,
    primaryContainer = primaryContainerLight,
    secondary = primaryPurpleDark,
    onPrimary = Color.White,
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