package com.example.composeapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Inter font family ─────────────────────────────────────────────────────────
// Add the Inter .ttf files to res/font/ and reference them here.
// Example (adjust resource IDs to match your actual file names):
//
// val Inter = FontFamily(
//     Font(R.font.inter_regular,    FontWeight.Normal),
//     Font(R.font.inter_medium,     FontWeight.Medium),
//     Font(R.font.inter_bold,       FontWeight.Bold),
//     Font(R.font.inter_extrabold,  FontWeight.ExtraBold),
// )
//
// Then replace every `FontFamily.Default` below with `Inter`.
//
// Until you add the font files, FontFamily.Default is used as a safe fallback.

private val Inter = FontFamily.Default   // ← swap for the real Inter once files are in res/font/

// ── Type scale (mapped from the Figma design tokens) ─────────────────────────
val Typography = Typography(

    // Screen / dialog titles — Bold 19 sp (header pattern from every view)
    titleLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize   = 19.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),

    // Card section headings — ExtraBold 16 sp
    titleMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.ExtraBold,
        fontSize   = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),

    // Section title labels — UPPERCASE Bold ~12 sp (gold #FFC23C in the composable)
    titleSmall = TextStyle(
        fontFamily    = Inter,
        fontWeight    = FontWeight.Bold,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 1.2.sp,   // simulates the uppercase tracking
    ),

    // Primary body copy
    bodyLarge = TextStyle(
        fontFamily    = Inter,
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.5.sp,
    ),

    // Secondary body / card content
    bodyMedium = TextStyle(
        fontFamily    = Inter,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.25.sp,
    ),

    // Small descriptive text — Medium 13 sp (used in inputs)
    bodySmall = TextStyle(
        fontFamily    = Inter,
        fontWeight    = FontWeight.Medium,
        fontSize      = 13.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.25.sp,
    ),

    // CTA button label — Bold 15 sp
    labelLarge = TextStyle(
        fontFamily    = Inter,
        fontWeight    = FontWeight.Bold,
        fontSize      = 15.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.sp,
    ),

    // Field labels above inputs — Medium 11 sp (#A8A2CC)
    labelMedium = TextStyle(
        fontFamily    = Inter,
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp,
    ),

    // Bottom-nav labels, placeholder text — Regular 11 sp
    labelSmall = TextStyle(
        fontFamily    = Inter,
        fontWeight    = FontWeight.Normal,
        fontSize      = 11.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.5.sp,
    ),
)
