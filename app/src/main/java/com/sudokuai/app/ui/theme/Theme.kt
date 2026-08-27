package com.sudokuai.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.sudokuai.app.data.datastore.AppTheme

// Small local aliases so the color-scheme tables below read without repeating
// androidx.compose.ui.graphics.Color. Must be declared before LightColors/DarkColors since
// Kotlin top-level properties initialize in declaration order.
private val Color_White = androidx.compose.ui.graphics.Color.White
private val Color_Black = androidx.compose.ui.graphics.Color.Black

private val LightColors = lightColorScheme(
    primary = Blue40,
    onPrimary = Color_White,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue10,
    secondary = Teal40,
    onSecondary = Color_White,
    secondaryContainer = Teal90,
    onSecondaryContainer = Teal10,
    tertiary = Amber40,
    onTertiary = Color_White,
    tertiaryContainer = Amber90,
    error = Error40,
    onError = Color_White,
    errorContainer = Error90,
    onErrorContainer = Error10,
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = NeutralVariant90,
    onSurfaceVariant = NeutralVariant30,
    outline = NeutralVariant50,
)

private val DarkColors = darkColorScheme(
    primary = Blue80,
    onPrimary = Blue20,
    primaryContainer = Blue30,
    onPrimaryContainer = Blue90,
    secondary = Teal80,
    onSecondary = Teal20,
    secondaryContainer = Teal30,
    onSecondaryContainer = Teal90,
    tertiary = Amber80,
    onTertiary = Color_Black,
    tertiaryContainer = Amber40,
    error = Error80,
    onError = Error10,
    errorContainer = Error40,
    onErrorContainer = Error90,
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = NeutralVariant30,
    onSurfaceVariant = NeutralVariant80,
    outline = NeutralVariant50,
)

/**
 * @param appTheme user preference from [com.sudokuai.app.data.datastore.SettingsDataStore]
 *   (Hell/Dunkel/System); System defers to [isSystemInDarkTheme].
 * @param dynamicColor whether to prefer Android 12+ dynamic (wallpaper-derived) color over the
 *   custom palette above. Defaults to true but the app never *depends* on it being available —
 *   [LightColors]/[DarkColors] are always the fallback on API < 31 or when disabled.
 */
@Composable
fun SudokuAiTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = when (appTheme) {
        AppTheme.HELL -> false
        AppTheme.DUNKEL -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        useDarkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
