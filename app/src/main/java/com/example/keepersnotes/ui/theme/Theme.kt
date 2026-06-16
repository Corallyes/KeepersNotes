package com.example.keepersnotes.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.keepersnotes.util.ThemePreferences

private val VintageLightColorScheme = lightColorScheme(
    primary = BronzeAccent,
    onPrimary = PaperBackground,
    primaryContainer = AntiqueBrass,
    onPrimaryContainer = InkText,
    secondary = DarkBronze,
    onSecondary = PaperBackground,
    secondaryContainer = PaperSurfaceVariant,
    onSecondaryContainer = InkText,
    tertiary = PlotWine,
    onTertiary = PaperBackground,
    tertiaryContainer = PaperSurfaceVariant,
    onTertiaryContainer = InkText,
    background = PaperBackground,
    onBackground = InkText,
    surface = PaperSurface,
    onSurface = InkText,
    surfaceVariant = PaperSurfaceVariant,
    onSurfaceVariant = SecondaryInk,
    error = StatusDead,
    onError = PaperBackground,
    errorContainer = PaperSurfaceVariant,
    onErrorContainer = InkText,
    outline = DividerLight,
    outlineVariant = PaperSurfaceVariant,
    inverseSurface = InkText,
    inverseOnSurface = PaperBackground,
    inversePrimary = AntiqueBrass,
    surfaceTint = BronzeAccent
)

private val VintageDarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = DarkBackground,
    primaryContainer = BronzeDark,
    onPrimaryContainer = LightInk,
    secondary = WarmGold,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = LightInk,
    tertiary = PlotWine,
    onTertiary = DarkBackground,
    tertiaryContainer = DarkSurfaceVariant,
    onTertiaryContainer = LightInk,
    background = DarkBackground,
    onBackground = LightInk,
    surface = DarkSurface,
    onSurface = LightInk,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = SecondaryLightInk,
    error = Color(0xFFD4887A),   // 暖红色，不刺眼
    onError = DarkBackground,
    errorContainer = DarkSurfaceVariant,
    onErrorContainer = LightInk,
    outline = DividerDark,
    outlineVariant = DarkSurfaceVariant,
    inverseSurface = LightInk,
    inverseOnSurface = DarkBackground,
    inversePrimary = BronzeDark,
    surfaceTint = GoldAccent
)

private val VintageShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp)
)

@Composable
fun KeepersNotesTheme(
    darkTheme: Boolean = ThemePreferences.isDarkTheme(isSystemInDarkTheme()),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) VintageDarkColorScheme else VintageLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VintageTypography,
        shapes = VintageShapes,
        content = content
    )
}
