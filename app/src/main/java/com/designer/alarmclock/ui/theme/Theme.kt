package com.designer.alarmclock.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    secondary = DarkSecondary,
    outline = DarkBorder,
    error = DarkError
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    secondary = LightSecondary,
    outline = LightBorder,
    error = LightError
)

@Composable
fun AlarmClockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalAppColors provides appColors,
        // Make Urbanist the default for every Text(), even ones without an
        // explicit style. Body/secondary text opts into Inter where needed.
        LocalTextStyle provides DefaultTextStyle.copy(color = appColors.textPrimary)
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
