package com.nightcatchers.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Section 09 typography spec:
//   Fredoka One  — display (monster names, CTAs)       weight 400 only
//   Nunito       — body copy, UI labels                weights 400, 600, 700
//   Space Mono   — stat readouts, PKE meter values     weights 400, 700
//
// TODO: add fredoka_one_regular.ttf, nunito_{regular,semibold,bold}.ttf,
//       space_mono_{regular,bold}.ttf to core/ui/src/main/res/font/ and
//       replace these FontFamily.Default fallbacks with Font(R.font.*) calls.

val FredokaOne: FontFamily = FontFamily.Default
val Nunito: FontFamily = FontFamily.Default
val SpaceMono: FontFamily = FontFamily.Monospace

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
