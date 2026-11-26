package com.oppzippy.openscq30.ui.devicesettings.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.ui.theme.OpenSCQ30Theme
import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.absoluteValue

private val KILOHERTZ_MATH_CONTEXT = MathContext(1)

@Composable
fun ReadOnlyEqualizer(
    modifier: Modifier,
    color: Color,
    bands: List<UShort>,
    values: List<Short>,
    minValue: Short,
    maxValue: Short,
    fractionDigits: Short,
    drawHorizontalGuide: Boolean = true,
    strokeWidthDp: Dp = 2.dp,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val contentDescription = remember(bands, values, fractionDigits, configuration, context) {
        val resources = context.resources
        bands.take(values.size).mapIndexed { index, bandHz ->
            val bandLabel = if (bandHz < 1000.toUShort()) {
                resources.getString(R.string.hz, bandHz.toInt())
            } else {
                resources.getString(
                    R.string.khz,
                    BigDecimal(bandHz.toInt())
                        .scaleByPowerOfTen(-3)
                        .round(KILOHERTZ_MATH_CONTEXT)
                        .toPlainString(),
                )
            }
            val valueLabel = BigDecimal(values[index].toInt()).scaleByPowerOfTen(-fractionDigits)
            "$bandLabel: $valueLabel"
        }.joinToString(separator = ", ")
    }

    val guideColor = remember(color) { color.copy(alpha = 0.3f) }
    val range = remember(minValue, maxValue) { (maxValue - minValue).toFloat() }
    val zeroRatio = remember(minValue, range) {
        if (range != 0f) minValue.toFloat().absoluteValue / range else 0f
    }
    val unscaledPoints = remember(minValue, maxValue, values) {
        equalizerLinePoints(
            minValue = minValue,
            maxValue = maxValue,
            values = values,
        )
    }
    val linePath = remember { Path() }
    val fillPath = remember { Path() }

    Canvas(modifier, contentDescription) {
        val strokeWidth = strokeWidthDp.toPx()
        val padding = Offset(strokeWidth, strokeWidth)
        val sizeWithoutPadding = Offset(
            x = size.width - padding.x * 2f,
            y = size.height - padding.y * 2f,
        )

        if (drawHorizontalGuide) {
            val zeroValueY = size.height - size.height * zeroRatio
            drawLine(
                color = guideColor,
                start = Offset(0f, strokeWidth),
                end = Offset(size.width, strokeWidth),
                strokeWidth = strokeWidth,
            )
            drawLine(
                color = guideColor,
                start = Offset(0f, zeroValueY),
                end = Offset(size.width, zeroValueY),
                strokeWidth = strokeWidth,
            )
            drawLine(
                color = guideColor,
                start = Offset(0f, size.height - strokeWidth),
                end = Offset(size.width, size.height - strokeWidth),
                strokeWidth = strokeWidth,
            )
        }

        if (unscaledPoints.isEmpty()) return@Canvas

        val firstPoint = unscaledPoints.first() * sizeWithoutPadding + padding
        val lastPoint = unscaledPoints.last() * sizeWithoutPadding + padding
        val leftEdgeX = firstPoint.x - strokeWidth
        val rightEdgeX = lastPoint.x + strokeWidth

        linePath.reset()
        linePath.moveTo(firstPoint.x, firstPoint.y)

        fillPath.reset()
        fillPath.moveTo(leftEdgeX, firstPoint.y)
        fillPath.lineTo(firstPoint.x, firstPoint.y)

        for (i in 1 until unscaledPoints.size) {
            val pt = unscaledPoints[i] * sizeWithoutPadding + padding
            linePath.lineTo(pt.x, pt.y)
            fillPath.lineTo(pt.x, pt.y)
        }

        fillPath.lineTo(rightEdgeX, lastPoint.y)
        fillPath.lineTo(rightEdgeX, size.height)
        fillPath.lineTo(leftEdgeX, size.height)
        fillPath.close()

        // shading below the line
        drawPath(
            path = fillPath,
            color = color,
            alpha = 0.3f,
        )
        // line
        drawPath(
            path = linePath,
            color = color,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

private fun equalizerLinePoints(minValue: Short, maxValue: Short, values: List<Short>): List<Offset> {
    val range = (maxValue - minValue).toFloat()
    val denom = (values.size - 1).coerceAtLeast(1).toFloat()
    return values.mapIndexed { index, value ->
        val x = if (values.size > 1) index.toFloat() / denom else 0.5f
        val y = if (range != 0f) (value - minValue).toFloat() / range else 0.5f
        Offset(x, 1f - y)
    }
}

private operator fun Offset.times(other: Offset): Offset = Offset(x * other.x, y * other.y)

@Preview(showBackground = true)
@Composable
private fun PreviewEqualizer() {
    val bands = listOf<UShort>(100u, 200u, 400u, 800u, 1600u, 3200u, 6400u, 12000u)
    OpenSCQ30Theme {
        Surface {
            ReadOnlyEqualizer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                color = MaterialTheme.colorScheme.onSurface,
                bands = bands,
                values = listOf(60, -60, 30, -30, 120, -120, 0, 0),
                minValue = -120,
                maxValue = 134,
                fractionDigits = 1,
            )
        }
    }
}
