package com.oppzippy.openscq30.ui.soundcore.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.lib.wrapper.Setting
import com.oppzippy.openscq30.lib.wrapper.Value
import com.oppzippy.openscq30.lib.wrapper.toValue
import com.oppzippy.openscq30.ui.soundcore.SoundcoreSettingIds
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreCard
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreDivider
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreSwitch
import com.oppzippy.openscq30.ui.soundcore.components.SoundcoreTopBar
import com.oppzippy.openscq30.ui.soundcore.getSetting
import com.oppzippy.openscq30.ui.theme.SoundcoreColors
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.delay

private val RefreshOptionShape = RoundedCornerShape(12.dp)
private val DbStepShape = RoundedCornerShape(10.dp)
private val LiquidGaugeShape = RoundedCornerShape(10.dp)

private enum class VolumeStatusLevel {
    Normal,
    Acceptable,
    High,
}

@Composable
fun SafeVolumeScreen(
    settings: Map<String, Setting>,
    onSetSetting: (String, Value) -> Unit,
    onBack: () -> Unit,
) {
    val limitEnabled = settings.getSetting<Setting.ToggleSetting>(SoundcoreSettingIds.LIMIT_HIGH_VOLUME)
    val dbLimit = settings.getSetting<Setting.I32RangeSetting>(SoundcoreSettingIds.LIMIT_HIGH_VOLUME_DB_LIMIT)
    val refreshRate =
        settings.getSetting<Setting.SelectSetting>(SoundcoreSettingIds.LIMIT_HIGH_VOLUME_REFRESH_RATE)
    val liveDbInfo =
        settings.getSetting<Setting.InformationSetting>(SoundcoreSettingIds.LIMIT_HIGH_VOLUME_CURRENT_DB)
    val hasDecibelMonitoring = settings.containsKey(SoundcoreSettingIds.LIMIT_HIGH_VOLUME_DECIBEL_MONITORING)

    val currentOnSetSetting by rememberUpdatedState(onSetSetting)

    LaunchedEffect(hasDecibelMonitoring) {
        if (hasDecibelMonitoring) {
            delay(320)
            currentOnSetSetting(
                SoundcoreSettingIds.LIMIT_HIGH_VOLUME_DECIBEL_MONITORING,
                true.toValue(),
            )
        }
    }

    DisposableEffect(hasDecibelMonitoring) {
        onDispose {
            if (hasDecibelMonitoring) {
                currentOnSetSetting(
                    SoundcoreSettingIds.LIMIT_HIGH_VOLUME_DECIBEL_MONITORING,
                    false.toValue(),
                )
            }
        }
    }

    var showInfoDialog by remember { mutableStateOf(false) }

    val configuredLimit = dbLimit?.value ?: 90
    val minDb = dbLimit?.setting?.start ?: 75
    val maxDb = dbLimit?.setting?.end ?: 100
    val stepDb = (dbLimit?.setting?.step ?: 5).coerceAtLeast(1)

    val liveDb = (liveDbInfo?.value?.toIntOrNull() ?: 0).coerceIn(0, 120)
    val gaugeMaxDb = maxOf(100, configuredLimit)
    val targetFraction = if (liveDbInfo != null) {
        (liveDb.toFloat() / gaugeMaxDb.toFloat()).coerceIn(0f, 1f)
    } else {
        (configuredLimit.toFloat() / gaugeMaxDb.toFloat()).coerceIn(0f, 1f)
    }
    val displayedDb = if (liveDbInfo != null) liveDb else configuredLimit

    val animatedFillState = animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 420),
        label = "decibel_level",
    )

    val statusLevel = when {
        displayedDb >= 85 -> VolumeStatusLevel.High
        displayedDb >= 70 -> VolumeStatusLevel.Acceptable
        else -> VolumeStatusLevel.Normal
    }

    if (showInfoDialog) {
        AlertDialog(
            containerColor = SoundcoreColors.SurfaceCard,
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.safe_volume_info_title),
                    color = SoundcoreColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.safe_volume_info_body),
                    color = SoundcoreColors.TextSecondary,
                    lineHeight = 20.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text(stringResource(R.string.ok), color = SoundcoreColors.Cyan)
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoundcoreColors.Background),
    ) {
        SoundcoreTopBar(
            title = stringResource(R.string.safe_volume),
            onBack = onBack,
            actions = {
                IconButton(onClick = { showInfoDialog = true }) {
                    Icon(
                        painter = painterResource(R.drawable.info_24px),
                        contentDescription = stringResource(R.string.info),
                        tint = SoundcoreColors.TextPrimary,
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.current_headphone_volume),
                    color = SoundcoreColors.TextSecondary,
                    fontSize = 15.sp,
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SafeVolumeFaceIcon(level = statusLevel)
                    Text(
                        text = when (statusLevel) {
                            VolumeStatusLevel.Normal -> stringResource(R.string.volume_status_normal)
                            VolumeStatusLevel.Acceptable -> stringResource(R.string.volume_status_acceptable)
                            VolumeStatusLevel.High -> stringResource(R.string.volume_status_high)
                        },
                        color = SoundcoreColors.TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            LiveDecibelGauge(
                gaugeMaxDb = gaugeMaxDb,
                configuredLimit = configuredLimit,
                limitActive = limitEnabled?.value == true,
                displayedDb = displayedDb,
                animatedFillState = animatedFillState,
                statusLevel = statusLevel,
            )

            SoundcoreDivider()

            if (refreshRate != null) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.decibel_reading_refresh_rate),
                        color = SoundcoreColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        refreshRate.setting.options.forEachIndexed { idx, opt ->
                            val selected = opt == refreshRate.value
                            val label = refreshRate.setting.localizedOptions.getOrElse(idx) { opt }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RefreshOptionShape)
                                    .background(if (selected) SoundcoreColors.Cyan else SoundcoreColors.SurfaceCard)
                                    .clickable {
                                        onSetSetting(SoundcoreSettingIds.LIMIT_HIGH_VOLUME_REFRESH_RATE, opt.toValue())
                                    }
                                    .padding(vertical = 14.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = label,
                                    color = if (selected) SoundcoreColors.OnAccent else SoundcoreColors.TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }

            if (limitEnabled != null) {
                SoundcoreCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.limit_high_volume),
                            color = SoundcoreColors.TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        SoundcoreSwitch(
                            checked = limitEnabled.value,
                            onCheckedChange = {
                                onSetSetting(SoundcoreSettingIds.LIMIT_HIGH_VOLUME, it.toValue())
                            },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.limit_high_volume_description),
                        color = SoundcoreColors.TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "${configuredLimit}dB",
                        color = SoundcoreColors.TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    if (dbLimit != null) {
                        Spacer(Modifier.height(14.dp))
                        val steps = remember(minDb, maxDb, stepDb) {
                            generateSequence(minDb) { it + stepDb }.takeWhile { it <= maxDb }.toList()
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            steps.forEach { db ->
                                val selected = db == configuredLimit
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(DbStepShape)
                                        .background(
                                            if (selected) SoundcoreColors.Cyan else SoundcoreColors.SurfaceElevated,
                                        )
                                        .clickable {
                                            onSetSetting(SoundcoreSettingIds.LIMIT_HIGH_VOLUME_DB_LIMIT, db.toValue())
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    val stepTextColor = if (selected) {
                                        SoundcoreColors.OnAccent
                                    } else {
                                        SoundcoreColors.TextSecondary
                                    }
                                    Text(
                                        text = "$db",
                                        color = stepTextColor,
                                        fontSize = 13.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

private class VerticalWaveBrushCache {
    private var cachedColors: List<Color>? = null
    private var cachedStartY: Float = Float.NaN
    private var cachedEndY: Float = Float.NaN
    private var cachedBrush: Brush? = null

    fun getOrCreate(colors: List<Color>, startY: Float, endY: Float): Brush {
        val existing = cachedBrush
        if (
            existing != null &&
            cachedColors === colors &&
            cachedStartY == startY &&
            cachedEndY == endY
        ) {
            return existing
        }
        cachedColors = colors
        cachedStartY = startY
        cachedEndY = endY
        return Brush.verticalGradient(
            colors = colors,
            startY = startY,
            endY = endY,
        ).also { cachedBrush = it }
    }
}

@Composable
private fun LiveDecibelGauge(
    gaugeMaxDb: Int,
    configuredLimit: Int,
    limitActive: Boolean,
    displayedDb: Int,
    animatedFillState: State<Float>,
    statusLevel: VolumeStatusLevel,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "decibel_wave")
    val wavePhaseState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wave_phase",
    )

    val tickActiveColor = SoundcoreColors.Cyan
    val tickInactiveColor = SoundcoreColors.TextTertiary
    val gradientPink = SoundcoreColors.GradientPink
    val waveColors = remember(statusLevel, tickActiveColor, gradientPink) {
        val waveTopColor = when (statusLevel) {
            VolumeStatusLevel.High -> lerp(gradientPink, tickActiveColor, 0.25f)
            else -> lerp(tickActiveColor, Color(0xFF1A78F2), 0.55f)
        }
        listOf(waveTopColor, tickActiveColor)
    }
    val waveBackColor = remember(waveColors) {
        waveColors[0].copy(alpha = 0.45f)
    }
    val backWave = remember { Path() }
    val frontWave = remember { Path() }
    val waveBrushCache = remember { VerticalWaveBrushCache() }
    val gaugeHeight = 220.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(gaugeHeight),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .width(56.dp)
                .fillMaxHeight(),
        ) {
            val maxPx = constraints.maxHeight.toFloat()
            val limitFraction = (1f - (configuredLimit.toFloat() / gaugeMaxDb.toFloat())).coerceIn(0.12f, 0.85f)
            val limitOffsetPx = (maxPx * limitFraction - 18f).roundToInt().coerceIn(0, maxPx.toInt())

            Text(
                text = "${gaugeMaxDb}dB",
                color = SoundcoreColors.TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 4.dp),
            )
            if (configuredLimit < gaugeMaxDb) {
                Text(
                    text = "${configuredLimit}dB",
                    color = if (limitActive) SoundcoreColors.Cyan else SoundcoreColors.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (limitActive) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset { IntOffset(0, limitOffsetPx) }
                        .padding(end = 4.dp),
                )
            }
            Text(
                text = "0dB",
                color = SoundcoreColors.TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp),
            )
        }

        Spacer(Modifier.width(8.dp))

        Canvas(
            modifier = Modifier
                .width(26.dp)
                .fillMaxHeight()
                .padding(vertical = 6.dp),
        ) {
            val fill = animatedFillState.value
            val tickCount = 41
            val limitFrac = (configuredLimit.toFloat() / gaugeMaxDb.toFloat()).coerceIn(0f, 1f)
            val majorStroke = 1.5.dp.toPx()
            val minorStroke = 1.dp.toPx()
            for (i in 0 until tickCount) {
                val fracFromBottom = 1f - (i.toFloat() / (tickCount - 1))
                val y = size.height * (i.toFloat() / (tickCount - 1))
                val isMajor = i % 5 == 0
                val isLimitLine = kotlin.math.abs(fracFromBottom - limitFrac) <= (0.5f / (tickCount - 1))
                val startX = when {
                    isLimitLine || i == 0 || i == tickCount - 1 -> 0f
                    isMajor -> size.width * 0.25f
                    else -> size.width * 0.52f
                }
                val active = fracFromBottom <= fill
                drawLine(
                    color = when {
                        isLimitLine && limitActive -> tickActiveColor
                        active -> tickActiveColor
                        else -> tickInactiveColor
                    },
                    start = Offset(startX, y),
                    end = Offset(size.width, y),
                    strokeWidth = if (isLimitLine || isMajor) majorStroke else minorStroke,
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .width(78.dp)
                .fillMaxHeight()
                .clip(LiquidGaugeShape)
                .background(SoundcoreColors.SurfaceCard),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val fill = animatedFillState.value
                if (fill > 0.005f) {
                    val wavePhase = wavePhaseState.value
                    val effectiveFill = fill.coerceAtLeast(0.03f)
                    val baseY = size.height * (1f - effectiveFill)
                    val amplitude = (4.dp.toPx()).coerceAtMost(size.height * effectiveFill * 0.4f)
                    val steps = 32
                    val twoPi = (2.0 * PI).toFloat()

                    backWave.reset()
                    backWave.moveTo(0f, size.height)
                    for (i in 0..steps) {
                        val t = i.toFloat() / steps
                        val x = size.width * t
                        val y = (baseY + sin(t * twoPi - wavePhase + 1.3f) * amplitude * 0.85f)
                            .coerceIn(0f, size.height)
                        backWave.lineTo(x, y)
                    }
                    backWave.lineTo(size.width, size.height)
                    backWave.close()
                    drawPath(path = backWave, color = waveBackColor)

                    frontWave.reset()
                    frontWave.moveTo(0f, size.height)
                    for (i in 0..steps) {
                        val t = i.toFloat() / steps
                        val x = size.width * t
                        val y = (baseY + sin(t * twoPi + wavePhase) * amplitude)
                            .coerceIn(0f, size.height)
                        frontWave.lineTo(x, y)
                    }
                    frontWave.lineTo(size.width, size.height)
                    frontWave.close()
                    val waveBrush = waveBrushCache.getOrCreate(
                        colors = waveColors,
                        startY = (baseY - amplitude).coerceAtLeast(0f),
                        endY = size.height,
                    )
                    drawPath(
                        path = frontWave,
                        brush = waveBrush,
                    )
                }
            }
        }

        Spacer(Modifier.width(14.dp))

        Row(
            modifier = Modifier.width(110.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "▸ $displayedDb",
                color = SoundcoreColors.TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "dB",
                color = SoundcoreColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
    }
}

@Composable
private fun SafeVolumeFaceIcon(level: VolumeStatusLevel) {
    val bgColor = when (level) {
        VolumeStatusLevel.Normal, VolumeStatusLevel.Acceptable -> Color(0xFFE7B64C)
        VolumeStatusLevel.High -> Color(0xFFF07B52)
    }
    val inkColor = Color(0xFF1E1E22)
    Canvas(modifier = Modifier.size(26.dp)) {
        drawCircle(color = bgColor)
        val eyeY = size.height * 0.39f
        val leftEyeX = size.width * 0.34f
        val rightEyeX = size.width * 0.66f
        val eyeRadius = size.minDimension * 0.07f
        drawCircle(color = inkColor, radius = eyeRadius, center = Offset(leftEyeX, eyeY))
        drawCircle(color = inkColor, radius = eyeRadius, center = Offset(rightEyeX, eyeY))

        when (level) {
            VolumeStatusLevel.Normal -> {
                drawArc(
                    color = inkColor,
                    startAngle = 15f,
                    sweepAngle = 150f,
                    useCenter = true,
                    topLeft = Offset(size.width * 0.28f, size.height * 0.45f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.44f, size.height * 0.32f),
                )
            }
            VolumeStatusLevel.Acceptable -> {
                drawArc(
                    color = inkColor,
                    startAngle = 20f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.30f, size.height * 0.47f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.40f, size.height * 0.24f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                )
            }
            VolumeStatusLevel.High -> {
                drawLine(
                    color = inkColor,
                    start = Offset(size.width * 0.33f, size.height * 0.67f),
                    end = Offset(size.width * 0.67f, size.height * 0.67f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}
