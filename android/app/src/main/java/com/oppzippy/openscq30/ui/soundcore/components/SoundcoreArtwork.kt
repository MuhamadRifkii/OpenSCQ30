package com.oppzippy.openscq30.ui.soundcore.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

private val EarbudBodyColors = listOf(
    Color(0xFF565B63),
    Color(0xFF25282D),
    Color(0xFF141619),
)
private val EarbudHighlightR60i = Color(0xFF8C94A0)
private val EarbudHighlightDefault = Color(0xFF7B828D)
private val EarbudTipColor = Color(0xFF3A3E45)
private val EarbudTipInnerColor = Color(0xFF1D1F23)

/**
 * Sleek dark-metallic Soundcore stem earbuds artwork inspired by R50i NC / R60i NC.
 */
@Composable
fun SoundcoreEarbudsArtwork(modifier: Modifier = Modifier, model: String = "") {
    val isR60i = model.contains("D1202", ignoreCase = true)
    val highlightColor = if (isR60i) EarbudHighlightR60i else EarbudHighlightDefault
    val bodyBrush = remember(EarbudBodyColors) {
        Brush.linearGradient(colors = EarbudBodyColors)
    }
    val stemHighlightColor = remember(highlightColor) {
        highlightColor.copy(alpha = 0.55f)
    }
    val ringAccentColor = remember(highlightColor) {
        highlightColor.copy(alpha = 0.65f)
    }
    val tipColor = EarbudTipColor
    val tipInnerColor = EarbudTipInnerColor

    Canvas(modifier = modifier.size(140.dp)) {
        val w = size.width
        val h = size.height
        val ringStroke = Stroke(width = w * 0.012f)

        // Left earbud
        rotate(degrees = -14f, pivot = Offset(w * 0.34f, h * 0.45f)) {
            // Ear tip
            drawOval(
                color = tipInnerColor,
                topLeft = Offset(w * 0.13f, h * 0.24f),
                size = Size(w * 0.15f, h * 0.19f),
            )
            drawOval(
                color = tipColor,
                topLeft = Offset(w * 0.145f, h * 0.255f),
                size = Size(w * 0.12f, h * 0.16f),
            )
            // Earbud head
            drawOval(
                brush = bodyBrush,
                topLeft = Offset(w * 0.20f, h * 0.18f),
                size = Size(w * 0.24f, h * 0.27f),
            )
            // Stem
            drawRoundRect(
                brush = bodyBrush,
                topLeft = Offset(w * 0.31f, h * 0.33f),
                size = Size(w * 0.105f, h * 0.44f),
                cornerRadius = CornerRadius(w * 0.055f, w * 0.055f),
            )
            // Gloss highlight along stem
            drawLine(
                color = stemHighlightColor,
                start = Offset(w * 0.345f, h * 0.39f),
                end = Offset(w * 0.345f, h * 0.70f),
                strokeWidth = w * 0.015f,
                cap = StrokeCap.Round,
            )
            // Subtle Soundcore 'd' accent ring
            drawCircle(
                color = ringAccentColor,
                radius = w * 0.025f,
                center = Offset(w * 0.36f, h * 0.32f),
                style = ringStroke,
            )
        }

        // Right earbud (slightly lower and overlapping subtly)
        rotate(degrees = 14f, pivot = Offset(w * 0.66f, h * 0.50f)) {
            // Ear tip
            drawOval(
                color = tipInnerColor,
                topLeft = Offset(w * 0.72f, h * 0.29f),
                size = Size(w * 0.15f, h * 0.19f),
            )
            drawOval(
                color = tipColor,
                topLeft = Offset(w * 0.735f, h * 0.305f),
                size = Size(w * 0.12f, h * 0.16f),
            )
            // Earbud head
            drawOval(
                brush = bodyBrush,
                topLeft = Offset(w * 0.56f, h * 0.23f),
                size = Size(w * 0.24f, h * 0.27f),
            )
            // Stem
            drawRoundRect(
                brush = bodyBrush,
                topLeft = Offset(w * 0.585f, h * 0.38f),
                size = Size(w * 0.105f, h * 0.44f),
                cornerRadius = CornerRadius(w * 0.055f, w * 0.055f),
            )
            // Gloss highlight along stem
            drawLine(
                color = stemHighlightColor,
                start = Offset(w * 0.655f, h * 0.44f),
                end = Offset(w * 0.655f, h * 0.75f),
                strokeWidth = w * 0.015f,
                cap = StrokeCap.Round,
            )
            // Subtle Soundcore accent ring
            drawCircle(
                color = ringAccentColor,
                radius = w * 0.025f,
                center = Offset(w * 0.64f, h * 0.37f),
                style = ringStroke,
            )
        }
    }
}
