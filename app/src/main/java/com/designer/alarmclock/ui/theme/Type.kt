@file:OptIn(ExperimentalTextApi::class)
package com.designer.alarmclock.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.designer.alarmclock.R

// ─────────────────────────────────────────────────────────────────────────────
// Figma onboarding/app typefaces.
// We ship ONE variable font file per family (res/font/*_variable.ttf) and pull
// each weight out of it with FontVariation. This gives us the exact weights the
// Figma uses (Urbanist Regular/Medium/SemiBold/Bold/ExtraBold and Inter
// Regular/Bold) from a single file each. Variable fonts are supported on
// Android API 26+, which matches the app's minSdk.
// ─────────────────────────────────────────────────────────────────────────────

private fun urbanist(weight: FontWeight, axis: Int) = Font(
    R.font.urbanist_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(axis))
)

private fun inter(weight: FontWeight, axis: Int) = Font(
    R.font.inter_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(axis))
)

// Urbanist — used for all headings, titles, time displays, buttons and labels.
val Urbanist = FontFamily(
    urbanist(FontWeight.Normal, 400),
    urbanist(FontWeight.Medium, 500),
    urbanist(FontWeight.SemiBold, 600),
    urbanist(FontWeight.Bold, 700),
    urbanist(FontWeight.ExtraBold, 800)
)

// Inter — used for subtitles / secondary text and the small day-letter rows.
val Inter = FontFamily(
    inter(FontWeight.Normal, 400),
    inter(FontWeight.Medium, 500),
    inter(FontWeight.SemiBold, 600),
    inter(FontWeight.Bold, 700)
)

// App-wide default text style. Provided via LocalTextStyle in the theme so that
// even plain Text() calls (without an explicit style) render in Urbanist.
val DefaultTextStyle = TextStyle(
    fontFamily = Urbanist,
    fontWeight = FontWeight.Normal,
    color = LightOnBackground
)

// Material typography scale, rebuilt on Urbanist (display/headline/title/label)
// and Inter (body). Screens that use MaterialTheme.typography.* inherit these
// families automatically, even when they .copy(fontWeight = ...).
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = Urbanist, fontWeight = FontWeight.ExtraBold,
        fontSize = 72.sp, lineHeight = 80.sp, letterSpacing = (-1.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = Urbanist, fontWeight = FontWeight.ExtraBold,
        fontSize = 48.sp, lineHeight = 56.sp, letterSpacing = (-1).sp
    ),
    displaySmall = TextStyle(
        fontFamily = Urbanist, fontWeight = FontWeight.Bold,
        fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Urbanist, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Urbanist, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Urbanist, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Urbanist, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Urbanist, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Urbanist, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Urbanist, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Urbanist, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Urbanist, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    )
)
