package com.huamanager.modem.router.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Dynamic application color container supporting dark, light, AMOLED, and color accent themes
 */
data class AppColors(
    val isLight: Boolean = false,
    val background: Color,
    val surface: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val accentGradientStart: Color,
    val accentGradientEnd: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val onAccent: Color = if (isLight) Color.White else Color(0xFF0F1115)
)

val LocalAppColors = staticCompositionLocalOf {
    DarkCyanColors
}

// Dynamic properties delegating to the active LocalAppColors
val DarkBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.background

val SurfaceDark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.surface

val CardBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.cardBackground

val CardBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.cardBorder

val AccentPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.accentPrimary

val AccentSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.accentSecondary

val AccentGradientStart: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.accentGradientStart

val AccentGradientEnd: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.accentGradientEnd

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.textPrimary

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.textSecondary

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.textMuted

val OnAccentColor: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.onAccent

// Universal 3GPP Signal Quality Threshold Colors
val SignalExcellent = Color(0xFF10B981) // Green
val SignalGood = Color(0xFF06B6D4)      // Cyan
val SignalFair = Color(0xFFF59E0B)      // Amber
val SignalPoor = Color(0xFFEF4444)      // Red
val SignalInactive = Color(0xFF64748B)  // Slate Muted

// =========================================================================
// Pre-defined Theme Color Palettes
// =========================================================================

// 1. Dark Cyan (Default Modern Dark Slate)
val DarkCyanColors = AppColors(
    isLight = false,
    background = Color(0xFF0F1115),
    surface = Color(0xFF181B21),
    cardBackground = Color(0xFF21252D),
    cardBorder = Color(0xFF2C323D),
    accentPrimary = Color(0xFF00B4D8),
    accentSecondary = Color(0xFF0077B6),
    accentGradientStart = Color(0xFF00E5FF),
    accentGradientEnd = Color(0xFF0072FF),
    textPrimary = Color(0xFFF1F5F9),
    textSecondary = Color(0xFF94A3B8),
    textMuted = Color(0xFF64748B),
    onAccent = Color(0xFF0F1115)
)

// 2. Light Clean (Mode Terang Elegan)
val LightCleanColors = AppColors(
    isLight = true,
    background = Color(0xFFF1F5F9),
    surface = Color(0xFFFFFFFF),
    cardBackground = Color(0xFFFFFFFF),
    cardBorder = Color(0xFFE2E8F0),
    accentPrimary = Color(0xFF0284C7),
    accentSecondary = Color(0xFF0369A1),
    accentGradientStart = Color(0xFF38BDF8),
    accentGradientEnd = Color(0xFF0284C7),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF94A3B8),
    onAccent = Color.White
)

// 3. AMOLED Black (Hitam Pekat 100% untuk OLED)
val AmoledBlackColors = AppColors(
    isLight = false,
    background = Color(0xFF000000),
    surface = Color(0xFF0A0A0A),
    cardBackground = Color(0xFF111111),
    cardBorder = Color(0xFF242424),
    accentPrimary = Color(0xFF00E5FF),
    accentSecondary = Color(0xFF00B4D8),
    accentGradientStart = Color(0xFF00E5FF),
    accentGradientEnd = Color(0xFF0099FF),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFA1A1AA),
    textMuted = Color(0xFF71717A),
    onAccent = Color.Black
)

// 4. Crimson Red (Merah Crimson Modern)
val CrimsonRedColors = AppColors(
    isLight = false,
    background = Color(0xFF140D0F),
    surface = Color(0xFF1E1317),
    cardBackground = Color(0xFF26181D),
    cardBorder = Color(0xFF3D242C),
    accentPrimary = Color(0xFFE11D48),
    accentSecondary = Color(0xFFBE123C),
    accentGradientStart = Color(0xFFFB7185),
    accentGradientEnd = Color(0xFFE11D48),
    textPrimary = Color(0xFFFFF1F2),
    textSecondary = Color(0xFFFDA4AF),
    textMuted = Color(0xFF9F1239),
    onAccent = Color.White
)

// 5. Emerald Green (Hijau Taktis Futuristik)
val EmeraldGreenColors = AppColors(
    isLight = false,
    background = Color(0xFF0C1410),
    surface = Color(0xFF121F18),
    cardBackground = Color(0xFF182B21),
    cardBorder = Color(0xFF244434),
    accentPrimary = Color(0xFF10B981),
    accentSecondary = Color(0xFF059669),
    accentGradientStart = Color(0xFF34D399),
    accentGradientEnd = Color(0xFF059669),
    textPrimary = Color(0xFFECFDF5),
    textSecondary = Color(0xFF6EE7B7),
    textMuted = Color(0xFF047857),
    onAccent = Color(0xFF0C1410)
)

// 6. Royal Purple (Ungu Neon Elegan)
val RoyalPurpleColors = AppColors(
    isLight = false,
    background = Color(0xFF100D1C),
    surface = Color(0xFF181329),
    cardBackground = Color(0xFF211B38),
    cardBorder = Color(0xFF352B57),
    accentPrimary = Color(0xFF8B5CF6),
    accentSecondary = Color(0xFF7C3AED),
    accentGradientStart = Color(0xFFA78BFA),
    accentGradientEnd = Color(0xFF7C3AED),
    textPrimary = Color(0xFFF5F3FF),
    textSecondary = Color(0xFFC4B5FD),
    textMuted = Color(0xFF6D28D9),
    onAccent = Color.White
)

// 7. Solar Amber (Oranye Keemasan Hangat)
val SolarAmberColors = AppColors(
    isLight = false,
    background = Color(0xFF14120C),
    surface = Color(0xFF1F1B12),
    cardBackground = Color(0xFF2B2519),
    cardBorder = Color(0xFF453B28),
    accentPrimary = Color(0xFFF59E0B),
    accentSecondary = Color(0xFFD97706),
    accentGradientStart = Color(0xFFFBBF24),
    accentGradientEnd = Color(0xFFD97706),
    textPrimary = Color(0xFFFFFBEB),
    textSecondary = Color(0xFFFDE68A),
    textMuted = Color(0xFFB45309),
    onAccent = Color(0xFF14120C)
)
