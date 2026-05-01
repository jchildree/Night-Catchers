package com.nightcatchers.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nightcatchers.core.ui.R

// Section 09 typography spec:
//   Fredoka One  — display (monster names, CTAs)       weight 400 only
//   Nunito       — body copy, UI labels                weights 400, 600, 700
//   Space Mono   — stat readouts, PKE meter values     weights 400, 700
//
// Fonts must be added to res/font/. Using FontFamily.Default as fallback
// until assets are added; replace Font() references when font files are in place.

val FredokaOne = FontFamily(
    Font(R.font.fredoka_one_regular, FontWeight.Normal),
)

val Nunito = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
)

val SpaceMono = FontFamily(
    Font(R.font.space_mono_regular, FontWeight.Normal),
    Font(R.font.space_mono_bold, FontWeight.Bold),
)

val NightCatchersTypography = Typography(
    // Monster names, main CTA labels, capture success banners
    displayLarge = TextStyle(
        fontFamily = FredokaOne,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        color = OnSurface,
    ),
    displayMedium = TextStyle(
        fontFamily = FredokaOne,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        color = OnSurface,
    ),
    headlineLarge = TextStyle(
        fontFamily = FredokaOne,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        color = OnSurface,
    ),
    headlineMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        color = OnSurface,
    ),
    titleLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        color = OnSurface,
    ),
    // Body copy — min 16sp per WCAG
    bodyLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = OnSurface,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = OnSurface,
    ),
    // Stat readouts, PKE meter values, timers
    labelLarge = TextStyle(
        fontFamily = SpaceMono,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = OnSurface,
    ),
    labelSmall = TextStyle(
        fontFamily = SpaceMono,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        color = OnSurfaceMuted,
    ),
)
