package com.oppzippy.openscq30.ui.soundcore.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.lib.wrapper.Setting
import com.oppzippy.openscq30.ui.soundcore.SoundcoreSettingIds
import com.oppzippy.openscq30.ui.soundcore.getSetting
import com.oppzippy.openscq30.ui.soundcore.isCharging
import com.oppzippy.openscq30.ui.soundcore.parseBatteryFraction
import com.oppzippy.openscq30.ui.theme.SoundcoreColors

data class ConnectedBatteryInfo(
    val leftPercent: String?,
    val leftFraction: Float,
    val leftCharging: Boolean,
    val rightPercent: String?,
    val rightFraction: Float,
    val rightCharging: Boolean,
    val casePercent: String?,
    val caseFraction: Float,
) {
    val hasAny: Boolean
        get() = leftPercent != null || rightPercent != null || casePercent != null

    companion object {
        fun fromSettings(settings: Map<String, Setting>): ConnectedBatteryInfo {
            val left = settings.getSetting<Setting.InformationSetting>(SoundcoreSettingIds.BATTERY_LEVEL_LEFT)
            val right = settings.getSetting<Setting.InformationSetting>(SoundcoreSettingIds.BATTERY_LEVEL_RIGHT)
            val caseBat = settings.getSetting<Setting.InformationSetting>(SoundcoreSettingIds.CASE_BATTERY_LEVEL)
            return ConnectedBatteryInfo(
                leftPercent = left?.translatedValue,
                leftFraction = parseBatteryFraction(left) ?: 0f,
                leftCharging = isCharging(
                    settings.getSetting<Setting.InformationSetting>(SoundcoreSettingIds.IS_CHARGING_LEFT),
                ),
                rightPercent = right?.translatedValue,
                rightFraction = parseBatteryFraction(right) ?: 0f,
                rightCharging = isCharging(
                    settings.getSetting<Setting.InformationSetting>(SoundcoreSettingIds.IS_CHARGING_RIGHT),
                ),
                casePercent = caseBat?.translatedValue,
                caseFraction = parseBatteryFraction(caseBat) ?: 0f,
            )
        }
    }
}

@Composable
fun SoundcoreBatteryStrip(
    battery: ConnectedBatteryInfo,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val spacing = if (compact) 14.dp else 24.dp
    val barWidth = if (compact) 46.dp else 64.dp
    val fontSize = if (compact) 11.sp else 13.sp
    val iconSize = if (compact) 15.dp else 17.dp

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        battery.leftPercent?.let {
            BatteryBadgeItem(
                side = BatteryBadgeKind.Left,
                percentText = it,
                fraction = battery.leftFraction,
                isCharging = battery.leftCharging,
                barWidth = barWidth,
                fontSize = fontSize,
                iconSize = iconSize,
            )
        }
        battery.rightPercent?.let {
            BatteryBadgeItem(
                side = BatteryBadgeKind.Right,
                percentText = it,
                fraction = battery.rightFraction,
                isCharging = battery.rightCharging,
                barWidth = barWidth,
                fontSize = fontSize,
                iconSize = iconSize,
            )
        }
        battery.casePercent?.let {
            BatteryBadgeItem(
                side = BatteryBadgeKind.Case,
                percentText = it,
                fraction = battery.caseFraction,
                isCharging = false,
                barWidth = barWidth,
                fontSize = fontSize,
                iconSize = iconSize,
            )
        }
    }
}

private enum class BatteryBadgeKind {
    Left,
    Right,
    Case,
}

private val BatteryBarShape = RoundedCornerShape(2.dp)

@Composable
private fun BatteryBadgeItem(
    side: BatteryBadgeKind,
    percentText: String,
    fraction: Float,
    isCharging: Boolean,
    barWidth: Dp,
    fontSize: TextUnit,
    iconSize: Dp,
) {
    val labelColor = SoundcoreColors.TextSecondary
    val trackColor = SoundcoreColors.BatteryTrack
    val fillColor = SoundcoreColors.Green
    val trackModifier = remember(barWidth, trackColor) {
        Modifier
            .width(barWidth)
            .height(3.dp)
            .clip(BatteryBarShape)
            .background(trackColor)
    }
    val fillBaseModifier = remember(fillColor) {
        Modifier
            .height(3.dp)
            .clip(BatteryBarShape)
            .background(fillColor)
    }
    val displayText = remember(percentText, isCharging) {
        if (isCharging) "$percentText ⚡" else percentText
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            when (side) {
                BatteryBadgeKind.Left -> SideBadgeIcon(isLeft = true, color = labelColor, size = iconSize)
                BatteryBadgeKind.Right -> SideBadgeIcon(isLeft = false, color = labelColor, size = iconSize)
                BatteryBadgeKind.Case -> Icon(
                    painter = painterResource(R.drawable.earbud_case_24px),
                    contentDescription = null,
                    tint = labelColor,
                    modifier = Modifier.size(iconSize),
                )
            }
            Text(
                text = displayText,
                color = labelColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
            )
        }
        Spacer(Modifier.height(4.dp))
        Box(modifier = trackModifier) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .then(fillBaseModifier),
            )
        }
    }
}

/**
 * Vector-drawn circular L / R icon so it stays geometrically centered regardless of font padding or scaling.
 */
@Composable
private fun SideBadgeIcon(
    isLeft: Boolean,
    color: Color,
    size: Dp,
) {
    val letterPath = remember { Path() }
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = w * 0.075f
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

        drawCircle(
            color = color,
            radius = w * 0.44f,
            style = stroke,
        )

        letterPath.reset()
        if (isLeft) {
            letterPath.moveTo(w * 0.41f, h * 0.30f)
            letterPath.lineTo(w * 0.41f, h * 0.69f)
            letterPath.lineTo(w * 0.63f, h * 0.69f)
            drawPath(path = letterPath, color = color, style = stroke)
        } else {
            letterPath.moveTo(w * 0.39f, h * 0.69f)
            letterPath.lineTo(w * 0.39f, h * 0.31f)
            letterPath.lineTo(w * 0.53f, h * 0.31f)
            letterPath.arcTo(
                rect = Rect(
                    left = w * 0.43f,
                    top = h * 0.31f,
                    right = w * 0.63f,
                    bottom = h * 0.51f,
                ),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false,
            )
            letterPath.lineTo(w * 0.39f, h * 0.51f)
            drawPath(path = letterPath, color = color, style = stroke)
            drawLine(
                color = color,
                start = Offset(w * 0.51f, h * 0.51f),
                end = Offset(w * 0.63f, h * 0.69f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}
