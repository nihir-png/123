package com.designer.alarmclock.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Semantic, theme-aware color tokens used across the app's custom (non-Material)
 * surfaces. Screens hardcoded light hex values everywhere, which is why dark mode
 * looked broken — they now read these tokens so they flip with the theme while the
 * light palette stays pixel-identical to the original Figma design.
 *
 * Gold accent colors (Gold / GoldLight / GoldenYellowGradient) are intentionally
 * shared between themes — they read well on both light and dark backgrounds.
 */
@Immutable
data class AppColors(
    val isDark: Boolean,
    val background: Color,
    val card: Color,
    val cardGradient: Brush,
    val cardDisabled: Brush,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textSubtle: Color,
    val textFaint: Color,
    val navBar: Color,
    val navActive: Color,
    val navInactive: Color,
    val divider: Color,
    val chipUnselected: Color,
    val fieldBorder: Color,
    val toggleTrackOff: Color,
    val dayInactive: Color
)

// Light palette — exact values carried over from the original screens so the
// light design is unchanged.
val LightAppColors = AppColors(
    isDark = false,
    background = Color(0xFFF7F7F9),
    card = Color(0xFFFFFFFF),
    cardGradient = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFFBFBFD))),
    cardDisabled = Brush.linearGradient(listOf(Color(0xB3FFFFFF), Color(0xB3FFFFFF))),
    textPrimary = Color(0xFF1A1A1A),
    textSecondary = Color(0xFF7E7E7E),
    textMuted = Color(0xFF777782),
    textSubtle = Color(0xFF8E8E98),
    textFaint = Color(0xFF9A9AA4),
    navBar = Color(0xFFFFFFFF),
    navActive = Color(0xFFE6A900),
    navInactive = Color(0xFF898989),
    divider = Color(0xFFF1F1F4),
    chipUnselected = Color(0xFFFAF8F5),
    fieldBorder = Color(0xFFEFEFEF),
    toggleTrackOff = Color(0xFFE7E7EC),
    dayInactive = Color(0xFFC7C7CF)
)

// Dark palette — warm charcoal/brown to match the app's cream-and-gold identity.
val DarkAppColors = AppColors(
    isDark = true,
    background = Color(0xFF1E1D1A),
    card = Color(0xFF2A2824),
    cardGradient = Brush.verticalGradient(listOf(Color(0xFF2D2B26), Color(0xFF2A2824))),
    cardDisabled = Brush.linearGradient(listOf(Color(0xFF242220), Color(0xFF242220))),
    textPrimary = Color(0xFFFAF8F5),
    textSecondary = Color(0xFFB7B3AC),
    textMuted = Color(0xFFAEAAA3),
    textSubtle = Color(0xFF9E9B95),
    textFaint = Color(0xFF85827B),
    navBar = Color(0xFF26241F),
    navActive = Color(0xFFFFD233),
    navInactive = Color(0xFF8E8B85),
    divider = Color(0xFF383530),
    chipUnselected = Color(0xFF3A3833),
    fieldBorder = Color(0xFF44413B),
    toggleTrackOff = Color(0xFF44413B),
    dayInactive = Color(0xFF6B6862)
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
