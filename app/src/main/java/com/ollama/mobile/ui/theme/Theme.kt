package com.ollama.mobile.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.ollama.mobile.data.config.AppConfig

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun OllamaMobileTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    
    val themeMode by AppConfig.themeModeFlow.collectAsState()
    val fontSize by AppConfig.fontSizeFlow.collectAsState()
    
    val darkTheme = when (themeMode) {
        AppConfig.THEME_DARK -> true
        AppConfig.THEME_LIGHT -> false
        else -> systemDark
    }
    
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            try {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } catch (e: Exception) {
                if (darkTheme) DarkColorScheme else LightColorScheme
            }
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val fontScale = when (fontSize) {
        AppConfig.FONT_SMALL -> 0.85f
        AppConfig.FONT_LARGE -> 1.15f
        else -> 1f
    }

    val dynamicTypography = Typography(
        bodyLarge = TextStyle(
            fontSize = (16 * fontScale).sp,
            lineHeight = (24 * fontScale).sp
        ),
        titleLarge = TextStyle(
            fontSize = (22 * fontScale).sp,
            lineHeight = (28 * fontScale).sp
        ),
        titleMedium = TextStyle(
            fontSize = (18 * fontScale).sp,
            lineHeight = (24 * fontScale).sp
        ),
        labelSmall = TextStyle(
            fontSize = (11 * fontScale).sp,
            lineHeight = (16 * fontScale).sp
        )
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            try {
                val window = (view.context as? Activity)?.window
                window?.let {
                    it.statusBarColor = colorScheme.primary.toArgb()
                    WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !darkTheme
                }
            } catch (e: Exception) {
                // Ignore status bar errors
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = dynamicTypography,
        content = content
    )
}