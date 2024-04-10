package com.langdroid.sample.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = TextBlackColor,
    secondary = TextBlackColor,
    tertiary = TextBlackColor,


    background = LightWhiteColor,
    surface = TextBlackColor,
    primaryContainer = ContainerColor,
    onPrimary = LightWhiteColor,
    onSecondary = LightWhiteColor,
    onTertiary = LightWhiteColor,
    onBackground = TextBlackColor,
    onSurface = TextBlackColor,
)

@Composable
fun LangdroidTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
