package com.huamanager.modem.router.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

enum class AppThemeMode(
    val title: String,
    val description: String
) {
    DARK_CYAN(
        title = "Dark Cyan",
        description = "Tema gelap modern dengan aksen cyan neon (Default)"
    ),
    LIGHT_CLEAN(
        title = "Light Clean",
        description = "Tema terang bersih, nyaman di bawah sinar matahari"
    ),
    AMOLED_BLACK(
        title = "AMOLED Black",
        description = "Hitam pekat 100% untuk layar OLED & hemat baterai"
    ),
    HUAWEI_RED(
        title = "Huawei Red",
        description = "Nuansa merah crimson khas perangkat modem Huawei"
    ),
    EMERALD_GREEN(
        title = "Emerald Matrix",
        description = "Gaya taktis hijau emerald bernuansa futuristik"
    ),
    ROYAL_PURPLE(
        title = "Royal Purple",
        description = "Aksen ungu neon elegan dengan latar violet gelap"
    ),
    SOLAR_AMBER(
        title = "Solar Amber",
        description = "Aksen oranye keemasan dengan kontras lembut"
    );

    fun toAppColors(): AppColors {
        return when (this) {
            DARK_CYAN -> DarkCyanColors
            LIGHT_CLEAN -> LightCleanColors
            AMOLED_BLACK -> AmoledBlackColors
            HUAWEI_RED -> HuaweiRedColors
            EMERALD_GREEN -> EmeraldGreenColors
            ROYAL_PURPLE -> RoyalPurpleColors
            SOLAR_AMBER -> SolarAmberColors
        }
    }

    fun toColorScheme(): ColorScheme {
        val colors = toAppColors()
        return if (colors.isLight) {
            lightColorScheme(
                primary = colors.accentPrimary,
                secondary = colors.accentSecondary,
                tertiary = SignalExcellent,
                background = colors.background,
                surface = colors.surface,
                surfaceVariant = colors.cardBackground,
                onPrimary = colors.onAccent,
                onSecondary = colors.textPrimary,
                onTertiary = colors.background,
                onBackground = colors.textPrimary,
                onSurface = colors.textPrimary,
                onSurfaceVariant = colors.textSecondary
            )
        } else {
            darkColorScheme(
                primary = colors.accentPrimary,
                secondary = colors.accentSecondary,
                tertiary = SignalExcellent,
                background = colors.background,
                surface = colors.surface,
                surfaceVariant = colors.cardBackground,
                onPrimary = colors.onAccent,
                onSecondary = colors.textPrimary,
                onTertiary = colors.background,
                onBackground = colors.textPrimary,
                onSurface = colors.textPrimary,
                onSurfaceVariant = colors.textSecondary
            )
        }
    }
}

@Composable
fun HuaweiManagerTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK_CYAN,
    content: @Composable () -> Unit
) {
    val appColors = themeMode.toAppColors()
    CompositionLocalProvider(
        LocalAppColors provides appColors
    ) {
        MaterialTheme(
            colorScheme = themeMode.toColorScheme(),
            typography = Typography,
            content = content
        )
    }
}
