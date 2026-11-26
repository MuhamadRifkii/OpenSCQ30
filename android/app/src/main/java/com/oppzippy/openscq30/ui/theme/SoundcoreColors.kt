package com.oppzippy.openscq30.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Theme-aware color accessors backed by MaterialTheme.colorScheme so Light, Dark,
 * Material You Dynamic Color, and Pure Black AMOLED modes all apply consistently.
 */
object SoundcoreColors {
    val Background: Color
        @Composable get() = MaterialTheme.colorScheme.background

    val SurfaceCard: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerLow

    val SurfaceElevated: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainer

    val SurfacePill: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh

    val Divider: Color
        @Composable get() = MaterialTheme.colorScheme.outlineVariant

    val Cyan: Color
        @Composable get() = MaterialTheme.colorScheme.primary

    val CyanBright: Color
        @Composable get() = MaterialTheme.colorScheme.primary

    val OnAccent: Color
        @Composable get() = MaterialTheme.colorScheme.onPrimary

    val Green: Color = Color(0xFF3FD667)

    val BatteryTrack: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerHighest

    val GradientPink: Color
        @Composable get() = MaterialTheme.colorScheme.tertiary

    val GradientCyan: Color
        @Composable get() = MaterialTheme.colorScheme.primary

    val TextPrimary: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface

    val TextSecondary: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

    val TextTertiary: Color
        @Composable get() = MaterialTheme.colorScheme.outline
}
