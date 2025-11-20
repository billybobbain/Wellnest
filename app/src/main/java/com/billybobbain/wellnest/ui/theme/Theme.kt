package com.billybobbain.wellnest.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val TealLightColorScheme = lightColorScheme(
    primary = TealPrimary,
    secondary = TealSecondary,
    tertiary = TealTertiary
)

private val TealDarkColorScheme = darkColorScheme(
    primary = TealSecondary,
    secondary = TealPrimary,
    tertiary = TealTertiary
)

private val PurpleLightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    secondary = PurpleSecondary,
    tertiary = PurpleTertiary
)

private val PurpleDarkColorScheme = darkColorScheme(
    primary = PurpleSecondary,
    secondary = PurplePrimary,
    tertiary = PurpleTertiary
)

private val BlueLightColorScheme = lightColorScheme(
    primary = BluePrimary,
    secondary = BlueSecondary,
    tertiary = BlueTertiary
)

private val BlueDarkColorScheme = darkColorScheme(
    primary = BlueSecondary,
    secondary = BluePrimary,
    tertiary = BlueTertiary
)

private val GreenLightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = GreenSecondary,
    tertiary = GreenTertiary
)

private val GreenDarkColorScheme = darkColorScheme(
    primary = GreenSecondary,
    secondary = GreenPrimary,
    tertiary = GreenTertiary
)

private val OrangeLightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    secondary = OrangeSecondary,
    tertiary = OrangeTertiary
)

private val OrangeDarkColorScheme = darkColorScheme(
    primary = OrangeSecondary,
    secondary = OrangePrimary,
    tertiary = OrangeTertiary
)

private val PinkLightColorScheme = lightColorScheme(
    primary = PinkPrimary,
    secondary = PinkSecondary,
    tertiary = PinkTertiary
)

private val PinkDarkColorScheme = darkColorScheme(
    primary = PinkSecondary,
    secondary = PinkPrimary,
    tertiary = PinkTertiary
)

@Composable
fun WellnestTheme(
    selectedTheme: String = "Teal",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (selectedTheme) {
        "Purple" -> if (darkTheme) PurpleDarkColorScheme else PurpleLightColorScheme
        "Blue" -> if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
        "Green" -> if (darkTheme) GreenDarkColorScheme else GreenLightColorScheme
        "Orange" -> if (darkTheme) OrangeDarkColorScheme else OrangeLightColorScheme
        "Pink" -> if (darkTheme) PinkDarkColorScheme else PinkLightColorScheme
        else -> if (darkTheme) TealDarkColorScheme else TealLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
