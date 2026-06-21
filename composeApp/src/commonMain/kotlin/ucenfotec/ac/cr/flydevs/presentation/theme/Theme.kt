package ucenfotec.ac.cr.flydevs.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ── Fly App is dark-first: the "dark" scheme IS the brand ────────────────────
private val FlyDarkColorScheme = darkColorScheme(
    primary            = AccentViolet,        // CTA buttons, FAB, active nav
    onPrimary          = TextPrimary,
    primaryContainer   = BgSurface,           // Elevated chips / active headers
    onPrimaryContainer = TextLight,

    secondary          = AccentGold,          // Mensajero role, badges, section titles
    onSecondary        = BgDarkest,
    secondaryContainer = BgCard,
    onSecondaryContainer = TextSecondary,

    tertiary           = AccentMint,          // Tienda role, success states, "Gratis"
    onTertiary         = BgDarkest,

    error              = AccentRed,           // Admin role, errors, disputes, delete
    onError            = TextPrimary,

    background         = BgDarkest,           // #141029 — all screens
    onBackground       = TextPrimary,

    surface            = BgDark,              // Bottom nav, secondary surfaces
    onSurface          = TextPrimary,
    surfaceVariant     = BgCard,              // Cards, inputs
    onSurfaceVariant   = TextSecondary,

    outline            = TextMuted,           // Dividers, inactive nav items
    outlineVariant     = BgSurface,
)

// ── Light scheme (fallback — app is intentionally dark) ──────────────────────
private val FlyLightColorScheme = lightColorScheme(
    primary            = AccentViolet,
    onPrimary          = TextPrimary,
    primaryContainer   = AccentVioletLight,
    onPrimaryContainer = BgDarkest,

    secondary          = AccentGold,
    onSecondary        = BgDarkest,

    tertiary           = AccentMint,
    onTertiary         = BgDarkest,

    error              = AccentRed,
    onError            = TextPrimary,

    background         = BgDarkest,
    onBackground       = TextPrimary,

    surface            = BgDark,
    onSurface          = TextPrimary,
    surfaceVariant     = BgCard,
    onSurfaceVariant   = TextSecondary,

    outline            = TextMuted,
)

@Composable
fun FlyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) FlyDarkColorScheme else FlyLightColorScheme,
        typography  = Typography,
        content     = content,
    )
}
