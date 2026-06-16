package com.designer.alarmclock.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Light Palette Colors (Cream, Gold, White, Charcoal)
val LightBackground = Color(0xFFF7F7F9)     // Light grey background matching Figma
// Note: kept a hair off pure-white on purpose. Material3 only applies its
// gold-tinted tonal-elevation overlay when a Surface/Card's color *equals*
// colorScheme.surface. Cards in the app pass Color.White explicitly; keeping
// surface at 0xFFFFFFFE means those white cards render pure white (no tint).
val LightSurface = Color(0xFFFFFFFE)        // ~Pure White cards (see note above)
val LightPrimary = Color(0xFFFFB800)        // Warm Golden Accent
val LightOnBackground = Color(0xFF1E1E1E)   // Dark Charcoal text
val LightOnSurface = Color(0xFF1E1E1E)      // Dark Charcoal text on cards
val LightSecondary = Color(0xFF7E7E7E)      // Medium Grey secondary text/labels
val LightBorder = Color(0xFFEFEFEF)         // Subtle borders
val LightError = Color(0xFFEF4444)          // Soft red

// Warm golden/yellow accent gradient colors
val AccentYellow = Color(0xFFFFD233)
val AccentYellowDark = Color(0xFFFFB800)

val GoldenYellowGradient = Brush.linearGradient(
    colors = listOf(AccentYellow, AccentYellowDark)
)

// Ringing screen soft pink-to-cream gradient
val RingingGradientStart = Color(0xFFFFECEF) // Soft pink
val RingingGradientEnd = Color(0xFFFFFDF9)   // Cream

// Dark Palette Colors (Styled to match the new warm cream identity in dark mode)
val DarkBackground = Color(0xFF1E1D1A)      // Warm Dark Brown/Charcoal
val DarkSurface = Color(0xFF2A2824)         // Slightly lighter brown/charcoal card
val DarkPrimary = Color(0xFFFFD233)         // Bright gold
val DarkOnBackground = Color(0xFFFAF8F5)    // Cream text
val DarkOnSurface = Color(0xFFFAF8F5)       // Cream text
val DarkSecondary = Color(0xFF9E9B95)       // Warm slate grey
val DarkBorder = Color(0xFF383530)          // Warm dark border
val DarkError = Color(0xFFFF5252)           // Crimson

// Shared Colors
val ActiveGreen = Color(0xFFFFB800)         // Matches yellow/gold active switch
val IndicatorBackground = Color(0x1FFFB800)  // Translucent primary background for selected items

// ─────────────────────────────────────────────────────────────────────────────
// Exact Figma design tokens (hex values read straight from the Figma frames).
// Use these instead of hardcoding colors in screens.
// ─────────────────────────────────────────────────────────────────────────────
val FigmaBackground = Color(0xFFF7F7F9)     // page background (#f7f7f9)
val TextPrimary = Color(0xFF1A1A1A)         // headings / time (#1a1a1a)
val TextMuted = Color(0xFF777782)           // card label (#777782)
val TextSubtle = Color(0xFF8E8E98)          // empty-state subtitle (#8e8e98)
val TextFaint = Color(0xFF9A9AA4)           // AM/PM on cards (#9a9aa4)
val Gold = Color(0xFFFFB800)                // primary gold (#ffb800)
val GoldLight = Color(0xFFFFD233)           // gradient start (#ffd233)
val DayActive = Gold                        // active day letter (#ffb800)
val DayInactive = Color(0xFFC7C7CF)         // inactive day letter (#c7c7cf)
val DotInactive = Color(0xFFE1E1E7)         // inactive page dot (#e1e1e7)
val ToggleTrackOff = Color(0xFFE7E7EC)      // off toggle track (#e7e7ec)
val NavActive = Color(0xFFE6A900)           // bottom-nav active (#e6a900)
val NavInactive = Color(0xFF898989)         // bottom-nav inactive (#898989)
val NavBarBackground = Color(0xD9FFFFFF)    // 85% white bottom-nav bar

// Near-vertical gold gradient (~172°) used by buttons, active dots, toggles.
val GoldButtonGradient = Brush.verticalGradient(
    colors = listOf(GoldLight, Gold)
)

// Subtle top→bottom white card gradient (Figma AlarmCard: #ffffff → #fbfbfd).
val CardSurfaceGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFFFFF), Color(0xFFFBFBFD))
)

