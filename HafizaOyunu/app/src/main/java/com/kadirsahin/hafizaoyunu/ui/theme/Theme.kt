package com.kadirsahin.hafizaoyunu.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.kadirsahin.hafizaoyunu.data.AppSettings
import com.kadirsahin.hafizaoyunu.data.ThemeMode

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB69CFF),
    onPrimary = Color(0xFF1E1048),
    primaryContainer = Color(0xFF4A2DA6),
    onPrimaryContainer = Color(0xFFEDE7FF),
    secondary = Color(0xFF3FDDF5),
    onSecondary = Color(0xFF00313A),
    secondaryContainer = Color(0xFF0D4D5B),
    onSecondaryContainer = Color(0xFFCFF8FF),
    tertiary = Color(0xFFFF86C3),
    onTertiary = Color(0xFF4A0D2C),
    tertiaryContainer = Color(0xFF7A1F52),
    onTertiaryContainer = Color(0xFFFFD9EA),
    error = Color(0xFFFF6B81),
    onError = Color(0xFF4A0010),
    errorContainer = Color(0xFF5C1525),
    onErrorContainer = Color(0xFFFFD9DE),
    background = Color(0xFF0C0A1D),
    onBackground = Color(0xFFECE9FF),
    surface = Color(0xFF0C0A1D),
    onSurface = Color(0xFFECE9FF),
    surfaceVariant = Color(0xFF2A2548),
    onSurfaceVariant = Color(0xFFB3ADD6),
    surfaceContainerLowest = Color(0xFF08061A),
    surfaceContainerLow = Color(0xFF141130),
    surfaceContainer = Color(0xFF1A1736),
    surfaceContainerHigh = Color(0xFF231F44),
    surfaceContainerHighest = Color(0xFF2D2852),
    outline = Color(0xFF5D5785),
    outlineVariant = Color(0xFF37325A),
    inverseSurface = Color(0xFFECE9FF),
    inverseOnSurface = Color(0xFF1A1736),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF6A3DE0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4DBFF),
    onPrimaryContainer = Color(0xFF230A64),
    secondary = Color(0xFF0889A8),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDF4FB),
    onSecondaryContainer = Color(0xFF003641),
    tertiary = Color(0xFFD02E7F),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD9EA),
    onTertiaryContainer = Color(0xFF4A0D2C),
    error = Color(0xFFD02A47),
    errorContainer = Color(0xFFFFDADF),
    onErrorContainer = Color(0xFF410010),
    background = Color(0xFFF6F3FF),
    onBackground = Color(0xFF1B1730),
    surface = Color(0xFFF6F3FF),
    onSurface = Color(0xFF1B1730),
    surfaceVariant = Color(0xFFE4DEF7),
    onSurfaceVariant = Color(0xFF5A5475),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF1EDFF),
    surfaceContainer = Color(0xFFEBE6FF),
    surfaceContainerHigh = Color(0xFFE4DEFB),
    surfaceContainerHighest = Color(0xFFDCD5F5),
    outline = Color(0xFF8E88AD),
    outlineVariant = Color(0xFFD3CDEB),
)

/** Game specific accents that Material's scheme has no slot for. */
@Immutable
data class GameColors(
    val gold: Color,
    val success: Color,
    val danger: Color,
    val ice: Color,
    val heart: Color,
    val playerOne: Color,
    val playerTwo: Color,
    val isDark: Boolean,
)

private val DarkGameColors = GameColors(
    gold = Color(0xFFFFC93D),
    success = Color(0xFF3EE6A0),
    danger = Color(0xFFFF5C7A),
    ice = Color(0xFF9BE7FF),
    heart = Color(0xFFFF4F7B),
    playerOne = Color(0xFF3FDDF5),
    playerTwo = Color(0xFFFF86C3),
    isDark = true,
)

private val LightGameColors = GameColors(
    gold = Color(0xFFE0A100),
    success = Color(0xFF0F9F6E),
    danger = Color(0xFFD02A47),
    ice = Color(0xFF2BA7D6),
    heart = Color(0xFFE4335F),
    playerOne = Color(0xFF0889A8),
    playerTwo = Color(0xFFD02E7F),
    isDark = false,
)

val LocalGameColors = staticCompositionLocalOf { DarkGameColors }

object GameTheme {
    val colors: GameColors
        @Composable get() = LocalGameColors.current
}

@Composable
fun MemoryGameTheme(settings: AppSettings, content: @Composable () -> Unit) {
    val darkTheme = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val colorScheme = when {
        settings.dynamicColorsEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        SideEffect {
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalGameColors provides if (darkTheme) DarkGameColors else LightGameColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MemoryTypography,
            content = content,
        )
    }
}
