package com.siju.acexplorer.common.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColors(
    primary = primaryPurple,
    secondary = primaryPurpleDark,
    onPrimary = accent,
    onSecondary = Color.White
)

private val LightColorScheme = lightColors(
    primary = primaryPurple,
    secondary = primaryPurpleDark,
    onPrimary = accent,
    onSecondary = Color.White

)

@Composable
fun MyApplicationTheme(
    appTheme: Theme = Theme.DARK,
    isDarkMode: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit) {

    val colorScheme = when (appTheme) {
        Theme.DEVICE -> {
            if (isDarkMode) {
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
        colors = colorScheme,
        typography = DarkTypography,
        content = content
    )
}